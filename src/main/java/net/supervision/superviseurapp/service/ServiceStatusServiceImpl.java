package net.supervision.superviseurapp.service;

import lombok.RequiredArgsConstructor;
import net.supervision.superviseurapp.entities.ServiceStatus;
import net.supervision.superviseurapp.repositories.ServiceStatusRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
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
    public void testServicesFromFile(String filePath) {
        List<String[]> ipPortList = MSReadIpPortFromFile(filePath);

        ExecutorService threadPool = Executors.newFixedThreadPool(10); // adapt le pool si besoin

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
                                System.err.println("Impossible de récupérer la version du MS " + serviceName);
                            }

                        } else {
                            status = "DOWN";
                        }

                    } catch (Exception e) {
                        boolean isOpen = PortChecker.isPortOpen(ip, port, 2000);
                        status = isOpen ? "NO_HEALTH_ENDPOINT" : "DOWN";
                    }






                    //boolean isDownNow = "DOWN".equals(status);

                    if ("DOWN".equals(status)) {
                        System.out.println("📣 Notification envoyée pour : " + serviceName);
                        notificationService.notifyServiceDown(serviceName);

                        try {
                            Thread.sleep(200); // 🔁 éviter que les messages STOMP se superposent trop vite
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


    private List<String[]> MSReadIpPortFromFile(String filePath) {
        List<String[]> MSipPorts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank() && line.contains(":")) {
                    MSipPorts.add(line.trim().split(":"));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture fichier MS: " + e.getMessage());
        }
        return MSipPorts;
    }
}
