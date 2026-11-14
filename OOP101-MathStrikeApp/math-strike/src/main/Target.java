package mathstrike;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Abstract parent of every falling target. It defines WHAT a target must do
 * (move, draw, explode) while subclasses decide HOW.
 */
public abstract class Target {
    public static final int WORLD_WIDTH = 800;
    public static final int WORLD_HEIGHT = 600;
    public static final int GROUND_Y = 540;

    private static int idCounter = 0;

    private final int id;
    private final Problem problem;
    private final double speed;   // pixels per second, downward
    private final int radius;
    protected double x;
    protected double y;

    protected Target(Problem problem, double speed) {
        this.id = ++idCounter;
        this.problem = problem;
        this.speed = speed;
        this.radius = Math.max(42, Math.min(88, 30 + 5 * problem.getQuestion().length()));
        this.y = -radius;
    }

    public int getId() { return id; }
    public Problem getProblem() { return problem; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSpeed() { return speed; }
    public int getRadius() { return radius; }

    /** Puts the target at a horizontal position. Subclasses may override. */
    public void placeAt(double newX) {
        this.x = newX;
    }

    /** Moves the target vertically (used by menu decorations). */
    public void setY(double newY) {
        this.y = newY;
    }

    /** Advance the target by dt seconds. */
    public abstract void move(double dt);

    /** Draws this target (rock, trail and the math problem) at its current position. */
    public abstract void draw(Graphics2D g, double time);

    /** Spawns this kind of target's explosion at its position. */
    public abstract void explode(Effects fx, boolean big);

    public boolean hasReachedGround() {
        return y + radius >= GROUND_Y;
    }

    /** Draws the math problem centred on the target (shared by all subclasses). */
    protected void drawLabel(Graphics2D g) {
        BufferedImage label = Sprites.label(this);
        g.drawImage(label, (int) Math.round(x - label.getWidth() / 2.0),
                (int) Math.round(y - label.getHeight() / 2.0), null);
    }
}
