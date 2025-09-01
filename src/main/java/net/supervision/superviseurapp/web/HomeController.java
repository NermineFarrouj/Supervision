package net.supervision.superviseurapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

    @Controller
    public class HomeController {

        @GetMapping("/")
        public String home() {
            return "home";  // Correspond à home.html
        }


        @GetMapping("/test")
        public String test() {
            return "test";  // Correspond à test.html
        }

        @GetMapping({"/history", "/historique"})
        public String showHistoryPage() {
            return "historique"; // Returns historique.html
        }

    }
