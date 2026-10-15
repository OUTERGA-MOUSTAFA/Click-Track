package ma.youcode.clickTrack.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Transforme les exceptions Java standard lancées par les services en réponses HTTP.
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody.
 * Le bean est déclaré dans dispatcher-servlet.xml.
 */
@RestControllerAdvice 
public class GlobalExceptionHandler {
 
    // Publisher / lien introuvable -> 404
    @ExceptionHandler (NoSuchFieldException.class)
    @ResponseStatus (HttpStatus.NOT_FOUND)
    public Map<String, Object> handleIntrouvable(NoSuchElementException ex) {
        return erreur(404, ex.getMessage());
    }
 
    // Données invalides (offre ou url vide...) -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRequeteInvalide(IllegalArgumentException ex) {
        return erreur(400, ex.getMessage());
    }
 
    // Corps absent ou JSON mal formé -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleCorpsIllisible(HttpMessageNotReadableException ex) {
        return erreur(400, "Corps de la requête absent ou JSON invalide");
    }
 
    private Map<String, Object> erreur(int status, String message) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("status", status);
        corps.put("message", message);
        return corps;
    }
}
 