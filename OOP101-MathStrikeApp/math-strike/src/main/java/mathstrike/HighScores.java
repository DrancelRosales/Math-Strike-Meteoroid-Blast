package mathstrike;

import java.util.prefs.Preferences;

/** Remembers the best score of each mode/difficulty on this computer (no accounts needed). */
public final class HighScores {
    private static final Preferences PREFS = Preferences.userNodeForPackage(HighScores.class);

    private HighScores() { }

    public static int get(String key) {
        try {
            return PREFS.getInt(key, 0);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    /** Stores the score if it beats the old one; returns true for a new best. */
    public static boolean submit(String key, int score) {
        try {
            if (score > PREFS.getInt(key, 0)) {
                PREFS.putInt(key, score);
                return score > 0;
            }
        } catch (RuntimeException e) {
            // storage unavailable: just skip saving
        }
        return false;
    }
}
