package asteroids;

import java.awt.event.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

import asteroids.participants.Alien;
import asteroids.participants.Asteroid;
import asteroids.participants.Beatbox;
import asteroids.participants.BossAlien;
import asteroids.participants.Shield;
import asteroids.participants.Ship;
import static asteroids.Constants.*;

/**
 * Controls a game of Asteroids.
 */
public class Controller implements KeyListener, ActionListener {
	// The state of all the Participants
	private ParticipantState pstate;

	// The ship (if one is active) or null (otherwise)
	private Ship ship;

	// Alien of the game
	private Alien alien;

	// Boss if enhanced
	private BossAlien boss;

	// Beat of the game
	private Beatbox beatbox;

	// If it is enhanced
	private boolean enhanced;

	// When this timer goes off, it is time to refresh the animation
	private Timer refreshTimer;

	// The time at which a transition to a new stage of the game should be made.
	// A transition is scheduled a few seconds in the future to give the user
	// time to see what has happened before doing something like going to a new
	// level or resetting the current level.
	private long transitionTime;

	// Number of lives left
	private int lives;

	// Score
	private long score;

	// Level
	private int level;

	// bestSoFar
	private long bestScoreSoFar;

	// keeps track of every 5000 points for extra lives in enhanced mode
	private long extraLifeInterval;

	// placed a shield this round;
	private boolean shieldRound;

	// The game display
	private Display display;

	// set containing all the keys that are currently pressed
	private HashSet<Integer> keys = new HashSet<Integer>();

	/**
	 * Constructs a controller to coordinate the game and screen
	 */
	public Controller(Boolean enhanced) {
		// Initialize the ParticipantState
		pstate = new ParticipantState();

		// Set up the refresh timer.
		refreshTimer = new Timer(FRAME_INTERVAL, this);

		// Clear the transitionTime
		transitionTime = Long.MAX_VALUE;

		// sets enhanced to game State
		this.enhanced = enhanced;

		// sets level to 1 so 4 asteroids are created on initial screen
		level = 1;

		// Record the display object
		display = new Display(this);

		// Bring up the splash screen and start the refresh timer
		splashScreen();
		display.setVisible(true);
		refreshTimer.start();
	}

	/**
	 * Returns enhanced state of this game.
	 */
	public boolean getEnhanced() {
		return enhanced;
	}

	/**
	 * Returns the ship, or null if there isn't one
	 */
	public Ship getShip() {
		return ship;
	}

	/**
	 * Returns the alien, or null if there isn't one
	 */
	public Alien getAlien() {
		return alien;
	}

	/**
	 * Returns the boss or null if there isn't one
	 */
	public BossAlien getBoss() {
		return boss;
	}

	/**
	 * Returns the number of lives
	 */
	public int getLives() {
		return lives;
	}

	/**
	 * Returns the score of the current game
	 */
	public long getScore() {
		return score;
	}

	/**
	 * adds points to the score of the game
	 */
	public void addScore(int points) {
		score += points;
	}

	/**
	 * Returns the level that player is on
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Configures the game screen to display the splash screen
	 */
	private void splashScreen() {
		// Clear the screen, reset the level, and display the legend
		clear();
		display.setLegend("Asteroids");

		// Place four asteroids near the corners of the screen.
		placeAsteroids();
	}

	/**
	 * The game is over. Displays a message to that effect.
	 */
	private void finalScreen() {
		display.setLegend(GAME_OVER);
		display.removeKeyListener(this);
	}

	/**
	 * Place a new ship in the center of the screen. Remove any existing ship
	 * first.
	 */
	private void placeShip() {
		boolean shield;
		if (ship != null) {
			shield = ship.getShield();
		} else {
			shield = false;
		}
		// Place a new ship
		Participant.expire(ship);
		ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
		addParticipant(ship);
		display.setLegend("");
		keys.removeAll(keys);
		if (shield && enhanced) {
			ship.shieldUp();
		}
	}

	/**
	 * Places four asteroids near the corners of the screen. Gives them random
	 * velocities and rotations.
	 */
	private void placeAsteroids() {
		Random r = new Random();
		resetPlacement();
		for (int i = 1; i <= getLevel() + 3; i++) {
			if (placement[0] == false) {
				addParticipant(new Asteroid(r.nextInt(3), 2, EDGE_OFFSET, EDGE_OFFSET, 3, this));
				placement[0] = true;
			} else if (placement[1] == false) {
				addParticipant(new Asteroid(r.nextInt(3), 2, SIZE - EDGE_OFFSET, EDGE_OFFSET, 3, this));
				placement[1] = true;
			} else if (placement[2] == false) {
				addParticipant(new Asteroid(r.nextInt(3), 2, EDGE_OFFSET, SIZE - EDGE_OFFSET, 3, this));
				placement[2] = true;
			} else if (placement[3] == false) {
				addParticipant(new Asteroid(r.nextInt(3), 2, SIZE - EDGE_OFFSET, SIZE - EDGE_OFFSET, 3, this));
				placement[3] = true;
				resetPlacement();
			}
		}
	}

	/**
	 * places a large alien at level 2 a small alien at level 3 and one of the
	 * two at level 4 and above with even probability
	 */
	private void placeAlien() {
		// places a medium alien on the board on level 2
		if (this.level == 2 && pstate.countAliens() == 0) {
			alien = new Alien(2, SIZE, RANDOM.nextDouble() * SIZE, 5, this);
			new ParticipantCountdownTimer(alien, "spawn", ALIEN_DELAY);
		}
		// places a small or medium alien on the board with equal probability
		// after level 3
		if (!enhanced || level % 5 != 0) {
			if (this.level >= 3 && pstate.countAliens() == 0) {
				int y = RANDOM.nextInt(2) + 1;
				alien = new Alien(y, SIZE, RANDOM.nextDouble() * SIZE, 5, this);
				new ParticipantCountdownTimer(alien, "spawn", ALIEN_DELAY);
			}
		} else if (level % 5 == 0 && enhanced) {
			boss = new BossAlien(75, -5, this);
			new ParticipantCountdownTimer(boss, "spawn", ALIEN_DELAY);
		}
	}

	/**
	 * an array that keeps track of where the last asteroid was placed
	 */
	private boolean[] placement = new boolean[4];

	/**
	 * resets where all the asteroids were placed
	 */
	private void resetPlacement() {
		for (int i = 0; i < placement.length; i++) {
			placement[i] = false;
		}
	}

	/**
	 * Clears the screen so that nothing is displayed
	 */
	private void clear() {
		pstate.clear();
		display.setLegend("");
		ship = null;
		resetPlacement();
	}

	/**
	 * Sets things up and begins a new game.
	 */
	private void initialScreen() {
		if (alien != null) {
			alien.stopSound();
		}
		// Clear the screen
		clear();

		// Reset statistics
		lives = 3;
		score = 0;
		level = 1;
		shieldRound = false;
		extraLifeInterval = 5000;

		// Place four asteroids
		if (enhanced && level % 5 != 0) {
			placeAsteroids();
		} else if (!enhanced || level % 5 != 0) {
			placeAsteroids();
		}

		// Place the ship
		placeShip();

		// Place alien
		if (level != 1) {
			placeAlien();
		}
		// Initialize beatbox
		if (beatbox != null) {
			Participant.expire(beatbox);
		}
		beatbox = new Beatbox();

		// Start listening to events (but don't listen twice)
		display.removeKeyListener(this);
		display.addKeyListener(this);

		// Give focus to the game screen
		display.requestFocusInWindow();
		// Refresh the display
		display.refresh();
	}

	/**
	 * Adds a new Participant
	 */
	public void addParticipant(Participant p) {
		pstate.addParticipant(p);
	}

	/**
	 * The ship has been destroyed
	 */
	public void shipDestroyed() {
		// Null out the ship
		ship = null;

		// Decrement lives
		lives--;

		// Since the ship was destroyed, schedule a transition
		scheduleTransition(END_DELAY);
	}

	/**
	 * The Boss has been destroyed
	 */
	public void bossDestroyed() {
		scheduleTransition(END_DELAY * 2);
	}

	/**
	 * The alien has been destroyed
	 */
	public void alienDestroyed() {
		placeAlien();
		scheduleTransition(END_DELAY);
	}

	/**
	 * An asteroid of the given size has been destroyed
	 */
	public void asteroidDestroyed(int size) {
		// If all the asteroids are gone, schedule a transition
		if (pstate.countAsteroids() == 0) {
			scheduleTransition(END_DELAY);
		}
	}

	/**
	 * Schedules a transition m msecs in the future
	 */
	private void scheduleTransition(int m) {
		transitionTime = System.currentTimeMillis() + m;
	}

	/**
	 * This method will be invoked because of button presses and timer events.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// The start button has been pressed. Stop whatever we're doing
		// and bring up the initial screen
		if (e.getSource() instanceof JButton) {
			initialScreen();
		}
		// Time to refresh the screen and deal with keyboard input
		else if (e.getSource() == refreshTimer) {
			// It may be time to make a game transition
			performTransition();

			// Move the participants to their new locations
			pstate.moveParticipants();
			// Check what keys are in the keys set
			// Perform certain actions according to what is in the set
			if ((keys.contains(KeyEvent.VK_UP) || keys.contains(KeyEvent.VK_W)) && ship != null) {
				ship.accelerate();
			} else if (!(keys.contains(KeyEvent.VK_UP) || keys.contains(KeyEvent.VK_W)) && ship != null) {
				ship.decelerate();
			}
			if ((keys.contains(KeyEvent.VK_LEFT) || keys.contains(KeyEvent.VK_A)) && ship != null) {
				ship.turnLeft();
			}
			if ((keys.contains(KeyEvent.VK_RIGHT) || keys.contains(KeyEvent.VK_D)) && ship != null) {
				ship.turnRight();
			}

			// Update the best score so far if enhanced version
			if (enhanced && score > bestScoreSoFar) {
				bestScoreSoFar = score;
			}

			// extra lives
			if (score >= extraLifeInterval && enhanced) {
				lives++;
				extraLifeInterval += 5000;
			}
			// places new shield as long as there is a ship on the board
			if (level > 1 && ship != null && !ship.getShield() && !shieldRound && enhanced) {
				if (level % 5 != 0) {
					Shield s = new Shield(RANDOM.nextInt(SIZE), RANDOM.nextInt(SIZE));
					addParticipant(s);
				} else {
					Shield s = new Shield(RANDOM.nextInt(SIZE), RANDOM.nextInt(SIZE));
					addParticipant(s);
				}
				shieldRound = true;
			}
			// Refresh screen
			display.refresh();
		}
	}

	/**
	 * Returns the best score so far in the current game
	 */
	public long getBestSoFar() {
		return bestScoreSoFar;
	}

	/**
	 * Returns an iterator over the active participants
	 */
	public Iterator<Participant> getParticipants() {
		return pstate.getParticipants();
	}

	/**
	 * If the transition time has been reached, transition to a new state
	 */
	private void performTransition() {
		// Do something only if the time has been reached
		if (transitionTime <= System.currentTimeMillis()) {
			// Clear the transition time
			transitionTime = Long.MAX_VALUE;
			// If there are no lives left, the game is over. Show the final
			// screen.
			if (lives <= 0) {
				finalScreen();
			}
			// If the ship was destroyed, place a new one and continue
			if (ship == null && !(lives <= 0)) {
				placeShip();
			}
			// If no asteroids are left proceed to the next level
			if (!enhanced || level % 5 != 0) {
				if (pstate.countAsteroids() == 0 && ship != null && pstate.countAliens() == 0) {
					nextLevel();
				}
			} else if (enhanced && level % 5 == 0 && boss.isExpired()) {
				nextLevel();
			}
		}
	}

	/**
	 * advance game to the next level
	 */
	private void nextLevel() {
		// stop sound from playing when game advances
		if (alien != null) {
			alien.stopSound();
		}
		// clear the board
		pstate.clear();
		// increase level
		level++;
		// set wether or not a shield has spawned yet to false
		shieldRound = false;
		// reset beat for the next level
		if (beatbox != null) {
			Participant.expire(beatbox);
		}
		beatbox = new Beatbox();
		// place all the new objects for the new level
		if (enhanced && level % 5 != 0) {
			//placeAsteroids();
		} else if (!enhanced) {
			//placeAsteroids();
		}
		placeShip();
		Participant.expire(alien);
		placeAlien();
	}

	/**
	 * If a key of interest is pressed, add it to the keys set and perform some
	 * actions.
	 */
	@Override
	public void keyPressed(KeyEvent e1) {
		keys.add(e1.getKeyCode());
		if ((e1.getKeyCode() == KeyEvent.VK_E || e1.getKeyCode() == KeyEvent.VK_SHIFT) && ship != null && enhanced) {
			ship.teleport();
		}
		if ((e1.getKeyCode() == KeyEvent.VK_S || e1.getKeyCode() == KeyEvent.VK_SPACE
				|| e1.getKeyCode() == KeyEvent.VK_DOWN) && ship != null && pstate.countBullets() <= BULLET_LIMIT) {
			ship.fire();
		}
		if ((e1.getKeyCode() == KeyEvent.VK_F) && ship != null && alien != null) {
			ship.fireMissile();
		}
		if ((e1.getKeyCode() == KeyEvent.VK_P)) {
			if (refreshTimer.isRunning()) {
				display.setLegend("PAUSED");
				display.refresh();
				refreshTimer.stop();
			} else {
				display.setLegend("");
				display.refresh();
				refreshTimer.start();
			}
		}
	}

	/**
	 * Ignore this event.
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * if a key is released remove it from the set
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		keys.remove(e.getKeyCode());
	}
}
