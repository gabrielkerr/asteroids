package asteroids.participants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

import asteroids.Constants;
import asteroids.Controller;
import asteroids.Participant;
import asteroids.ParticipantCountdownTimer;

/**
 * Dust when asteroids are destroyed
 * 
 * @author Gabriel Kerr, and Jasper Slaff
 */
public class Dust extends Participant {

	private Shape outline;

	private Controller controller;

	public Dust(double x, double y, Controller controller) {
		this.controller = controller;
		setSpeed(Constants.RANDOM.nextInt(5));
		setDirection(Constants.RANDOM.nextInt(360));

		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(x + 3, y);
		poly.lineTo(x + 3, y + 1);
		poly.lineTo(x + 2, y + 1);
		poly.lineTo(x + 2, y);
		poly.lineTo(x + 3, y);
		poly.closePath();
		outline = poly;

		new ParticipantCountdownTimer(this, "floatAway", Constants.RANDOM.nextInt(2000));
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
		if (payload.equals("floatAway")) {
			expire(this);
		}
	}

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
}
