package net.supervision.superviseurapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SupervisionRunner {

    private final VMStatusServiceImpl vmService;
    private final ServiceStatusServiceImpl msService;

    private ScheduledExecutorService executorVM;
    private ScheduledExecutorService executorMS;

    public void startSupervisionVM(String vmFile, long intervalMinutes) {
        stopVM(); // arrêt de l'ancien exécuteur s’il existe

        executorVM = Executors.newSingleThreadScheduledExecutor();
        executorVM.scheduleAtFixedRate(() -> {
            System.out.println("Supervision VM lancée à " + java.time.LocalDateTime.now());
            vmService.testVMsFromFile(vmFile);
        }, 0, intervalMinutes, TimeUnit.MINUTES);
    }

    public void startSupervisionMS(String msFile, long intervalMinutes) {
        stopMS(); // arrêt de l'ancien exécuteur s’il existe

        executorMS = Executors.newSingleThreadScheduledExecutor();
        executorMS.scheduleAtFixedRate(() -> {
            System.out.println("Supervision MS lancée à " + java.time.LocalDateTime.now());
            msService.testServicesFromFile(msFile);
        }, 0, intervalMinutes, TimeUnit.MINUTES);
    }

    public void stopVM() {
        if (executorVM != null && !executorVM.isShutdown()) {
            executorVM.shutdownNow();
            System.out.println("Supervision VM arrêtée.");
        }
    }

    public void stopMS() {
        if (executorMS != null && !executorMS.isShutdown()) {
            executorMS.shutdownNow();
            System.out.println("Supervision MS arrêtée.");
        }
    }
}
