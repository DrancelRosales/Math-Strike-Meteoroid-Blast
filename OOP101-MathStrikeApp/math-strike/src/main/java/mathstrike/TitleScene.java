package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;

/** Main screen: animated title over a living space backdrop. */
public class TitleScene extends Scene {
    private final Backdrop backdrop = new Backdrop(Theme.of("space"), true);

    public TitleScene(GamePanel app) {
        super(app);
        int cx = GamePanel.W / 2;
        buttons.add(new Button(cx - 160, 290, 320, 60, "PLAY", new Color(0xd9531e), app::showModes));
        buttons.add(new Button(cx - 160, 366, 320, 52, "HOW TO PLAY", new Color(0x3b2a8a), app::showHowTo).scale(2));
        buttons.add(new Button(cx - 160, 434, 152, 44, "SOUND: ON", new Color(0x2b2360),
                () -> Sound.setEnabled(!Sound.isEnabled())) {
            @Override public void paint(Graphics2D g, double t) {
                label = Sound.isEnabled() ? "SOUND: ON" : "SOUND: OFF";
                super.paint(g, t);
            }
        }.scale(2));
        buttons.add(new Button(cx + 8, 434, 152, 44, "EXIT", new Color(0x8f2a3a), app::exit).scale(2));
        setFocus(0);
    }

    @Override
    public void update(double dt) {
        backdrop.update(dt);
    }

    @Override
    protected void onEscape() { app.exit(); }

    @Override
    public void paint(Graphics2D g, double t) {
        backdrop.paint(g, t);
        int cx = GamePanel.W / 2;
        int bob = (int) Math.round(Math.sin(t * 2) * 4 / 4) * 4;
        String title = "MATH STRIKE";
        int x = cx - PixelFont.width(title, 12) / 2;
        PixelFont.draw(g, title, x + 8, 48 + bob + 8, 12, new Color(0xb4472a));
        PixelFont.outlinePx(g, title, x, 48 + bob, 12, new Color(0xffd166), Color.BLACK, 4);
        PixelFont.centeredShadow(g, "METEOROID BLAST", cx, 168, 5, new Color(0x6ff3ff), Color.BLACK);
        PixelFont.centeredShadow(g, "TYPE THE ANSWER. BLAST THE ROCK. SAVE THE PLANET.", cx, 232, 2, new Color(0xc9bff0), Color.BLACK);
        paintButtons(g, t);
        PixelFont.centeredShadow(g, "TEAM 11 - BLAST DEVS   OOP 101   CS-2103", cx, 576, 2, new Color(0x9a90c0), Color.BLACK);
    }
}
