package teammates.common.exception;

@SuppressWarnings("serial")
public class NonRmitLoginException extends RuntimeException {
    public NonRmitLoginException() {
        super();
    }

    public NonRmitLoginException(String message) {
        super(message);
    }
}
