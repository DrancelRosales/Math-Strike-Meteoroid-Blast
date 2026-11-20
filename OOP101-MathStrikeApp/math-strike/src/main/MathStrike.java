package mathstrike;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * Entry point of Math Strike: Meteoroid Blast.
 * Pure Java (Swing) - no libraries, no browser, no server.
 *
 * Run with:  java -jar MathStrike.jar
 */
public class MathStrike {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Math Strike: Meteoroid Blast");
            GamePanel panel = new GamePanel(() -> {
                frame.dispose();
                System.exit(0);
            });
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setIconImage(icon());
            frame.setContentPane(panel);
            frame.pack();
            frame.setMinimumSize(new Dimension(500, 330));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
            panel.start();
        });
    }

    /** A small meteor drawn in code, used as the window icon. */
    private static BufferedImage icon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        PixelArt.disc(g, 36, 36, 20, new java.awt.Color(0x62382c));
        PixelArt.disc(g, 32, 32, 12, new java.awt.Color(0x8a5a46));
        PixelArt.disc(g, 52, 52, 8, new java.awt.Color(0xff7a2b));
        PixelArt.disc(g, 12, 12, 8, new java.awt.Color(0xffd166));
        g.dispose();
        return img;
    }
}
