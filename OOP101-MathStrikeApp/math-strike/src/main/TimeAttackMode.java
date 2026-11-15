package mathstrike;

import java.util.Random;

/**
 * Time Attack rules: a countdown that grows with every correct answer while
 * the problems get harder. Space-themed. The round ends when time runs out.
 */
public class TimeAttackMode extends GameMode {
    public static final double START_SECONDS = 60;
    public static final double MAX_SECONDS = 99;
    public static final double MISS_PENALTY = 2;
    private static final int CORRECT_PER_LEVEL = 3;

    private double timeLeft = START_SECONDS;
    private int level = 1;
    private final double bonusSeconds = 4;

    public TimeAttackMode(Random random) {
        super(random, new ProblemGenerator(random, Difficulty.HARD));
    }

    @Override public String getKey() { return "timeattack"; }
    @Override public String getName() { return "Time Attack"; }
    @Override public String getTheme() { return "space"; }
    @Override public String getSubtitle() { return "Level " + level; }

    @Override public void start() { timeLeft = START_SECONDS; level = 1; }

    @Override
    public void update(double dt) {
        timeLeft = Math.max(0, timeLeft - dt);
    }

    @Override
    public int onCorrect(AnswerRecord record, double heightRatio) {
        int points = score.recordCorrect(record, heightRatio);
        timeLeft = Math.min(MAX_SECONDS, timeLeft + bonusSeconds);
        if (score.getCorrect() % CORRECT_PER_LEVEL == 0) level++;
        return points;
    }

    @Override
    public void onMiss(AnswerRecord record) {
        score.recordMiss(record);
        timeLeft = Math.max(0, timeLeft - MISS_PENALTY);
    }

    @Override public boolean isGameOver() { return timeLeft <= 0; }

    @Override public Problem nextProblem() { return generator.generateForLevel(level); }

    @Override
    public Target createTarget(Problem problem) {
        double fallSeconds = Math.max(6.0, 12.0 - 0.6 * (level - 1));
        double speed = (Target.GROUND_Y + 60) / fallSeconds;
        return new Asteroid(problem, speed, random);
    }

    @Override
    public double getSpawnInterval() { return Math.max(1.4, 3.2 - 0.2 * (level - 1)); }

    @Override
    public int getMaxTargets() { return Math.min(6, 3 + level / 2); }

    @Override public double getTimeLeft() { return timeLeft; }
    @Override public int getLevel() { return level; }
    @Override public double getBonusSeconds() { return bonusSeconds; }
}
