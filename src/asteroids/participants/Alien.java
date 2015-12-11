package asteroids.participants;

import static asteroids.Constants.RANDOM;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import asteroids.Constants;
import asteroids.Controller;
import asteroids.Participant;
import asteroids.ParticipantCountdownTimer;
import asteroids.Sounds;
import asteroids.destroyers.AlienDestroyer;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.ShipDestroyer;

/**
 * Alien that is either small or big and shoots either at the player or randomly
 * respectively
 * 
 * @author Gabriel Kerr, and Jasper Slaff
 */
public class Alien extends Participant implements AsteroidDestroyer, ShipDestroyer, Sounds {

	// The outline of the alien ship
	private Shape outline;

	// Game controller
	private Controller controller;

	// Size of the alien (1 for small, 2 for medium)
	private int size;

	// decides if it is moving left or right
	private boolean left;

	// Alien sound clips
	private Clip fireClip;
	private Clip crashClip;
	private Clip bigAlienClip;
	private Clip smallAlienClip;

	// Whether or not it is time for the alien to move
	private boolean timeToMove = true;

	// Whether or not it is time for the alien to fire a bullet
	private boolean timeToFire = true;
	
	//Timers
	ParticipantCountdownTimer move;
	ParticipantCountdownTimer fire;

	public Alien(int size, double x, double y, int speed, Controller controller) {
		// Initialize the alien
		this.controller = controller;
		setPosition(x, y);
		// decide if it is going left or right
		if (RANDOM.nextInt(2) == 1) {
			left = false;
		} else {
			left = true;
		}
		// set the speed and direction of ship
		if (size == 2 && left) {
			setVelocity(-speed, 0);
		} else if (size == 2 && !left) {
			setVelocity(speed, 0);
		} else if (size == 1 && left) {
			setVelocity(-speed * 1.5, 0);
		} else if (size == 1 && !left) {
			setVelocity(speed * 1.5, 0);
		}
		this.size = size;

		if (size == 1) {
			// Create the shape of the alien
			Path2D.Double poly = new Path2D.Double();
			poly.moveTo(0, 0);
			poly.lineTo(15, 0);
			poly.lineTo(20, -5);
			poly.lineTo(-5, -5);
			poly.closePath();
			poly.moveTo(20, -5);
			poly.lineTo(15, -10);
			poly.lineTo(0, -10);
			poly.lineTo(-5, -5);
			poly.moveTo(15, -10);
			poly.lineTo(12, -13);
			poly.lineTo(3, -13);
			poly.lineTo(0, -10);
			outline = poly;
		} else if (size == 2) {
			Path2D.Double poly = new Path2D.Double();
			poly.moveTo(0, 0);
			poly.lineTo(30, 0);
			poly.lineTo(40, -10);
			poly.lineTo(-10, -10);
			poly.closePath();
			poly.moveTo(40, -10);
			poly.lineTo(30, -20);
			poly.lineTo(0, -20);
			poly.lineTo(-10, -10);
			poly.moveTo(30, -20);
			poly.lineTo(24, -26);
			poly.lineTo(6, -26);
			poly.lineTo(0, -20);
			outline = poly;
		}

		// initialize sounds
		fireClip = createClip("/sounds/fire.wav");
		crashClip = createClip("/sounds/bangAlienShip.wav");
		bigAlienClip = createClip("/sounds/saucerBig.wav");
		smallAlienClip = createClip("/sounds/saucerSmall.wav");

		// set initial timer for when alien moves and shoots
		move = new ParticipantCountdownTimer(this, "move", Constants.ALIEN_DELAY + 750);
		fire = new ParticipantCountdownTimer(this, "fire", Constants.ALIEN_DELAY + 750);
	}

	/**
	 * Returns the x-coordinate of the point on the screen where the ship's nose
	 * is located.
	 */
	public double getXCenter() {
		if (this.size == 1) {
			Point2D.Double point = new Point2D.Double(7, 0);
			transformPoint(point);
			return point.getX();
		} else if (this.size == 2) {
			Point2D.Double point = new Point2D.Double(15, 0);
			transformPoint(point);
			return point.getX();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the y-coordinate of the point on the screen where the ship's nose
	 * is located.
	 */
	public double getYCenter() {
		if (this.size == 1) {
			Point2D.Double point = new Point2D.Double(0, 5);
			transformPoint(point);
			return point.getY();
		} else if (this.size == 2) {
			Point2D.Double point = new Point2D.Double(0, 10);
			transformPoint(point);
			return point.getY();
		} else {
			return 0;
		}
	}

	/**
	 * Fires a bullet in a random direction or at the ship depending on the
	 * alien size.
	 */
	public void fire() {
		if (this != null) {
			if (this.size == 2) {
				AlienBullet b = new AlienBullet(getXCenter(), getYCenter(), RANDOM.nextDouble() * Math.PI * 2,
						controller);
				controller.addParticipant(b);
				if (fireClip != null) {
					if (fireClip.isRunning()) {
						fireClip.stop();
					}
					fireClip.setFramePosition(0);
					fireClip.start();
				}
			} else if (size == 1 && controller.getShip() != null) {
				double targetX = controller.getShip().getX() - this.getX();
				double targetY = controller.getShip().getY() - this.getY();
				AlienBullet b = new AlienBullet(getXCenter(), getYCenter(), Math.atan2(targetY, targetX), controller);
				controller.addParticipant(b);
				if (fireClip != null) {
					if (fireClip.isRunning()) {
						fireClip.stop();
					}
					fireClip.setFramePosition(0);
					fireClip.start();
				}
			}
		}
	}

	/**
	 * returns the shape of the alien
	 */
	@Override
	protected Shape getOutline() {
		return outline;
	}

	/**
	 * what happens when it collides with another object in the game
	 */
	@Override
	public void collidedWith(Participant p) {
		if (p instanceof AlienDestroyer) {
			if (crashClip != null) {
				if (crashClip.isActive()) {
					crashClip.stop();
				}
				crashClip.setFramePosition(0);
				crashClip.start();
			}
			if (smallAlienClip != null && smallAlienClip.isActive()) {
				smallAlienClip.stop();
			}
			if (bigAlienClip != null && bigAlienClip.isActive()) {
				bigAlienClip.stop();
			}
			Participant.expire(this);
			controller.alienDestroyed();
			if (p instanceof PlayerBullet) {
				if (size == 1) {
					controller.addScore(Constants.ALIENSHIP_SCORE[0]);
				} else {
					controller.addScore(Constants.ALIENSHIP_SCORE[1]);
				}
			}
			Debris l1 = new AlienDebris(getX(), getY(), controller);
			Debris l2 = new AlienDebris(getX() + 2, getY() + 2, controller);
			Debris l3 = new AlienDebris(getX() - 1, getY() + 3, controller);
			Debris l4 = new AlienDebris(getX() - 2, getY() + 4, controller);
			Debris l5 = new AlienDebris(getX() + 1, getY() + 1, controller);
			controller.addParticipant(l1);
			controller.addParticipant(l2);
			controller.addParticipant(l3);
			controller.addParticipant(l4);
			controller.addParticipant(l5);
		}

	}

	/**
	 * stops the alien from making a sound when the game either ends and a new
	 * one begins or a new level begins
	 */
	public void stopSound() {
		if (size == 1 && smallAlienClip != null && smallAlienClip.isActive()) {
			smallAlienClip.stop();
		}
		if (size == 2 && bigAlienClip != null && bigAlienClip.isActive()) {
			bigAlienClip.stop();
		}
	}

	/**
	 * This method is invoked when a ParticipantCountdownTimer completes its
	 * count down.
	 */
	@Override
	public void countdownComplete(Object payload) {
		if (size == 1 && smallAlienClip != null) {
			smallAlienClip.loop(20);
		} else if (size == 2 && bigAlienClip != null) {
			bigAlienClip.loop(10);
		}
		if (payload.equals("move")) {
			if (timeToMove && !left) {
				timeToMove = false;
				double x = RANDOM.nextDouble() * 4;
				if (x > 1 && x < 2) {
					if (this.getSpeed() < 0) {
						setVelocity(5, 3 * Math.PI / 4);
					} else {
						setVelocity(5, Math.PI / 4);
					}
				} else if (x >= 2) {
					if (this.getSpeed() < 0) {
						setVelocity(5, -3 * Math.PI / 4);
					} else {
						setVelocity(5, -Math.PI / 4);
					}
				} else {
					if (this.getSpeed() < 0) {
						setVelocity(-this.getSpeed(), 0);
					} else {
						setVelocity(this.getSpeed(), 0);
					}
				}
				move = new ParticipantCountdownTimer(this, "move", 750);
			} else if (timeToMove && left) {
				timeToMove = false;
				double x = RANDOM.nextDouble() * 4;
				if (x > 1 && x < 2) {
					if (this.getSpeed() < 0) {
						setVelocity(-5, 3 * Math.PI / 4);
					} else {
						setVelocity(-5, Math.PI / 4);
					}
				} else if (x >= 2) {
					if (this.getSpeed() < 0) {
						setVelocity(-5, -3 * Math.PI / 4);
					} else {
						setVelocity(-5, -Math.PI / 4);
					}
				} else {
					if (this.getSpeed() < 0) {
						setVelocity(this.getSpeed(), 0);
					} else {
						setVelocity(-this.getSpeed(), 0);
					}
				}
				move = new ParticipantCountdownTimer(this, "move", 750);
			} else if (!timeToMove) {
				timeToMove = true;
				move = new ParticipantCountdownTimer(this, "move", 750);
			}
		}

		if (payload.equals("fire") && this != null) {
			if (timeToFire) {
				timeToFire = false;
				fire = new ParticipantCountdownTimer(this, "fire", 750);
				fire();
			} else if (!timeToFire && this != null) {
				timeToFire = true;
				fire = new ParticipantCountdownTimer(this, "fire", 750);
			}
		}
		if (payload.equals("spawn") && this != null) {
			controller.addParticipant(this);
		}
	}

	/**
	 * draws a colorful ship if enhanced version
	 * @param g
	 */
	@Override
	public void draw(Graphics2D g) {
		if (controller.getEnhanced()) {
			g.setColor(Color.GREEN);
			super.draw(g);
			g.setColor(Color.WHITE);
		} else {
			super.draw(g);
		}
	}

	/**
	 * Creates sound clips
	 */
	@Override
	public Clip createClip(String soundFile) {
		// Opening the sound file this way will work no matter how the
		// project is exported. The only restriction is that the
		// sound files must be stored in a package.
		try (BufferedInputStream sound = new BufferedInputStream(getClass().getResourceAsStream(soundFile))) {
			// Create and return a Clip that will play a sound file. There are
			// various reasons that the creation attempt could fail. If it
			// fails, return null.
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(sound));
			return clip;
		} catch (LineUnavailableException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (UnsupportedAudioFileException e) {
			return null;
		}
	}

}
