package ma.youcode.clickTrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ma.youcode.clickTrack.dto.TrackingResponse;
import ma.youcode.clickTrack.service.LienService;

@Controller 
@RequestMapping("/t")
public class TrackingController {
     private final LienService lienService;
 
    public TrackingController(LienService lienService) {
        this.lienService = lienService;
    }
 
    // GET /t/{id}  ->  click compté + infos du lien
    @GetMapping("/{id}")
    @ResponseBody
    public TrackingResponse ouvrirLien(@PathVariable("id") Integer id) {
        return lienService.enregistrerClick(id);
    }
}
