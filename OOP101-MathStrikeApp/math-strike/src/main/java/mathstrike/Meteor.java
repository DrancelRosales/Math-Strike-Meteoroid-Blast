package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Classic Mode target: a charred, glowing meteor that streaks diagonally and bounces off the walls. */
public class Meteor extends Target {
    private static final Color[] FLAME = {new Color(0xffd166), new Color(0xff9a3c), new Color(0xe0562a)};

    private double drift; // px/s sideways

    public Meteor(Problem problem, double speed, Random random) {
        super(problem, speed);
        double d = 10 + random.nextInt(26);
        this.drift = random.nextBoolean() ? d : -d;
    }

    @Override
    public void move(double dt) {
        y += getSpeed() * dt;
        x += drift * dt;
        int r = getRadius();
        if (x < r) { x = r; drift = Math.abs(drift); }
        if (x > WORLD_WIDTH - r) { x = WORLD_WIDTH - r; drift = -Math.abs(drift); }
    }

    @Override
    public void draw(Graphics2D g, double time) {
        // flaming tail, opposite to the direction of travel
        double len = Math.hypot(drift, getSpeed());
        double ux = drift / len, uy = getSpeed() / len;
        int r = getRadius();
        for (int i = 7; i >= 1; i--) {
            double wob = Math.sin(time * 18 + i * 1.7) * 5;
            int alpha = (int) (255 * Math.max(0.1, 0.85 - i * 0.1));
            Color base = FLAME[Math.min(2, i / 3)];
            PixelArt.disc(g, x - ux * i * 10 + wob, y - uy * i * 10 - r * 0.2 * (i / 7.0),
                    Math.max(6, r * (0.85 - i * 0.1)), new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
        }
        BufferedImage rock = Sprites.rock(this);
        g.drawImage(rock, (int) Math.round((x - rock.getWidth() / 2.0) / 2) * 2,
                (int) Math.round((y - rock.getHeight() / 2.0) / 2) * 2, null);
        drawLabel(g);
    }

    @Override
    public void explode(Effects fx, boolean big) {
        fx.fireball(x, y, getRadius(), big);
    }
}
