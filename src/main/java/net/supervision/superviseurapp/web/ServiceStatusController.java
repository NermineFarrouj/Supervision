package net.supervision.superviseurapp.web;

import lombok.RequiredArgsConstructor;
import net.supervision.superviseurapp.entities.ServiceStatus;
import net.supervision.superviseurapp.repositories.ServiceStatusRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/service-status")
@RequiredArgsConstructor
public class ServiceStatusController {

    private final ServiceStatusRepository serviceStatusRepository;

    // Obtenir l’historique d’un microservice selon IP et port
    @GetMapping("/history")
    public List<ServiceStatus> getHistoryByIpAndPort(
            @RequestParam String serviceName
    ) {
        return serviceStatusRepository.findByServiceName(serviceName);
    }

    //  Obtenir les statuts de tous les microservices dans une période donnée
    @GetMapping("/between")
    public List<ServiceStatus> getStatusBetween(
            @RequestParam String start,
            @RequestParam String end
    ) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        return serviceStatusRepository.findByTestedAtBetween(startDate, endDate);
    }

    //  Obtenir le statut d’un microservice par son nom à un moment précis
    @GetMapping("/at")
    public List<ServiceStatus> getStatusAt(
            @RequestParam String serviceName,
            @RequestParam String start,
            @RequestParam String end
    ) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        return serviceStatusRepository.findByServiceNameAndTestedAtBetween(
                serviceName,
                startDate, endDate
        );
    }
}
