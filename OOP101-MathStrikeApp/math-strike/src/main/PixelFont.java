package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/** A built-in 5x7 bitmap font so the whole game looks like a real pixel-art arcade cabinet. */
public final class PixelFont {
    private static final Map<Character, int[]> GLYPHS = new HashMap<>();

    static {
        def('A', "01110/10001/10001/11111/10001/10001/10001");
        def('B', "11110/10001/10001/11110/10001/10001/11110");
        def('C', "01110/10001/10000/10000/10000/10001/01110");
        def('D', "11110/10001/10001/10001/10001/10001/11110");
        def('E', "11111/10000/10000/11110/10000/10000/11111");
        def('F', "11111/10000/10000/11110/10000/10000/10000");
        def('G', "01110/10001/10000/10111/10001/10001/01111");
        def('H', "10001/10001/10001/11111/10001/10001/10001");
        def('I', "01110/00100/00100/00100/00100/00100/01110");
        def('J', "00111/00010/00010/00010/00010/10010/01100");
        def('K', "10001/10010/10100/11000/10100/10010/10001");
        def('L', "10000/10000/10000/10000/10000/10000/11111");
        def('M', "10001/11011/10101/10101/10001/10001/10001");
        def('N', "10001/11001/10101/10011/10001/10001/10001");
        def('O', "01110/10001/10001/10001/10001/10001/01110");
        def('P', "11110/10001/10001/11110/10000/10000/10000");
        def('Q', "01110/10001/10001/10001/10101/10010/01101");
        def('R', "11110/10001/10001/11110/10100/10010/10001");
        def('S', "01111/10000/10000/01110/00001/00001/11110");
        def('T', "11111/00100/00100/00100/00100/00100/00100");
        def('U', "10001/10001/10001/10001/10001/10001/01110");
        def('V', "10001/10001/10001/10001/10001/01010/00100");
        def('W', "10001/10001/10001/10101/10101/11011/10001");
        def('X', "10001/10001/01010/00100/01010/10001/10001");
        def('Y', "10001/10001/01010/00100/00100/00100/00100");
        def('Z', "11111/00001/00010/00100/01000/10000/11111");
        def('0', "01110/10001/10011/10101/11001/10001/01110");
        def('1', "00100/01100/00100/00100/00100/00100/01110");
        def('2', "01110/10001/00001/00010/00100/01000/11111");
        def('3', "11110/00001/00001/01110/00001/00001/11110");
        def('4', "00010/00110/01010/10010/11111/00010/00010");
        def('5', "11111/10000/11110/00001/00001/10001/01110");
        def('6', "00110/01000/10000/11110/10001/10001/01110");
        def('7', "11111/00001/00010/00100/01000/01000/01000");
        def('8', "01110/10001/10001/01110/10001/10001/01110");
        def('9', "01110/10001/10001/01111/00001/00010/01100");
        def('+', "00000/00100/00100/11111/00100/00100/00000");
        def('-', "00000/00000/00000/11111/00000/00000/00000");
        def('×', "00000/10001/01010/00100/01010/10001/00000");
        def('÷', "00000/00100/00000/11111/00000/00100/00000");
        def('(', "00010/00100/01000/01000/01000/00100/00010");
        def(')', "01000/00100/00010/00010/00010/00100/01000");
        def('.', "00000/00000/00000/00000/00000/01100/01100");
        def(',', "00000/00000/00000/00000/00110/00100/01000");
        def('!', "00100/00100/00100/00100/00100/00000/00100");
        def('?', "01110/10001/00001/00010/00100/00000/00100");
        def(':', "00000/01100/01100/00000/01100/01100/00000");
        def('%', "11001/11010/00010/00100/01000/01011/10011");
        def('/', "00001/00001/00010/00100/01000/10000/10000");
        def('\'', "00100/00100/01000/00000/00000/00000/00000");
        def('=', "00000/00000/11111/00000/11111/00000/00000");
        def('<', "00010/00100/01000/10000/01000/00100/00010");
        def('>', "01000/00100/00010/00001/00010/00100/01000");
        def('*', "00000/10101/01110/11111/01110/10101/00000");
        def('_', "00000/00000/00000/00000/00000/00000/11111");
        def('•', "00000/00000/01110/01110/01110/00000/00000");
    }

    private PixelFont() { }

    private static void def(char c, String rows) {
        String[] r = rows.split("/");
        int[] bits = new int[7];
        for (int i = 0; i < 7; i++) bits[i] = Integer.parseInt(r[i], 2);
        GLYPHS.put(c, bits);
    }

    private static char normalize(char c) {
        if (c == '−' || c == '–') return '-';
        if (c == 'x') return 'X';
        char u = Character.toUpperCase(c);
        return GLYPHS.containsKey(u) || u == ' ' ? u : '?';
    }

    private static int advance(char c) { return c == ' ' ? 4 : 6; }

    public static int width(String s, int scale) {
        int w = 0;
        for (int i = 0; i < s.length(); i++) w += advance(normalize(s.charAt(i))) * scale;
        return Math.max(0, w - scale);
    }

    public static int height(int scale) { return 7 * scale; }

    public static void draw(Graphics2D g, String s, int x, int y, int scale, Color color) {
        g.setColor(color);
        int cx = x;
        for (int i = 0; i < s.length(); i++) {
            char c = normalize(s.charAt(i));
            int[] bits = GLYPHS.get(c);
            if (bits != null) {
                for (int row = 0; row < 7; row++) {
                    for (int col = 0; col < 5; col++) {
                        if ((bits[row] >> (4 - col) & 1) != 0) {
                            g.fillRect(cx + col * scale, y + row * scale, scale, scale);
                        }
                    }
                }
            }
            cx += advance(c) * scale;
        }
    }

    public static void shadow(Graphics2D g, String s, int x, int y, int scale, Color color, Color shadow) {
        draw(g, s, x + scale, y + scale, scale, shadow);
        draw(g, s, x, y, scale, color);
    }

    public static void outline(Graphics2D g, String s, int x, int y, int scale, Color color, Color outline) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) draw(g, s, x + dx * scale, y + dy * scale, scale, outline);
            }
        }
        draw(g, s, x, y, scale, color);
    }

    public static void centered(Graphics2D g, String s, int cx, int y, int scale, Color color) {
        draw(g, s, cx - width(s, scale) / 2, y, scale, color);
    }

    public static void centeredShadow(Graphics2D g, String s, int cx, int y, int scale, Color color, Color shadow) {
        shadow(g, s, cx - width(s, scale) / 2, y, scale, color, shadow);
    }

    public static void centeredOutline(Graphics2D g, String s, int cx, int y, int scale, Color color, Color outline) {
        outline(g, s, cx - width(s, scale) / 2, y, scale, color, outline);
    }

    public static void right(Graphics2D g, String s, int rx, int y, int scale, Color color) {
        draw(g, s, rx - width(s, scale), y, scale, color);
    }

    /** Outline with a custom thickness in pixels (for big titles). */
    public static void outlinePx(Graphics2D g, String s, int x, int y, int scale, Color color, Color outline, int px) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) draw(g, s, x + dx * px, y + dy * px, scale, outline);
            }
        }
        draw(g, s, x, y, scale, color);
    }

    public static void centeredOutlinePx(Graphics2D g, String s, int cx, int y, int scale, Color color, Color outline, int px) {
        outlinePx(g, s, cx - width(s, scale) / 2, y, scale, color, outline, px);
    }

    /** Splits text into lines that fit maxWidth. */
    public static java.util.List<String> wrap(String text, int scale, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (width(candidate, scale) > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    /** Pre-renders outlined text into an image (used for the problems on targets). */
    public static BufferedImage render(String s, int scale, Color fill, Color outline) {
        int pad = scale;
        BufferedImage img = new BufferedImage(width(s, scale) + pad * 2, height(scale) + pad * 2,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        outline(g, s, pad, pad, scale, fill, outline);
        g.dispose();
        return img;
    }
}
