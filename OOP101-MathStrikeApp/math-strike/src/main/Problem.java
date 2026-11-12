package mathstrike;

/** One math question and its (whole-number) answer. Immutable. */
public class Problem {
    private final String question;
    private final int answer;

    public Problem(String question, int answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public int getAnswer() {
        return answer;
    }

    public boolean isCorrect(int guess) {
        return guess == answer;
    }
}
