package net.supervision.superviseurapp.service;



import lombok.RequiredArgsConstructor;
import net.supervision.superviseurapp.entities.VMStatus;
import net.supervision.superviseurapp.repositories.VMStatusRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
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
    public void testVMsFromFile(String filePath) {
        List<String[]> ipPortList = VMReadIpPortFromFile(filePath);

        // Pool fixe avec 10 threads
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

        threadPool.shutdown(); // on n'accepte plus de nouvelles tâches

        try {
            // Attendre que tous les tests soient terminés (max 30 secondes)
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                System.err.println("Certaines tâches n'ont pas terminé dans les temps.");
                threadPool.shutdownNow(); // forcer l'arrêt
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

    private List<String[]> VMReadIpPortFromFile(String filePath) {
        List<String[]> ipPorts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank() && line.contains(":")) {
                    ipPorts.add(line.trim().split(":"));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture fichier MS: " + e.getMessage());
        }
        return ipPorts;
    }

}