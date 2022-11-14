package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvalidSignatureException extends RuntimeException {

    public InvalidSignatureException(String message) {
        super("Invalid signature: " + message);
    }

}
