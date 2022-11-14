package authentication;

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.stereotype.Component;

import object.Token;
import repository.TokenRepository;
import configuration.Constant;

import java.util.Optional;

/**
 * to manage the token
 */

@Component
public class TokenManager {

    private final int COOKIE_LENGTH = 128;
    
    private final Base64StringKeyGenerator keyGen = new Base64StringKeyGenerator(COOKIE_LENGTH);
    
    private final TokenRepository repository;

    TokenManager(TokenRepository repository){
    	this.repository = repository;
    }

    public String createToken(String userId) {
    	
    	
    	//create a new token and store into token databases for token
    	Token newToken = new Token(userId,keyGen.generateKey(), System.currentTimeMillis() + Constant.EXPIRATION_TIME);
    	repository.save(newToken);
    	return newToken.getKey();
 
    }

    // check if the user is log in?
    // and check if the user's cookie is expire?
    public boolean checkToken(String key) {
    	if(key == null) {
    		return false;
    	}
        Optional<Token> checkedToken = repository.findByKey(key);
        if (checkedToken.isEmpty()) {
            return false;
        }

        
        // if the cookie is invalid , return false
        // and delete token in database
        // some refresh method need to be extended 
        if(checkedToken.get().getExpiry() < System.currentTimeMillis()) {
        	repository.deleteByKey(key);
        	return false;
        }
        
        // update the expire for the token if the validation is passed
        long newExpiry = System.currentTimeMillis() + Constant.EXPIRATION_TIME;
        checkedToken.map(updateToken ->{
        	updateToken.setExpiry(newExpiry);
        	return repository.save(updateToken);
        });
        		
        return true;
    }
    
    public void deleteToken(String key) {
    	repository.deleteByKey(key);
    }

    
    
}
