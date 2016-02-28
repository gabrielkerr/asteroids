package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;

import asteroids.Constants;
import asteroids.Controller;
import asteroids.Participant;
import asteroids.ParticipantCountdownTimer;
import asteroids.destroyers.AlienDestroyer;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.ShipDestroyer;

/**
 * Represents missile. Keeps track of how many missiles are active. Missile
 * simulates realistic missile by accelerating gradually over time.
 */
public class Missile extends Participant implements AlienDestroyer, AsteroidDestroyer {
	// The outline of the missile
	private Shape outline;

	// Count of Missiles
	private static int missileCount;

	// Controller of the game
	private Controller controller;

	// Restricts the velocity of missile
	private int velocityBrake;

	// Determines whether or not the flame is on
	private boolean flameOn = false;

	/**
	 * Draws one missile. Sets it at constant speed in direction of parameter.
	 * Starts timer to expire Bullet. Increments missileCount. Starts timer for
	 * gradual acceleration.
	 */
	public Missile(double x, double y, double direction, Controller controller) {
		this.controller = controller;
		this.setPosition(x, y);
		this.setRotation(direction);
		velocityBrake = Constants.BULLET_SPEED;
		this.setVelocity(Constants.BULLET_SPEED / velocityBrake, direction);

		drawMissile();

		missileCount++;

		new ParticipantCountdownTimer(this, "expire", 10000);
		new ParticipantCountdownTimer(this, "accelerate", 75);
	}

	/**
	 * Returns the number of missiles.
	 */
	public static int getMissileCount() {
		return missileCount;
	}

	/**
	 * Sets the number of missiles.
	 */
	public static void setMissileCount(int n) {
		missileCount = n;
	}

	/**
	 * Calibrates the direction and rotation of the missile to the alienShip.
	 * Adjusts course of the missile by PI/24.
	 */
	public void calibrateMissile(Alien alienShip) {
		// Finds the distance to the alienShip from this missile.
		// double direction = alienShip.findDirectionFrom(this.getX(),
		// this.getY());
		double direction = Math.atan2(alienShip.getY() - this.getY(), alienShip.getX() - this.getX());
		// chooses which way the missile should turn
		double delta = chooseTheta(direction, this.getDirection());
		// If delta is positive adjusts course by adding some rad.
		if (delta > Math.PI / 6) {
			setDirection(getDirection() - Math.PI / 16);
		}
		// If delta is negative adjusts course by subtracting some rad.
		else if (delta < -Math.PI / 6) {
			setDirection(getDirection() + Math.PI / 16);
		}
		// Hones directly into the missile if delta is small enough.
		else {
			setDirection(direction);
		}
		setRotation(getDirection());
	}

	/**
	 * Returns the normalized shortest angle in radians between vector
	 * direction1 and direction2.
	 */
	private double chooseTheta(double direction1, double direction2) {
		direction1 = normalize(direction1);
		direction2 = normalize(direction2);
		double difference1 = normalize(direction1 - direction2);
		double difference2 = normalize(direction2 - direction1);
		if (Math.abs(difference1) < Math.abs(difference2))
			return difference1;
		else
			return difference2;
	}

	@Override
	protected Shape getOutline() {
		return outline;
	}

	/**
	 * Expires missiles when destroyed.
	 */
	@Override
	public void collidedWith(Participant p) {
		if (p instanceof ShipDestroyer) {
			Participant.expire(this);
			missileCount--;
			// Notify controller of destroyed missile
			// controller.missileDestroyed();
		}
	}

	/**
	 * Expires missile after time is reached.
	 */
	@Override
	public void countdownComplete(Object payload) {
		if (payload.equals("expire")) {
			Participant.expire(this);
			missileCount--;
			// notify controller of destroyed missile
			// controller.missileDestroyed();
		}
		if (payload.equals("accelerate")) {
			if (velocityBrake > 1) {
				velocityBrake--;
			}
			setVelocity(Constants.BULLET_SPEED / velocityBrake, getDirection());
			// setVelocity(Constants.BULLET_SPEED, getDirection());
			calibrateMissile(controller.getAlien());
			// turn missile flame on and off
			if (flameOn) {
				turnMissileFlameOn();
				flameOn = false;
			} else {
				turnMissileFlameOff();
				flameOn = true;
			}
			new ParticipantCountdownTimer(this, "accelerate", 50);

		}
	}

	/**
	 * Creates outline of the missile.
	 */
	private void drawMissile() {
		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(10, 0);
		poly.lineTo(-10, 5);
		poly.lineTo(-6, 5);
		poly.lineTo(-6, -5);
		poly.lineTo(-10, -5);
		poly.closePath();
		outline = poly;
	}

	/**
	 * Creates outline of the missile with flame.
	 */
	private void drawFlameMissile() {
		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(10, 0);
		poly.lineTo(-10, 5);
		poly.lineTo(-6, 5);
		poly.lineTo(-6, -5);
		poly.lineTo(-10, -5);
		poly.closePath();
		poly.moveTo(-6, 2);
		poly.lineTo(-20, 0);
		poly.lineTo(-6, -2);
		poly.closePath();
		outline = poly;
	}

	/**
	 * Turns the thruster flame of missile on.
	 */
	public void turnMissileFlameOn() {
		drawFlameMissile();
	}

	/**
	 * Turns the thruster flame of missile off.
	 */
	public void turnMissileFlameOff() {
		drawMissile();
	}
}