package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;
import asteroids.Participant;

/**
 * Turns on the shield of the ship
 * 
 * @author Gabriel Kerr, and Jasper Slaff
 */
public class Shield extends Participant {
	// The outline of the shield
	private Shape outline;

	public Shield(double x, double y) {
		setPosition(x, y);

		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(x, y);
		poly.lineTo(x - 10, y - 5);
		poly.lineTo(x - 10, y + 15);
		poly.lineTo(x, y + 20);
		poly.lineTo(x + 10, y + 15);
		poly.lineTo(x + 10, y - 5);
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
		if (p instanceof Ship) {
			((Ship) p).shieldUp();
			expire(this);
		}
	}
}
