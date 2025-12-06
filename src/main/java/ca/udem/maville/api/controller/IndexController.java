package ca.udem.maville.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour la route racine
 */
@Controller
public class IndexController {
    
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }
}

