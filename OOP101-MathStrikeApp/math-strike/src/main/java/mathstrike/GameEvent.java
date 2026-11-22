package mathstrike;

/**
 * Something the screen should animate once: a destroyed target, a ground hit,
 * a wrong answer, or a floating feedback message.
 *
 * @param type   destroy | ground | wrong | feedback | points | bonus | gameover
 * @param x      world x position
 * @param y      world y position
 * @param text   message text
 * @param tone   good | bad | info
 * @param target the target involved (destroy / ground), otherwise null
 */
public record GameEvent(String type, double x, double y, String text, String tone, Target target) { }
