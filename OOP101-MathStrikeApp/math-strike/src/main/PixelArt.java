package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;

/** Tiny helpers that draw everything on a chunky 4-pixel grid for the retro look. */
public final class PixelArt {
    public static final int P = 4;

    private PixelArt() { }

    public static Color c(int rgb) { return new Color(rgb); }

    public static Color c(int rgb, int alpha) {
        return new Color((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, alpha);
    }

    public static int snap(double v) {
        return (int) Math.round(v / P) * P;
    }

    /** Filled rectangle snapped to the pixel grid (at least one pixel big). */
    public static void rect(Graphics2D g, double x, double y, double w, double h, Color color) {
        int x0 = snap(x), y0 = snap(y), x1 = snap(x + w), y1 = snap(y + h);
        g.setColor(color);
        g.fillRect(x0, y0, Math.max(P, x1 - x0), Math.max(P, y1 - y0));
    }

    /** Filled pixel-art ellipse. */
    public static void ellipse(Graphics2D g, double cx, double cy, double rx, double ry, Color color) {
        g.setColor(color);
        for (double gy = -ry; gy <= ry; gy += P) {
            double half = rx * Math.sqrt(Math.max(0, 1 - (gy * gy) / (ry * ry)));
            int x0 = snap(cx - half), x1 = snap(cx + half);
            g.fillRect(x0, snap(cy + gy), Math.max(P, x1 - x0), P);
        }
    }

    public static void disc(Graphics2D g, double cx, double cy, double r, Color color) {
        ellipse(g, cx, cy, r, r, color);
    }

    /** Pixel-art outline of an ellipse made of little squares. */
    public static void ring(Graphics2D g, double cx, double cy, double rx, double ry, Color color) {
        g.setColor(color);
        int steps = (int) Math.max(24, (rx + ry) * 0.9);
        for (int i = 0; i < steps; i++) {
            double a = i * Math.PI * 2 / steps;
            g.fillRect(snap(cx + Math.cos(a) * rx), snap(cy + Math.sin(a) * ry), P, P);
        }
    }
}
