package execption;

public class CustomApiException extends Exception {

    public CustomApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
