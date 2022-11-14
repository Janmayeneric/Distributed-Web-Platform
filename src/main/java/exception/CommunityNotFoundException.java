package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CommunityNotFoundException extends RuntimeException {

    public CommunityNotFoundException(String id) {
        super("Could not find community " + id);
    }

}
