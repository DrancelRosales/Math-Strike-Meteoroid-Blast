package mathstrike;

import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Drives the real GamePanel without a window: clicks through every menu, plays both
 * modes with a bot and saves a PNG of each screen.   Usage: UiSmoke <outputDir>
 */
public class UiSmoke {
    static GamePanel panel;
    static File out;
    static int failures = 0;

    static void check(boolean ok, String what) {
        if (!ok) { failures++; System.out.println("FAIL: " + what); } else System.out.println("ok:   " + what);
    }

    static void run(double seconds) {
        for (double t = 0; t < seconds; t += 1 / 60.0) panel.tick(1 / 60.0);
    }

    static void shot(String name) throws Exception {
        ImageIO.write(panel.renderFrame(), "png", new File(out, name + ".png"));
    }

    static void click(int x, int y) { panel.getScene().mouseMoved(x, y); panel.getScene().mousePressed(x, y); }

    static void key(int code, char c) {
        KeyEvent e = new KeyEvent(panel, KeyEvent.KEY_PRESSED, 0, 0, code, c);
        panel.getScene().keyPressed(e);
        if (Character.isDigit(c)) panel.getScene().keyTyped(c);
    }

    static void type(int number) {
        for (char c : String.valueOf(number).toCharArray()) key(KeyEvent.VK_0 + (c - '0'), c);
        key(KeyEvent.VK_ENTER, '\n');
    }

    static boolean is(Class<?> c) { return c.isInstance(panel.getScene()); }

    /** Plays the current PlayScene, answering the lowest target every 0.7 s. */
    static void bot(double seconds, int maxSolved) {
        int solved = 0;
        for (double t = 0; t < seconds && solved < maxSolved && is(PlayScene.class); t += 0.35) {
            run(0.35);
            PlayScene ps = (PlayScene) panel.getScene();
            Game g = ps.getGame();
            if (g.getPhase() != Game.Phase.PLAYING || g.getTargets().isEmpty()) continue;
            Target low = g.getTargets().get(0);
            for (Target x : g.getTargets()) if (x.getY() > low.getY()) low = x;
            type(low.getProblem().getAnswer());
            solved++;
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        out = new File(args[0]);
        out.mkdirs();
        Sound.setEnabled(false);
        panel = new GamePanel(() -> { });
        panel.setSize(GamePanel.W, GamePanel.H);

        run(3.0);
        check(is(TitleScene.class), "title screen shows");
        shot("01-title");

        click(500, 320);                       // PLAY
        check(is(ChoiceScene.class), "PLAY opens mode selection");
        run(1.0); shot("02-modes");

        click(295, 300);                       // CLASSIC card
        check(is(ChoiceScene.class), "CLASSIC opens difficulty selection");
        run(1.0); shot("03-difficulty");

        click(800, 300);                       // HARD card
        check(is(TextScene.class), "difficulty opens the briefing");
        run(0.5); shot("04-briefing-hard");
        key(KeyEvent.VK_ESCAPE, (char) 27);    // back to difficulty
        click(500, 300);                       // MEDIUM
        click(510, 540);                       // BEGIN MISSION (touches 280..540 button)
        check(is(PlayScene.class), "BEGIN MISSION starts the game");

        run(1.5); shot("05-countdown");
        run(3.0);
        run(9.0);
        shot("06-classic-play");
        bot(8, 6);
        run(0.12); shot("07-classic-hit");
        key(KeyEvent.VK_5, '5'); key(KeyEvent.VK_9, '9'); key(KeyEvent.VK_9, '9'); key(KeyEvent.VK_ENTER, '\n'); // wrong answer
        run(0.25); shot("08-classic-wrong");
        PlayScene ps = (PlayScene) panel.getScene();
        int hearts = ps.getGame().getMode().getHearts();
        check(hearts < ps.getGame().getMode().getMaxHearts(), "wrong answer cost a heart (" + hearts + ")");

        // keypad via mouse: type "4", FIRE
        click(GamePanel.W - 150, 158 + 51 * 1);   // key "4"
        click(GamePanel.W - 30, 158 + 51 * 3);    // FIRE
        key(KeyEvent.VK_ESCAPE, (char) 27);       // quit confirmation
        run(0.2); shot("09-quit-confirm");
        key(KeyEvent.VK_N, 'n');

        for (int i = 0; i < 60 * 150 && is(PlayScene.class); i++) panel.tick(1 / 60.0);
        check(is(ResultsScene.class), "classic round ends on the results screen");
        run(1.0); shot("10-results");

        // Time Attack
        click(670, 552);                          // MAIN SCREEN
        check(is(TitleScene.class), "MAIN SCREEN returns to title");
        click(500, 320); click(705, 300);         // PLAY, TIME ATTACK
        check(is(TextScene.class), "TIME ATTACK opens the briefing");
        run(0.5); shot("11-briefing-ta");
        key(KeyEvent.VK_ENTER, '\n');             // focused BEGIN MISSION
        check(is(PlayScene.class), "ENTER starts Time Attack");
        run(4.5);
        bot(14, 8);
        run(0.1); shot("12-ta-play");
        run(6); shot("13-ta-play2");
        for (int i = 0; i < 60 * 200 && is(PlayScene.class); i++) panel.tick(1 / 60.0);
        check(is(ResultsScene.class), "time attack ends on the results screen");
        run(1.0); shot("14-results-ta");

        click(330, 552);                          // PLAY AGAIN
        check(is(PlayScene.class), "PLAY AGAIN starts a new round");
        key(KeyEvent.VK_ESCAPE, (char) 27); key(KeyEvent.VK_Y, 'y');
        check(is(TitleScene.class), "quit confirmation returns to the title");
        click(500, 392);                          // HOW TO PLAY
        check(is(TextScene.class), "HOW TO PLAY opens");
        run(0.4); shot("15-howto");
        click(840, 546);                          // NEXT >
        run(0.2); shot("16-howto-2");

        System.out.println(failures == 0 ? "UI SMOKE TEST PASSED" : failures + " FAILURES");
        System.exit(failures == 0 ? 0 : 1);
    }
}
