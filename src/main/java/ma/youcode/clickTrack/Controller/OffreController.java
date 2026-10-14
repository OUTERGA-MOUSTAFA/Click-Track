package ma.youcode.clickTrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ma.youcode.clickTrack.dto.OffreStatsResponse;
import ma.youcode.clickTrack.service.LienService;

@RestController
@RequestMapping("/api/publishers/{id}/offres")

public class OffreController {

    private LienService lienService;

    public OffreController(LienService lienService) {
        this.lienService = lienService;
    }

    // GET /api/publishers/{publisherId}/offres -> 200 (ou 404 si le publisher
    // n'existe pas)
    @GetMapping
    @ResponseBody
    public List<OffreStatsResponse> listerOffres(@PathVariable("publisherId") Integer publisherId) {
        return lienService.listOffres(publisherId);
    }
}
