package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The living wallpaper behind the menus: the real themed world, with real
 * Meteors / Asteroids falling and (optionally) the cannon shooting them.
 */
public final class Backdrop {
    public static final int WIDTH = 1000;
    private static final int OFFSET = (WIDTH - Target.WORLD_WIDTH) / 2;

    private final Theme theme;
    private final boolean cannon;
    private final Random random = new Random();
    private final ProblemGenerator generator = new ProblemGenerator(random, Difficulty.HARD);
    private final List<Target> targets = new ArrayList<>();
    private final Effects fx = new Effects();
    private double aim = -Math.PI / 2;
    private double recoil;
    private double fireTimer = 1.8;

    public Backdrop(Theme theme, boolean cannon) {
        this.theme = theme;
        this.cannon = cannon;
        for (int i = 0; i < 5; i++) spawn(true);
    }

    private void spawn(boolean scatter) {
        Problem p = generator.generate();
        double speed = 55 + random.nextInt(30);
        Target t = theme instanceof PrehistoricTheme ? new Meteor(p, speed, random) : new Asteroid(p, speed, random);
        t.placeAt(90 + random.nextDouble() * 620);
        t.setY(scatter ? -t.getRadius() + random.nextDouble() * 460 : -t.getRadius());
        targets.add(t);
    }

    public void update(double dt) {
        for (int i = targets.size() - 1; i >= 0; i--) {
            Target t = targets.get(i);
            t.move(dt);
            if (t.getY() - t.getRadius() > Target.GROUND_Y) { targets.remove(i); spawn(false); }
        }
        Target lowest = null;
        for (Target t : targets) if (lowest == null || t.getY() > lowest.getY()) lowest = t;
        if (cannon && lowest != null) {
            double goal = Math.atan2(lowest.getY() - (Target.GROUND_Y - 22), lowest.getX() - Target.WORLD_WIDTH / 2.0);
            goal = Math.max(-Math.PI + 0.25, Math.min(-0.25, goal));
            aim += (goal - aim) * Math.min(1, dt * 6);
            fireTimer -= dt;
            if (fireTimer <= 0 && lowest.getY() > 160) {
                double[] m = theme.muzzle(aim, 0);
                fx.beam(m[0], m[1], lowest.getX(), lowest.getY(), theme.getBeamColor());
                lowest.explode(fx, false);
                targets.remove(lowest);
                spawn(false);
                recoil = 1;
                fireTimer = 1.6 + random.nextDouble() * 1.4;
            }
        }
        recoil = Math.max(0, recoil - dt * 4);
        fx.update(dt);
        Sprites.prune(aliveIds());
    }

    private java.util.Set<Integer> aliveIds() {
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        for (Target t : targets) ids.add(t.getId());
        return ids;
    }

    public void paint(Graphics2D g, double t) {
        theme.paintBackdrop(g, t, WIDTH);
        g.translate(OFFSET, 0);
        fx.paintScars(g);
        for (Target target : targets) target.draw(g, t);
        if (cannon) theme.drawTurret(g, aim, recoil, t);
        fx.paint(g);
        g.translate(-OFFSET, 0);
        g.setColor(new Color(4, 2, 16, 110));
        g.fillRect(0, 0, WIDTH, Theme.HEIGHT);
    }
}
