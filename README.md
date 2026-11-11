# Math Strike: Meteoroid Blast

OOP 101 project of **Team 11 – Blast Devs** (CS-2103). A pixel-art arcade game where math problems fall on
meteors (Classic Mode, prehistoric valley) and asteroids (Time Attack, lunar base in space).
Type the answer, press Enter, blast the target.

**100 % Java** (Swing/AWT from the JDK – no libraries, no browser, no server, no image or sound files).

## Run it

Needs a JDK 17 or newer (`java -version`).

```bash
./build.sh run          # Linux / macOS: compile, package and start the game
./build.sh test         # also run the rule self-tests (250k+ checks)
build.bat               # Windows: compile + package, then:  java -jar MathStrike.jar
java -jar MathStrike.jar    # start the prebuilt jar
```

## Controls

| Input | Action |
|---|---|
| Keyboard digits / on-screen keypad | Type the answer |
| Enter or FIRE | Shoot |
| Backspace or `<` | Delete a digit |
| Esc | Quit the round (asks first) / go back in menus |
| Mouse or arrows + Enter | Navigate menus |
| M | Mute (menus) |

The window can be resized; the game scales to fit.

## The two worlds

- **Prehistoric Valley (all Classic difficulties):** dusk sky, erupting volcano with a lava fountain and smoke, drifting clouds,
  pterodactyls, a walking brontosaurus, fireflies, fossil bones, palms; charred meteors with lava cracks and flame
  tails; a log cannon; landed meteors leave scorch marks.
- **Lunar Defence Base (Time Attack):** nebula sky, ringed planet, moon, three-layer scrolling parallax stars,
  comet, shooting stars, blinking satellite and base lights, energy shield; tumbling rocky asteroids with
  crystal glints and dust trails; a laser turret.
- Custom 5×7 **bitmap pixel font**, animated menus with the cannon shooting real targets, screen shake and red
  flash on impacts, particle explosions that differ per target type, danger vignette on your last heart / last
  10 seconds, and **synthesized retro sound effects** (silently skipped if the computer has no audio).

## Classes (from the proposal) – `src/main/java/mathstrike`

| Class | Role |
|---|---|
| `Problem` | One question + answer: `getQuestion()`, `getAnswer()`, `isCorrect(int)` |
| `Difficulty` (enum) | Easy / Medium / Hard rules: range, operators, hearts, fall time |
| `ProblemGenerator` | `generate()`, `generateForLevel()`, `basic()`, `expression()` |
| `Target` (abstract) | Position, speed, problem; `move()`, `draw()`, `explode()`, `hasReachedGround()` |
| `Meteor`, `Asteroid` | Extend `Target`; different movement, drawing and explosion |
| `GameMode` (abstract) | `start()`, `onCorrect()`, `onMiss()`, `isGameOver()`, target factory |
| `ClassicMode`, `TimeAttackMode` | Extend `GameMode` (hearts vs. countdown timer + levels) |
| `Health` | Hearts: `loseHeart()`, `getHearts()`, `isDead()` |
| `ScoreManager` | Score, streak, multiplier, accuracy, answer records: `recordCorrect()`, `recordMiss()`, `addScore()`, `getMultiplier()`, `getAccuracy()` |
| `Game` | Game loop: `update()`, `spawnTarget()`, `checkAnswer()`, `endRound()` |

Presentation classes: `Theme` (abstract) → `PrehistoricTheme`, `SpaceTheme`; `Scene` (abstract) → `TitleScene`,
`ChoiceScene`, `TextScene`, `PlayScene`, `ResultsScene`; plus `GamePanel` (window + game loop), `MathStrike` (entry
point), `Button`, `Backdrop`, `Effects`, `Sprites`, `PixelFont`, `PixelArt`, `Sound`, `HighScores`.
Helpers: `InputValidator` + `InvalidAnswerException`, `AnswerRecord`, `GameEvent`.

### OOP concepts in the code

- **Encapsulation** – score, streak, hearts and answers are `private`, reached through getters/methods.
- **Inheritance** – `Meteor`/`Asteroid` extend `Target`; `ClassicMode`/`TimeAttackMode` extend `GameMode`; `PrehistoricTheme`/`SpaceTheme` extend `Theme`; every screen extends `Scene`.
- **Polymorphism** – `Game` handles every target as a `Target` and every mode as a `GameMode`; each subclass overrides `move()`, `draw()`, `explode()`, `onCorrect()`, `onMiss()`, `isGameOver()`, `paintAnimated()`, `drawTurret()`.
- **Abstraction** – `Target`, `GameMode`, `Theme` and `Scene` are abstract; `Difficulty` hides its rules behind getters.
- **Exception handling** – `InputValidator.parseAnswer()` throws `InvalidAnswerException`; `Game.submitAnswer()` catches it so the game never crashes and the player pays no penalty. Sound and saving best scores fail safely.
- **Arrays / collections** – `ArrayList<Target>` for on-screen targets, `char[]` operator arrays per difficulty, `AnswerRecord[]` for the end-of-round report.

UML class diagram: `docs/class-diagram.png` (editable source: `docs/class-diagram.mmd`).

## Rules as built

- **Classic:** Easy 1–10 (+ −, 3 hearts) · Medium 1–50 (+ − ×, 4 hearts) · Hard 1–100 (+ − × ÷ and order of operations, 5 hearts). A wrong answer or a landed meteor costs 1 heart; 0 hearts = game over. The game speeds up slowly as you score.
- **Time Attack:** 60 s start, +4 s per correct answer (max 99 s), level up every 3 correct answers (more operations, bigger numbers, faster asteroids). Time hits 0 = round over.
- **Scoring:** 100 points + up to 50 speed bonus, × streak multiplier (x2 at 3 in a row, x3 at 6, x4 at 9, x5 at 12+). Any mistake resets the streak.
- **Results screen:** final score, correct answers, accuracy %, best streak, congratulation text, scrollable review of every problem in the round, and your personal best.
- Only positive whole numbers; division always has a whole-number answer. No accounts, multiplayer, pause/save, or voice input (per the proposal's limitations).

### Small design decisions the proposal left open

- For × and ÷ one factor is capped at 12 (so Hard doesn't ask for 87 × 94 in your head); + and − use the full range.
- In Time Attack a wrong answer or a landed asteroid costs 2 seconds (otherwise misses would be free).
- A 3-2-1 countdown starts each round; a wrong answer blasts your base (boom effect) and the target keeps falling.
- Your best score per mode/difficulty is stored on this computer with `java.util.prefs`.

## Project layout

```
src/main/java/mathstrike/     the whole game
src/test/java/mathstrike/     SelfTest (rules) and UiSmoke (clicks through every screen headlessly)
build.sh / build.bat          build scripts
```
