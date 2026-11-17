package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Classic Mode world: a volcanic prehistoric valley at dusk with an erupting
 * volcano, drifting clouds, pterodactyls, a walking brontosaurus and fireflies.
 */
public class PrehistoricTheme extends Theme {
    private static final int VOLCANO_X = 190;
    private static final int VOLCANO_TOP = 290;
    private static final int DINO = 0x241a33;

    @Override public String getName() { return "Prehistoric Valley"; }
    @Override public Color getAccent() { return new Color(0xff8a3c); }
    @Override public Color getBeamColor() { return new Color(0xffb347); }

    // ------------------------------------------------------------ static backdrop

    @Override
    protected void paintStatic(Graphics2D g, int w) {
        bands(g, w, new int[]{0x241238, 0x34194a, 0x4a2056, 0x6b2a5e, 0x8e3560, 0xb0425a, 0xd05a4f,
                0xe87a4a, 0xf39a4b, 0xf8b95e, 0xfbd380}, GROUND);

        // sun with glow rings
        int sx = (int) (w * 0.76), sy = 372;
        PixelArt.disc(g, sx, sy, 110, a(0xffb35a, 0.18));
        PixelArt.disc(g, sx, sy, 88, a(0xffc870, 0.28));
        PixelArt.disc(g, sx, sy, 68, new Color(0xfbd88a));
        PixelArt.disc(g, sx, sy, 54, new Color(0xffeaa8));

        // far mountains (two layers)
        for (int x = 0; x < w; x += P) {
            double h = 120 + 34 * Math.sin(x * 0.011 + 1) + 22 * Math.sin(x * 0.034);
            PixelArt.rect(g, x, GROUND - h, P, h + P, new Color(0x5b2f5c));
        }
        for (int x = 0; x < w; x += P) {
            double h = 84 + 30 * Math.sin(x * 0.015 + 3) + 18 * Math.sin(x * 0.047 + 1);
            PixelArt.rect(g, x, GROUND - h, P, h + P, new Color(0x452650));
        }

        // volcano
        for (int y = VOLCANO_TOP; y < GROUND; y += P) {
            double half = 26 + (y - VOLCANO_TOP) * 0.82;
            PixelArt.rect(g, VOLCANO_X - half, y, half, P, new Color(0x3a2233));
            PixelArt.rect(g, VOLCANO_X, y, half, P, new Color(0x2a1826));
        }
        PixelArt.rect(g, VOLCANO_X - 28, VOLCANO_TOP - 4, 56, 12, new Color(0xff7a2b));
        PixelArt.rect(g, VOLCANO_X - 12, VOLCANO_TOP - 4, 24, 8, new Color(0xffd04a));
        int[][] lava = {{-20, 24, 60}, {14, 36, 80}, {-6, 50, 52}, {26, 70, 44}, {-30, 90, 40}};
        for (int[] l : lava) PixelArt.rect(g, VOLCANO_X + l[0], VOLCANO_TOP + l[1], P, l[2], new Color(0xff6a2b));

        // rolling hills
        for (int x = 0; x < w; x += P) {
            double h = 40 + 18 * Math.sin(x * 0.02 + 2) + 9 * Math.sin(x * 0.07);
            PixelArt.rect(g, x, GROUND - h, P, h + P, new Color(0x2f5a2c));
            PixelArt.rect(g, x, GROUND - h, P, P, new Color(0x3f7a38));
        }

        // ground
        g.setColor(new Color(0x6b4a2e));
        g.fillRect(0, GROUND, w, Theme.HEIGHT - GROUND);
        PixelArt.rect(g, 0, GROUND, w, 14, new Color(0x58a23c));
        PixelArt.rect(g, 0, GROUND, w, 4, new Color(0x7fd05a));
        Random rnd = new Random(7);
        for (int i = 0; i < 110; i++) {
            PixelArt.rect(g, rnd.nextDouble() * w, GROUND + 18 + rnd.nextDouble() * (Theme.HEIGHT - GROUND - 20),
                    P * (1 + rnd.nextInt(3)), P, rnd.nextBoolean() ? new Color(0x523720) : new Color(0x85603b));
        }
        // fossil bones in the dirt
        for (int i = 0; i < 3; i++) {
            int bx = 120 + i * 260 + rnd.nextInt(60), by = GROUND + 30 + rnd.nextInt(14);
            PixelArt.rect(g, bx, by, 28, P, new Color(0xe7dcc0));
            PixelArt.rect(g, bx - P, by - P, P, P * 3, new Color(0xe7dcc0));
            PixelArt.rect(g, bx + 28, by - P, P, P * 3, new Color(0xe7dcc0));
        }
        // grass tufts & ferns
        for (int x = 8; x < w; x += 28 + rnd.nextInt(30)) fern(g, x, GROUND + 4, 14 + rnd.nextInt(14));
        palm(g, 62, GROUND + 6, 180, 12);
        palm(g, w - 58, GROUND + 6, 140, -10);
        palm(g, 330, GROUND + 6, 96, 8);
        palm(g, w - 250, GROUND + 6, 120, -6);
    }

    private static void fern(Graphics2D g, int x, int y, int h) {
        Color dark = new Color(0x2f8a3a), light = new Color(0x43b34a);
        for (int i = 0; i < h; i += P) {
            PixelArt.rect(g, x - i / 2.0, y - i, P, P, i % 8 == 0 ? light : dark);
            PixelArt.rect(g, x + i / 2.0, y - i, P, P, i % 8 == 0 ? dark : light);
            PixelArt.rect(g, x, y - i, P, P, dark);
        }
    }

    private static void palm(Graphics2D g, int x, int y, int h, double lean) {
        double tx = x;
        for (int i = 0; i < h; i += P) {
            tx = x + Math.sin(i * 0.03) * lean;
            PixelArt.rect(g, tx, y - i, P * 2, P, i % 16 < 8 ? new Color(0x5c3b1e) : new Color(0x6d4726));
        }
        double ty = y - h;
        double[] dirs = {-1, -0.62, -0.25, 0.25, 0.62, 1};
        for (int n = 0; n < dirs.length; n++) {
            for (int k = 0; k < 11; k++) {
                double fx = tx + dirs[n] * k * 9;
                double fy = ty + k * k * 0.55 - k * 5;
                PixelArt.rect(g, fx, fy, P * 3, P * 2, (k + n) % 2 == 0 ? new Color(0x43b34a) : new Color(0x2f8a3a));
            }
        }
    }

    // ------------------------------------------------------------ animated layer

    @Override
    public void paintAnimated(Graphics2D g, double t, int w) {
        // drifting clouds (two parallax layers)
        for (int i = 0; i < 6; i++) {
            double speed = i < 3 ? 6 : 13;
            double cx = ((i * 233 + t * speed) % (w + 240)) - 120;
            double cy = 60 + (i * 47) % 150;
            Color c = a(i < 3 ? 0xe8a0a0 : 0xf7c4a8, i < 3 ? 0.35 : 0.5);
            PixelArt.ellipse(g, cx, cy, 56, 12, c);
            PixelArt.ellipse(g, cx - 20, cy - 8, 30, 10, c);
            PixelArt.ellipse(g, cx + 24, cy - 6, 26, 8, c);
        }

        volcano(g, t);

        // pterodactyls
        pterodactyl(g, ((t * 46) % (w + 240)) - 120, 120 + Math.sin(t * 0.9) * 14, t, 1);
        pterodactyl(g, w + 120 - ((t * 30 + 300) % (w + 240)), 190 + Math.sin(t * 0.7 + 2) * 10, t + 1, -1);

        brontosaurus(g, w + 220 - ((t * 11) % (w + 520)), t);

        // fireflies / embers
        for (int i = 0; i < 16; i++) {
            double ph = (t * 0.1 + i / 16.0) % 1;
            double x = (i * 97 + Math.sin(t * 0.8 + i) * 30) % w;
            double y = GROUND - 10 - ph * 220;
            g.setColor(a(i % 2 == 0 ? 0xffd166 : 0xff8a3c, Math.sin(ph * Math.PI) * 0.9));
            g.fillRect(PixelArt.snap(x), PixelArt.snap(y), P, P);
        }
    }

    private void volcano(Graphics2D g, double t) {
        double flick = 0.35 + 0.25 * Math.sin(t * 9) + 0.1 * Math.sin(t * 23);
        double cycle = 12, ph = t % cycle;
        boolean erupting = ph < 3;
        PixelArt.disc(g, VOLCANO_X, VOLCANO_TOP - 8, erupting ? 66 : 50, a(0xff7a2b, erupting ? 0.5 : flick));
        // smoke column
        for (int i = 0; i < 7; i++) {
            double p = (t * 0.14 + i / 7.0) % 1;
            PixelArt.disc(g, VOLCANO_X + Math.sin(p * 7 + i) * 16 + p * 46, VOLCANO_TOP - 8 - p * 200,
                    10 + p * 32, a(0x4a3a52, 0.45 * (1 - p)));
        }
        if (erupting) { // lava fountain
            Random r = new Random((long) (t / cycle) * 31 + 1);
            for (int i = 0; i < 34; i++) {
                double vx = (r.nextDouble() - 0.5) * 240, vy = -(240 + r.nextDouble() * 240);
                double u = ph - i * 0.06;
                if (u < 0 || u > 1.9) continue;
                double px = VOLCANO_X + vx * u, py = VOLCANO_TOP - 4 + vy * u + 0.5 * 430 * u * u;
                PixelArt.rect(g, px, py, P * 2, P * 2, i % 3 == 0 ? new Color(0xffd04a) : new Color(0xff6a2b));
            }
        }
    }

    private void pterodactyl(Graphics2D g, double x, double y, double t, int dir) {
        Color c = new Color(DINO);
        int flap = Math.sin(t * 8) > 0 ? -1 : 1;
        PixelArt.rect(g, x - 4, y, 12, 8, c);
        for (int i = 1; i < 6; i++) {
            PixelArt.rect(g, x - 4 - i * 8, y + flap * i * 3, 12, 6, c);
            PixelArt.rect(g, x + 4 + i * 8, y + flap * i * 3, 12, 6, c);
        }
        PixelArt.rect(g, x + (dir > 0 ? 16 : -24), y, 12, 4, c);
        PixelArt.rect(g, x + (dir > 0 ? -16 : 12), y + 4, 8, 4, c);
    }

    private void brontosaurus(Graphics2D g, double x, double t) {
        Color dc = new Color(DINO);
        double cy = GROUND - 68, bob = Math.sin(t * 2.2) * 2;
        PixelArt.ellipse(g, x, cy + bob, 62, 30, dc);
        for (int i = 0; i < 12; i++) PixelArt.disc(g, x - 54 - i * 3, cy + bob - 14 - i * 9, 12 - i * 0.4, dc);
        PixelArt.disc(g, x - 90, cy + bob - 128, 14, dc);
        PixelArt.rect(g, x - 108, cy + bob - 134, 8, 4, new Color(0xf08a4b));
        for (int i = 0; i < 14; i++) {
            PixelArt.disc(g, x + 54 + i * 8 + Math.sin(t * 2 + i * 0.4) * 2, cy + bob + 6 - i * 1.5 + (i > 8 ? (i - 8) * 3 : 0),
                    Math.max(4, 16 - i), dc);
        }
        double[] legs = {-38, -12, 22, 48};
        for (int i = 0; i < legs.length; i++) {
            double swing = Math.sin(t * 3 + i * Math.PI / 2) * 5;
            PixelArt.rect(g, x + legs[i] + swing, cy + 10, 20, GROUND - cy - 2 - Math.abs(swing) * 0.3, dc);
        }
    }

    // ------------------------------------------------------------ turret

    @Override
    public void drawTurret(Graphics2D g, double aim, double recoil, double t) {
        double bx = Target.WORLD_WIDTH / 2.0, by = GROUND - 22;
        // stone mound
        PixelArt.ellipse(g, bx, GROUND + 4, 66, 30, new Color(0x6f6050));
        PixelArt.ellipse(g, bx, GROUND, 54, 24, new Color(0x8f7d68));
        PixelArt.ellipse(g, bx, GROUND - 4, 36, 14, new Color(0xa8946e));
        // wheels
        PixelArt.disc(g, bx - 34, GROUND + 6, 14, new Color(0x5c3b1e));
        PixelArt.disc(g, bx + 34, GROUND + 6, 14, new Color(0x5c3b1e));
        PixelArt.disc(g, bx - 34, GROUND + 6, 6, new Color(0xc9863d));
        PixelArt.disc(g, bx + 34, GROUND + 6, 6, new Color(0xc9863d));
        PixelArt.disc(g, bx, by + 8, 26, new Color(0xa8946e));
        // log barrel
        java.awt.geom.AffineTransform old = g.getTransform();
        g.translate(bx, by);
        g.rotate(aim);
        g.translate(-recoil * 8, 0);
        g.setColor(new Color(0x8a5a2b)); g.fillRect(0, -11, 54, 22);
        g.setColor(new Color(0xa8733a)); g.fillRect(0, -11, 54, 6);
        g.setColor(new Color(0x5c3b1e)); g.fillRect(12, -11, 6, 22); g.fillRect(34, -11, 6, 22);
        g.setColor(new Color(0xc9863d)); g.fillRect(54, -9, 8, 18);
        double glow = 0.5 + 0.5 * Math.sin(t * 6);
        g.setColor(a(0xffb347, 0.5 + 0.4 * glow)); g.fillRect(62, -5, 6, 10);
        g.setTransform(old);
    }
}
