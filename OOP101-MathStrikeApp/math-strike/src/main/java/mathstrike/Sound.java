package mathstrike;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Retro sound effects synthesized in code (no audio files). If the computer has
 * no usable audio device the game simply stays silent.
 */
public final class Sound {
    private static final float RATE = 22050f;
    private static final int VOICES = 3;

    private static final Map<String, Clip[]> CLIPS = new HashMap<>();
    private static final Map<String, Integer> NEXT = new HashMap<>();
    private static boolean available = true;
    private static boolean enabled = true;

    private Sound() { }

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean on) { enabled = on; }

    /** Names: shoot, boom, impact, wrong, good, streak, tick, go, gameover, click. */
    public static void play(String name) {
        if (!enabled || !available) return;
        try {
            Clip[] clips = CLIPS.get(name);
            if (clips == null) {
                byte[] pcm = synth(name);
                if (pcm == null) return;
                AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
                clips = new Clip[VOICES];
                for (int i = 0; i < VOICES; i++) {
                    clips[i] = AudioSystem.getClip();
                    clips[i].open(fmt, pcm, 0, pcm.length);
                }
                CLIPS.put(name, clips);
            }
            int n = NEXT.merge(name, 1, Integer::sum) % VOICES;
            Clip c = clips[n];
            c.stop();
            c.setFramePosition(0);
            c.start();
        } catch (Throwable t) { // no mixer, headless, line busy ...
            available = false;
        }
    }

    // ---------- synthesis ----------

    private static byte[] synth(String name) {
        switch (name) {
            case "shoot":    return tone(1400, 300, 0.12, Wave.SQUARE, 0.25);
            case "boom":     return noise(0.45, 0.5, 900);
            case "impact":   return mix(noise(0.55, 0.6, 500), tone(110, 40, 0.55, Wave.SQUARE, 0.35));
            case "wrong":    return tone(160, 110, 0.28, Wave.SQUARE, 0.3);
            case "good":     return seq(new double[]{660, 880}, 0.07, Wave.SQUARE, 0.22);
            case "streak":   return seq(new double[]{523, 659, 784, 1047}, 0.07, Wave.SQUARE, 0.22);
            case "tick":     return tone(520, 520, 0.08, Wave.SQUARE, 0.2);
            case "go":       return tone(1040, 1040, 0.25, Wave.SQUARE, 0.25);
            case "gameover": return seq(new double[]{392, 330, 262, 196}, 0.18, Wave.TRIANGLE, 0.35);
            case "click":    return tone(800, 800, 0.03, Wave.SQUARE, 0.15);
            default:         return null;
        }
    }

    private enum Wave { SQUARE, TRIANGLE }

    private static double wave(Wave w, double phase) {
        double p = phase - Math.floor(phase);
        return w == Wave.SQUARE ? (p < 0.5 ? 1 : -1) : (p < 0.5 ? 4 * p - 1 : 3 - 4 * p);
    }

    private static byte[] tone(double f0, double f1, double seconds, Wave w, double volume) {
        int n = (int) (RATE * seconds);
        short[] s = new short[n];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double k = i / (double) n;
            phase += (f0 + (f1 - f0) * k) / RATE;
            s[i] = (short) (wave(w, phase) * volume * (1 - k) * 32000);
        }
        return pcm(s);
    }

    private static byte[] seq(double[] freqs, double each, Wave w, double volume) {
        int per = (int) (RATE * each);
        short[] s = new short[per * freqs.length];
        for (int j = 0; j < freqs.length; j++) {
            double phase = 0;
            for (int i = 0; i < per; i++) {
                phase += freqs[j] / RATE;
                double env = 1 - i / (double) per * 0.6;
                s[j * per + i] = (short) (wave(w, phase) * volume * env * 32000);
            }
        }
        return pcm(s);
    }

    private static byte[] noise(double seconds, double volume, double lowpassHz) {
        int n = (int) (RATE * seconds);
        short[] s = new short[n];
        Random r = new Random(5);
        double lp = 0, a = Math.min(1, lowpassHz * 2 * Math.PI / RATE);
        for (int i = 0; i < n; i++) {
            double k = i / (double) n;
            lp += a * ((r.nextDouble() * 2 - 1) - lp);
            s[i] = (short) (lp * 3 * volume * (1 - k) * (1 - k) * 32000);
        }
        return pcm(s);
    }

    private static byte[] mix(byte[] a, byte[] b) {
        int n = Math.max(a.length, b.length) / 2;
        short[] s = new short[n];
        for (int i = 0; i < n; i++) {
            int x = i * 2 < a.length ? (short) ((a[i * 2] & 0xff) | (a[i * 2 + 1] << 8)) : 0;
            int y = i * 2 < b.length ? (short) ((b[i * 2] & 0xff) | (b[i * 2 + 1] << 8)) : 0;
            s[i] = (short) Math.max(-32000, Math.min(32000, x + y));
        }
        return pcm(s);
    }

    private static byte[] pcm(short[] s) {
        byte[] out = new byte[s.length * 2];
        for (int i = 0; i < s.length; i++) {
            out[i * 2] = (byte) (s[i] & 0xff);
            out[i * 2 + 1] = (byte) ((s[i] >> 8) & 0xff);
        }
        return out;
    }
}
