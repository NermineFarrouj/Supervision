package net.supervision.superviseurapp.service;



import lombok.RequiredArgsConstructor;
import net.supervision.superviseurapp.entities.VMStatus;
import net.supervision.superviseurapp.repositories.VMStatusRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;




@Slf4j
@Service
@RequiredArgsConstructor
public class VMStatusServiceImpl implements IVMStatusService {

    private final VMStatusRepository vmStatusRepository;

    @Override
    public void testVMsFromFile(String filePath) throws IOException {
        List<String[]> ipPortList = VMReadIpPortFromFile(filePath);

        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        for (String[] ipPort : ipPortList) {
            threadPool.submit(() -> {
                try {
                    String ip = ipPort[0];
                    int port = Integer.parseInt(ipPort[1]);

                    boolean reachable = PortChecker.isPortOpen(ip, port, 2000);
                    String status = reachable ? "UP" : "DOWN";

                    VMStatus vmStatus = VMStatus.builder()
                            .ipAddress(ip)
                            .port(port)
                            .status(status)
                            .testedAt(LocalDateTime.now())
                            .build();

                    vmStatusRepository.save(vmStatus);
                } catch (Exception e) {
                    System.err.println("Erreur lors du test de la VM: " + Arrays.toString(ipPort));
                    e.printStackTrace();
                }
            });
        }

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                System.err.println("Certaines tâches n'ont pas terminé dans les temps.");
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }



     /* Only for linux cus cmd bash:
        for (String ip : ips) {
            boolean reachable = isSSHPortOpen(ip);
            String status = reachable ? "UP" : "DOWN";

            VMStatus vmStatus = VMStatus.builder()
                    .ipAddress(ip)
                    .status(status)
                    .testedAt(LocalDateTime.now())
                    .build();

            vmStatusRepository.save(vmStatus);
                                 }

      private boolean isSSHPortOpen(String ip) {
        try {
            Process process = new ProcessBuilder("bash", "-c", "timeout 2 bash -c \"</dev/tcp/" + ip + "/22\"")
                    .start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    *///

    // Supprime le try-catch dans cette méthode, pour qu’elle lance IOException si probleme
    @Override
    public List<String[]> VMReadIpPortFromFile(String filePath) throws IOException {
        List<String[]> ipPorts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (!line.contains(":")) {
                        throw new IOException("Format invalid : ':' missing in line \"" + line + "\"");
                    }
                    String[] parts = line.split(":");
                    if (parts.length != 2) {
                        throw new IOException("Format invalid (expected: IP : Port) in line : \"" + line + "\"");
                    }
                    String ip = parts[0];
                    String portStr = parts[1];

                    // Optionnel : validation simple de l'IP et port
                    if (!isValidIp(ip)) {
                        throw new IOException("IP invalid in line : \"" + line + "\"");
                    }
                    if (!isValidPort(portStr)) {
                        throw new IOException("Port invalid in line : \"" + line + "\"");
                    }

                    ipPorts.add(new String[] {ip, portStr});
                }
            }
        }
        return ipPorts;
    }

    // Méthode d'exemple pour vérifier format IP (simple regex)
    private boolean isValidIp(String ip) {
        return ip.matches("^\\d{1,3}(\\.\\d{1,3}){3}$");
    }

    // Méthode d'exemple pour vérifier port valide (1-65535)
    private boolean isValidPort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            return port >= 1 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void validateVmFile(String filePath) throws IOException {
        List<String[]> entries = VMReadIpPortFromFile(filePath);

    }



}