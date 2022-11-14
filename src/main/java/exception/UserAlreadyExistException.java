package exception;

public class UserAlreadyExistException extends RuntimeException{
	UserAlreadyExistException(String name) {
		super("user " + name + "is already exist");
	}
}
