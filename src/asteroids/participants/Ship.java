package asteroids.participants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.*;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import asteroids.Controller;
import asteroids.Participant;
import asteroids.ParticipantCountdownTimer;
import asteroids.Sounds;
import asteroids.destroyers.*;
import static asteroids.Constants.*;

/**
 * Represents ships
 */
public class Ship extends Participant implements AsteroidDestroyer, AlienDestroyer, Sounds {
	// The outline of the ship
	private Shape outline;

	// Game controller
	private Controller controller;

	// Shield
	private boolean shield;
	
	// Ships initial invincibility lasts 2 seconds
	private boolean invincible;

	// Flame
	private boolean on = true;

	// Audio clips
	private Clip fireClip;
	private Clip thrustClip;
	private Clip crashClip;

	// Constructs a ship at the specified coordinates
	// that is pointed in the given direction.
	public Ship(int x, int y, double direction, Controller controller) {
		// Make invincible
		invincible = true;
		
		// initialize ship
		this.controller = controller;
		setPosition(x, y);
		setRotation(direction);

		// draw the ship
		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(20, 0);
		poly.lineTo(-20, 12);
		poly.lineTo(-13, 10);
		poly.lineTo(-13, -10);
		poly.lineTo(-20, -12);
		poly.closePath();
		outline = poly;

		// sound Clips
		fireClip = createClip("/sounds/fire.wav");
		thrustClip = createClip("/sounds/thrust.wav");
		crashClip = createClip("/sounds/bangShip.wav");

		// initialize shield
		shield = false;
		
		// invincibility timer that lets you be invincible until it goes off
		new ParticipantCountdownTimer(this, "invincible", 1000);
	}

	/**
	 * fires player bullets in the direction the ship is pointed
	 */
	public void fire() {
		Bullet b = new PlayerBullet(getXNose(), getYNose(), getRotation(), controller);
		new ParticipantCountdownTimer(b, "furthestDistance", BULLET_DURATION);
		controller.addParticipant(b);
		if (fireClip != null) {
			if (fireClip.isRunning()) {
				fireClip.stop();
			}
			fireClip.setFramePosition(0);
			fireClip.start();
		}
	}
	
	/**
	 * fires a missile
	 */
	public void fireMissile(){
		//HomingMissile h = new HomingMissile(controller.getAlien(), getXNose(), getYNose(), getDirection());
		//controller.addParticipant(h);
	}

	/**
	 * Returns the x-coordinate of the point on the screen where the ship's nose
	 * is located.
	 */
	public double getXNose() {
		Point2D.Double point = new Point2D.Double(20, 0);
		transformPoint(point);
		return point.getX();
	}

	/**
	 * Returns the x-coordinate of the point on the screen where the ship's nose
	 * is located.
	 */
	public double getYNose() {
		Point2D.Double point = new Point2D.Double(20, 0);
		transformPoint(point);
		return point.getY();
	}

	@Override
	protected Shape getOutline() {
		return outline;
	}

	/**
	 * Customizes the base move method by imposing friction
	 */
	@Override
	public void move() {
		applyFriction(SHIP_FRICTION);
		super.move();
	}

	/**
	 * Turns right by Pi/16 radians
	 */
	public void turnRight() {
		rotate(Math.PI / 16);
	}

	/**
	 * Turns left by Pi/16 radians
	 */
	public void turnLeft() {
		rotate(-Math.PI / 16);
	}

	/**
	 * Accelerates by SHIP_ACCELERATION
	 */
	public void accelerate() {
		accelerate(SHIP_ACCELERATION);
		if (thrustClip != null) {
			thrustClip.loop(10);
		}
		new ParticipantCountdownTimer(this, "flame", 10);
	}

	/**
	 * Draws original ship
	 */
	public void decelerate() {
		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(20, 0);
		poly.lineTo(-20, 12);
		poly.lineTo(-13, 10);
		poly.lineTo(-13, -10);
		poly.lineTo(-20, -12);
		poly.closePath();
		outline = poly;
		if (thrustClip != null) {
			thrustClip.stop();
		}
	}

	/**
	 * When a Ship collides with a ShipKiller, it expires
	 */
	@Override
	public void collidedWith(Participant p) {
		if (p instanceof ShipDestroyer && !invincible) {
			if (thrustClip != null && thrustClip.isActive() && !shield) {
				thrustClip.stop();
			}
			if (crashClip != null && !shield) {
				if (crashClip.isRunning()) {
					crashClip.stop();
				}
				crashClip.setFramePosition(0);
				crashClip.start();
			}

			if (!shield) {
				// Expire the ship from the game
				Participant.expire(this);

				// Tell the controller the ship was destroyed
				controller.shipDestroyed();

				// display debris
				Debris l1 = new Debris(getX(), getY());
				Debris l2 = new Debris(getX() + 2, getY() + 2);
				Debris l3 = new Debris(getX() - 1, getY() + 3);
				controller.addParticipant(l1);
				controller.addParticipant(l2);
				controller.addParticipant(l3);
			} else {
				shield = false;
			}
		}
	}

	/**
	 * sets player position to a random location on the map
	 */
	public void teleport() {
		setPosition(RANDOM.nextInt(SIZE), RANDOM.nextInt(SIZE));
	}

	/**
	 * turns the shield on
	 */
	public void shieldUp() {
		shield = true;
	}

	/**
	 * Returns status of shield
	 */
	public boolean getShield() {
		return shield;
	}

	/**
	 * draws a colorful ship if enhanced version
	 * 
	 * @param g
	 */
	@Override
	public void draw(Graphics2D g) {
		if (shield) {
			g.setColor(Color.CYAN);
			super.draw(g);
			g.setColor(Color.WHITE);
		} else {
			super.draw(g);
		}
	}

	/**
	 * causes flame to flicker
	 */
	@Override
	public void countdownComplete(Object payload) {
		if (payload.equals("flame") && on) {
			Path2D.Double poly = new Path2D.Double();
			poly.moveTo(20, 0);
			poly.lineTo(-20, 12);
			poly.lineTo(-13, 10);
			poly.lineTo(-13, -10);
			poly.lineTo(-20, -12);
			poly.closePath();
			poly.moveTo(-13, -10);
			poly.lineTo(-25, 0);
			poly.lineTo(-13, 10);
			outline = poly;
			on = false;
		} else if(payload.equals("flame") && !on){
			Path2D.Double poly = new Path2D.Double();
			poly.moveTo(20, 0);
			poly.lineTo(-20, 12);
			poly.lineTo(-13, 10);
			poly.lineTo(-13, -10);
			poly.lineTo(-20, -12);
			poly.closePath();
			outline = poly;
			on = true;
		}
		if(payload.equals("invincible")){
			invincible = false;
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
