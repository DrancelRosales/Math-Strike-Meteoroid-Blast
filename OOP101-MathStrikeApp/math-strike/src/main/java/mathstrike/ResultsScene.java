package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/** End-of-round report: score, accuracy, congratulation text and a review of every problem. */
public class ResultsScene extends Scene {
    private static final int ROWS = 7;
    private static final Color GOLD = new Color(0xffd166);
    private static final Color MUTED = new Color(0x9a90c0);

    private final Game game;
    private final ScoreManager score;
    private final Backdrop backdrop;
    private final AnswerRecord[] records;
    private final boolean newBest;
    private final String title;
    private int scroll;

    public ResultsScene(GamePanel app, Game game, boolean newBest) {
        super(app);
        this.game = game;
        this.score = game.getMode().getScore();
        this.records = score.getRecords();
        this.newBest = newBest;
        this.title = game.getMode().getKey().equals("classic") ? "GAME OVER" : "TIME'S UP!";
        this.backdrop = new Backdrop(Theme.of(game.getMode().getTheme()), false);
        scroll = Math.max(0, records.length - ROWS);
        buttons.add(new Button(180, 526, 300, 52, "PLAY AGAIN", new Color(0xd9531e), app::playAgain).scale(3));
        buttons.add(new Button(520, 526, 300, 52, "MAIN SCREEN", new Color(0x3b2a8a), app::showTitle).scale(3));
        setFocus(0);
        Sound.play("good");
    }

    @Override protected void onEscape() { app.showTitle(); }

    @Override
    protected void onDefault() { app.playAgain(); }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) { mouseWheel(-1); return; }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) { mouseWheel(1); return; }
        super.keyPressed(e);
    }

    @Override
    public void mouseWheel(int rotation) {
        scroll = Math.max(0, Math.min(Math.max(0, records.length - ROWS), scroll + rotation));
    }

    @Override
    public void update(double dt) { backdrop.update(dt); }

    @Override
    public void paint(Graphics2D g, double t) {
        backdrop.paint(g, t);
        int cx = GamePanel.W / 2;
        PixelFont.centeredOutlinePx(g, title, cx, 24, 8, new Color(0xff5d5d), Color.BLACK, 4);
        int ms = 3;
        String msg = score.getCongratulation().toUpperCase();
        while (ms > 2 && PixelFont.width(msg, ms) > 900) ms--;
        PixelFont.centeredShadow(g, msg, cx, 96, ms, new Color(0xf4efff), Color.BLACK);

        String[] labels = {"FINAL SCORE", "CORRECT", "ACCURACY", "BEST STREAK"};
        String[] values = {String.format("%,d", score.getScore()), score.getCorrect() + "/" + score.getTotal(),
                String.format("%.1f%%", score.getAccuracy()), String.valueOf(score.getBestStreak())};
        for (int i = 0; i < 4; i++) {
            int x = 50 + i * 230, y = 136, w = 210, h = 96;
            g.setColor(Color.BLACK);
            g.fillRect(x - 4, y - 4, w + 8, h + 8);
            g.setColor(new Color(0x1a1238));
            g.fillRect(x, y, w, h);
            g.setColor(game.getMode().getKey().equals("classic") ? new Color(0xff8a3c) : new Color(0x6ff3ff));
            g.fillRect(x, y, w, 6);
            PixelFont.centered(g, labels[i], x + w / 2, y + 18, 2, MUTED);
            int vs = 5;
            while (vs > 2 && PixelFont.width(values[i], vs) > w - 16) vs--;
            PixelFont.centeredShadow(g, values[i], x + w / 2, y + 50, vs, GOLD, Color.BLACK);
        }
        if (newBest && Math.sin(t * 8) > -0.3) {
            PixelFont.centeredOutline(g, "NEW BEST!", 50 + 105, 244, 3, new Color(0x7cff6b), Color.BLACK);
        }

        // review list
        int x = 50, y = 270, w = 900, h = 236;
        g.setColor(Color.BLACK);
        g.fillRect(x - 4, y - 4, w + 8, h + 8);
        g.setColor(new Color(0x1a1238));
        g.fillRect(x, y, w, h);
        PixelFont.draw(g, "ROUND REVIEW", x + 16, y + 10, 2, new Color(0x6ff3ff));
        if (records.length > ROWS) {
            PixelFont.right(g, "SCROLL: WHEEL OR UP/DOWN", x + w - 16, y + 10, 2, MUTED);
        }
        if (records.length == 0) {
            PixelFont.centered(g, "NO PROBLEMS ANSWERED THIS ROUND.", cx, y + 100, 2, MUTED);
        }
        for (int i = 0; i < ROWS && scroll + i < records.length; i++) {
            AnswerRecord r = records[scroll + i];
            int ry = y + 38 + i * 27;
            boolean ok = r.outcome() == AnswerRecord.Outcome.CORRECT;
            Color c = ok ? new Color(0x7cff6b) : new Color(0xff7a7a);
            g.setColor(new Color(255, 255, 255, i % 2 == 0 ? 10 : 0));
            g.fillRect(x + 8, ry - 4, w - 16, 26);
            String left = r.question().equals("-") ? "WRONG ANSWER" : r.question() + " = " + r.correctAnswer();
            PixelFont.draw(g, left, x + 20, ry, 2, c);
            String right = ok ? "CORRECT" : r.outcome() == AnswerRecord.Outcome.MISSED ? "MISSED" : "YOU TYPED " + r.given();
            PixelFont.right(g, right, x + w - 20, ry, 2, c);
        }
        paintButtons(g, t);
    }
}
