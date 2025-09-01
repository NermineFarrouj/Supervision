package net.supervision.superviseurapp.service;

import lombok.RequiredArgsConstructor;
import net.supervision.superviseurapp.entities.ServiceStatus;
import net.supervision.superviseurapp.repositories.ServiceStatusRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ServiceStatusServiceImpl implements IServiceStatusService {

    private final NotificationService notificationService;
    private final ServiceStatusRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void testServicesFromFile(String filePath) throws IOException {
        List<String[]> ipPortList = MSReadIpPortFromFile(filePath);

        ExecutorService threadPool = Executors.newFixedThreadPool(10); // le pool

        for (String[] ipPort : ipPortList) {
            threadPool.submit(() -> {
                try {
                    String serviceName = ipPort[0];
                    String ip = ipPort[1];
                    int port = Integer.parseInt(ipPort[2]);

                    String status;
                    String version = null;

                    try {
                        String healthUrl = "http://" + ip + ":" + port + "/actuator/health";
                        ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);

                        if (response.getStatusCode().is2xxSuccessful() && response.getBody().contains("UP")) {
                            status = "UP";

                            try {
                                String infoUrl = "http://" + ip + ":" + port + "/actuator/info";
                                ResponseEntity<Map> infoResponse = restTemplate.getForEntity(infoUrl, Map.class);
                                if (infoResponse.getStatusCode().is2xxSuccessful()) {
                                    Object v = infoResponse.getBody().get("version");
                                    version = (v != null) ? v.toString() : null;
                                }
                            } catch (Exception e) {
                                System.err.println("Not able to recover MS version " + serviceName);
                            }

                        } else {
                            status = "DOWN";
                        }

                    } catch (Exception e) {
                        boolean isOpen = PortChecker.isPortOpen(ip, port, 2000);
                        status = isOpen ? "NO_HEALTH_ENDPOINT(UP)" : "DOWN";
                    }



                    //boolean isDownNow = "DOWN".equals(status);

                    if ("DOWN".equals(status)) {
                        System.out.println(" Notification sent for : " + serviceName);
                        notificationService.notifyServiceDown(serviceName);

                        try {
                            Thread.sleep(100); // eviter que messages STOMP se superposent trop vite
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // bonne pratique
                        }
                    }



                    ServiceStatus serviceStatus = ServiceStatus.builder()
                            .ipAddress(ip)
                            .serviceName(serviceName)
                            .port(port)
                            .status(status)
                            .version(version)
                            .testedAt(LocalDateTime.now())
                            .build();

                    repository.save(serviceStatus);

                } catch (Exception ex) {
                    System.err.println("Erreur lors du test du MS: " + Arrays.toString(ipPort));
                    ex.printStackTrace();
                }
            });
        }

        threadPool.shutdown();

        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Certaines tâches de MS n'ont pas terminé à temps.");
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<String[]> MSReadIpPortFromFile(String filePath) throws IOException {
        List<String[]> MSipPorts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(":");
                    if (parts.length != 3) {
                        throw new IOException("Invalid format (expected: Name : IP : Port ) in line : \"" + line + "\"");
                    }
                    String name = parts[0];
                    String ip = parts[1];
                    String portStr = parts[2];

                    if (!isValidIp(ip)) {
                        throw new IOException("IP invalid in line : \"" + line + "\"");
                    }
                    if (!isValidPort(portStr)) {
                        throw new IOException("Port invalid in line : \"" + line + "\"");
                    }

                    MSipPorts.add(parts);
                }
            }
        }
        return MSipPorts;
    }

    private boolean isValidIp(String ip) {
        return ip.matches("^\\d{1,3}(\\.\\d{1,3}){3}$");
    }

    private boolean isValidPort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            return port >= 1 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void validateMsFile(String filePath) throws IOException {
        MSReadIpPortFromFile(filePath); // si exception => fichier invalide
    }

}
