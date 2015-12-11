package asteroids.participants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

import asteroids.Constants;
import asteroids.Participant;
import asteroids.ParticipantCountdownTimer;
import asteroids.destroyers.ShipDestroyer;

public class BossBullet extends Participant implements ShipDestroyer {

	private Shape outline;

	public BossBullet(double x, double y, double direction) {
		setVelocity(15, direction);
		setDirection(direction);

		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(x, y);
		poly.lineTo(x - 1, y);
		poly.lineTo(x - 1, y + 1);
		poly.lineTo(x, y + 1);
		poly.lineTo(x, y);
		poly.closePath();
		outline = poly;
		
		new ParticipantCountdownTimer(this, "expire", Constants.BULLET_DURATION+500);
	}

	@Override
	protected Shape getOutline() {
		return outline;
	}

	@Override
	public void collidedWith(Participant p) {
		if(!(p instanceof BossAlien)){
		expire(this);
		}
	}
	
	@Override
	public void countdownComplete(Object payload){
		if(payload.equals("expire")){
			expire(this);
		}
	}

	@Override
	public void draw(Graphics2D g){
		g.setColor(Color.YELLOW);
		super.draw(g);
		g.setColor(Color.WHITE);
	}
}
