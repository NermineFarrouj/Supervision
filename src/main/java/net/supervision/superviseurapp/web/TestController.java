package net.supervision.superviseurapp.web;


import net.supervision.superviseurapp.dtos.TestRequest;
import net.supervision.superviseurapp.dtos.TestResponse;
import net.supervision.superviseurapp.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test") // Préfixe commun pour tous les endpoints de ce contrôleur
public class TestController {

    private final TestService testService;

    // Injection du service via le constructeur
    public TestController(TestService testService) {
        this.testService = testService;
    }

    /**
     * Endpoint pour tester la connectivité d'une machine.
     * Mappé sur POST /test/machine
     *
     * @param request Le corps de la requête contenant l'IP et le port.
     * @return Une réponse HTTP avec le résultat du test.
     */
    @PostMapping("/machine")
    public ResponseEntity<TestResponse> testMachine(@RequestBody TestRequest request) {
        TestResponse response = testService.testMachine(request.ip(), request.port());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour tester la disponibilité d'un service.
     * Mappé sur POST /test/service
     *
     * @paramrequest Le corps de la requête contenant l'IP et le port.
     * @return Une réponse HTTP avec le résultat du test.
     */
    @PostMapping("/service")
    public ResponseEntity<TestResponse> testService(@RequestBody TestRequest request) {
        TestResponse response = testService.testService(request.ip(), request.port());
        return ResponseEntity.ok(response);
    }
}
