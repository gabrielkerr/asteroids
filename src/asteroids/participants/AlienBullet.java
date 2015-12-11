package asteroids.participants;

import asteroids.Participant;
import asteroids.ParticipantCountdownTimer;
import asteroids.destroyers.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

import asteroids.Constants;
import asteroids.Controller;

/**
 * Bullet fired by the alien
 * 
 * @author Gabriel Kerr and Jasper Slaff
 */
public class AlienBullet extends Participant implements ShipDestroyer, AsteroidDestroyer {

	// bullet shape
	private Shape outline;
	// used to set color
	private Controller controller;

	public AlienBullet(double x, double y, double direction, Controller controller) {
		setDirection(direction);
		setVelocity(Constants.BULLET_SPEED, direction);
		this.controller = controller;

		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(x, y);
		poly.lineTo(x - 1, y);
		poly.lineTo(x - 1, y + 1);
		poly.lineTo(x, y + 1);
		poly.lineTo(x, y);
		poly.closePath();
		outline = poly;

		new ParticipantCountdownTimer(this, "expire", Constants.BULLET_DURATION);
	}

	/**
	 * Shape of bullet
	 */
	@Override
	protected Shape getOutline() {
		return outline;
	}

	/**
	 * expire bullet when it collides with ships and asteroids
	 */
	@Override
	public void collidedWith(Participant p) {
		if (p instanceof Asteroid || p instanceof Ship) {
			Participant.expire(this);
		}
	}

	/**
	 * Expires after bullet has traveled so far
	 */
	@Override
	public void countdownComplete(Object payload) {
		if (payload.equals("expire")) {
			expire(this);
		}
	}

	/**
	 * changes color if enhanced
	 */
	@Override
	public void draw(Graphics2D g) {
		if (controller.getEnhanced()) {
			g.setColor(Color.YELLOW);
			super.draw(g);
			g.setColor(Color.WHITE);
		} else {
			super.draw(g);
		}
	}
}
