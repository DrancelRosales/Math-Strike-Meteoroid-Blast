package mathstrike;

import java.util.Random;

/** Classic rules: chosen difficulty, hearts, meteors; prehistoric background. */
public class ClassicMode extends GameMode {
    private final Difficulty difficulty;
    private final Health health;

    public ClassicMode(Random random, Difficulty difficulty) {
        super(random, new ProblemGenerator(random, difficulty));
        this.difficulty = difficulty;
        this.health = new Health(difficulty.getHearts());
    }

    @Override public String getKey() { return "classic"; }
    @Override public String getName() { return "Classic Mode"; }
    /** Classic Mode is always set in the prehistoric valley. */
    @Override public String getTheme() { return "prehistoric"; }
    @Override public String getSubtitle() { return difficulty.getLabel(); }

    @Override public void start() { /* hearts are full from construction */ }

    @Override public void update(double dt) { /* no timer in Classic Mode */ }

    /** The game slowly speeds up the more targets the player destroys. */
    private double ramp() {
        return Math.min(1.5, 1.0 + score.getCorrect() * 0.01);
    }

    @Override
    public int onCorrect(AnswerRecord record, double heightRatio) {
        return score.recordCorrect(record, heightRatio);
    }

    @Override
    public void onMiss(AnswerRecord record) {
        score.recordMiss(record);
        health.loseHeart();
    }

    @Override public boolean isGameOver() { return health.isDead(); }

    @Override public Problem nextProblem() { return generator.generate(); }

    @Override
    public Target createTarget(Problem problem) {
        double fallSeconds = difficulty.getSeconds() / ramp();
        double speed = (Target.GROUND_Y + 60) / fallSeconds;
        return new Meteor(problem, speed, random);
    }

    @Override public double getSpawnInterval() { return difficulty.getSpawnSeconds() / ramp(); }

    @Override public int getMaxTargets() { return 3 + difficulty.ordinal(); }

    @Override public int getHearts() { return health.getHearts(); }
    @Override public int getMaxHearts() { return health.getMaxHearts(); }
}
