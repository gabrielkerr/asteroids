package asteroids.participants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import asteroids.Constants;
import asteroids.Controller;
import asteroids.Participant;
import asteroids.ParticipantCountdownTimer;
import asteroids.Sounds;
import asteroids.destroyers.BossDestroyer;
import asteroids.destroyers.ShipDestroyer;

public class BossAlien extends Participant implements ShipDestroyer, Sounds{
	//controller of the current game
	private Controller controller;
	//shape of the boss
	private Shape outline;
	//keeps track of when to fire
	private boolean timeToFire = true;
	// health of the boss alien
	private int health;
	// crash clip
	private Clip destroyClip;
	//timers
	ParticipantCountdownTimer fire;
	ParticipantCountdownTimer stop;

	public BossAlien(double x, double y, Controller controller) {
		setPosition(x, y);
		this.controller = controller;
		health = (controller.getLevel()+5)*10;
		
		Path2D.Double poly = new Path2D.Double();
		poly.moveTo(0, 0);
		poly.lineTo(600, 0);
		poly.lineTo(650, -50);
		poly.lineTo(-50, -50);
		poly.closePath();
		poly.moveTo(-50, -50);
		poly.lineTo(0, -100);
		poly.lineTo(600, -100);
		poly.lineTo(650, -50);
		poly.closePath();
		outline = poly;
		
		destroyClip = createClip("/sounds/bangLarge.wav");
		
		stop = new ParticipantCountdownTimer(this, "stop", 1000 + Constants.ALIEN_DELAY);
		fire = new ParticipantCountdownTimer(this, "fire", 1500 + Constants.ALIEN_DELAY);
	}

	@Override
	protected Shape getOutline() {
		return outline;
	}

	@Override
	public void collidedWith(Participant p) {
		if(p instanceof BossDestroyer){
			health--;
		}
		if(health <= 0){
			expire(this);
			controller.bossDestroyed();
			for(int i = 0; i < 25; i++){
				Debris d = new AlienDebris(Constants.RANDOM.nextInt(Constants.SIZE), Constants.RANDOM.nextInt(200), controller);
				controller.addParticipant(d);
			}
			controller.addScore(5000);
			if(destroyClip != null){
				if(destroyClip.isActive()){
					destroyClip.stop();
				}
				destroyClip.setFramePosition(0);
				destroyClip.start();
			}
		}
	}
	
	public void fire(){
		if(controller.getShip() != null){
		double targetX = controller.getShip().getX() - this.getX();
		double targetY = controller.getShip().getY() - this.getY();
		BossBullet b1 = new BossBullet(getX()+600, getY(), Math.atan2(targetY, targetX-600));
		BossBullet b2 = new BossBullet(getX(), getY(), Math.atan2(targetY, targetX));
		controller.addParticipant(b1);
		controller.addParticipant(b2);
		}
	}
	
	@Override
	public void countdownComplete(Object payload){
		if(payload.equals("spawn")){
			controller.addParticipant(this);
			setVelocity(5, -3*Math.PI/2);
		}
		if(payload.equals("stop")){
			setVelocity(0, 0);
		}
		if (payload.equals("fire") && this != null) {
			if (timeToFire) {
				timeToFire = false;
				fire = new ParticipantCountdownTimer(this, "fire", 250);
				fire();
			} else if (!timeToFire && this != null) {
				timeToFire = true;
				fire = new ParticipantCountdownTimer(this, "fire", 1500);
			}
		}
	}
	
	public int getHealth(){
		return health;
	}

	@Override
	public void draw(Graphics2D g){
		g.setColor(Color.GREEN);
		super.draw(g);
		g.setColor(Color.WHITE);
	}

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
