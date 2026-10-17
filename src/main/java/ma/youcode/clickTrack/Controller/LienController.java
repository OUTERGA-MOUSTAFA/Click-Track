package ma.youcode.clickTrack.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import ma.youcode.clickTrack.dto.CreerLienRequest;
import ma.youcode.clickTrack.dto.LienResponse;
import ma.youcode.clickTrack.service.LienService;


@Controller 
@RequestMapping ("/api/publishers/{publisherId}/liens")
public class LienController {
    
    private final LienService lienService;

    public LienController(LienService lienService){
        this.lienService =lienService;
    }

    @PostMapping //POST http://localhost:8081/clicktrack/api/publishers -> 201 t crée avec succes
    @ResponseBody
    @ResponseStatus (HttpStatus.CREATED)
    public LienResponse creeLien(@PathVariable("publisherId") Integer publisherId, @RequestBody CreerLienRequest lienRequest){

        return lienService.creeLien(publisherId,lienRequest);
    }

    
}
