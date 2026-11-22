package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** A row of big cards to pick from (used for the mode and difficulty screens). */
public class ChoiceScene extends Scene {
    /** Draws the little picture at the top of a card. */
    public interface Icon {
        void paint(Graphics2D g, int cx, int cy, double t);
    }

    /** Describes one card. */
    public static final class Choice {
        final String title, sub;
        final String[] lines;
        final Color color;
        final Icon icon;
        final Runnable action;

        public Choice(String title, String sub, String[] lines, Color color, Icon icon, Runnable action) {
            this.title = title; this.sub = sub; this.lines = lines; this.color = color; this.icon = icon; this.action = action;
        }
    }

    private static final class Card extends Button {
        final Choice choice;

        Card(int x, int y, int w, int h, Choice c) {
            super(x, y, w, h, c.title, c.color, c.action);
            this.choice = c;
        }

        @Override
        public void paint(Graphics2D g, double t) {
            int x = bounds.x, y = bounds.y - (hover ? 6 : 0), w = bounds.width, h = bounds.height;
            if (focus) {
                g.setColor(Math.sin(t * 8) > 0 ? new Color(0x6ff3ff) : new Color(0xffd166));
                g.fillRect(x - 8, y - 8, w + 16, h + 16);
            }
            g.setColor(Color.BLACK);
            g.fillRect(x - 4, y - 4, w + 8, h + 8);
            g.setColor(hover ? new Color(0x33276b) : new Color(0x241a4d));
            g.fillRect(x, y, w, h);
            g.setColor(choice.color);
            g.fillRect(x, y, w, 8);
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRect(x, y + h - 8, w, 8);
            int cx = x + w / 2;
            choice.icon.paint(g, cx, y + 92, t);
            int s = 4;
            while (s > 2 && PixelFont.width(choice.title, s) > w - 24) s--;
            PixelFont.centeredShadow(g, choice.title, cx, y + 178, s, choice.color, Color.BLACK);
            PixelFont.centered(g, choice.sub, cx, y + 214, 2, new Color(0x6ff3ff));
            int ly = y + 248;
            for (String line : choice.lines) {
                for (String part : PixelFont.wrap(line, 2, w - 32)) {
                    PixelFont.centered(g, part, cx, ly, 2, new Color(0xc9bff0));
                    ly += 20;
                }
            }
        }
    }

    private final Backdrop backdrop;
    private final String title;

    public ChoiceScene(GamePanel app, Theme theme, String title, List<Choice> choices, Runnable back) {
        super(app);
        this.backdrop = new Backdrop(theme, false);
        this.title = title;
        int n = choices.size();
        int cw = n == 2 ? 380 : 280, gap = 30;
        int x0 = (GamePanel.W - (n * cw + (n - 1) * gap)) / 2;
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < n; i++) cards.add(new Card(x0 + i * (cw + gap), 130, cw, 340, choices.get(i)));
        buttons.addAll(cards);
        Button b = new Button(40, 512, 180, 52, "< BACK", new Color(0x2b2360), back).scale(2);
        buttons.add(b);
        this.back = back;
        setFocus(0);
    }

    private final Runnable back;

    @Override protected void onEscape() { back.run(); }

    @Override
    public void update(double dt) { backdrop.update(dt); }

    @Override
    public void paint(Graphics2D g, double t) {
        backdrop.paint(g, t);
        PixelFont.centeredShadow(g, title, GamePanel.W / 2, 50, 5, new Color(0xffd166), Color.BLACK);
        paintButtons(g, t);
    }

    // ---------- ready-made icons ----------

    /** Icon showing a real falling target (Meteor or Asteroid). */
    public static Icon targetIcon(boolean meteor) {
        Random r = new Random();
        Problem p = new Problem(meteor ? "7 × 8" : "36 ÷ 4", meteor ? 56 : 9);
        Target t = meteor ? new Meteor(p, 50, r) : new Asteroid(p, 50, r);
        return (g, cx, cy, time) -> {
            t.move(0.016);
            t.placeAt(Target.WORLD_WIDTH / 2.0);
            t.setY(0);
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(cx, cy);
            g.scale(0.75, 0.75);
            g.translate(-Target.WORLD_WIDTH / 2.0, 0);
            t.draw(g, time);
            g.setTransform(old);
        };
    }

    /** Icon showing a row of pixel hearts. */
    public static Icon heartsIcon(int hearts) {
        return (g, cx, cy, time) -> {
            int w = hearts * 40 - 8;
            for (int i = 0; i < hearts; i++) PlayScene.drawHeart(g, cx - w / 2 + i * 40, cy - 14, 4, true);
        };
    }
}
