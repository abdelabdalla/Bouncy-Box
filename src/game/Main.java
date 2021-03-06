/**
 * Made by Harnaam Jandoo
 * With thanks to Abdel-Rahim Adballa, Yousuf Mohammed-Ahmed and Dominic Lobo
 */
package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;
import org.parse4j.callback.GetCallback;

public class Main extends JPanel implements ActionListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Timer timer = new Timer(13, this);
	Random random = new Random(); // well this was random

	int score = 0;
	int yPosition = 0;
	int xPos = 615;
	int pipe1X = 1280;
	int distPipes = 300;
	int[] pipeX = { pipe1X, pipe1X + distPipes, pipe1X + (2 * distPipes),
			pipe1X + (3 * distPipes), pipe1X + (4 * distPipes) };
	int[] pipeY = new int[10];
	int pipeGap;
	int pointAward = 1305;
	int clearIntersectPoint = 640;
	int highScore;
	int onlineScore;

	int treeY = 550;
	int twinkle;
	int[] pipeYSpeed1 = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
	int[] pipeYSpeed2 = { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };
	int[] allHighScores = new int[6];
	int iDiff;

	double treeX = 80.0;
	double cloudX = 20.0;
	double cloudY = random.nextInt(100) + 40;
	double terminalVelocity = 18.0;
	double gravity = 0.38;
	double yVelocity = 0.0;
	double cloudSpeed = 0.3;
	double pipeSpeed = -2.0;
	double snowSpeed = 0.4;
	double snoopX = 1600;
	double snoopY = 500;
	double[] starsX = new double[200];
	double[] starsY = new double[200];
	double[] snowX = new double[100];
	double[] snowY = new double[100];

	boolean gameOver = false;
	boolean gameStarted = false;
	boolean gamePaused = false;
	boolean resume = false;
	boolean exit = false;
	boolean saveOptions = false;
	boolean delay = false;

	Pipe object = new Pipe(pipeX[1], pipeY[1]);

	File pointSFX = new File("Cleared.wav");
	Icon snoop = new ImageIcon("snoop.gif");

	String playerName;
	String worldBestScore;
	String difficultyOption;
	String colourOption;
	String modeOption = "Normal";
	String[] scoreStrings = new String[6];
	String[] difficulty = { "Very Easy", "Easy", "Normal", "Hard",
			"Impossible", "Don't even try" };
	String[] boxColour = { "Black", "Blue", "Green", "Grey", "Orange", "Pink",
			"Purple", "Red", "Yellow" };
	String[] mode = { "Normal", "Night", "High", "Drunk", "Christmas", "Easter" };
	String sDiff;

	JFrame optionsFrame = new JFrame("Options");

	JPanel optionsPanel = new JPanel();

	JLabel difficultyLabel = new JLabel("Difficulty");
	JLabel colourLabel = new JLabel("Colour");
	JLabel modeLabel = new JLabel("Mode");
	JLabel descriptionLabel = new JLabel();
	JLabel snoopLabel = new JLabel();

	JComboBox<String> difficultyBox = new JComboBox<>(difficulty);
	JComboBox<String> colourBox = new JComboBox<>(boxColour);
	JComboBox<String> modeBox = new JComboBox<>(mode);

	JButton saveDifficulty = new JButton("Save");

	Color backgroundColour;
	Color pipeColour;
	Color cloudColour;
	Color trunkColour;
	Color leafColour;

	public Main(String name) {
		playerName = name;

		try {
			BufferedReader difficultyReader = new BufferedReader(
					new FileReader("Difficulty.txt")); // see
														// if
														// Difficulty.txt
														// exists
			difficultyOption = difficultyReader.readLine();

			System.out.println("Saved difficulty: " + difficultyOption);
			difficultyBox.setSelectedItem(difficultyOption);
			difficultyReader.close();

			setPipeGap();
			System.out.println("Gap read: " + pipeGap);

		} catch (Exception e3) {
			difficultyBox.setSelectedItem("Normal");
			difficultyOption = (String) difficultyBox.getSelectedItem();
			System.out.println("Default difficulty has been set");
		}

		for (int i = 0; i < 10; i += 2) { // Initialise y axis for pipes
			pipeY[i] = random.nextInt(600) - 600;
			pipeY[i + 1] = pipeY[i] + pipeGap;
		}

		if (saveOptions) {
			for (int i = 0; i < 10; i += 2) { // Initialise y axis for pipes
												// when save button's pressed
				pipeY[i] = random.nextInt(600) - 600;
				pipeY[i + 1] = pipeY[i] + pipeGap;
			}
		}

		for (int i = 0; i < 200; i++) { // make stars randomly blotted on the
										// screen
			starsX[i] = random.nextInt(1280);
			starsY[i] = random.nextInt(720);
		}

		for (int i = 0; i < 100; i++) { // make snow randomly blotted onto the
										// screen
			snowX[i] = random.nextInt(1280);
			snowY[i] = random.nextInt(720);
		}

		for (int i = 0; i < 6; i++) { // Load up individual high scores

			iDiff = i;
			String[] s = new String[6];

			try {

				BufferedReader r;

				switch (iDiff) {
				case 0:
					r = new BufferedReader(new FileReader("Very Easy.txt"));
					s[0] = r.readLine();
					allHighScores[iDiff] = Integer.parseInt(s[0]);
					break;
				case 1:
					r = new BufferedReader(new FileReader("Easy.txt"));
					s[1] = r.readLine();
					allHighScores[iDiff] = Integer.parseInt(s[1]);
					break;
				case 2:
					r = new BufferedReader(new FileReader("Normal.txt"));
					s[2] = r.readLine();
					allHighScores[iDiff] = Integer.parseInt(s[2]);
					break;
				case 3:
					r = new BufferedReader(new FileReader("Hard.txt"));
					s[3] = r.readLine();
					allHighScores[iDiff] = Integer.parseInt(s[3]);
					break;
				case 4:
					r = new BufferedReader(new FileReader("Impossible.txt"));
					s[4] = r.readLine();
					allHighScores[iDiff] = Integer.parseInt(s[4]);
					break;
				case 5:
					r = new BufferedReader(new FileReader("Don't even try.txt"));
					s[5] = r.readLine();
					allHighScores[iDiff] = Integer.parseInt(s[5]);
					break;
				}

			} catch (Exception e) {
				allHighScores[i] = 0;
			}

			getDifficulty(iDiff);

			System.out.println(sDiff + ": " + allHighScores[i]);

		}

		try {
			BufferedReader or = new BufferedReader(
					new FileReader("Options.txt")); // see
													// if
													// Options.txt
													// exists
			colourOption = or.readLine();
			System.out.println("Saved colour: " + colourOption);
			or.close();

		} catch (Exception e2) {
			colourBox.setSelectedItem("Yellow");
			colourOption = (String) colourBox.getSelectedItem();
			System.out.println("Default colour has been set");

		}

		addKeyListener(this);
		setFocusable(true);
	}

	public void paintComponent(final Graphics g) {

		Rectangle[] pipe = new Rectangle[10];
		Rectangle[] stars = new Rectangle[200];
		Rectangle[] snow = new Rectangle[100];
		Rectangle box = new Rectangle((int) xPos, (int) yPosition, 35, 35);

		Graphics2D g2d = (Graphics2D) g; // Anti-aliasing the drawn objects
											// (smoothening the edges)

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g2d);
		
		for (int i = 0; i < 10; i += 2) {// initialise pipe y axis
			setPipeGap();
			while (pipeY[i] < -500) {
				pipeY[i] = random.nextInt(600) - 600;
				pipeY[i + 1] = pipeY[i] + pipeGap;

			}

		}

		for (int i = 0; i < 10; i += 2) { // initialise pipes
			pipe[i] = new Rectangle(pipeX[i / 2], pipeY[i], 50, 500);
			pipe[i + 1] = new Rectangle(pipeX[i / 2], pipeY[i + 1], 50, 1000);
		}

		setMode(g);

		g.setColor(backgroundColour); // draw the background
		g.fillRect(0, 0, 1280, 720);

		if ("Night".equals(modeOption)) { // set the theme when night mode is
											// selected

			for (int i = 0; i < 200; i++) { // initialise stars

				stars[i] = new Rectangle((int) starsX[i], (int) starsY[i], 1, 1);
			}

			for (Rectangle p : stars) { // draw stars
				twinkle = random.nextInt(255);
				g.setColor(new Color(twinkle, twinkle, twinkle));

				g.fillOval(p.x, p.y, p.width, p.height);
			}

			g.setColor(Color.WHITE);
			g.fillOval(140, 50, 150, 150); // draw the moon
			g.setColor(Color.BLACK);
			g.fillOval(158, 42, 150, 150);

		}

		for (int i = 0; i < 3000; i += 500) {
			paintTrees(g, (int) treeX, treeY);
			paintTrees(g, (int) treeX + i, treeY);
		}

		g.setColor(cloudColour);
		if ("Christmas".equals(modeOption)) { // set the theme when Christmas
												// mode is selected
			cloudY = 50;

			for (int i = 0; i <= 1400; i += 100) { // set clouds to look like a
													// typical overcast day
													// (although with a blue
													// sky)
				paintClouds(g, cloudX, cloudY);
				paintClouds(g, cloudX + i, cloudY);
			}

			for (int i = 0; i < 100; i++) { // initialise the snow
				snow[i] = new Rectangle((int) snowX[i], (int) snowY[i], 10, 10);
			}
			for (Rectangle s : snow) { // draw the snow
				g.setColor(cloudColour);
				g.fillOval(s.x, s.y, s.width, s.height);
			}
		} else {
			g.setColor(cloudColour);
			for (int i = 350; i <= 1400; i += 350) { // draw clouds
				paintClouds(g, cloudX, cloudY);
				paintClouds(g, cloudX + i, cloudY);
			}
		}

		g.setColor(pipeColour);
		for (Rectangle p : pipe) { // draw pipes
			g.fillRect(p.x, p.y, p.width, p.height);
		}

		if (gameStarted) { // display box and score
			setColour(g);
			setPipeGap();

			if (modeOption.equals("High")) { // Welcome Snoop Dogg to the team!
				Image image = ((ImageIcon) snoop).getImage();
				g.drawImage(image, (int) snoopX, (int) snoopY, 100, 200, this);

			}

			if (!(modeOption.equals("Christmas"))) {

				g.fillRect(box.x, box.y, box.width, box.height);

			} else {
				try {
					BufferedImage image = ImageIO.read(new File(
							"santa icon.png"));
					g.drawImage(image, xPos, yPosition, this);
				} catch (IOException e) {

				}
			}

			g.setColor(Color.RED);
			g.setFont(new Font("Arial", Font.PLAIN, 35));
			g.drawString(Integer.toString(score), 635, 30);
		}

		displayMenu(g);

		for (int i = 0; i <= 9; i++) { // checks to see if box has collided with
										// a pipe
			if (box.intersects(pipe[i])) {
				gameOver = true;
				timer.stop();
			}
		}

		if (gamePaused && !gameOver) { // when paused
			g.setColor(Color.RED);
			g.setFont(new Font("Arial ", Font.PLAIN, 45));
			g.drawString("Paused", 545, 250);
			g.drawString("Press enter to resume", 420, 300);
			g.drawString("Press Q to quit the game", 390, 350);
			timer.stop();
		}

		if (gameOver) { // at game end

			onlineCheck();
			
			snoopX = 1600;

			if (allHighScores[iDiff] < score) { // if new high score is achieved
				allHighScores[iDiff] = score;

				System.out.println("High score on that difficulty");

				try { // save individual high scores

					scoreStrings[iDiff] = Integer
							.toString(allHighScores[iDiff]);

					System.out.println("Difficulty: " + iDiff);

					Writer highScoreWriter;

					switch (iDiff) {
					case 0:
						highScoreWriter = new FileWriter("Very Easy.txt");
						highScoreWriter.write(scoreStrings[iDiff]);
						System.out.println("Very easy high score: "
								+ scoreStrings[iDiff]);
						highScoreWriter.close();
						break;
					case 1:
						highScoreWriter = new FileWriter("Easy.txt");
						highScoreWriter.write(scoreStrings[iDiff]);
						System.out.println("Easy high score: "
								+ scoreStrings[iDiff]);
						highScoreWriter.close();
						break;
					case 2:
						highScoreWriter = new FileWriter("Normal.txt");
						highScoreWriter.write(scoreStrings[iDiff]);
						System.out.println("Normal high score: "
								+ scoreStrings[iDiff]);
						highScoreWriter.close();
						break;
					case 3:
						highScoreWriter = new FileWriter("Hard.txt");
						highScoreWriter.write(scoreStrings[iDiff]);
						System.out.println("Hard high score: "
								+ scoreStrings[iDiff]);
						highScoreWriter.close();
						break;
					case 4:
						highScoreWriter = new FileWriter("Impossible.txt");
						highScoreWriter.write(scoreStrings[iDiff]);
						System.out.println("Impossible high score: "
								+ scoreStrings[iDiff]);
						highScoreWriter.close();
						break;
					case 5:
						highScoreWriter = new FileWriter("Don't even try.txt");
						highScoreWriter.write(scoreStrings[iDiff]);
						System.out.println("Don't even try high score: "
								+ scoreStrings[iDiff]);
						highScoreWriter.close();
						break;
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
			// Display the 'game over' message
			g.setColor(Color.RED);
			g.setFont(new Font("Arial", Font.PLAIN, 45));
			g.drawString("You lose!", 545, 250);
			g.drawString(
					"Local High Score: "
							+ Integer.toString(allHighScores[iDiff]), 450, 300);
			g.drawString("Press enter to retry or esc to exit", 323, 350);
			g.drawString("Press L to reveal the top scorer", 335, 400);
			g.drawString("Press O to view options", 402, 450);
		}

	}

	public void setMode(Graphics g) {

		switch (modeOption) { // Get what ever mode is selected and change
								// things accordingly

		case "Normal":
			backgroundColour = new Color(48, 179, 255);
			pipeColour = new Color(0, 212, 49);
			cloudColour = new Color(254, 251, 255);
			trunkColour = new Color(110, 58, 0);
			leafColour = new Color(0, 128, 3);
			delay = false;
			break;
		case "Night":
			backgroundColour = Color.BLACK;
			pipeColour = new Color(0, 97, 24);
			cloudColour = new Color(64, 61, 65);
			trunkColour = new Color(110, 58, 0);
			leafColour = new Color(0, 128, 3);
			delay = false;
			break;
		case "High":
			backgroundColour = new Color(random.nextInt(255),
					random.nextInt(255), random.nextInt(255));
			pipeColour = new Color(random.nextInt(255), random.nextInt(255),
					random.nextInt(255));
			cloudColour = new Color(random.nextInt(255), random.nextInt(255),
					random.nextInt(255));
			trunkColour = new Color(random.nextInt(255), random.nextInt(255),
					random.nextInt(255));
			leafColour = new Color(random.nextInt(255), random.nextInt(255),
					random.nextInt(255));
			delay = false;
			break;
		case "Drunk":
			backgroundColour = new Color(48, 179, 255);
			pipeColour = new Color(0, 212, 49);
			cloudColour = new Color(254, 251, 255);
			trunkColour = new Color(110, 58, 0);
			leafColour = new Color(0, 128, 3);
			delay = true;
			break;
		case "Christmas":
			backgroundColour = new Color(48, 179, 255);
			pipeColour = new Color(0, 212, 49);
			cloudColour = new Color(254, 251, 255);
			trunkColour = new Color(110, 58, 0);
			leafColour = new Color(0, 128, 3);
			delay = false;
			break;
		case "Easter": // TODO Easter mode

			break;
		}
	}

	public void paintClouds(Graphics g, double cloudX2, double cloudY2) { // draw
																			// a
																			// cloud
		g.fillOval((int) cloudX2 - 10, (int) cloudY2 - 42, 140, 55);
		g.fillOval((int) cloudX2 + 30, (int) (cloudY2 - 70), 150, 65);
		g.fillOval((int) cloudX2 - 32, (int) (cloudY2 - 80), 150, 70); // Top
																		// right
																		// oval?
		g.fillOval((int) cloudX2 + 78, (int) cloudY2 - 74, 80, 30);
		g.fillOval((int) cloudX2 + 80, (int) cloudY2 - 44, 80, 50);

	}

	public void paintTrees(Graphics g, int treeX2, int treeY2) { // TODO TREES
																	// V2.0

		g.setColor(trunkColour);
		g.fillRect(treeX2, treeY2, 25, 150);
		g.setColor(leafColour);
		g.fillOval(treeX2 + 5, treeY2 - 30, 50, 50);
		g.fillOval(treeX2 - 35, treeY2 - 10, 50, 50);
		g.fillOval(treeX2 - 42, treeY2 - 28, 50, 50);
		g.fillOval(treeX2 - 12, treeY2 - 58, 50, 50);
		g.fillOval(treeX2, treeY2 - 8, 50, 50);

	}

	public void onlineCheck() {

		final int s1 = score;
		ParseObject object = new ParseObject("Scores");
		object.put("User", playerName);
		object.put("Score", s1);
		object.put("Difficulty", iDiff);
		object.saveInBackground(); // save score

		ParseQuery<ParseObject> q = ParseQuery.getQuery("Scores"); // Tests if
																	// score is
																	// high
																	// score

		q.getInBackground("kdSoMFNnio", new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject pObject, ParseException e) {
				if (e == null) {
					if (pObject.getInt("Score") < s1
							&& pObject.getInt("Difficulty") == iDiff) {
						pObject.put("User", playerName);
						System.out.println("Achieved score: " + score);
						pObject.put("Score", s1);

						try {
							pObject.save();
							JOptionPane.showMessageDialog(Frame.frame,
									"You have the best high score online!",
									"Congratulations!",
									JOptionPane.DEFAULT_OPTION);
							worldBestScore = "Online high score: "
									+ pObject.getInt("Score") + "\n" + "User: "
									+ pObject.getString("User");

						} catch (ParseException e1) {

							e1.printStackTrace();
						}
					} else {

						final int d1 = pObject.getInt("Difficulty");

						getDifficulty(d1);

						worldBestScore = "Online high score: "
								+ pObject.getInt("Score") + "\n" + "User: "
								+ pObject.getString("User") + "\n"
								+ "Difficulty: " + sDiff;
						System.out.println(worldBestScore);
						onlineScore = pObject.getInt("Score");
					}
				}
			}
		});
	}

	public void actionPerformed(ActionEvent e) {

		yVelocity += gravity; // make the box speed up

		snoopX -= 1.5;

		if (snoopX <= -100) {
			snoopX = 1400;
		}

		if (yVelocity >= terminalVelocity) {
			yVelocity = terminalVelocity;
		}
		if (yPosition >= 670 || yPosition <= -75) {
			gameOver = true;
			timer.stop();
		}
		yPosition += yVelocity; // make the ball's position move

		if (pointAward <= 640) { // add a point each time the box goes through
									// the pipe
			score++;
			pointAward += 300;
			soundEffect();
		}

		if (delay) { // slows everything down when drunk mode is selected
			pipeSpeed = -1;
			cloudSpeed = 0.15;
			gravity = 0.15;
			terminalVelocity = 9.0;
		} else {
			terminalVelocity = 18.0; // the normal speeds of everything
			gravity = 0.34;
			cloudSpeed = 0.3;
			pipeSpeed = -2.0;
		}

		// Pipe things moving etc.
		for (int i = 0; i <= 4; i++) {
			pipeX[i] += pipeSpeed;
		}
		pointAward += pipeSpeed;
		cloudX -= cloudSpeed;

		treeX -= 1.7;

		if (treeX <= -50) {
			treeX = 449.0999999999999;
		}

		for (int i = 0; i < 200; i++) { // make the stars move slowly
			starsX[i] -= 0.1;
			if (starsX[i] < 0) {
				starsX[i] = 1282;
			}
		}

		for (int i = 0; i < 100; i++) { // make the snow fall slowly
			snowY[i] += snowSpeed;
			if (snowY[i] > 720) {
				snowX[i] = random.nextInt(1280);
				snowY[i] = -10;
			}
		}

		for (int i = 0; i < 5; i++) { // make the pipes come at a set space and
										// at a random y position
			if (pipeX[i] <= -50) {
				if (i != 0) {
					pipeX[i] = pipeX[i - 1] + 300;
				} else {
					pipeX[0] = pipeX[4] + 300;
				}
				pipeY[2 * i] = random.nextInt(600) - 600;
				pipeY[2 * i + 1] = pipeY[2 * i] + pipeGap;
			}
		}

		if ("Christmas".equals(modeOption)) { // Makes the clouds look like they
												// are infinite
			if (cloudX <= -185) {
				cloudX = -85;
			}
		} else if (cloudX <= -185) {
			cloudX = 164.79999999999868;
		}

		if ("Impossible".equals(difficultyOption)) {

			for (int i = 0; i < 10; i += 2) {
				if (pipeY[i] <= -490) {
					pipeYSpeed1[i] = 1;
					pipeYSpeed1[i + 1] = 1;
					pipeY[i] += pipeYSpeed1[i];
					pipeY[i + 1] += pipeYSpeed1[i + 1];
				}

				if (pipeY[i] >= 0) {
					pipeYSpeed1[i] = -1;
					pipeYSpeed1[i + 1] = -1;
					pipeY[i] += pipeYSpeed1[i];
					pipeY[i + 1] += pipeYSpeed1[i + 1];
				}

				pipeY[i] += pipeYSpeed1[i];
				pipeY[i + 1] += pipeYSpeed1[i + 1];
			}

		}

		if ("Don't even try".equals(difficultyOption)) { // TODO Faster moving
															// pipes

			
			
			for (int i = 0; i < 10; i += 2) {
			
				if (pipeY[i] <= -490) {
					pipeYSpeed2[i] = 7;
					pipeYSpeed2[i + 1] = 7;
					pipeY[i] += pipeYSpeed2[i];
					pipeY[i + 1] += pipeYSpeed2[i + 1];
				}

				if (pipeY[i] >= 0) {
					pipeYSpeed2[i] = -7;
					pipeYSpeed2[i + 1] = -7;
					pipeY[i] += pipeYSpeed2[i];
					pipeY[i + 1] += pipeYSpeed2[i + 1];
				}

				pipeY[i] += pipeYSpeed2[i];
				pipeY[i + 1] += pipeYSpeed2[i + 1];
			}

		}

		repaint();
	}

	public void displayMenu(Graphics g) {

		boolean newUser = true;

		if (allHighScores[0] != 0 || allHighScores[1] != 0
				|| allHighScores[2] != 0 || allHighScores[3] != 0
				|| allHighScores[4] != 0 || allHighScores[5] != 0) {
			if (!gameStarted && !gameOver) { // displays
												// starting
												// menu
				g.setColor(Color.RED);
				g.setFont(new Font("Arial ", Font.PLAIN, 45));
				g.drawString("Bouncy Box", 510, 100);
				g.drawString("Press space to begin", 420, 300);
				g.drawString("Press O to view options", 402, 350);
				newUser = false;

			}
		} else if (!gameStarted && newUser && !gameOver) { // at
															// start
															// of
															// first
															// use
			g.setColor(Color.RED);
			g.setFont(new Font("Arial", Font.PLAIN, 45));
			g.drawString("Bouncy Box", 510, 100);
			g.drawString("Press space to jump", 425, 250);
			g.drawString("Avoid the green pipes", 410, 300);
			g.drawString("Don't touch the ground", 405, 350);
			g.drawString("Press space to begin", 420, 400);
			g.drawString("Press O to view options", 402, 450);
		}

	}

	public void soundEffect() { // method which is called to play a sound when
								// scoring a point

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					AudioInputStream inputStream = AudioSystem
							.getAudioInputStream(pointSFX);
					Clip clip = AudioSystem.getClip();
					clip.open(inputStream);
					clip.start();
				} catch (Exception e) {

				}
			}

		}).start(); 
	}

	public void setColour(Graphics g) { // sets the colour of the box
		switch (colourOption) {
		case "Black":
			g.setColor(Color.BLACK);
			break;
		case "Blue":
			g.setColor(Color.BLUE);
			break;
		case "Green":
			g.setColor(Color.GREEN);
			break;
		case "Grey":
			g.setColor(Color.GRAY);
			break;
		case "Orange":
			g.setColor(Color.ORANGE);
			break;
		case "Pink":
			g.setColor(Color.PINK);
			break;
		case "Purple":
			g.setColor(new Color(174, 0, 255));
			break;
		case "Red":
			g.setColor(Color.RED);
			break;
		case "Yellow":
			g.setColor(Color.YELLOW);
			break;
		}
	}

	public void getDifficulty(int iDiff) {
		switch (iDiff) {
		case 0:
			sDiff = "Very Easy";
			break;
		case 1:
			sDiff = "Easy";
			break;
		case 2:
			sDiff = "Normal";
			break;
		case 3:
			sDiff = "Hard";
			break;
		case 4:
			sDiff = "Impossible";
			break;
		case 5:
			sDiff = "Don't even try";
			break;
		}
	}

	public void options() { // TODO Options Menu

		optionsPanel.setLayout(null);
		optionsPanel.setBackground(backgroundColour);

		optionsFrame.setTitle("Options");
		optionsFrame.setSize(230, 260);
		optionsFrame.setLocationRelativeTo(null);
		optionsFrame.setResizable(false);
		optionsFrame.setVisible(true);

		difficultyLabel.setBounds(20, 20, 70, 20);
		difficultyLabel.setForeground(Color.RED);
		colourLabel.setBounds(20, 60, 70, 20);
		colourLabel.setForeground(Color.RED);
		modeLabel.setBounds(20, 100, 70, 20);
		modeLabel.setForeground(Color.RED);
		descriptionLabel.setBounds(20, 140, 180, 45);
		descriptionLabel.setForeground(Color.RED);

		modeBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				modeOption = (String) e.getItem();
				if (modeOption.equals("Normal")) {
					descriptionLabel
							.setText("<html>Normal: The default mode for a simple playing experience"); // description
																										// of
																										// the
																										// mode
				} else if (modeOption.equals("Night")) {
					descriptionLabel
							.setText("<html>Night: Don't get distracted by all the pretty stars! (P.S. black isn't a good colour)"); // description
																																		// of
																																		// the
																																		// mode
				} else if (modeOption.equals("High")) {
					descriptionLabel
							.setText("<html>High: You might've had a bit too much to smoke... (WARNING: contains flasing images...)"); // description
																																		// of
																																		// the
																																		// mode
				} else if (modeOption.equals("Drunk")) {
					descriptionLabel
							.setText("<html>Drunk: Alcohol tends to slow your reactions down..."); // description
																									// of
																									// the
																									// mode
				} else if (modeOption.equals("Christmas")) {
					descriptionLabel
							.setText("<html>Christmas: 'Snow is falling... <br>all around you...'</html>"); // description
																											// of
																											// the
																											// mode
				} else if (modeOption.equals("Easter")) {
					descriptionLabel
							.setText("<html>Easter: Hopping is the new bouncing!"); // description
																					// of
																					// the
																					// mode
				}
			}

		});

		difficultyBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				difficultyOption = (String) e.getItem();
				if (difficultyOption.equals("Very Easy")) {
					descriptionLabel
							.setText("<html>Very Easy: Come on, you can't be THAT bad?");
				} else if (difficultyOption.equals("Easy")) {
					descriptionLabel
							.setText("<html>Easy: For those that don't seek a challenge...");
				} else if (difficultyOption.equals("Normal")) {
					descriptionLabel
							.setText("<html>Normal: The default difficulty - not too easy, not too hard...");
				} else if (difficultyOption.equals("Hard")) {
					descriptionLabel
							.setText("<html>Hard: For those that seek a challenge...");
				} else if (difficultyOption.equals("Impossible")) {
					descriptionLabel
							.setText("<html>Impossible: Okay, it's not exactly impossible, but still ruddy difficult...");
				} else if (difficultyOption.equals("Don't even try")) {
					descriptionLabel
							.setText("<html>Don't even try: Get a point on this and I'll personally give you a medal...");
				}

			}

		});

		difficultyBox.setBounds(80, 20, 110, 20);
		difficultyBox.setBackground(Color.WHITE);
		difficultyBox.setForeground(Color.RED);
		colourBox.setBounds(80, 60, 110, 20);
		colourBox.setBackground(Color.WHITE);
		colourBox.setForeground(Color.RED);
		modeBox.setBounds(80, 100, 110, 20);
		modeBox.setBackground(Color.WHITE);
		modeBox.setForeground(Color.RED);

		difficultyBox.setSelectedItem(difficultyOption);
		colourBox.setSelectedItem(colourOption);
		modeBox.setSelectedItem(modeOption);

		saveDifficulty.setBounds(80, 185, 110, 20);
		saveDifficulty.setBackground(Color.WHITE);
		saveDifficulty.setForeground(Color.RED);

		saveDifficulty.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) { // save the settings

				difficultyOption = (String) difficultyBox.getSelectedItem();
				colourOption = (String) colourBox.getSelectedItem();
				modeOption = (String) modeBox.getSelectedItem();

				setPipeGap();
				System.out.println("Gap set: " + pipeGap);

				try {
					Writer optionsWriter = new FileWriter("Options.txt");
					optionsWriter.write(colourOption);
					optionsWriter.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				try {
					Writer difficultyWriter = new FileWriter("Difficulty.txt");
					difficultyWriter.write(difficultyOption);
					difficultyWriter.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}

				optionsFrame.setVisible(false);

				snoopX = 1600;

			}

		});

		optionsPanel.add(difficultyBox);
		optionsPanel.add(colourBox);
		optionsPanel.add(modeBox);

		optionsPanel.add(colourLabel);
		optionsPanel.add(difficultyLabel);
		optionsPanel.add(modeLabel);
		optionsPanel.add(descriptionLabel);

		optionsPanel.add(saveDifficulty);

		optionsFrame.add(optionsPanel);
	}

	public void setPipeGap() {

		switch (difficultyOption) {
		case "Very Easy":
			pipeGap = 680;
			iDiff = 0;
			break;
		case "Easy":
			pipeGap = 650;
			iDiff = 1;
			break;
		case "Normal":
			pipeGap = 635;
			iDiff = 2;
			break;
		case "Hard":
			pipeGap = 620;
			iDiff = 3;
			break;
		case "Impossible":
			pipeGap = 610;
			iDiff = 4;
			break;
		case "Don't even try":
			pipeGap = 590;
			iDiff = 5;
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();

		if (c == KeyEvent.VK_SPACE && gameStarted && !delay) { // bounce
			yVelocity = -7;

			if (yPosition <= 50) {
				yVelocity = -4;
			}
		}
		if (c == KeyEvent.VK_ESCAPE) {
			gamePaused = true;
		}
		if (gamePaused) {
			if (c == KeyEvent.VK_ENTER) {
				timer.start();
				gamePaused = false;
			}

			if (c == KeyEvent.VK_Q) {
				int choice = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to quit?", "Are you sure?",
						JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		}

		if (c == KeyEvent.VK_O && !gameStarted) {
			options();
		}

		if (c == KeyEvent.VK_SPACE && !gameStarted) { // start the
														// game
			// animations
			timer.start();
			gameStarted = true;

		}

		if (gameOver) {

			if (c == KeyEvent.VK_ENTER) { // reset the positions of the pipes
											// etc.
				timer.start();

				score = 0;
				gameOver = false;
				yVelocity = 0;
				yPosition = 0;
				pipeX[0] = 1280;

				setPipeGap();

				for (int i = 1; i <= 4; i++) {
					pipeX[i] = pipeX[i - 1] + 300;
				}
				for (int i = 0; i <= 8; i += 2) {
					pipeY[i] = random.nextInt(600) - 600;
				}
				for (int i = 1; i <= 9; i += 2) {
					pipeY[i] = pipeY[i - 1] + pipeGap;
				}
				pointAward = 1305;
				clearIntersectPoint = 640;
			}
			if (c == KeyEvent.VK_ESCAPE) { // close the program
				int choice = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to quit?", "Are you sure?",
						JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}

			if (c == KeyEvent.VK_L) { // shows online top scorer
				JOptionPane.showMessageDialog(Frame.frame, worldBestScore,
						"Top scorer", JOptionPane.PLAIN_MESSAGE);
			}

			if (c == KeyEvent.VK_O) { // display options menu
				options();

			}

		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int c = e.getKeyCode();
		if (delay) {
			if (c == KeyEvent.VK_SPACE) { // bounce when drunk mode is selected
				yVelocity = -4.5;

				if (yPosition <= 50) {
					yVelocity = -4;
				}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
