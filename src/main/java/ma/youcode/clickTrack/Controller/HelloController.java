package ma.youcode.clickTrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller TEMPORAIRE pour vérifier que Tomcat + Spring MVC démarrent (CLIC-7).
 * A supprimer ensuite (et retirer son <bean> de dispatcher-servlet.xml).
 *
 * Le bean est déclaré en XML (dispatcher-servlet.xml), sans component-scan.
 * Depuis Spring 6, @Controller est NÉCESSAIRE pour que Spring reconnaisse la classe
 * comme handler : @RequestMapping seul sur la classe ne suffit plus.
 */
@Controller
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello ClickTrack";
    }
}