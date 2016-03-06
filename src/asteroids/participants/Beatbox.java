package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import asteroids.Constants;
import asteroids.Participant;
import asteroids.ParticipantCountdownTimer;
import asteroids.Sounds;

public class Beatbox extends Participant implements Sounds {

	// beat frequency
	private int beatFrequency = Constants.INITIAL_BEAT;

	// the outline of the beatbox
	private Shape outline;

	// sound clips
	private Clip beat1;
	private Clip beat2;

	// If true plays beat1 else plays beat2
	private boolean beatSwitch = true;

	public Beatbox() {
		outline = new Path2D.Double();

		beat1 = createClip("/sounds/beat1.wav");
		beat2 = createClip("/sounds/beat2.wav");

		new ParticipantCountdownTimer(this, "beat", beatFrequency);
	}

	@Override
	protected Shape getOutline() {
		// TODO Auto-generated method stub
		return outline;
	}

	@Override
	public void collidedWith(Participant p) {
		// TODO Auto-generated method stub

	}

	// Plays a beat once the previous beat has finished
	@Override
	public void countdownComplete(Object payload) {
		if (payload.equals("beat")) {
			beatFrequency -= Constants.BEAT_DELTA;

			if (beatFrequency < Constants.FASTEST_BEAT) {
				beatFrequency = Constants.FASTEST_BEAT;
			}

			// Turns the beatSwitch on and off and plays the clip.
			if (beatSwitch) {
				if (beat1 != null) {
					beat1.setFramePosition(0);
					beat1.start();
				}
				beatSwitch = false;
			} else {
				if (beat2 != null) {
					beat2.setFramePosition(0);
					beat2.start();
				}
				beatSwitch = true;
			}

			new ParticipantCountdownTimer(this, "beat", beatFrequency);
		}
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