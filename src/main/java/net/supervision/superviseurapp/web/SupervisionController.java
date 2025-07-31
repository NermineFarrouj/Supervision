package net.supervision.superviseurapp.web;

import lombok.RequiredArgsConstructor;
import net.supervision.superviseurapp.service.SupervisionRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequestMapping("/supervision")
@RequiredArgsConstructor
public class SupervisionController {

    private final SupervisionRunner supervisionRunner;

    @GetMapping("/form")
    public String showForm() {
        return "supervision-form"; // Nom du fichier Thymeleaf : supervision-form.html
    }

    @PostMapping("/startVM")
    public String startSupervisionVM(@RequestParam("file") MultipartFile file,
                                     @RequestParam("intervalMinutes") long intervalMinutes,
                                     Model model) {
        try {
            Path tempFile = Files.createTempFile("vm-", ".txt");
            file.transferTo(tempFile.toFile());

            supervisionRunner.startSupervisionVM(tempFile.toAbsolutePath().toString(), intervalMinutes);
            model.addAttribute("message", "Supervision VM démarrée avec succès.");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur VM : " + e.getMessage());
        }
        return "supervision-form";
    }

    @PostMapping("/startMS")
    public String startSupervisionMS(@RequestParam("file") MultipartFile file,
                                     @RequestParam("intervalMinutes") long intervalMinutes,
                                     Model model) {
        try {
            Path tempFile = Files.createTempFile("ms-", ".txt");
            file.transferTo(tempFile.toFile());

            supervisionRunner.startSupervisionMS(tempFile.toAbsolutePath().toString(), intervalMinutes);
            model.addAttribute("message", "Supervision MS démarrée avec succès.");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur MS : " + e.getMessage());
        }
        return "supervision-form";
    }

    @PostMapping("/stop/vm")
    public String stopVM(Model model) {
        try {
            supervisionRunner.stopVM();
            model.addAttribute("message", "Supervision VM arrêtée.");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur arrêt VM : " + e.getMessage());
        }
        return "supervision-form";
    }

    @PostMapping("/stop/ms")
    public String stopMS(Model model) {
        try {
            supervisionRunner.stopMS();
            model.addAttribute("message", "Supervision MS arrêtée.");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur arrêt MS : " + e.getMessage());
        }
        return "supervision-form";
    }
}
