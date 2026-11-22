package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Parent of the two visual worlds. A theme paints a cached static backdrop,
 * an animated layer on top of it, and the player's turret.
 */
public abstract class Theme {
    public static final int HEIGHT = 600;
    protected static final int P = PixelArt.P;
    protected static final int GROUND = Target.GROUND_Y;

    private static Theme prehistoric;
    private static Theme space;

    private BufferedImage cache;
    private int cacheWidth;

    /** "space" gives the space world, anything else the prehistoric one. */
    public static synchronized Theme of(String key) {
        if ("space".equals(key)) {
            if (space == null) space = new SpaceTheme();
            return space;
        }
        if (prehistoric == null) prehistoric = new PrehistoricTheme();
        return prehistoric;
    }

    public abstract String getName();

    /** Main colour used for panels and highlights. */
    public abstract Color getAccent();

    /** Colour of the player's blast beam. */
    public abstract Color getBeamColor();

    /** Paints the unchanging backdrop (sky, mountains, ground ...). */
    protected abstract void paintStatic(Graphics2D g, int width);

    /** Paints everything that moves in the background (clouds, stars, creatures ...). */
    public abstract void paintAnimated(Graphics2D g, double t, int width);

    /** Draws the player's cannon at the bottom centre. aim is an angle in radians. */
    public abstract void drawTurret(Graphics2D g, double aim, double recoil, double t);

    /** Where the muzzle is, so beams start in the right place. */
    public double[] muzzle(double aim, double recoil) {
        double len = 62 - recoil * 8;
        return new double[]{Target.WORLD_WIDTH / 2.0 + Math.cos(aim) * len, GROUND - 22 + Math.sin(aim) * len};
    }

    public BufferedImage background(int width) {
        if (cache == null || cacheWidth != width) {
            cache = new BufferedImage(width, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = cache.createGraphics();
            paintStatic(g, width);
            g.dispose();
            cacheWidth = width;
        }
        return cache;
    }

    /** Draws the whole backdrop (static + animated). */
    public void paintBackdrop(Graphics2D g, double t, int width) {
        g.drawImage(background(width), 0, 0, null);
        paintAnimated(g, t, width);
    }

    protected static void bands(Graphics2D g, int width, int[] colors, int height) {
        int bh = (int) Math.ceil(height / (double) colors.length / P) * P;
        for (int i = 0; i < colors.length; i++) {
            g.setColor(new Color(colors[i]));
            g.fillRect(0, i * bh, width, bh + P);
        }
    }

    protected static Color a(int rgb, double alpha) {
        return PixelArt.c(rgb, (int) Math.round(Math.max(0, Math.min(1, alpha)) * 255));
    }
}
