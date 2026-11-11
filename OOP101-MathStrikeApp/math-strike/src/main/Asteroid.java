package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Time Attack target: a tumbling rocky asteroid that sways side to side as it falls. */
public class Asteroid extends Target {
    private final double amplitude;
    private final double frequency;
    private final double phase;
    private final double spin;
    private double baseX;
    private double age = 0;
    private double angle;

    public Asteroid(Problem problem, double speed, Random random) {
        super(problem, speed);
        this.amplitude = 20 + random.nextInt(26);
        this.frequency = 1.2 + random.nextDouble();
        this.phase = random.nextDouble() * Math.PI * 2;
        this.spin = (random.nextBoolean() ? 1 : -1) * (0.3 + random.nextDouble() * 0.6);
        this.angle = random.nextDouble() * Math.PI * 2;
    }

    @Override
    public void placeAt(double newX) {
        double margin = getRadius() + amplitude;
        this.baseX = Math.max(margin, Math.min(WORLD_WIDTH - margin, newX));
        this.x = baseX;
    }

    @Override
    public void move(double dt) {
        age += dt;
        y += getSpeed() * dt;
        x = baseX + amplitude * Math.sin(phase + frequency * age);
        angle += spin * dt;
    }

    @Override
    public void draw(Graphics2D g, double time) {
        int r = getRadius();
        // faint dust trail
        for (int i = 1; i <= 4; i++) {
            PixelArt.rect(g, x + Math.sin(time * 9 + i) * 4 - 2, y - r - i * 12, 4, 4,
                    new Color(167, 169, 192, 200 - i * 40));
        }
        BufferedImage rock = Sprites.rock(this);
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(angle);
        g.drawImage(rock, -rock.getWidth() / 2, -rock.getHeight() / 2, null);
        g.setTransform(old);
        drawLabel(g);
    }

    @Override
    public void explode(Effects fx, boolean big) {
        fx.debris(x, y, getRadius(), big);
    }
}
