package mathstrike;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/** One screen of the game (menu, play field, results ...). */
public abstract class Scene {
    protected final GamePanel app;
    protected final List<Button> buttons = new ArrayList<>();
    protected int focus = -1;

    protected Scene(GamePanel app) {
        this.app = app;
    }

    public void update(double dt) { }

    public abstract void paint(Graphics2D g, double t);

    public void mouseMoved(int x, int y) {
        for (Button b : buttons) {
            b.hover = b.enabled && b.contains(x, y);
        }
    }

    public void mousePressed(int x, int y) {
        for (Button b : new ArrayList<>(buttons)) {
            if (b.enabled && b.contains(x, y)) {
                click(b);
                return;
            }
        }
    }

    public void mouseWheel(int rotation) { }

    public void keyTyped(char c) { }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) { onEscape(); return; }
        if (buttons.isEmpty()) return;
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_RIGHT) moveFocus(1);
        else if (code == KeyEvent.VK_UP || code == KeyEvent.VK_LEFT) moveFocus(-1);
        else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            if (focus >= 0 && focus < buttons.size()) click(buttons.get(focus));
            else onDefault();
        }
    }

    /** Called when ESC is pressed. */
    protected void onEscape() { }

    /** Called when ENTER is pressed with nothing focused. */
    protected void onDefault() { }

    protected void click(Button b) {
        Sound.play("click");
        b.action.run();
    }

    private void moveFocus(int dir) {
        int n = buttons.size();
        int next = focus < 0 ? (dir > 0 ? 0 : n - 1) : (focus + dir + n) % n;
        for (int i = 0; i < n; i++) {
            if (buttons.get(next).enabled) break;
            next = (next + dir + n) % n;
        }
        setFocus(next);
    }

    protected void setFocus(int index) {
        focus = index;
        for (int i = 0; i < buttons.size(); i++) buttons.get(i).focus = i == index;
    }

    protected void paintButtons(Graphics2D g, double t) {
        for (Button b : buttons) b.paint(g, t);
    }
}
