package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Builds and caches the pixel-art rock and problem label of each target. */
public final class Sprites {
    private static final int P = PixelArt.P;
    private static final Map<Integer, BufferedImage> ROCKS = new HashMap<>();
    private static final Map<Integer, BufferedImage> LABELS = new HashMap<>();

    private Sprites() { }

    public static BufferedImage rock(Target t) {
        return ROCKS.computeIfAbsent(t.getId(), id -> makeRock(t instanceof Meteor, t.getRadius(), id));
    }

    public static BufferedImage label(Target t) {
        return LABELS.computeIfAbsent(t.getId(), id -> makeLabel(t.getProblem().getQuestion(), t.getRadius()));
    }

    /** Drops cached images of targets that are gone. */
    public static void prune(Set<Integer> aliveIds) {
        ROCKS.keySet().retainAll(aliveIds);
        LABELS.keySet().retainAll(aliveIds);
    }

    public static void clear() {
        ROCKS.clear();
        LABELS.clear();
    }

    private static BufferedImage makeLabel(String q, int r) {
        int scale = PixelFont.width(q, 3) <= r * 1.75 ? 3 : 2;
        return PixelFont.render(q, scale, Color.WHITE, new Color(0x140a22));
    }

    private static BufferedImage makeRock(boolean meteor, int r, long seed) {
        int size = (int) Math.ceil((r + 4 * P) * 2.0 / P) * P;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Random rnd = new Random(seed * 2654435761L);

        Color[] pal = meteor
                ? new Color[]{new Color(0x8a5a46), new Color(0x62382c), new Color(0x44241c), new Color(0x2a1410)}
                : new Color[]{new Color(0xd7d9e6), new Color(0xa7a9c0), new Color(0x767894), new Color(0x4b4d68)};
        Color outline = meteor ? new Color(0x140806) : new Color(0x1d1d30);
        double[] lumps = new double[8];
        for (int i = 0; i < 8; i++) lumps[i] = meteor ? 0.96 + rnd.nextDouble() * 0.07 : 0.84 + rnd.nextDouble() * 0.26;

        double cx = size / 2.0, cy = size / 2.0;
        if (meteor) { // soft heat glow
            PixelArt.disc(g, cx, cy, r + 3 * P, new Color(255, 106, 43, 60));
        }
        for (int gy = -r - P; gy <= r + P; gy += P) {
            for (int gx = -r - P; gx <= r + P; gx += P) {
                double dist = Math.hypot(gx, gy);
                double rr = radiusAt(lumps, Math.atan2(gy, gx), r);
                if (dist > rr) continue;
                double light = (-gx * 0.6 - gy * 0.8) / r;
                int idx = light > 0.45 ? 0 : light > 0.0 ? 1 : light > -0.45 ? 2 : 3;
                Color c = dist > rr - P ? outline : pal[idx];
                if (meteor && dist > rr - 2 * P && light < -0.25) c = new Color(0xff7a2b); // heated underside
                g.setColor(c);
                g.fillRect(PixelArt.snap(cx + gx), PixelArt.snap(cy + gy), P, P);
            }
        }
        // craters
        for (int i = 0; i < 4; i++) {
            double a = rnd.nextDouble() * Math.PI * 2, d = r * (0.25 + rnd.nextDouble() * 0.4);
            double cr = P * (2 + rnd.nextInt(2));
            PixelArt.ellipse(g, cx + Math.cos(a) * d, cy + Math.sin(a) * d, cr, cr * 0.8, pal[3]);
            PixelArt.ellipse(g, cx + Math.cos(a) * d + 1, cy + Math.sin(a) * d + P, cr - P * 0.5, Math.max(P, cr * 0.5), pal[2]);
        }
        if (meteor) { // glowing lava cracks
            for (int k = 0; k < 3; k++) {
                double a = rnd.nextDouble() * Math.PI * 2;
                double px = cx + Math.cos(a) * r * 0.2, py = cy + Math.sin(a) * r * 0.2;
                double dir = a + (rnd.nextDouble() - 0.5);
                for (int s = 0; s < 6; s++) {
                    if (Math.hypot(px - cx, py - cy) > r * 0.8) break;
                    g.setColor(s % 2 == 0 ? new Color(0xffb347) : new Color(0xff6a2b));
                    g.fillRect(PixelArt.snap(px), PixelArt.snap(py), P, P);
                    dir += (rnd.nextDouble() - 0.5) * 1.2;
                    px += Math.cos(dir) * P;
                    py += Math.sin(dir) * P;
                }
            }
        } else { // ice/crystal glints
            for (int k = 0; k < 5; k++) {
                double a = rnd.nextDouble() * Math.PI * 2, d = r * rnd.nextDouble() * 0.7;
                g.setColor(new Color(0x6ff3ff));
                g.fillRect(PixelArt.snap(cx + Math.cos(a) * d), PixelArt.snap(cy + Math.sin(a) * d), P, P);
            }
        }
        g.dispose();
        return img;
    }

    private static double radiusAt(double[] lumps, double ang, int r) {
        double f = (ang + Math.PI) / (Math.PI * 2) * 8;
        int i0 = (int) Math.floor(f) % 8, i1 = (i0 + 1) % 8;
        double fr = f - Math.floor(f);
        return r * (lumps[i0] * (1 - fr) + lumps[i1] * fr);
    }
}
