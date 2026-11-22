package mathstrike;

import java.util.Random;

/**
 * Dependency-free checks of the game rules. Run with:  ./build.sh test
 * Exits with a non-zero status if any check fails.
 */
public class SelfTest {
    private static int failures = 0;
    private static int checks = 0;

    private static void check(boolean ok, String what) {
        checks++;
        if (!ok) {
            failures++;
            System.out.println("FAIL: " + what);
        }
    }

    public static void main(String[] args) throws Exception {
        Random rnd = new Random(42);

        // --- Problem generation: ranges, operators, whole-number results ---
        for (Difficulty d : Difficulty.values()) {
            ProblemGenerator g = new ProblemGenerator(rnd, d);
            for (int i = 0; i < 20000; i++) {
                Problem p = g.generate();
                check(p.getAnswer() > 0, d + " answer positive: " + p.getQuestion());
                check(evaluate(p.getQuestion()) == p.getAnswer(),
                        d + " answer matches question: " + p.getQuestion() + " = " + p.getAnswer());
                String q = p.getQuestion();
                if (d == Difficulty.EASY) {
                    check(!q.contains("×") && !q.contains("÷") && !q.contains("("),
                            "EASY only + and -: " + q);
                    check(p.getAnswer() <= 20, "EASY small answers: " + q);
                }
                if (d == Difficulty.MEDIUM) {
                    check(!q.contains("÷") && !q.contains("("), "MEDIUM has no division/expressions: " + q);
                }
            }
        }
        ProblemGenerator tg = new ProblemGenerator(rnd, Difficulty.HARD);
        for (int level = 1; level <= 12; level++) {
            for (int i = 0; i < 5000; i++) {
                Problem p = tg.generateForLevel(level);
                check(p.getAnswer() > 0 && evaluate(p.getQuestion()) == p.getAnswer(),
                        "TA level " + level + ": " + p.getQuestion());
                if (level <= 2) check(!p.getQuestion().contains("×"), "TA low level only +/-");
            }
        }

        // --- Input validation ---
        check(InputValidator.parseAnswer("42") == 42, "parse 42");
        check(InputValidator.parseAnswer(" 7 ") == 7, "parse trimmed");
        for (String bad : new String[]{"", "  ", null, "abc", "-5", "+5", "3.5", "1234567", "4 2"}) {
            try {
                InputValidator.parseAnswer(bad);
                check(false, "should reject '" + bad + "'");
            } catch (InvalidAnswerException e) {
                check(true, "rejected");
            }
        }

        // --- Scoring ---
        ScoreManager sm = new ScoreManager();
        AnswerRecord ok = new AnswerRecord("1 + 1", "2", 2, AnswerRecord.Outcome.CORRECT);
        AnswerRecord bad = new AnswerRecord("1 + 1", "3", 2, AnswerRecord.Outcome.WRONG);
        check(sm.getMultiplier() == 1, "start x1");
        sm.recordCorrect(ok, 1); sm.recordCorrect(ok, 1);
        check(sm.getMultiplier() == 1, "streak 2 still x1");
        sm.recordCorrect(ok, 1);
        check(sm.getMultiplier() == 2, "streak 3 -> x2");
        for (int i = 0; i < 20; i++) sm.recordCorrect(ok, 1);
        check(sm.getMultiplier() == ScoreManager.MAX_MULTIPLIER, "multiplier capped");
        sm.recordMiss(bad);
        check(sm.getStreak() == 0 && sm.getMultiplier() == 1, "miss resets streak");
        check(sm.getCorrect() == 23 && sm.getTotal() == 24, "counts");
        check(Math.abs(sm.getAccuracy() - 100.0 * 23 / 24) < 1e-9, "accuracy");
        check(sm.getBestStreak() == 23, "best streak kept");
        check(new ScoreManager().getAccuracy() == 0, "accuracy with no answers is 0");

        // --- Classic Mode: hearts, game over ---
        for (Difficulty d : Difficulty.values()) {
            Game g = new Game(new ClassicMode(rnd, d), rnd);
            g.start();
            check(g.getMode().getHearts() == d.getHearts(), d + " hearts");
            g.update(Game.READY_SECONDS + 0.1);
            check(g.getPhase() == Game.Phase.PLAYING, "ready -> playing");
            g.update(0.1);
            check(g.getTargets().size() == 1, "first target spawns at once");
            check(g.getTargets().get(0) instanceof Meteor, "Classic spawns Meteors");
            // blast the first target
            int ans = g.getTargets().get(0).getProblem().getAnswer();
            g.checkAnswer(String.valueOf(ans));
            check(g.getMode().getScore().getCorrect() == 1, "correct answer scored");
            check(g.getTargets().isEmpty(), "target destroyed");
            // invalid input costs nothing
            int hearts = g.getMode().getHearts();
            g.checkAnswer("abc"); g.checkAnswer("");
            check(g.getMode().getHearts() == hearts, "invalid input is free");
            // wrong answers cost one heart each until dead
            int guard = 0;
            while (g.getPhase() == Game.Phase.PLAYING && guard++ < 50) {
                g.checkAnswer("999999"); // never a real answer here
                check(g.getMode().getHearts() == Math.max(0, hearts - guard), "wrong answer -1 heart");
                if (g.getPhase() == Game.Phase.PLAYING) check(hearts - guard > 0, "still alive");
            }
            check(g.getPhase() == Game.Phase.GAME_OVER, d + " game over when hearts = 0");
        }

        // --- Meteors that hit the ground cost hearts ---
        {
            Game g = new Game(new ClassicMode(rnd, Difficulty.EASY), rnd);
            g.start();
            for (int i = 0; i < 400 && g.getPhase() != Game.Phase.GAME_OVER; i++) g.update(0.25);
            check(g.getPhase() == Game.Phase.GAME_OVER, "idle player eventually loses");
            check(g.getMode().getHearts() == 0, "hearts at zero");
            check(g.getMode().getScore().getAccuracy() == 0, "idle accuracy 0");
        }

        // --- Time Attack ---
        {
            TimeAttackMode m = new TimeAttackMode(rnd);
            Game g = new Game(m, rnd);
            g.start();
            g.update(Game.READY_SECONDS + 0.1);
            g.update(0.1);
            check(g.getTargets().get(0) instanceof Asteroid, "Time Attack spawns Asteroids");
            double before = m.getTimeLeft();
            g.checkAnswer(String.valueOf(g.getTargets().get(0).getProblem().getAnswer()));
            check(m.getTimeLeft() > before, "correct answer adds time");
            for (int i = 0; i < 3; i++) {
                g.spawnTarget();
                Target t = g.getTargets().get(g.getTargets().size() - 1);
                g.checkAnswer(String.valueOf(t.getProblem().getAnswer()));
            }
            check(m.getLevel() >= 2, "level rises with correct answers");
            check(m.getTimeLeft() <= TimeAttackMode.MAX_SECONDS, "time capped");
            while (g.getPhase() != Game.Phase.GAME_OVER) g.update(0.25);
            check(m.getTimeLeft() == 0, "ends when time runs out");
        }

        // --- Polymorphism: each target draws and explodes in its own way ---
        System.setProperty("java.awt.headless", "true");
        Target m = new Meteor(new Problem("1 + 1", 2), 50, rnd);
        Target a = new Asteroid(new Problem("1 + 1", 2), 50, rnd);
        m.placeAt(100); a.placeAt(100); m.setY(100); a.setY(100);
        java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(300, 300, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.image.BufferedImage ia = new java.awt.image.BufferedImage(300, 300, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D gm = im.createGraphics(); m.draw(gm, 0); gm.dispose();
        java.awt.Graphics2D ga = ia.createGraphics(); a.draw(ga, 0); ga.dispose();
        boolean differs = false, drew = false;
        for (int x = 0; x < 300; x++) for (int y = 0; y < 300; y++) {
            if (im.getRGB(x, y) != ia.getRGB(x, y)) differs = true;
            if ((im.getRGB(x, y) >>> 24) != 0) drew = true;
        }
        check(drew && differs, "Meteor and Asteroid draw different pictures");
        m.explode(new Effects(), false);
        a.explode(new Effects(), true);
        a.placeAt(400);
        double y0 = a.getY();
        a.move(1);
        check(a.getY() > y0, "target falls");

        // --- Pixel font covers every symbol the problems can contain ---
        for (Difficulty d : Difficulty.values()) {
            ProblemGenerator g = new ProblemGenerator(rnd, d);
            for (int i = 0; i < 2000; i++) {
                String q = g.generate().getQuestion();
                check(PixelFont.width(q, 2) > 0 && q.chars().allMatch(c -> " 0123456789+()\u2212\u00d7\u00f7".indexOf(c) >= 0),
                        "font supports: " + q);
            }
        }

        System.out.println(checks + " checks, " + failures + " failures");
        if (failures > 0) System.exit(1);
    }

    /** Independent evaluator for the display strings (supports + - x / and parentheses). */
    private static long evaluate(String expr) {
        String s = expr.replace("×", "*").replace("÷", "/").replace("−", "-").replace(" ", "");
        return new Object() {
            int pos = 0;
            long parseExpr() {
                long v = parseTerm();
                while (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) {
                    char op = s.charAt(pos++);
                    long r = parseTerm();
                    v = op == '+' ? v + r : v - r;
                }
                return v;
            }
            long parseTerm() {
                long v = parseFactor();
                while (pos < s.length() && (s.charAt(pos) == '*' || s.charAt(pos) == '/')) {
                    char op = s.charAt(pos++);
                    long r = parseFactor();
                    if (op == '/') {
                        if (r == 0 || v % r != 0) throw new IllegalStateException("non-whole division in " + expr);
                        v = v / r;
                    } else v = v * r;
                }
                return v;
            }
            long parseFactor() {
                if (s.charAt(pos) == '(') {
                    pos++;
                    long v = parseExpr();
                    pos++; // ')'
                    return v;
                }
                int start = pos;
                while (pos < s.length() && Character.isDigit(s.charAt(pos))) pos++;
                return Long.parseLong(s.substring(start, pos));
            }
        }.parseExpr();
    }
}
