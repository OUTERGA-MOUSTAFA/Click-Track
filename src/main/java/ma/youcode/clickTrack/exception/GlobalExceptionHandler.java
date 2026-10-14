package ma.youcode.clickTrack.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import ma.youcode.clickTrack.dto.ErreurResponse;

public class GlobalExceptionHandler {
        @ExceptionHandler(IllegalArgumentException.class)
        // @ResponseStatus (ResponseStatus.NOT_FOUND)

        public ErreurResponse HandlerNotFound(IllegalArgumentException ex){
            
            return new ErreurResponse(404,ex.getMessage());
        }

}
