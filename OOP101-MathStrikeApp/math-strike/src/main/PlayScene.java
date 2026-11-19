package mathstrike;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** The play field: themed world, falling targets, HUD, answer box and keypad. */
public class PlayScene extends Scene {
    private static final int FIELD_W = Target.WORLD_WIDTH;
    private static final int SIDE_X = FIELD_W;
    private static final Color GOLD = new Color(0xffd166);
    private static final Color MUTED = new Color(0x9a90c0);
    private static final Color[] MULT_COLORS = {Color.WHITE, new Color(0x7cff6b), new Color(0x6ff3ff), GOLD, new Color(0xff6bd6)};
    private static final String[] HEART = {".XX..XX.", "XXXXXXXX", "XXXXXXXX", "XXXXXXXX", ".XXXXXX.", "..XXXX..", "...XX..."};

    private final Game game;
    private final GameMode mode;
    private final Theme theme;
    private final Effects fx = new Effects();
    private final Random random = new Random();
    private final StringBuilder input = new StringBuilder();
    private final String scoreKey;
    private final Button[] keypad = new Button[12];
    private final Button soundButton;
    private final Button quitButton;
    private final Button yes;
    private final Button no;

    private double time;
    private double aim = -Math.PI / 2;
    private double recoil;
    private double goTimer;
    private double endTimer;
    private double heartShake;
    private int lastHearts;
    private int lastTick = -1;
    private boolean confirming;
    private boolean finished;
    private boolean newBest;

    public PlayScene(GamePanel app, Game game, String modeKey, Difficulty difficulty) {
        super(app);
        this.game = game;
        this.mode = game.getMode();
        this.theme = Theme.of(mode.getTheme());
        this.scoreKey = "best." + modeKey + "." + (modeKey.equals("classic") ? difficulty.name() : "all");
        this.lastHearts = mode.getHearts();
        Sprites.clear();

        String[] labels = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "<", "0", "FIRE"};
        Color accent = theme.getAccent();
        for (int i = 0; i < 12; i++) {
            final String key = labels[i];
            int col = i % 3, row = i / 3;
            Color c = key.equals("FIRE") ? new Color(0xd9531e) : key.equals("<") ? new Color(0x8f2a3a) : new Color(0x3b2a8a);
            Button b = new Button(SIDE_X + 14 + col * 61, 158 + row * 51, 54, 44, key, c, () -> pressKey(key)).scale(key.equals("FIRE") ? 2 : 3);
            keypad[i] = b;
            buttons.add(b);
        }
        soundButton = new Button(SIDE_X + 14, 478, 172, 34, "SOUND ON", new Color(0x2b2360), () -> Sound.setEnabled(!Sound.isEnabled())) {
            @Override public void paint(Graphics2D g, double t) {
                label = Sound.isEnabled() ? "SOUND ON" : "SOUND OFF";
                super.paint(g, t);
            }
        }.scale(2);
        quitButton = new Button(SIDE_X + 14, 530, 172, 40, "QUIT", new Color(0x8f2a3a), () -> confirming = true).scale(2);
        buttons.add(soundButton);
        buttons.add(quitButton);
        yes = new Button(330, 330, 150, 54, "YES", new Color(0x8f2a3a), this::quit).scale(3);
        no = new Button(520, 330, 150, 54, "NO", new Color(0x2f8a3a), () -> confirming = false).scale(3);
        accentColor = accent;
    }

    private final Color accentColor;

    public Game getGame() { return game; }

    // ------------------------------------------------------------ input

    private void pressKey(String key) {
        if (key.equals("<")) {
            if (input.length() > 0) input.setLength(input.length() - 1);
        } else if (key.equals("FIRE")) {
            fire();
        } else if (input.length() < 6) {
            input.append(key);
        }
    }

    private void fire() {
        String text = input.toString();
        input.setLength(0);
        game.checkAnswer(text);
    }

    private void quit() {
        finished = true;
        app.showTitle();
    }

    @Override
    public void keyTyped(char c) {
        if (confirming) return;
        if (c >= '0' && c <= '9' && input.length() < 6) input.append(c);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (confirming) {
            if (code == KeyEvent.VK_Y || code == KeyEvent.VK_ENTER) quit();
            else if (code == KeyEvent.VK_N || code == KeyEvent.VK_ESCAPE) confirming = false;
            return;
        }
        if (code == KeyEvent.VK_BACK_SPACE) pressKey("<");
        else if (code == KeyEvent.VK_ENTER) fire();
        else if (code == KeyEvent.VK_ESCAPE) confirming = true;
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        yes.hover = confirming && yes.contains(x, y);
        no.hover = confirming && no.contains(x, y);
    }

    @Override
    public void mousePressed(int x, int y) {
        if (confirming) {
            if (yes.contains(x, y)) click(yes);
            else if (no.contains(x, y)) click(no);
            return;
        }
        super.mousePressed(x, y);
    }

    // ------------------------------------------------------------ update

    @Override
    public void update(double dt) {
        time += dt;
        if (confirming || finished) return;
        dt = Math.min(dt, 0.05);

        Game.Phase before = game.getPhase();
        game.update(dt);
        if (before == Game.Phase.READY && game.getPhase() == Game.Phase.PLAYING) {
            goTimer = 0.8;
            Sound.play("go");
        }
        if (game.getPhase() == Game.Phase.READY) {
            int n = (int) Math.ceil(game.getReadyLeft());
            if (n != lastTick) { lastTick = n; Sound.play("tick"); }
        }
        goTimer = Math.max(0, goTimer - dt);

        for (GameEvent e : game.drainEvents()) handle(e);

        Target lowest = lowestTarget();
        if (lowest != null) {
            double goal = Math.atan2(lowest.getY() - (Target.GROUND_Y - 22), lowest.getX() - FIELD_W / 2.0);
            goal = Math.max(-Math.PI + 0.25, Math.min(-0.25, goal));
            aim += (goal - aim) * Math.min(1, dt * 12);
        } else {
            aim += (-Math.PI / 2 - aim) * Math.min(1, dt * 5);
        }
        recoil = Math.max(0, recoil - dt * 5);
        heartShake = Math.max(0, heartShake - dt);
        if (mode.getHearts() < lastHearts) heartShake = 0.45;
        lastHearts = mode.getHearts();
        fx.update(dt);

        Set<Integer> alive = new HashSet<>();
        for (Target t : game.getTargets()) alive.add(t.getId());
        Sprites.prune(alive);

        if (game.getPhase() == Game.Phase.GAME_OVER) {
            endTimer += dt;
            if (endTimer > 1.9 && !finished) {
                finished = true;
                newBest = HighScores.submit(scoreKey, mode.getScore().getScore());
                app.showResults(game, newBest, scoreKey);
            }
        }
    }

    private Target lowestTarget() {
        Target lowest = null;
        for (Target t : game.getTargets()) if (lowest == null || t.getY() > lowest.getY()) lowest = t;
        return lowest;
    }

    private void handle(GameEvent e) {
        switch (e.type()) {
            case "destroy": {
                double[] m = theme.muzzle(aim, 0);
                fx.beam(m[0], m[1], e.x(), e.y(), theme.getBeamColor());
                e.target().explode(fx, false);
                for (int i = 0; i < 6; i++) {
                    fx.spark(m[0], m[1], Math.cos(aim) * 200 + (random.nextDouble() - 0.5) * 160,
                            Math.sin(aim) * 200 + (random.nextDouble() - 0.5) * 160, theme.getBeamColor(), 0.25);
                }
                recoil = 1;
                Sound.play("shoot");
                Sound.play("boom");
                break;
            }
            case "points":
                fx.floatText(e.text(), e.x(), e.y() - 40, -60, 0.9, GOLD, 4);
                break;
            case "bonus":
                fx.floatText(e.text().replace("s", " SEC"), 690, 72, 40, 1.1, new Color(0x7cff6b), 3);
                break;
            case "ground":
                e.target().explode(fx, true);
                fx.scar(e.x());
                fx.shake(0.35);
                fx.flash(0.35);
                Sound.play("impact");
                break;
            case "wrong":
                fx.fireball(FIELD_W / 2.0, Target.GROUND_Y - 14, 40, true);
                fx.shake(0.3);
                fx.flash(0.3);
                Sound.play("wrong");
                break;
            case "feedback":
                fx.feedback(e.text(), e.tone());
                if (e.text().startsWith("Streak")) Sound.play("streak");
                break;
            case "gameover":
                Sound.play("gameover");
                break;
            default:
                break;
        }
    }

    // ------------------------------------------------------------ painting

    @Override
    public void paint(Graphics2D g, double t) {
        Graphics2D f = (Graphics2D) g.create(0, 0, FIELD_W, Theme.HEIGHT);
        if (fx.getShake() > 0) {
            double m = fx.getShake() * 26;
            f.translate((random.nextDouble() - 0.5) * m, (random.nextDouble() - 0.5) * m);
        }
        theme.paintBackdrop(f, time, FIELD_W);
        fx.paintScars(f);
        for (Target target : game.getTargets()) target.draw(f, time);
        theme.drawTurret(f, aim, recoil, time);
        fx.paint(f);
        paintDanger(f);
        if (fx.getFlash() > 0) {
            f.setColor(new Color(255, 60, 40, (int) (Math.min(0.45, fx.getFlash()) * 255)));
            f.fillRect(-20, -20, FIELD_W + 40, Theme.HEIGHT + 40);
        }
        paintHud(f);
        paintOverlay(f);
        f.dispose();

        paintSidebar(g, t);
        paintButtons(g, t);
        if (confirming) paintConfirm(g, t);
    }

    private boolean inDanger() {
        if (game.getPhase() != Game.Phase.PLAYING) return false;
        if (mode.getMaxHearts() > 1) return mode.getHearts() == 1;
        return mode.getTimeLeft() >= 0 && mode.getTimeLeft() <= 10;
    }

    private void paintDanger(Graphics2D g) {
        if (!inDanger()) return;
        double pulse = 0.5 + 0.5 * Math.sin(time * 6);
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(255, 40, 40, (int) ((70 - i * 11) * (0.4 + 0.6 * pulse))));
            g.drawRect(i * 4, 56 + i * 4, FIELD_W - i * 8 - 1, Theme.HEIGHT - 56 - i * 8 - 1);
        }
    }

    static void drawHeart(Graphics2D g, int x, int y, int cell, boolean full) {
        for (int r = 0; r < HEART.length; r++) {
            for (int c = 0; c < 8; c++) {
                if (HEART[r].charAt(c) != 'X') continue;
                g.setColor(full ? (r < 2 && c < 3 ? new Color(0xff9aae) : new Color(0xff4d6d)) : new Color(0x3a2f5e));
                g.fillRect(x + c * cell, y + r * cell, cell, cell);
            }
        }
    }

    private void paintHud(Graphics2D g) {
        ScoreManager sc = mode.getScore();
        g.setColor(new Color(8, 3, 20, 190));
        g.fillRect(0, 0, FIELD_W, 56);
        g.setColor(accentColor);
        g.fillRect(0, 56, FIELD_W, 4);

        PixelFont.draw(g, "SCORE", 16, 6, 2, MUTED);
        PixelFont.draw(g, String.format("%,d", sc.getScore()), 16, 24, 4, GOLD);

        int mult = sc.getMultiplier();
        Color mc = MULT_COLORS[Math.min(MULT_COLORS.length, mult) - 1];
        PixelFont.centered(g, "STREAK " + sc.getStreak(), FIELD_W / 2 - 30, 6, 2, MUTED);
        PixelFont.centeredOutline(g, "X" + mult, FIELD_W / 2 + 112, 14, 4, mc, Color.BLACK);
        int barW = 160, barX = FIELD_W / 2 - 110;
        g.setColor(Color.BLACK);
        g.fillRect(barX - 2, 40, barW + 4, 12);
        g.setColor(new Color(0x2a2150));
        g.fillRect(barX, 42, barW, 8);
        double frac = mult >= ScoreManager.MAX_MULTIPLIER ? 1 : (sc.getStreak() % 3) / 3.0;
        g.setColor(mc);
        g.fillRect(barX, 42, (int) (barW * frac), 8);

        if (mode.getMaxHearts() > 0) {
            int n = mode.getMaxHearts();
            int x0 = FIELD_W - 16 - n * 36;
            for (int i = 0; i < n; i++) {
                int shake = (i == mode.getHearts() && heartShake > 0) ? (int) (Math.sin(time * 60) * 4) : 0;
                drawHeart(g, x0 + i * 36 + shake, 14, 4, i < mode.getHearts());
            }
        } else {
            double left = mode.getTimeLeft();
            boolean low = left <= 10 && game.getPhase() == Game.Phase.PLAYING;
            Color c = low && Math.sin(time * 12) > 0 ? new Color(0xff5d5d) : low ? Color.WHITE : GOLD;
            PixelFont.right(g, "TIME", FIELD_W - 16, 6, 2, MUTED);
            PixelFont.right(g, String.format("%.1f", Math.max(0, left)), FIELD_W - 16, 24, 4, c);
        }
    }

    private void paintOverlay(Graphics2D g) {
        Game.Phase phase = game.getPhase();
        if (phase == Game.Phase.READY) {
            g.setColor(new Color(8, 3, 20, 120));
            g.fillRect(0, 60, FIELD_W, Theme.HEIGHT);
            int n = Math.max(1, (int) Math.ceil(game.getReadyLeft()));
            PixelFont.centeredOutlinePx(g, String.valueOf(n), FIELD_W / 2, 190, 24, GOLD, Color.BLACK, 8);
            PixelFont.centeredOutline(g, "GET READY", FIELD_W / 2, 400, 5, Color.WHITE, Color.BLACK);
            PixelFont.centeredOutline(g, mode.getName().toUpperCase(), FIELD_W / 2, 100, 3, theme.getAccent(), Color.BLACK);
        } else if (goTimer > 0) {
            PixelFont.centeredOutlinePx(g, "GO!", FIELD_W / 2, 220, 16, new Color(0x7cff6b), Color.BLACK, 6);
        } else if (phase == Game.Phase.GAME_OVER) {
            g.setColor(new Color(8, 3, 20, (int) Math.min(150, endTimer * 160)));
            g.fillRect(0, 60, FIELD_W, Theme.HEIGHT);
            String msg = mode.getKey().equals("classic") ? "GAME OVER" : "TIME'S UP!";
            PixelFont.centeredOutlinePx(g, msg, FIELD_W / 2, 240, 9, new Color(0xff5d5d), Color.BLACK, 4);
        }
    }

    private void paintSidebar(Graphics2D g, double t) {
        g.setColor(new Color(0x120a2a));
        g.fillRect(SIDE_X, 0, GamePanel.W - SIDE_X, Theme.HEIGHT);
        g.setColor(accentColor);
        g.fillRect(SIDE_X, 0, 4, Theme.HEIGHT);
        g.setColor(Color.BLACK);
        g.fillRect(SIDE_X + 4, 0, 4, Theme.HEIGHT);
        int cx = SIDE_X + 100;

        PixelFont.centered(g, mode.getName().toUpperCase(), cx, 14, 2, accentColor);
        PixelFont.centered(g, mode.getSubtitle().toUpperCase(), cx, 36, 2, Color.WHITE);
        PixelFont.centered(g, "BEST " + HighScores.get(scoreKey), cx, 60, 2, MUTED);

        PixelFont.centered(g, "ANSWER", cx, 86, 2, MUTED);
        g.setColor(Color.BLACK);
        g.fillRect(SIDE_X + 10, 106, 180, 48 + 4);
        g.setColor(new Color(0x0d0820));
        g.fillRect(SIDE_X + 14, 110, 172, 44);
        g.setColor(accentColor);
        g.fillRect(SIDE_X + 14, 150, 172, 4);
        String shown = input.toString();
        boolean cursor = (int) (t * 2) % 2 == 0 && game.getPhase() == Game.Phase.PLAYING;
        int tx = SIDE_X + 14 + 172 - 10;
        if (cursor) { g.setColor(GOLD); g.fillRect(tx - (shown.isEmpty() ? 0 : PixelFont.width(shown, 4) + 4) - 8, 118, 8, 28); }
        PixelFont.right(g, shown, tx - (cursor ? 14 : 0), 118, 4, GOLD);

        PixelFont.centered(g, "OPERATIONS", cx, 418, 2, MUTED);
        PixelFont.centeredShadow(g, operations(), cx, 440, 3, Color.WHITE, Color.BLACK);
        PixelFont.centered(g, "ENTER = FIRE", cx, 466, 1, MUTED);
    }

    private String operations() {
        if (mode.getKey().equals("timeattack")) return "+ - × ÷ ( )";
        Difficulty d = Difficulty.fromString(mode.getSubtitle());
        StringBuilder sb = new StringBuilder();
        for (char c : d.getOperators()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(c == '*' ? '×' : c == '/' ? '÷' : c);
        }
        if (d.usesOrderOfOperations()) sb.append(" ( )");
        return sb.toString();
    }

    private void paintConfirm(Graphics2D g, double t) {
        g.setColor(new Color(4, 2, 16, 200));
        g.fillRect(0, 0, GamePanel.W, Theme.HEIGHT);
        g.setColor(Color.BLACK);
        g.fillRect(246, 196, 508, 216);
        g.setColor(new Color(0x1a1238));
        g.fillRect(250, 200, 500, 208);
        PixelFont.centeredShadow(g, "QUIT THIS ROUND?", GamePanel.W / 2, 226, 4, GOLD, Color.BLACK);
        PixelFont.centered(g, "YOUR PROGRESS WILL NOT BE SAVED.", GamePanel.W / 2, 282, 2, new Color(0xc9bff0));
        yes.paint(g, t);
        no.paint(g, t);
    }
}
