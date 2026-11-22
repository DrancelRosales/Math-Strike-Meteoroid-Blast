package mathstrike;

/** Tracks the player's hearts (Classic Mode). */
public class Health {
    private final int maxHearts;
    private int hearts;

    public Health(int hearts) {
        this.maxHearts = hearts;
        this.hearts = hearts;
    }

    public void loseHeart() {
        if (hearts > 0) hearts--;
    }

    public int getHearts() { return hearts; }

    public int getMaxHearts() { return maxHearts; }

    public boolean isDead() { return hearts <= 0; }
}
