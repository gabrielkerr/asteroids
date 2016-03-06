package asteroids;

import javax.swing.*;
import java.awt.*;
import static asteroids.Constants.*;

/**
 * Defines the top-level appearance of an Asteroids game.
 */
@SuppressWarnings("serial")
public class Display extends JFrame {
	// The area where the action takes place
	private Screen screen;

	// the controller
	private Controller controller;

	// Spacing constant
	private final String SPACER = "   ";

	// contains the lives
	JLabel lives;

	// contains the best score so far
	JLabel bestScore;

	// contains the boss health
	JLabel bossHealth;

	// contains the score
	JLabel score;

	// contains the level
	JLabel level;

	// contains the stats of the game
	JPanel stats;

	// enhanced displays
	private boolean enhanced;

	/**
	 * Lays out the game and creates the controller
	 */
	public Display(Controller controller) {
		// controller of the game
		this.controller = controller;

		// Title at the top
		setTitle(TITLE);

		// Default behavior on closing
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// The main playing area and the controller
		screen = new Screen(controller);

		// state of enhancement
		enhanced = controller.getEnhanced();

		// This panel contains the screen to prevent the screen from being
		// resized
		JPanel screenPanel = new JPanel();
		screenPanel.setLayout(new GridBagLayout());
		screenPanel.add(screen);

		// This panel contains buttons and labels
		JPanel controls = new JPanel();
		controls.setLayout(new BorderLayout());

		// The button that starts the game
		JPanel buttonSection = new JPanel();
		buttonSection.setLayout(new BorderLayout());
		JButton startGame = new JButton(START_LABEL);
		buttonSection.add(startGame, "Center");
		controls.add(buttonSection, "West");

		// This panel contains the stats of the game
		stats = new JPanel();
		stats.setLayout(new GridLayout(1, 4));
		controls.add(stats, "East");

		// contains the number of lives
		lives = new JLabel();
		stats.add(lives);

		// contains the level
		level = new JLabel();
		stats.add(level);

		// contains the score
		score = new JLabel();
		stats.add(score);

		// contains the bestScore
		bestScore = new JLabel();
		if (enhanced) {
			stats.add(bestScore);
		}

		// contains the boss health
		bossHealth = new JLabel();

		// Organize everything
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(screenPanel, "Center");
		mainPanel.add(controls, "North");
		setContentPane(mainPanel);
		pack();

		// Connect the controller to the start button
		startGame.addActionListener(controller);
	}

	/**
	 * Called when it is time to update the screen display. This is what drives
	 * the animation.
	 */
	public void refresh() {
		lives.setText("Lives: " + controller.getLives() + SPACER);
		score.setText("Score: " + controller.getScore() + SPACER);
		level.setText("Level: " + controller.getLevel() + SPACER);
		if (enhanced) {
			bestScore.setText("Best: " + controller.getBestSoFar() + SPACER);
		}
		if (enhanced && controller.getLevel() % 5 == 0) {
			stats.add(bossHealth);
			bossHealth.setText("Boss Health: " + controller.getBoss().getHealth() + SPACER);
		} else if (enhanced && controller.getLevel() % 5 != 0) {
			try {
				stats.remove(bossHealth);
			} catch (NullPointerException e) {
			}
		}
		screen.repaint();
	}

	/**
	 * Sets the large legend
	 */
	public void setLegend(String s) {
		screen.setLegend(s);
	}
}
