package net.supervision.superviseurapp.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class VMStatus {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ipAddress;
    private int port;
    private String status;
    private LocalDateTime testedAt;



}
