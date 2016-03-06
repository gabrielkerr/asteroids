package asteroids;

import javax.swing.JOptionPane;

/**
 * The main class for the application.
 */
public class Asteroids {
	/**
	 * Launches a dialog that lets the user choose between a classic and an
	 * enhanced game of Asteroids.
	 */
	public static void main(String[] args) {
		chooseVersion();
	}

	/**
	 * Interacts with the user to determine whether to run classic Asteroids or
	 * enhanced Asteroids.
	 */
	@SuppressWarnings("unused")
	private static void chooseVersion() {
		String[] options = { "Classic", "Enhanced" };
		int choice = JOptionPane.showOptionDialog(null, "Which version would you like to run?", "Choose a Version",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (choice == 0) {
			// creates classic controller
			Controller c = new Controller(false);
		} else if (choice == 1) {
			// creates enhanced controller
			Controller c = new Controller(true);
		}
	}
}
