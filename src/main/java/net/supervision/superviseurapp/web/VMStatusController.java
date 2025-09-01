package net.supervision.superviseurapp.web;

import lombok.RequiredArgsConstructor;
import net.supervision.superviseurapp.entities.VMStatus;
import net.supervision.superviseurapp.repositories.VMStatusRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/vm-status")
@RequiredArgsConstructor
public class VMStatusController {

    private final VMStatusRepository vmStatusRepository;

    //  Obtenir l’historique d’une VM
    @GetMapping("/history")
    public List<VMStatus> getHistory(@RequestParam String ip) {
        return vmStatusRepository.findByIpAddress(ip);
    }

    //  Obtenir les statuts de toutes les VMs dans une periode
    @GetMapping("/between")
    public List<VMStatus> getStatusBetween(@RequestParam String ip,@RequestParam int port,@RequestParam String start, @RequestParam String end) {


        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        return vmStatusRepository.findByIpAddressAndPortAndTestedAtBetween(ip,port,startDate, endDate);
    }

    //  Obtenir le statut d’une VM dans une periode
    @GetMapping("/at")
    public List<VMStatus> getStatusAt(@RequestParam String ip, @RequestParam String start, @RequestParam String end) {

        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        return vmStatusRepository.findByIpAddressAndTestedAtBetween(ip, startDate, endDate);
    }
}
