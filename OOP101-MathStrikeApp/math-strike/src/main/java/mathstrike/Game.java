package mathstrike;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Controls the main game loop: spawning, movement, answers, and the end of the round. */
public class Game {
    public enum Phase { READY, PLAYING, GAME_OVER }

    public static final double READY_SECONDS = 3.0;

    private static final String[] CORRECT_MESSAGES =
            {"Nailed it!", "Direct hit!", "Boom!", "Nice shot!", "Calculated!", "Bullseye!"};
    private static final String[] WRONG_MESSAGES =
            {"Try Again!", "You can do better", "Not quite!", "Check your math!"};
    private static final String[] GROUND_MESSAGES =
            {"Too slow!", "It landed!", "Impact!"};

    private final GameMode mode;
    private final Random random;
    private final List<Target> targets = new ArrayList<>();
    private final List<GameEvent> events = new ArrayList<>();

    private Phase phase = Phase.READY;
    private double readyLeft = READY_SECONDS;
    private double spawnTimer = 0;

    public Game(GameMode mode, Random random) {
        this.mode = mode;
        this.random = random;
    }

    public void start() {
        mode.start();
        targets.clear();
        events.clear();
        phase = Phase.READY;
        readyLeft = READY_SECONDS;
        spawnTimer = 0;
    }

    /** Advance the game by dt seconds. */
    public void update(double dt) {
        if (phase == Phase.READY) {
            readyLeft -= dt;
            if (readyLeft <= 0) {
                phase = Phase.PLAYING;
                spawnTimer = 0;
            }
            return;
        }
        if (phase != Phase.PLAYING) return;

        mode.update(dt);

        for (Iterator<Target> it = targets.iterator(); it.hasNext() && !mode.isGameOver(); ) {
            Target t = it.next();
            t.move(dt);
            if (t.hasReachedGround()) {
                it.remove();
                Problem p = t.getProblem();
                mode.onMiss(new AnswerRecord(p.getQuestion(), "-", p.getAnswer(),
                        AnswerRecord.Outcome.MISSED));
                events.add(new GameEvent("ground", t.getX(), Target.GROUND_Y, "", "bad", t));
                say(pick(GROUND_MESSAGES), "bad");
            }
        }

        if (mode.isGameOver()) {
            endRound();
            return;
        }

        spawnTimer -= dt;
        if (targets.isEmpty() && spawnTimer > 0.5) spawnTimer = 0.5; // don't leave the sky empty
        if (spawnTimer <= 0 && targets.size() < mode.getMaxTargets()) {
            spawnTarget();
            spawnTimer = mode.getSpawnInterval();
        }
    }

    /** Creates a new target with a fresh problem at a free spot near the top. */
    public void spawnTarget() {
        Problem problem = mode.nextProblem();
        for (int i = 0; i < 10 && answerOnScreen(problem.getAnswer()); i++) {
            problem = mode.nextProblem(); // avoid two targets sharing an answer
        }
        Target t = mode.createTarget(problem);
        double chosen = Target.WORLD_WIDTH / 2.0;
        for (int i = 0; i < 12; i++) {
            double candidate = t.getRadius() + 30
                    + random.nextDouble() * (Target.WORLD_WIDTH - 2 * (t.getRadius() + 30));
            chosen = candidate;
            if (isFreeNearTop(candidate, t.getRadius())) break;
        }
        t.placeAt(chosen);
        targets.add(t);
    }

    private boolean answerOnScreen(int answer) {
        for (Target t : targets) {
            if (t.getProblem().getAnswer() == answer) return true;
        }
        return false;
    }

    private boolean isFreeNearTop(double x, int radius) {
        for (Target other : targets) {
            if (other.getY() < 180 && Math.abs(other.getX() - x) < other.getRadius() + radius + 20) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates the player's input and checks it against the falling targets.
     * Invalid input (empty, non-numeric) never costs a heart.
     * @return true if a target was destroyed
     */
    public boolean checkAnswer(String raw) {
        if (phase != Phase.PLAYING) return false;

        int guess;
        try {
            guess = InputValidator.parseAnswer(raw);
        } catch (InvalidAnswerException e) {
            say(e.getMessage(), "info");
            return false;
        }

        Target hit = null; // if two targets share the answer, blast the lowest one
        for (Target t : targets) {
            if (t.getProblem().isCorrect(guess) && (hit == null || t.getY() > hit.getY())) hit = t;
        }

        if (hit != null) {
            targets.remove(hit);
            Problem p = hit.getProblem();
            double ratio = Math.max(0, hit.getY()) / Target.GROUND_Y;
            int before = mode.getScore().getMultiplier();
            int points = mode.onCorrect(new AnswerRecord(p.getQuestion(), String.valueOf(guess),
                    p.getAnswer(), AnswerRecord.Outcome.CORRECT), ratio);
            events.add(new GameEvent("destroy", hit.getX(), hit.getY(), "", "good", hit));
            events.add(new GameEvent("points", hit.getX(), hit.getY(), "+" + points, "good", null));
            int after = mode.getScore().getMultiplier();
            say(after > before ? "Streak! x" + after + " multiplier!" : pick(CORRECT_MESSAGES), "good");
            if (mode.getBonusSeconds() > 0) {
                events.add(new GameEvent("bonus", Target.WORLD_WIDTH / 2.0, 90,
                        "+" + (int) mode.getBonusSeconds() + "s", "good", null));
            }
            return true;
        } else {
            // Wrong answer: the player's base takes a hit.
            Target lowest = null;
            for (Target t : targets) if (lowest == null || t.getY() > lowest.getY()) lowest = t;
            AnswerRecord rec = lowest != null
                    ? new AnswerRecord(lowest.getProblem().getQuestion(), String.valueOf(guess),
                            lowest.getProblem().getAnswer(), AnswerRecord.Outcome.WRONG)
                    : new AnswerRecord("-", String.valueOf(guess), 0, AnswerRecord.Outcome.WRONG);
            mode.onMiss(rec);
            events.add(new GameEvent("wrong", Target.WORLD_WIDTH / 2.0, Target.GROUND_Y, "", "bad", null));
            say(pick(WRONG_MESSAGES), "bad");
            if (mode.isGameOver()) endRound();
            return false;
        }
    }

    /** Stops the round and announces it to the browser. */
    public void endRound() {
        if (phase == Phase.GAME_OVER) return;
        phase = Phase.GAME_OVER;
        events.add(new GameEvent("gameover", 0, 0, "", "info", null));
    }

    private void say(String text, String tone) {
        events.add(new GameEvent("feedback", Target.WORLD_WIDTH / 2.0, 150, text, tone, null));
    }

    private String pick(String[] options) {
        return options[random.nextInt(options.length)];
    }

    /** Returns and clears the pending events. */
    public List<GameEvent> drainEvents() {
        List<GameEvent> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }

    public Phase getPhase() { return phase; }
    public double getReadyLeft() { return Math.max(0, readyLeft); }
    public GameMode getMode() { return mode; }
    public List<Target> getTargets() { return targets; }
}
