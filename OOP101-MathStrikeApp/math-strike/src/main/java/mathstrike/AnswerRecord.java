package mathstrike;

/** One line of the end-of-round accuracy report. */
public record AnswerRecord(String question, String given, int correctAnswer, Outcome outcome) {
    public enum Outcome { CORRECT, WRONG, MISSED }
}
