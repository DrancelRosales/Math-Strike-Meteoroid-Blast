package mathstrike;

/** Thrown when the player submits something that is not a usable answer. */
public class InvalidAnswerException extends Exception {
    public InvalidAnswerException(String message) {
        super(message);
    }
}
