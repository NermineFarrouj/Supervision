package net.supervision.superviseurapp.repositories;

import net.supervision.superviseurapp.entities.ServiceStatus;

import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDateTime;
import java.util.List;

public interface ServiceStatusRepository extends JpaRepository<ServiceStatus, Long> {


    List<ServiceStatus> findByServiceName(String serviceName);


    List<ServiceStatus> findByTestedAtBetween(LocalDateTime start, LocalDateTime end);



    List<ServiceStatus> findByServiceNameAndTestedAtBetween(String serviceName, LocalDateTime start, LocalDateTime end);






}
