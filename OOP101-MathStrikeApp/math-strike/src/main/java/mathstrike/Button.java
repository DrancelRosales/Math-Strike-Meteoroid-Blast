package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** A clickable pixel-art button drawn with the game's own font. */
public class Button {
    public final Rectangle bounds;
    public String label;
    public final Runnable action;
    public Color color;
    public int scale = 3;
    public boolean hover;
    public boolean focus;
    public boolean enabled = true;

    public Button(int x, int y, int w, int h, String label, Color color, Runnable action) {
        this.bounds = new Rectangle(x, y, w, h);
        this.label = label;
        this.color = color;
        this.action = action;
    }

    public Button scale(int s) { this.scale = s; return this; }

    public boolean contains(int x, int y) { return bounds.contains(x, y); }

    public static Color mix(Color c, Color with, double k) {
        return new Color((int) (c.getRed() + (with.getRed() - c.getRed()) * k),
                (int) (c.getGreen() + (with.getGreen() - c.getGreen()) * k),
                (int) (c.getBlue() + (with.getBlue() - c.getBlue()) * k));
    }

    public void paint(Graphics2D g, double t) {
        int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
        Color face = !enabled ? new Color(0x4a4560) : hover ? mix(color, Color.WHITE, 0.2) : color;
        if (focus) {
            g.setColor(Math.sin(t * 8) > 0 ? new Color(0x6ff3ff) : new Color(0xffd166));
            g.fillRect(x - 8, y - 8, w + 16, h + 16);
        }
        g.setColor(Color.BLACK);
        g.fillRect(x - 4, y - 4, w + 8, h + 8);
        g.setColor(face);
        g.fillRect(x, y, w, h);
        g.setColor(mix(face, Color.BLACK, 0.35));
        g.fillRect(x, y + h - 8, w, 8);
        g.setColor(mix(face, Color.WHITE, 0.3));
        g.fillRect(x, y, w, 4);
        int ty = y + (h - 8 - PixelFont.height(scale)) / 2 + 2;
        PixelFont.centeredShadow(g, label, x + w / 2, ty, scale, Color.WHITE, new Color(0, 0, 0, 160));
    }
}
