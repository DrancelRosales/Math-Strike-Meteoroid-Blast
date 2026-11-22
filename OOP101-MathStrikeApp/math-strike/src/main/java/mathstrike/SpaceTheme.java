package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Time Attack world: a lunar defence base under a nebula sky, with scrolling
 * parallax stars, comets, drifting satellites, a blinking space station and an
 * energy shield above the cannon.
 */
public class SpaceTheme extends Theme {
    private static final int STAR_LAYERS = 3;
    private static final int STARS_PER_LAYER = 70;
    private static final double[] SPEED = {5, 11, 20};

    private final double[][][] stars = new double[STAR_LAYERS][STARS_PER_LAYER][3]; // x, y, phase

    public SpaceTheme() {
        Random r = new Random(99);
        for (double[][] layer : stars) {
            for (double[] s : layer) {
                s[0] = r.nextDouble() * 1000;
                s[1] = r.nextDouble() * GROUND;
                s[2] = r.nextDouble() * 6.28;
            }
        }
    }

    @Override public String getName() { return "Lunar Defence Base"; }
    @Override public Color getAccent() { return new Color(0x6ff3ff); }
    @Override public Color getBeamColor() { return new Color(0x6ff3ff); }

    // ------------------------------------------------------------ static backdrop

    @Override
    protected void paintStatic(Graphics2D g, int w) {
        bands(g, w, new int[]{0x02000a, 0x050112, 0x09021e, 0x0e042c, 0x150838, 0x1d0b48, 0x2a1160, 0x3a1a78, 0x4a2290}, GROUND);

        // nebula clouds
        g.setComposite(java.awt.AlphaComposite.SrcOver);
        PixelArt.ellipse(g, 260, 210, 240, 70, a(0x7b2ff7, 0.14));
        PixelArt.ellipse(g, 300, 220, 140, 40, a(0xb06bff, 0.12));
        PixelArt.ellipse(g, 540, 330, 300, 60, a(0x2f9bf7, 0.13));
        PixelArt.ellipse(g, w - 140, 150, 180, 52, a(0xf72fa5, 0.14));
        PixelArt.ellipse(g, w - 180, 170, 100, 30, a(0xff7bc8, 0.12));

        // ringed planet
        int px = 150, py = 175, pr = 66;
        int[] bandC = {0xd9694a, 0xc4563b, 0xe8896a, 0xb84a33, 0xd9694a};
        for (int gy = -pr; gy <= pr; gy += P) {
            double half = Math.sqrt(pr * pr - gy * gy);
            PixelArt.rect(g, px - half, py + gy, half * 2, P, new Color(bandC[Math.abs(Math.floorDiv(gy, P * 2)) % bandC.length]));
        }
        PixelArt.disc(g, px + 22, py + 22, pr - 12, new Color(0, 0, 0, 90));
        for (double ang = 0; ang < Math.PI * 2; ang += 0.012) {
            double rx = px + 118 * Math.cos(ang), ry = py + 22 * Math.sin(ang) + 0.18 * (rx - px);
            if (Math.sin(ang) < 0 && Math.hypot(rx - px, ry - py) < pr) continue;
            PixelArt.rect(g, rx, ry, P, P, new Color(0xf1d9a0));
        }
        // moon
        PixelArt.disc(g, w - 150, 110, 28, new Color(0xbfc3d6));
        PixelArt.disc(g, w - 158, 104, 6, new Color(0x9a9eb8));
        PixelArt.disc(g, w - 142, 120, 5, new Color(0x9a9eb8));
        PixelArt.disc(g, w - 140, 100, 4, new Color(0x9a9eb8));
        // distant blue world
        PixelArt.disc(g, w / 2 + 40, 70, 18, new Color(0x3b78d8));
        PixelArt.disc(g, w / 2 + 34, 64, 8, new Color(0x6fb0ff));

        // lunar surface
        g.setColor(new Color(0x3d3d55));
        g.fillRect(0, GROUND, w, Theme.HEIGHT - GROUND);
        PixelArt.rect(g, 0, GROUND, w, 14, new Color(0x6e6e8c));
        PixelArt.rect(g, 0, GROUND, w, 4, new Color(0x9a9ab8));
        Random rnd = new Random(5);
        for (int i = 0; i < 8; i++) {
            double cx = 40 + rnd.nextDouble() * (w - 80), cy = GROUND + 30 + rnd.nextDouble() * 36, rx = 20 + rnd.nextDouble() * 30;
            PixelArt.ellipse(g, cx, cy, rx, rx * 0.28, new Color(0x2b2b40));
            PixelArt.ellipse(g, cx - 4, cy - 4, rx - 6, rx * 0.2, new Color(0x35354d));
        }
        for (int i = 0; i < 90; i++) {
            PixelArt.rect(g, rnd.nextDouble() * w, GROUND + 18 + rnd.nextDouble() * (Theme.HEIGHT - GROUND - 20), P, P,
                    rnd.nextBoolean() ? new Color(0x2f2f46) : new Color(0x55556f));
        }
        // base: dome, tower, solar panels
        int bx = w - 132;
        PixelArt.ellipse(g, bx, GROUND + 4, 60, 42, new Color(0x7fa8cc));
        PixelArt.ellipse(g, bx, GROUND + 4, 48, 32, new Color(0xa8cfee));
        PixelArt.ellipse(g, bx - 12, GROUND - 6, 16, 10, new Color(0xe0f3ff));
        PixelArt.rect(g, bx - 4, GROUND - 82, 8, 44, new Color(0x8f93ad));
        PixelArt.rect(g, bx - 44, GROUND + 4, 24, 8, new Color(0x2b4a8a));
        PixelArt.rect(g, bx + 28, GROUND + 4, 24, 8, new Color(0x2b4a8a));
        // left outpost
        PixelArt.ellipse(g, 70, GROUND + 6, 34, 22, new Color(0x7fa8cc));
        PixelArt.ellipse(g, 70, GROUND + 6, 26, 16, new Color(0xa8cfee));
        PixelArt.rect(g, 66, GROUND - 40, 6, 26, new Color(0x8f93ad));
    }

    // ------------------------------------------------------------ animated layer

    @Override
    public void paintAnimated(Graphics2D g, double t, int w) {
        // three parallax star layers scrolling downward = falling-through-space feeling
        for (int layer = 0; layer < STAR_LAYERS; layer++) {
            int size = layer == 2 ? 4 : 2;
            for (double[] s : stars[layer]) {
                if (s[0] >= w) continue;
                double y = (s[1] + t * SPEED[layer]) % GROUND;
                double twinkle = 0.35 + 0.65 * (0.5 + 0.5 * Math.sin(t * 2 + s[2]));
                g.setColor(a(layer == 2 ? 0x6ff3ff : 0xffffff, twinkle * (0.5 + layer * 0.25)));
                g.fillRect((int) s[0] / 2 * 2, (int) y / 2 * 2, size, size);
            }
        }
        // shooting star
        double k = (t * 0.25) % 1;
        if (k < 0.18) {
            double sx = w - 100 - k * 3200, sy = 40 + k * 1200;
            for (int i = 0; i < 7; i++) {
                g.setColor(a(0xffffff, 1 - i / 7.0));
                g.fillRect(PixelArt.snap(sx + i * 14), PixelArt.snap(sy - i * 5), P * 2, P);
            }
        }
        // slow comet
        double cp = (t * 0.035) % 1;
        double cx = -60 + cp * (w + 160), cy = 80 + cp * 160;
        PixelArt.disc(g, cx, cy, 8, new Color(0xe8f6ff));
        for (int i = 1; i < 14; i++) {
            PixelArt.disc(g, cx - i * 12, cy - i * 5, Math.max(P, 8 - i * 0.5), a(0x9fe8ff, 0.6 * (1 - i / 14.0)));
        }
        // satellite with blinking light
        double sx = ((t * 18) % (w + 100)) - 50, sy = 300 + Math.sin(t * 0.5) * 20;
        PixelArt.rect(g, sx - 6, sy - 3, 12, 8, new Color(0xaeb7d6));
        PixelArt.rect(g, sx - 26, sy - 2, 16, 6, new Color(0x2b4a8a));
        PixelArt.rect(g, sx + 10, sy - 2, 16, 6, new Color(0x2b4a8a));
        if (Math.sin(t * 5) > 0.2) PixelArt.rect(g, sx - 2, sy - 8, P, P, new Color(0xff5d5d));

        // blinking lights on the base
        int bx = w - 132;
        if (Math.sin(t * 4) > 0) PixelArt.rect(g, bx - 4, GROUND - 90, 8, 8, new Color(0xff5d5d));
        if (Math.sin(t * 3 + 1) > 0) PixelArt.rect(g, bx + 20, GROUND - 6, P, P, new Color(0x7cff6b));
        if (Math.sin(t * 2.5) > 0.3) PixelArt.rect(g, 66, GROUND - 46, 6, 6, new Color(0xffd166));

        // faint energy shield over the cannon
        double pulse = 0.5 + 0.5 * Math.sin(t * 2);
        PixelArt.ring(g, w / 2.0, GROUND, 120, 90, a(0x6ff3ff, 0.10 + 0.10 * pulse));
        PixelArt.ring(g, w / 2.0, GROUND, 116, 86, a(0x6ff3ff, 0.05 + 0.05 * pulse));
    }

    // ------------------------------------------------------------ turret

    @Override
    public void drawTurret(Graphics2D g, double aim, double recoil, double t) {
        double bx = Target.WORLD_WIDTH / 2.0, by = GROUND - 22;
        PixelArt.ellipse(g, bx, GROUND + 4, 64, 28, new Color(0x59607a));
        PixelArt.ellipse(g, bx, GROUND, 52, 22, new Color(0x7d86a8));
        PixelArt.rect(g, bx - 40, GROUND - 2, 80, 4, new Color(0x4a516e));
        PixelArt.disc(g, bx, by + 6, 28, new Color(0x8fb7d9));
        PixelArt.disc(g, bx - 6, by, 12, new Color(0xcfe6f7));
        java.awt.geom.AffineTransform old = g.getTransform();
        g.translate(bx, by);
        g.rotate(aim);
        g.translate(-recoil * 8, 0);
        g.setColor(new Color(0xaeb7d6)); g.fillRect(0, -10, 54, 20);
        g.setColor(new Color(0xd6dcf0)); g.fillRect(0, -10, 54, 5);
        g.setColor(new Color(0x5b6488)); g.fillRect(10, -10, 6, 20); g.fillRect(32, -10, 6, 20);
        double glow = 0.5 + 0.5 * Math.sin(t * 5);
        g.setColor(new Color(0x6ff3ff)); g.fillRect(54, -7, 8, 14);
        g.setColor(a(0x6ff3ff, 0.25 + 0.25 * glow)); g.fillRect(62, -9, 8, 18);
        g.setTransform(old);
    }
}
