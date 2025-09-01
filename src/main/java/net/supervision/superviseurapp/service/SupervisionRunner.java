package net.supervision.superviseurapp.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor

public class SupervisionRunner {

    private final IVMStatusService vmService;
    private final IServiceStatusService msService;

    private ScheduledExecutorService executorVM;
    private ScheduledExecutorService executorMS;

    private final List<String> vmFiles = new ArrayList<>();
    private final List<String> msFiles = new ArrayList<>();

    private long currentVmInterval = -1;
    private long currentMsInterval = -1;

    /**
     * Démarre la supervision des VMs avec une liste de fichiers
     * @param files Liste des fichiers à traiter
     * @param intervalMinutes Intervalle en minutes
     */

    public synchronized void startSupervisionVM(List<String> files, long intervalMinutes) throws IOException {


        // Valider les fichiers AVANT de lancer la supervision
        for (String file : files) {

                vmService.VMReadIpPortFromFile(file); // test de lecture + validation

        }

        stopVM();

        vmFiles.clear();
        vmFiles.addAll(files);
        currentVmInterval = intervalMinutes;

        executorVM = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "VM-Supervision-Thread");
            t.setDaemon(true);
            return t;
        });

        executorVM.scheduleAtFixedRate(() -> {
            try {
                System.out.println("=== Supervision VM started at " + java.time.LocalDateTime.now() + " ===");
                System.out.println("File : " + vmFiles);

                for (String file : vmFiles) {
                    System.out.println("Traitement du file VM: " + file);
                    vmService.testVMsFromFile(file);
                }

                System.out.println("===== Fin VM =====");
            } catch (Exception e) {
                System.err.println("Erreur lors de la supervision VM: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);

        System.out.println("Supervision VM démarrée avec " + files.size() + " fichier(s), intervalle: " + intervalMinutes + " min");
    }


    /**
     * Démarre la supervision des microservices avec une liste de fichiers
     * @param files Liste des fichiers à traiter
     * @param intervalMinutes Intervalle en minutes
     */
    public synchronized void startSupervisionMS(List<String> files, long intervalMinutes) throws IOException {
        // Valider tous les fichiers AVANT de commencer la supervision
        for (String file : files) {
            msService.MSReadIpPortFromFile(file); // lève IOException si invalide
        }

        // Stopper l’ancienne supervision si elle existe
        stopMS();

        msFiles.clear();
        msFiles.addAll(files);
        currentMsInterval = intervalMinutes;

        executorMS = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MS-Supervision-Thread");
            t.setDaemon(true);
            return t;
        });

        executorMS.scheduleAtFixedRate(() -> {
            try {
                System.out.println("=== Supervision MS started at " + java.time.LocalDateTime.now() + " ===");
                System.out.println("file : " + msFiles);
                for (String file : msFiles) {
                    System.out.println("Traitement du file MS: " + file);
                    msService.testServicesFromFile(file);
                }
                System.out.println("====== Fin MS =====");
            } catch (Exception e) {
                System.err.println("Erreur lors de la supervision MS: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);

        System.out.println("Supervision MS démarrée avec " + files.size() + " fichier(s), intervalle: " + intervalMinutes + " min");
    }

    /**
     * Arrête la supervision des VMs
     */
    public synchronized void stopVM() {
        if (executorVM != null && !executorVM.isShutdown()) {
            executorVM.shutdownNow();
            try {
                if (!executorVM.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Le scheduler VM n'a pas pu s'arrêter dans les temps");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        executorVM = null;
        vmFiles.clear();
        currentVmInterval = -1;
        System.out.println("Supervision VM arretee et ressources nettoyees.");
    }

    /**
     * Arrête la supervision des microservices
     */
    public synchronized void stopMS() {
        if (executorMS != null && !executorMS.isShutdown()) {
            executorMS.shutdownNow();
            try {
                if (!executorMS.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Le scheduler MS n'a pas pu s'arrêter dans les temps");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        executorMS = null;
        msFiles.clear();
        currentMsInterval = -1;
        System.out.println("Supervision MS arretee et ressources nettoyees.");
    }

    /**
     * Vérifie si la supervision VM est en cours
     */
    public boolean isVmRunning() {
        return executorVM != null && !executorVM.isShutdown();
    }

    /**
     * Vérifie si la supervision MS est en cours
     */
    public boolean isMsRunning() {
        return executorMS != null && !executorMS.isShutdown();
    }

    /**
     * Retourne l'intervalle de supervision VM
     */
    public long getVmInterval() {
        return currentVmInterval;
    }

    /**
     * Retourne l'intervalle de supervision MS
     */
    public long getMsInterval() {
        return currentMsInterval;
    }

    /**
     * Retourne la liste des fichiers VM en cours de supervision
     */
    public List<String> getVmFiles() {
        return new ArrayList<>(vmFiles);
    }

    /**
     * Retourne la liste des fichiers MS en cours de supervision
     */
    public List<String> getMsFiles() {
        return new ArrayList<>(msFiles);
    }















}