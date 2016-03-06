package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;

import asteroids.Participant;
import asteroids.ParticipantCountdownTimer;
import asteroids.Constants;

/**
 * Debris of ships
 * 
 * @author Gabriel Kerr, Jasper Slaff
 */
public class Debris extends Participant {
	// outline of debris
	private Shape outline;

	public Debris(double x, double y) {
		setSpeed(Constants.RANDOM.nextInt(5));
		setDirection(Constants.RANDOM.nextInt(360));

		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(x, y);
		poly.lineTo(x + Constants.RANDOM.nextInt(20), y + Constants.RANDOM.nextInt(20));
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
}
