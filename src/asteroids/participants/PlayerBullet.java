package asteroids.participants;

import asteroids.Controller;
import asteroids.Participant;
import asteroids.destroyers.AlienDestroyer;
import asteroids.destroyers.BossDestroyer;

/**
 * Bullets shot by player ship
 * 
 * @author Gabriel Kerr, and Jasper Slaff
 */
public class PlayerBullet extends Bullet implements AlienDestroyer, BossDestroyer{

	public PlayerBullet(double x, double y, double direction, Controller controller) {
		super(x, y, direction, controller);
	}

	@Override
	public void collidedWith(Participant p) {
		if (p instanceof Asteroid || p instanceof Alien || p instanceof BossAlien) {
			expire(this);
		}
	}

}
