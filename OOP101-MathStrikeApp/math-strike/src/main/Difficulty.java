package mathstrike;

/** Rules of each Classic Mode level. The rules are hidden behind simple getters. */
public enum Difficulty {
    //        label     min max operators                  hearts fallSec spawnSec orderOfOps
    EASY("Easy",        1,  10,  new char[]{'+', '-'},               3, 14.0, 3.8, false),
    MEDIUM("Medium",    1,  50,  new char[]{'+', '-', '*'},          4, 11.0, 3.2, false),
    HARD("Hard",        1, 100,  new char[]{'+', '-', '*', '/'},     5,  9.0, 2.6, true);

    private final String label;
    private final int min;
    private final int max;
    private final char[] operators;
    private final int hearts;
    private final double seconds;      // time a target needs to fall to the ground
    private final double spawnSeconds; // time between new targets
    private final boolean orderOfOperations;

    Difficulty(String label, int min, int max, char[] operators, int hearts,
               double seconds, double spawnSeconds, boolean orderOfOperations) {
        this.label = label;
        this.min = min;
        this.max = max;
        this.operators = operators;
        this.hearts = hearts;
        this.seconds = seconds;
        this.spawnSeconds = spawnSeconds;
        this.orderOfOperations = orderOfOperations;
    }

    public String getLabel() { return label; }
    public int getMin() { return min; }
    public int getMax() { return max; }
    public char[] getOperators() { return operators.clone(); }
    public int getHearts() { return hearts; }
    public double getSeconds() { return seconds; }
    public double getSpawnSeconds() { return spawnSeconds; }
    public boolean usesOrderOfOperations() { return orderOfOperations; }

    /** Parses "easy", "MEDIUM", ... ; throws IllegalArgumentException when unknown. */
    public static Difficulty fromString(String s) {
        if (s == null) throw new IllegalArgumentException("difficulty missing");
        return valueOf(s.trim().toUpperCase());
    }
}
