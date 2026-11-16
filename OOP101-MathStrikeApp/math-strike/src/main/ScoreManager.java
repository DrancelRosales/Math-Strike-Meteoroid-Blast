package mathstrike;

import java.util.ArrayList;
import java.util.List;

/** Tracks score, streak, multiplier and accuracy. All state is private (encapsulation). */
public class ScoreManager {
    public static final int BASE_POINTS = 100;
    public static final int MAX_MULTIPLIER = 5;
    private static final int STREAK_PER_MULTIPLIER = 3;

    private int score;
    private int streak;
    private int bestStreak;
    private int correct;
    private int total;   // correct answers + wrong answers + targets that hit the ground
    private final List<AnswerRecord> records = new ArrayList<>();

    /** x1 at the start, +1 for every 3 correct answers in a row, capped at x5. */
    public int getMultiplier() {
        return Math.min(MAX_MULTIPLIER, 1 + streak / STREAK_PER_MULTIPLIER);
    }

    /**
     * Records a correct answer.
     * @param heightRatio 0 (just spawned) .. 1 (about to land); smaller = faster = bigger bonus
     * @return points awarded
     */
    public int recordCorrect(AnswerRecord record, double heightRatio) {
        streak++;
        bestStreak = Math.max(bestStreak, streak);
        correct++;
        total++;
        int speedBonus = (int) Math.round(50 * (1 - Math.max(0, Math.min(1, heightRatio))));
        int points = (BASE_POINTS + speedBonus) * getMultiplier();
        addScore(points);
        records.add(record);
        return points;
    }

    /** Adds points to the score (never lets the score go below zero). */
    public void addScore(int points) {
        score = Math.max(0, score + points);
    }

    /** Records a wrong answer or a target that reached the ground; resets the streak. */
    public void recordMiss(AnswerRecord record) {
        streak = 0;
        total++;
        records.add(record);
    }

    public int getScore() { return score; }
    public int getStreak() { return streak; }
    public int getBestStreak() { return bestStreak; }
    public int getCorrect() { return correct; }
    public int getTotal() { return total; }

    /** Accuracy as a percentage 0..100. */
    public double getAccuracy() {
        return total == 0 ? 0 : 100.0 * correct / total;
    }

    /** Congratulatory text for the results screen. */
    public String getCongratulation() {
        double acc = getAccuracy();
        if (total == 0) return "Give it another go - the meteors are waiting!";
        if (acc >= 90) return "Outstanding! You're a mental-math machine!";
        if (acc >= 75) return "Great job! Your calculations are getting sharp!";
        if (acc >= 50) return "Good effort! Practice makes perfect!";
        return "Keep going - every blast makes you faster!";
    }

    /** The round's answer records as an array, in the order they happened. */
    public AnswerRecord[] getRecords() {
        return records.toArray(new AnswerRecord[0]);
    }
}
