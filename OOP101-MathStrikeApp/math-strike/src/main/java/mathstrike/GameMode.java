package mathstrike;

import java.util.Random;

/** Parent of the two game modes; defines the rules each mode must follow. */
public abstract class GameMode {
    protected final ScoreManager score = new ScoreManager();
    protected final ProblemGenerator generator;
    protected final Random random;

    protected GameMode(Random random, ProblemGenerator generator) {
        this.random = random;
        this.generator = generator;
    }

    public ScoreManager getScore() { return score; }

    /** Machine-readable key used by the browser ("classic" / "timeattack"). */
    public abstract String getKey();
    public abstract String getName();
    /** Background theme: "prehistoric" or "space". */
    public abstract String getTheme();
    /** Short label shown in the HUD: difficulty name or current level. */
    public abstract String getSubtitle();

    public abstract void start();
    /** Called every frame while playing (Time Attack runs its countdown here). */
    public abstract void update(double dt);
    /** A target was destroyed; returns the points earned. */
    public abstract int onCorrect(AnswerRecord record, double heightRatio);
    /** A wrong answer or ground hit. */
    public abstract void onMiss(AnswerRecord record);
    public abstract boolean isGameOver();

    public abstract Problem nextProblem();
    /** Factory method: Classic spawns Meteors, Time Attack spawns Asteroids. */
    public abstract Target createTarget(Problem problem);

    public abstract double getSpawnInterval();
    public abstract int getMaxTargets();

    // Optional HUD values; modes that do not use them keep the defaults.
    public int getHearts() { return -1; }
    public int getMaxHearts() { return -1; }
    public double getTimeLeft() { return -1; }
    public int getLevel() { return 0; }
    public double getBonusSeconds() { return 0; }
}
