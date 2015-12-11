package asteroids.participants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

import asteroids.Participant;
import asteroids.destroyers.*;
import asteroids.Constants;
import asteroids.Controller;

/**
 * 
 * @author Gabriel Kerr, and Jasper Slaff
 */
public class Bullet extends Participant implements AsteroidDestroyer {
	// bullet shape
	private Shape outline;
	// used to set color
	private Controller controller;

	public Bullet(double x, double y, double direction, Controller controller) {
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
	}

	@Override
	protected Shape getOutline() {
		return outline;
	}

	@Override
	public void collidedWith(Participant p) {
	}

	@Override
	public void countdownComplete(Object payload) {
		if (payload.equals("furthestDistance")) {
			expire(this);
		}
	}

	/**
	 * color changes when enhanced
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
