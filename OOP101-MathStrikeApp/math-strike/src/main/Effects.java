package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Particles, beams, shockwaves, floating numbers, screen shake and scorch marks. */
public final class Effects {
    private static final Color[] FIRE = {new Color(0xfff1a8), new Color(0xffb347), new Color(0xff6a2b), new Color(0xd9421f)};
    private static final Color[] ROCK = {new Color(0xd7d9e6), new Color(0x8e90a8), new Color(0x6ff3ff), Color.WHITE};

    private static final class Particle {
        double x, y, vx, vy, gravity, life, age;
        int size;
        Color color;
    }

    private static final class Beam { double x0, y0, x1, y1, age; Color color; }
    private static final class Ring { double x, y, age; Color color; }
    private static final class Scar { double x, age; }
    private static final class Floating { String text; double x, y, vy, age, life; Color color; int scale; }

    private final Random random = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Beam> beams = new ArrayList<>();
    private final List<Ring> rings = new ArrayList<>();
    private final List<Scar> scars = new ArrayList<>();
    private final List<Floating> floats = new ArrayList<>();

    private String feedbackText;
    private Color feedbackColor;
    private double feedbackAge;
    private double shake;
    private double flash;

    // ---------- spawning ----------

    public void fireball(double x, double y, int radius, boolean big) {
        burst(x, y, FIRE, 260, big ? 54 : 32, big ? 340 : 250, big ? 150 : 40);
        ring(x, y, new Color(0xffb347));
    }

    public void debris(double x, double y, int radius, boolean big) {
        burst(x, y, ROCK, 110, big ? 54 : 32, big ? 340 : 250, big ? 150 : 40);
        ring(x, y, new Color(0x6ff3ff));
    }

    private void burst(double x, double y, Color[] palette, double gravity, int count, double speed, double lift) {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            double a = random.nextDouble() * Math.PI * 2, sp = 60 + random.nextDouble() * speed;
            p.x = x; p.y = y;
            p.vx = Math.cos(a) * sp;
            p.vy = Math.sin(a) * sp - lift;
            p.gravity = gravity;
            p.size = PixelArt.P * (1 + random.nextInt(3));
            p.life = 0.5 + random.nextDouble() * 0.7;
            p.color = palette[random.nextInt(palette.length)];
            particles.add(p);
        }
    }

    /** A single spark, used for ambient effects such as the muzzle flash. */
    public void spark(double x, double y, double vx, double vy, Color color, double life) {
        Particle p = new Particle();
        p.x = x; p.y = y; p.vx = vx; p.vy = vy; p.gravity = 0; p.size = PixelArt.P;
        p.life = life; p.color = color;
        particles.add(p);
    }

    public void beam(double x0, double y0, double x1, double y1, Color color) {
        Beam b = new Beam();
        b.x0 = x0; b.y0 = y0; b.x1 = x1; b.y1 = y1; b.color = color;
        beams.add(b);
    }

    public void ring(double x, double y, Color color) {
        Ring r = new Ring();
        r.x = x; r.y = y; r.color = color;
        rings.add(r);
    }

    public void scar(double x) {
        Scar s = new Scar();
        s.x = x;
        scars.add(s);
    }

    public void floatText(String text, double x, double y, double vy, double life, Color color, int scale) {
        Floating f = new Floating();
        f.text = text; f.x = x; f.y = y; f.vy = vy; f.life = life; f.color = color; f.scale = scale;
        floats.add(f);
    }

    public void feedback(String text, String tone) {
        feedbackText = text;
        feedbackAge = 0;
        feedbackColor = "good".equals(tone) ? new Color(0x7cff6b) : "bad".equals(tone) ? new Color(0xff5d5d) : new Color(0xffd166);
    }

    public void shake(double seconds) { shake = Math.max(shake, seconds); }
    public void flash(double seconds) { flash = Math.max(flash, seconds); }
    public double getShake() { return shake; }
    public double getFlash() { return flash; }

    public void clear() {
        particles.clear(); beams.clear(); rings.clear(); scars.clear(); floats.clear();
        feedbackText = null; shake = 0; flash = 0;
    }

    // ---------- update / draw ----------

    public void update(double dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.age += dt;
            if (p.age >= p.life) { particles.remove(i); continue; }
            p.vy += p.gravity * dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
        }
        beams.removeIf(b -> (b.age += dt) >= 0.2);
        rings.removeIf(r -> (r.age += dt) >= 0.5);
        scars.removeIf(s -> (s.age += dt) >= 7);
        floats.removeIf(f -> {
            f.age += dt;
            f.y += f.vy * dt;
            return f.age >= f.life;
        });
        if (feedbackText != null && (feedbackAge += dt) > 1.4) feedbackText = null;
        shake = Math.max(0, shake - dt);
        flash = Math.max(0, flash - dt);
    }

    /** Drawn on the ground, below the targets. */
    public void paintScars(Graphics2D g) {
        for (Scar s : scars) {
            double k = Math.min(1, (7 - s.age) / 2.0);
            PixelArt.ellipse(g, s.x, Target.GROUND_Y + 14, 44, 8, new Color(20, 8, 12, (int) (150 * k)));
            PixelArt.ellipse(g, s.x, Target.GROUND_Y + 14, 24, 4, new Color(255, 106, 43, (int) (90 * k)));
        }
    }

    /** Drawn above the targets. */
    public void paint(Graphics2D g) {
        for (Beam b : beams) {
            double k = 1 - b.age / 0.2;
            Color outer = new Color(b.color.getRed(), b.color.getGreen(), b.color.getBlue(), (int) (200 * k));
            g.setStroke(new java.awt.BasicStroke((float) (14 * k + 4), java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER));
            g.setColor(outer);
            g.drawLine((int) b.x0, (int) b.y0, (int) b.x1, (int) b.y1);
            g.setStroke(new java.awt.BasicStroke((float) (5 * k + 2)));
            g.setColor(new Color(255, 255, 255, (int) (255 * k)));
            g.drawLine((int) b.x0, (int) b.y0, (int) b.x1, (int) b.y1);
        }
        g.setStroke(new java.awt.BasicStroke(1));
        for (Ring r : rings) {
            double k = r.age / 0.5;
            Color c = new Color(r.color.getRed(), r.color.getGreen(), r.color.getBlue(), (int) (220 * (1 - k)));
            PixelArt.ring(g, r.x, r.y, 20 + k * 120, 14 + k * 80, c);
        }
        for (Particle p : particles) {
            double k = 1 - p.age / p.life;
            g.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int) (255 * Math.max(0, k))));
            g.fillRect(PixelArt.snap(p.x), PixelArt.snap(p.y), p.size, p.size);
        }
        for (Floating f : floats) {
            float alpha = (float) Math.min(1, (f.life - f.age) * 3);
            Color c = new Color(f.color.getRed(), f.color.getGreen(), f.color.getBlue(), (int) (255 * alpha));
            Color o = new Color(20, 10, 34, (int) (255 * alpha));
            PixelFont.centeredOutline(g, f.text, (int) f.x, (int) f.y, f.scale, c, o);
        }
        if (feedbackText != null) {
            double pop = 1 + 0.25 * Math.max(0, 1 - feedbackAge / 0.15);
            int scale = 6;
            while (scale > 2 && PixelFont.width(feedbackText, scale) > 720) scale--;
            scale = (int) Math.round(scale * pop);
            float alpha = (float) Math.min(1, (1.4 - feedbackAge) * 3);
            Color c = new Color(feedbackColor.getRed(), feedbackColor.getGreen(), feedbackColor.getBlue(), (int) (255 * alpha));
            Color o = new Color(0, 0, 0, (int) (255 * alpha));
            PixelFont.centeredOutline(g, feedbackText, Target.WORLD_WIDTH / 2, (int) (210 - feedbackAge * 24), scale, c, o);
        }
    }
}
