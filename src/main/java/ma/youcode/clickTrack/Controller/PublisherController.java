package ma.youcode.clickTrack.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ma.youcode.clickTrack.model.Publisher;
import ma.youcode.clickTrack.service.PublisherService;

@Controller// means this class is responcible on get requests from client
@RequestMapping("api/publishers") // katkelf ghir bhad url specifique
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService){// Dependency Injection
        this.publisherService = publisherService;
    }

    @GetMapping
    @ResponseBody // no html mais json
    public List<Publisher>/*List of Publishers*/ listPublisers(){
        
        return publisherService.listPublisers();// list of publisher with secces 200
    }
}
