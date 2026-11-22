package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;

/** A paged text screen used for How To Play and the mission briefing. */
public class TextScene extends Scene {
    /** One heading + paragraph. */
    public static final class Section {
        final String heading, body;

        public Section(String heading, String body) {
            this.heading = heading;
            this.body = body;
        }
    }

    private final Backdrop backdrop;
    private final String title;
    private final List<List<Section>> pages;
    private final Runnable back;
    private final Button prev;
    private final Button next;
    private int page = 0;

    /**
     * @param primaryLabel text of the big centre button (e.g. BEGIN MISSION) or null for none
     */
    public TextScene(GamePanel app, Theme theme, String title, List<List<Section>> pages,
                     String primaryLabel, Runnable primary, Runnable back) {
        super(app);
        this.backdrop = new Backdrop(theme, false);
        this.title = title;
        this.pages = pages;
        this.back = back;
        buttons.add(new Button(40, 520, 180, 52, "< BACK", new Color(0x2b2360), back).scale(2));
        prev = new Button(560, 520, 170, 52, "< PREV", new Color(0x3b2a8a), () -> turn(-1)).scale(2);
        next = new Button(750, 520, 170, 52, "NEXT >", new Color(0x3b2a8a), () -> turn(1)).scale(2);
        if (pages.size() > 1) { buttons.add(prev); buttons.add(next); }
        if (primaryLabel != null) {
            buttons.add(1, new Button(280, 512, 260, 60, primaryLabel, new Color(0xd9531e), primary).scale(3));
        }
        setFocus(primaryLabel != null ? 1 : 0);
        refresh();
    }

    private void turn(int d) {
        page = Math.max(0, Math.min(pages.size() - 1, page + d));
        refresh();
    }

    private void refresh() {
        prev.enabled = page > 0;
        next.enabled = page < pages.size() - 1;
    }

    @Override protected void onEscape() { back.run(); }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) { turn(1); return; }
        if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) { turn(-1); return; }
        super.keyPressed(e);
    }

    @Override
    public void mouseWheel(int rotation) { turn(rotation > 0 ? 1 : -1); }

    @Override
    public void update(double dt) { backdrop.update(dt); }

    @Override
    public void paint(Graphics2D g, double t) {
        backdrop.paint(g, t);
        PixelFont.centeredShadow(g, title, GamePanel.W / 2, 36, 5, new Color(0xffd166), Color.BLACK);

        int x = 60, y = 100, w = 880, h = 384;
        g.setColor(Color.BLACK);
        g.fillRect(x - 4, y - 4, w + 8, h + 8);
        g.setColor(new Color(0x1a1238));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(255, 255, 255, 14));
        g.fillRect(x, y, w, 4);

        int ty = y + 22;
        for (Section s : pages.get(page)) {
            PixelFont.draw(g, s.heading, x + 24, ty, 3, new Color(0x6ff3ff));
            ty += 32;
            for (String line : PixelFont.wrap(s.body, 2, w - 48)) {
                PixelFont.draw(g, line, x + 24, ty, 2, new Color(0xf4efff));
                ty += 20;
            }
            ty += 20;
        }
        if (pages.size() > 1) {
            PixelFont.centered(g, "PAGE " + (page + 1) + " OF " + pages.size(), GamePanel.W / 2, y + h - 26, 2, new Color(0x9a90c0));
        }
        paintButtons(g, t);
    }
}
