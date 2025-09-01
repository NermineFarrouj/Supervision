package net.supervision.superviseurapp.service;

import net.supervision.superviseurapp.dtos.TestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Objects;

@Service
public class TestService {

    private final RestTemplate restTemplate;

    // Injection de dépendance du bean RestTemplate
    public TestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TestResponse testMachine(String ip, int port) {
        // Utilise le PortChecker pour vérifier si le port est ouvert avec un timeout de 2 secondes.
        boolean reachable = PortChecker.isPortOpen(ip, port, 2000);
        String status = reachable ? "UP" : "DOWN";
        return new TestResponse(status);
    }

    public TestResponse testService(String ip, int port) {
        String status;
        String version = null;

        try {
            // Construit l'URL de l'endpoint de santé (health)
            String healthUrl = "http://" + ip + ":" + port + "/actuator/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);

            // Vérifie si la réponse est un succès (2xx) et si le corps contient "UP"
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().contains("\"status\":\"UP\"")) {
                status = "UP";

                // Si le service est UP, tente de récupérer les informations de version
                try {
                    String infoUrl = "http://" + ip + ":" + port + "/actuator/info";
                    // Utilisation de Map.class pour désérialiser une réponse JSON générique
                    ResponseEntity<Map> infoResponse = restTemplate.getForEntity(infoUrl, Map.class);
                    if (infoResponse.getStatusCode().is2xxSuccessful() && infoResponse.getBody() != null) {
                        // Extrait la version du corps de la réponse
                        Object versionObj = infoResponse.getBody().get("version");
                        version = Objects.toString(versionObj, null); // Gère le cas où la version est nulle
                    }
                } catch (Exception e) {
                    // Si la récupération de la version échoue, on l'ignore et on continue
                    System.err.println("Impossible de récupérer la version du service à l'adresse " + ip + ":" + port + ". Erreur: " + e.getMessage());
                }
            } else {
                status = "DOWN";
            }
        } catch (Exception e) {
            // Si l'appel à /actuator/health échoue, on vérifie si le port est au moins ouvert.
            // Cela permet de distinguer un service qui tourne mais n'a pas d'endpoint de santé,
            // d'un service qui est complètement arrêté.
            boolean isOpen = PortChecker.isPortOpen(ip, port, 2000);
            status = isOpen ? "NO_HEALTH_ENDPOINT(UP)" : "DOWN";
        }

        return new TestResponse(status, version, null); // Le champ 'error' n'est pas utilisé ici, géré par le statut
    }
}