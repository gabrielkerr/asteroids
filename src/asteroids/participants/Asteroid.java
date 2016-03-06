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
import asteroids.Sounds;
import asteroids.destroyers.AlienDestroyer;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.ShipDestroyer;
import static asteroids.Constants.*;

/**
 * Represents asteroids
 */
public class Asteroid extends Participant implements ShipDestroyer, Sounds, AlienDestroyer {
	// The size of the asteroid (0 = small, 1 = medium, 2 = large)
	private int size;

	// The outline of the asteroid
	private Shape outline;

	// The game controller
	private Controller controller;

	// Audio clips
	private Clip smallCrashClip;
	private Clip medCrashClip;
	private Clip bigCrashClip;

	/**
	 * Throws an IllegalArgumentException if size or variety is out of range.
	 * 
	 * Creates an asteroid of the specified variety (0 through 3) and size (0 =
	 * small, 1 = medium, 2 = large) and positions it at the provided
	 * coordinates with a random rotation. Its velocity has the given speed but
	 * is in a random direction.
	 */
	public Asteroid(int variety, int size, double x, double y, int speed, Controller controller) {
		// Make sure size and variety are valid
		if (size < 0 || size > 2) {
			throw new IllegalArgumentException("Invalid asteroid size: " + size);
		} else if (variety < 0 || variety > 3) {
			throw new IllegalArgumentException();
		}

		// Create the asteroid
		this.controller = controller;
		this.size = size;
		setPosition(x, y);
		setVelocity(speed, RANDOM.nextDouble() * 2 * Math.PI);
		setRotation(2 * Math.PI * RANDOM.nextDouble());
		createAsteroidOutline(variety, size);

		smallCrashClip = createClip("/sounds/bangSmall.wav");
		medCrashClip = createClip("/sounds/bangMedium.wav");
		bigCrashClip = createClip("/sounds/bangLarge.wav");
	}

	@Override
	protected Shape getOutline() {
		return outline;
	}

	/**
	 * Creates the outline of the asteroid based on its variety and size.
	 */
	private void createAsteroidOutline(int variety, int size) {
		// This will contain the outline
		Path2D.Double poly = new Path2D.Double();

		// Fill out according to variety
		if (variety == 0) {
			poly.moveTo(0, -30);
			poly.lineTo(28, -15);
			poly.lineTo(20, 20);
			poly.lineTo(4, 8);
			poly.lineTo(-1, 30);
			poly.lineTo(-12, 15);
			poly.lineTo(-5, 2);
			poly.lineTo(-25, 7);
			poly.lineTo(-10, -25);
			poly.closePath();
		} else if (variety == 1) {
			poly.moveTo(10, -28);
			poly.lineTo(7, -16);
			poly.lineTo(30, -9);
			poly.lineTo(30, 9);
			poly.lineTo(10, 13);
			poly.lineTo(5, 30);
			poly.lineTo(-8, 28);
			poly.lineTo(-6, 6);
			poly.lineTo(-27, 12);
			poly.lineTo(-30, -11);
			poly.lineTo(-6, -15);
			poly.lineTo(-6, -28);
			poly.closePath();
		} else if (variety == 2) {
			poly.moveTo(10, -30);
			poly.lineTo(30, 0);
			poly.lineTo(15, 30);
			poly.lineTo(0, 15);
			poly.lineTo(-15, 30);
			poly.lineTo(-30, 0);
			poly.lineTo(-10, -30);
			poly.closePath();
		} else {
			poly.moveTo(30, -18);
			poly.lineTo(5, 5);
			poly.lineTo(30, 15);
			poly.lineTo(15, 30);
			poly.lineTo(0, 25);
			poly.lineTo(-15, 30);
			poly.lineTo(-25, 8);
			poly.lineTo(-10, -25);
			poly.lineTo(0, -30);
			poly.lineTo(10, -30);
			poly.closePath();
		}

		// Scale to the desired size
		double scale = ASTEROID_SCALE[size];
		poly.transform(AffineTransform.getScaleInstance(scale, scale));

		// Save the outline
		outline = poly;
	}

	/**
	 * Returns the size of the asteroid
	 */
	public int getSize() {
		return size;
	}

	/**
	 * When an Asteroid collides with an AsteroidDestroyer, it expires.
	 */
	@Override
	public void collidedWith(Participant p) {
		if (p instanceof AsteroidDestroyer) {
			// Expire the asteroid
			Participant.expire(this);

			if (size == ASTEROID_SCALE[2] && bigCrashClip != null) {
				if (bigCrashClip.isRunning()) {
					bigCrashClip.stop();
				}
				bigCrashClip.setFramePosition(0);
				bigCrashClip.start();
				if (p instanceof PlayerBullet || p instanceof Ship) {
					controller.addScore(ASTEROID_SCORE[2]);
				}
			} else if (size == ASTEROID_SCALE[1] && medCrashClip != null) {
				if (medCrashClip.isRunning()) {
					medCrashClip.stop();
				}
				medCrashClip.setFramePosition(0);
				medCrashClip.start();
				if (p instanceof PlayerBullet || p instanceof Ship) {
					controller.addScore(ASTEROID_SCORE[1]);
				}
			} else if (size < ASTEROID_SCALE[1] && smallCrashClip != null) {
				if (smallCrashClip.isRunning()) {
					smallCrashClip.stop();
				}
				smallCrashClip.setFramePosition(0);
				smallCrashClip.start();
				if (p instanceof PlayerBullet || p instanceof Ship) {
					controller.addScore(ASTEROID_SCORE[0]);
				}
			}

			// Create two smaller asteroids. Put them at the same position
			// as the one that was just destroyed and give them a random
			// direction.
			int size = getSize() - 1;
			if (size >= 0) {
				if (size == 1) {
					double r = RANDOM.nextDouble() * 2;
					int speed;
					if (r < 1) {
						speed = 3;
					} else {
						speed = 4;
					}
					controller.addParticipant(new Asteroid(RANDOM.nextInt(4), size, getX(), getY(), speed, controller));
					controller.addParticipant(new Asteroid(RANDOM.nextInt(4), size, getX(), getY(), speed, controller));
				} else if (size == 0) {
					double r = RANDOM.nextDouble() * 3;
					int speed;
					if (r < 1) {
						speed = 3;
					} else if (r >= 1 && r < 2) {
						speed = 4;
					} else {
						speed = 5;
					}
					controller.addParticipant(new Asteroid(RANDOM.nextInt(4), size, getX(), getY(), speed, controller));
					controller.addParticipant(new Asteroid(RANDOM.nextInt(4), size, getX(), getY(), speed, controller));
				}
			}

			// Inform the controller
			controller.asteroidDestroyed(size + 1);

			// Create debris
			Dust d1 = new Dust(getX(), getY(), controller);
			Dust d2 = new Dust(getX(), getY(), controller);
			Dust d3 = new Dust(getX(), getY(), controller);
			Dust d4 = new Dust(getX(), getY(), controller);
			controller.addParticipant(d1);
			controller.addParticipant(d2);
			controller.addParticipant(d3);
			controller.addParticipant(d4);
		}
	}

	/**
	 * changes color if game is enhanced
	 */
	@Override
	public void draw(Graphics2D g) {
		if (controller.getEnhanced()) {
			g.setColor(Color.PINK);
			super.draw(g);
			g.setColor(Color.WHITE);
		} else {
			super.draw(g);
		}
	}

	/**
	 * creates sound clip
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
