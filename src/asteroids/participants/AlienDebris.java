package asteroids.participants;

import java.awt.Color;
import java.awt.Graphics2D;

import asteroids.Controller;

/**
 * Debris for the alien
 * @author Gabriel Kerr, and Jasper Slaff
 */
public class AlienDebris extends Debris {

	// used to set color
	private Controller controller;

	public AlienDebris(double x, double y, Controller controller) {
		super(x, y);
		this.controller = controller;
	}

	@Override
	public void draw(Graphics2D g) {
		if (controller.getEnhanced()) {
			g.setColor(Color.GREEN);
			super.draw(g);
			g.setColor(Color.WHITE);
		}
		else{
			super.draw(g);
		}
	}
}
