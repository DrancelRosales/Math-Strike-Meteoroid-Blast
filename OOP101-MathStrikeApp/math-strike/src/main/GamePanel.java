package mathstrike;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The one Swing component of the game. It owns the current Scene, draws it into a
 * fixed 1000x600 frame, scales that frame to the window, and forwards input.
 */
public class GamePanel extends JPanel {
    public static final int W = 1000;
    public static final int H = 600;

    private final BufferedImage frame = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
    private final Runnable exitAction;
    private Scene scene;
    private double time;
    private long lastNanos = System.nanoTime();
    private Timer timer;

    private String modeKey = "classic";
    private Difficulty difficulty = Difficulty.EASY;

    public GamePanel(Runnable exitAction) {
        this.exitAction = exitAction;
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.BLACK);
        setFocusable(true);
        setDoubleBuffered(true);

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_M && !(scene instanceof PlayScene)) {
                    Sound.setEnabled(!Sound.isEnabled());
                    return;
                }
                scene.keyPressed(e);
            }

            @Override public void keyTyped(KeyEvent e) { scene.keyTyped(e.getKeyChar()); }
        });
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { scene.mouseMoved(lx(e), ly(e)); }
            @Override public void mouseDragged(MouseEvent e) { scene.mouseMoved(lx(e), ly(e)); }
            @Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); scene.mousePressed(lx(e), ly(e)); }
            @Override public void mouseWheelMoved(MouseWheelEvent e) { scene.mouseWheel(e.getWheelRotation()); }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);

        scene = new TitleScene(this);
    }

    /** Starts the 60-frames-per-second game loop. */
    public void start() {
        lastNanos = System.nanoTime();
        timer = new Timer(14, e -> {
            long now = System.nanoTime();
            double dt = Math.min(0.1, (now - lastNanos) / 1e9);
            lastNanos = now;
            tick(dt);
            repaint();
        });
        timer.start();
    }

    /** Advances the current scene (also used by tests that drive the game without a window). */
    public void tick(double dt) {
        time += dt;
        scene.update(dt);
    }

    // ---------- coordinates: window -> logical 1000x600 ----------

    private double scale() { return Math.min(getWidth() / (double) W, getHeight() / (double) H); }
    private double offX() { return (getWidth() - W * scale()) / 2; }
    private double offY() { return (getHeight() - H * scale()) / 2; }
    private int lx(MouseEvent e) { return (int) ((e.getX() - offX()) / scale()); }
    private int ly(MouseEvent e) { return (int) ((e.getY() - offY()) / scale()); }

    // ---------- painting ----------

    /** Draws the current scene into the 1000x600 frame and returns it. */
    public BufferedImage renderFrame() {
        Graphics2D g = frame.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, W, H);
        scene.paint(g, time);
        g.dispose();
        return frame;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        renderFrame();
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        double s = scale();
        g.drawImage(frame, (int) offX(), (int) offY(), (int) Math.round(W * s), (int) Math.round(H * s), null);
    }

    // ---------- navigation between scenes ----------

    private void setScene(Scene next) {
        scene = next;
        Sprites.clear();
    }

    public Scene getScene() { return scene; }

    public void showTitle() { setScene(new TitleScene(this)); }

    public void showModes() {
        List<ChoiceScene.Choice> choices = new ArrayList<>();
        choices.add(new ChoiceScene.Choice("CLASSIC", "METEORS - PREHISTORIC",
                new String[]{"Pick a difficulty.", "Lose all your hearts and the round is over."},
                new Color(0xff8a3c), ChoiceScene.targetIcon(true), () -> { modeKey = "classic"; showDifficulty(); }));
        choices.add(new ChoiceScene.Choice("TIME ATTACK", "ASTEROIDS - SPACE",
                new String[]{"All operations switched on.", "Correct answers add time, but problems get harder."},
                new Color(0x6ff3ff), ChoiceScene.targetIcon(false), () -> { modeKey = "timeattack"; showBriefing(); }));
        setScene(new ChoiceScene(this, Theme.of("space"), "CHOOSE YOUR MODE", choices, this::showTitle));
    }

    public void showDifficulty() {
        List<ChoiceScene.Choice> choices = new ArrayList<>();
        for (Difficulty d : Difficulty.values()) {
            Color c = d == Difficulty.EASY ? new Color(0x7cff6b) : d == Difficulty.MEDIUM ? new Color(0xffb347) : new Color(0xff5d5d);
            String ops = d == Difficulty.EASY ? "+  -" : d == Difficulty.MEDIUM ? "+  -  ×" : "+  -  ×  ÷  ( )";
            choices.add(new ChoiceScene.Choice(d.getLabel().toUpperCase(), "NUMBERS " + d.getMin() + "-" + d.getMax(),
                    new String[]{ops, d.getHearts() + " hearts"}, c, ChoiceScene.heartsIcon(d.getHearts()),
                    () -> { difficulty = d; showBriefing(); }));
        }
        setScene(new ChoiceScene(this, Theme.of("prehistoric"), "SELECT DIFFICULTY", choices, this::showModes));
    }

    public void showHowTo() {
        List<List<TextScene.Section>> pages = new ArrayList<>();
        pages.add(List.of(
                new TextScene.Section("GOAL", "Math problems fall from the sky on meteors and asteroids. Type the answer and press ENTER to blast the matching target before it hits the ground."),
                new TextScene.Section("CONTROLS", "Type with your keyboard or click the on-screen keypad. ENTER fires, BACKSPACE deletes, ESC quits the round. Answers are positive whole numbers. Press M on menus to mute."),
                new TextScene.Section("OPERATIONS", "Addition (+), subtraction (-), multiplication (×), division (÷) and order of operations such as 4 + 3 × 2 or (5 + 3) × 2. Division always comes out even.")));
        pages.add(List.of(
                new TextScene.Section("CLASSIC MODE", "Choose Easy (3 hearts), Medium (4 hearts) or Hard (5 hearts), all set in a prehistoric valley. A wrong answer or a meteor that lands costs 1 heart. No hearts left means game over. The meteors speed up as you score."),
                new TextScene.Section("TIME ATTACK", "Start with 60 seconds. Every correct answer adds 4 seconds and every 3 correct answers raise the level and the difficulty. A wrong answer or a landed asteroid costs 2 seconds. When the clock hits zero the round is over.")));
        pages.add(List.of(
                new TextScene.Section("SCORING", "Each hit earns 100 points plus up to 50 for speed, times your streak multiplier: X2 at 3 in a row, X3 at 6, X4 at 9 and X5 at 12 or more. Any mistake resets the streak."),
                new TextScene.Section("GOOD TO KNOW", "Empty answers cost nothing. Quitting a round early does not save it. Your best score for each mode is remembered on this computer.")));
        setScene(new TextScene(this, Theme.of("space"), "HOW TO PLAY", pages, null, null, this::showTitle));
    }

    public void showBriefing() {
        boolean classic = modeKey.equals("classic");
        List<TextScene.Section> page = new ArrayList<>();
        String title;
        if (classic) {
            title = "CLASSIC - " + difficulty.getLabel().toUpperCase();
            StringBuilder ops = new StringBuilder();
            for (char c : difficulty.getOperators()) ops.append(c == '*' ? "×" : c == '/' ? "÷" : String.valueOf(c)).append("  ");
            if (difficulty.usesOrderOfOperations()) ops.append("( )  order of operations");
            page.add(new TextScene.Section("TARGET", "Meteors fall over a prehistoric valley. Blast them before they land."));
            page.add(new TextScene.Section("OPERATIONS", ops.toString().trim() + "    Numbers " + difficulty.getMin() + " to " + difficulty.getMax() + "."));
            page.add(new TextScene.Section("HEARTS", "You start with " + difficulty.getHearts() + " hearts. A wrong answer or a landed meteor costs one."));
        } else {
            title = "TIME ATTACK";
            page.add(new TextScene.Section("TARGET", "Asteroids sway down through space above your lunar base. Blast them before they land."));
            page.add(new TextScene.Section("OPERATIONS", "+  -  ×  ÷  ( )    Everything is on, but problems start easy and grow harder as you level up."));
            page.add(new TextScene.Section("TIMER", "You start with 60 seconds. Each correct answer adds 4 seconds. Mistakes cost 2 seconds."));
        }
        Runnable back = classic ? this::showDifficulty : this::showModes;
        Theme theme = Theme.of(classic ? "prehistoric" : "space");
        setScene(new TextScene(this, theme, "MISSION BRIEFING: " + title, List.of(page), "BEGIN MISSION", this::startGame, back));
    }

    public void startGame() {
        Random random = new Random();
        GameMode mode = modeKey.equals("timeattack") ? new TimeAttackMode(random) : new ClassicMode(random, difficulty);
        Game game = new Game(mode, random);
        game.start();
        setScene(new PlayScene(this, game, modeKey, difficulty));
    }

    public void playAgain() { startGame(); }

    public void showResults(Game game, boolean newBest, String key) {
        setScene(new ResultsScene(this, game, newBest));
    }

    public void exit() { exitAction.run(); }
}
