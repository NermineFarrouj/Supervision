package net.supervision.superviseurapp.repositories;

import net.supervision.superviseurapp.entities.VMStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VMStatusRepository extends JpaRepository<VMStatus, Long> {


    List<VMStatus> findByIpAddress(String ipAddress);



    List<VMStatus> findByTestedAtBetween(LocalDateTime start, LocalDateTime end);



    // Trouver status d'une VM dans une date precisé par User
        List<VMStatus> findByIpAddressAndTestedAtBetween(String ipAddress, LocalDateTime start, LocalDateTime end);


   
}
