package net.supervision.superviseurapp.web;

import lombok.RequiredArgsConstructor;
import net.supervision.superviseurapp.service.SupervisionRunner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/supervision")
@RequiredArgsConstructor
public class SupervisionController {


    private final SupervisionRunner supervisionRunner;
    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir");

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("vmRunning", supervisionRunner.isVmRunning());
        model.addAttribute("msRunning", supervisionRunner.isMsRunning());
        model.addAttribute("vmFiles", supervisionRunner.getVmFiles());
        model.addAttribute("msFiles", supervisionRunner.getMsFiles());
        model.addAttribute("vmInterval", supervisionRunner.getVmInterval());
        model.addAttribute("msInterval", supervisionRunner.getMsInterval());
        return "supervision-form";
    }

    @PostMapping("/startVM")
    public String startSupervisionVM(@RequestParam("files") MultipartFile[] files,
                                     @RequestParam("intervalMinutes") long intervalMinutes,
                                     RedirectAttributes redirectAttributes) {

        if (supervisionRunner.isVmRunning()) {
            redirectAttributes.addFlashAttribute("error", "La supervision VM est déjà en cours.");
            return "redirect:/supervision/form";
        }

        if (files == null || files.length == 0 || files[0].isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner au moins un fichier.");
            return "redirect:/supervision/form";
        }

        try {
            List<String> filePaths = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Path tempFile = Paths.get(UPLOAD_DIR, "vm-" + file.getOriginalFilename());
                    Files.createDirectories(tempFile.getParent());
                    file.transferTo(tempFile.toFile());



                    filePaths.add(tempFile.toAbsolutePath().toString());
                }
            }

            // Tous les fichiers sont valides on demarre la supervision
            supervisionRunner.startSupervisionVM(filePaths, intervalMinutes);
            redirectAttributes.addFlashAttribute("message", "Supervision VM started, " + filePaths.size() + " file(s).");

        } catch (Exception e) {
            // En cas d’erreur, on affiche un message et on ne démarre PAS la supervision
            redirectAttributes.addFlashAttribute("error", "Error VM : " + e.getMessage());
        }

        return "redirect:/supervision/form";
    }


    @PostMapping("/startMS")
    public String startSupervisionMS(@RequestParam("files") MultipartFile[] files,
                                     @RequestParam("intervalMinutes") long intervalMinutes,
                                     RedirectAttributes redirectAttributes) {
        if (supervisionRunner.isMsRunning()) {
            redirectAttributes.addFlashAttribute("error", "Impossible d'ajouter des fichiers, la supervision MS est en cours.");
            return "redirect:/supervision/form";
        }

        if (files == null || files.length == 0 || files[0].isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez selectionner au moins un fichier MS.");
            return "redirect:/supervision/form";
        }

        try {
            List<String> filePaths = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Path tempFile = Paths.get(UPLOAD_DIR, "ms-" + file.getOriginalFilename());
                    Files.createDirectories(tempFile.getParent());
                    file.transferTo(tempFile.toFile());
                    filePaths.add(tempFile.toAbsolutePath().toString());
                }
            }

            supervisionRunner.startSupervisionMS(filePaths, intervalMinutes);
            redirectAttributes.addFlashAttribute("message", "Supervision MS Started " + filePaths.size() + " file(s).");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error MS : " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected Error MS : " + e.getMessage());
        }

        return "redirect:/supervision/form";
    }


    @PostMapping("/stopVM")
    public String stopVM(RedirectAttributes redirectAttributes) {
        try {
            supervisionRunner.stopVM();
            redirectAttributes.addFlashAttribute("message", "Supervision VM Stopped.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error stopping supervision VM : " + e.getMessage());
        }
        return "redirect:/supervision/form";
    }

    @PostMapping("/stopMS")
    public String stopMS(RedirectAttributes redirectAttributes) {
        try {
            supervisionRunner.stopMS();
            redirectAttributes.addFlashAttribute("message", "Supervision MS stopped.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error stopping supervision MS : " + e.getMessage());
        }
        return "redirect:/supervision/form";
    }





}