package mathstrike;

import java.util.Random;

/**
 * Creates random problems for a chosen Difficulty (Classic) or for the current
 * level (Time Attack). Every answer is a positive whole number and every
 * division has a whole-number quotient.
 */
public class ProblemGenerator {
    /** Largest factor used for x and / so that mental math stays fair. */
    private static final int TABLE_LIMIT = 12;

    private final Random random;
    private Difficulty current;

    public ProblemGenerator(Random random, Difficulty difficulty) {
        this.random = random;
        this.current = difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.current = difficulty;
    }

    public Difficulty getDifficulty() {
        return current;
    }

    /** Problem for the current Classic difficulty. */
    public Problem generate() {
        return build(current.getMin(), current.getMax(), current.getOperators(),
                current.usesOrderOfOperations() ? 0.25 : 0.0);
    }

    /** Problem for a Time Attack level (1, 2, 3, ...): grows harder as level rises. */
    public Problem generateForLevel(int level) {
        if (level <= 1) return build(1, 10, new char[]{'+', '-'}, 0);
        if (level == 2) return build(1, 20, new char[]{'+', '-'}, 0);
        if (level == 3) return build(1, 30, new char[]{'+', '-', '*'}, 0);
        if (level == 4) return build(1, 50, new char[]{'+', '-', '*', '/'}, 0);
        if (level == 5) return build(1, 50, new char[]{'+', '-', '*', '/'}, 0.25);
        int max = Math.min(100, 50 + (level - 5) * 10);
        return build(1, max, new char[]{'+', '-', '*', '/'}, 0.3);
    }

    private Problem build(int min, int max, char[] ops, double expressionChance) {
        if (expressionChance > 0 && random.nextDouble() < expressionChance) {
            return expression(max);
        }
        return basic(min, max, ops[random.nextInt(ops.length)]);
    }

    /** A single-operation problem: a op b. */
    public Problem basic(int min, int max, char op) {
        int a, b;
        switch (op) {
            case '+':
                a = between(min, max);
                b = between(min, max);
                return new Problem(a + " + " + b, a + b);
            case '-':
                a = between(min + 1, max);       // a > b so the answer stays positive
                b = between(min, a - 1);
                return new Problem(a + " − " + b, a - b);
            case '*':
                a = between(min, max);
                b = between(Math.max(min, 2), Math.min(max, TABLE_LIMIT));
                return random.nextBoolean()
                        ? new Problem(a + " × " + b, a * b)
                        : new Problem(b + " × " + a, a * b);
            case '/':
                b = between(2, Math.max(2, Math.min(max, TABLE_LIMIT)));
                int q = between(1, Math.max(1, max / b));
                a = b * q;                       // whole-number quotient guaranteed
                return new Problem(a + " ÷ " + b, q);
            default:
                throw new IllegalArgumentException("Unknown operator " + op);
        }
    }

    /** A multi-step problem that tests order of operations. */
    public Problem expression(int max) {
        int a, b, c, d;
        switch (random.nextInt(7)) {
            case 0: // a + b x c
                a = between(1, Math.min(max, 50));
                b = between(2, 9);
                c = between(2, 9);
                return new Problem(a + " + " + b + " × " + c, a + b * c);
            case 1: // (a + b) x c
                a = between(1, Math.min(max, 20));
                b = between(1, Math.min(max, 20));
                c = between(2, 9);
                return new Problem("(" + a + " + " + b + ") × " + c, (a + b) * c);
            case 2: // a x b - c
                a = between(2, 9);
                b = between(2, 9);
                c = between(1, a * b - 1);
                return new Problem(a + " × " + b + " − " + c, a * b - c);
            case 3: // a - b x c
                do {
                    b = between(2, 9);
                    c = between(2, 9);
                } while (b * c >= Math.min(max, 60));
                a = between(b * c + 1, Math.min(max, 60));
                return new Problem(a + " − " + b + " × " + c, a - b * c);
            case 4: // (a - b) x c
                a = between(2, Math.min(max, 40));
                b = between(1, a - 1);
                c = between(2, 9);
                return new Problem("(" + a + " − " + b + ") × " + c, (a - b) * c);
            case 5: // a x b + c x d
                a = between(2, 9);
                b = between(2, 9);
                c = between(2, 9);
                d = between(2, 9);
                return new Problem(a + " × " + b + " + " + c + " × " + d, a * b + c * d);
            default: // a / b + c
                b = between(2, 9);
                int q = between(2, 9);
                c = between(1, Math.min(max, 30));
                return new Problem(b * q + " ÷ " + b + " + " + c, q + c);
        }
    }

    /** Inclusive random int in [lo, hi]; collapses to lo when the range is empty. */
    private int between(int lo, int hi) {
        if (hi <= lo) return lo;
        return lo + random.nextInt(hi - lo + 1);
    }
}
