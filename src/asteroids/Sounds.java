package asteroids;

import javax.sound.sampled.Clip;

public interface Sounds {
	/**
	 * Creates an audio clip from a sound file.
	 */
	public Clip createClip(String soundFile);
}
