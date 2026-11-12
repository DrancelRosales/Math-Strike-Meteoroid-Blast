package mathstrike;

/** Validates raw player input. Only positive whole numbers are accepted. */
public final class InputValidator {
    private static final int MAX_DIGITS = 6;

    private InputValidator() { }

    public static int parseAnswer(String raw) throws InvalidAnswerException {
        if (raw == null || raw.trim().isEmpty()) {
            throw new InvalidAnswerException("Type a number first!");
        }
        String s = raw.trim();
        if (s.length() > MAX_DIGITS) {
            throw new InvalidAnswerException("That number is too long!");
        }
        try {
            int value = Integer.parseInt(s);
            if (value < 0 || s.startsWith("+") || s.startsWith("-")) {
                throw new InvalidAnswerException("Whole numbers only (no signs)!");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidAnswerException("Numbers only, please!");
        }
    }
}
