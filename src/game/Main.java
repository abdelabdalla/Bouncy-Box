package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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
	Random rand = new Random();

	int score = 0;
	int yPos = 0;
	int xPos = 615;
	int pipe1X = 1280;
	int distPipes = 300;
	int[] pipeX = { pipe1X, pipe1X + distPipes, pipe1X + (2 * distPipes),
			pipe1X + (3 * distPipes), pipe1X + (4 * distPipes) };
	int[] pipeY = new int[10];
	int pipeGap = 635;
	int pipeSpeed = -2;
	int clear = 1305;
	int clearIntersectPoint = 640;
	int highScore;
	int onlineScore;

	double cloudX = 20;
	double cloudY = rand.nextInt(100) + 40;
	double terminalVelocity = 18.0;
	double gravity = 0.34;
	double yVelocity = 0.0;
	double cloudSpeed = 0.3;

	boolean gameOver = false;
	boolean gameStarted = false;
	boolean gamePaused = false;
	boolean resume = false;
	boolean exit = false;
	boolean saveOptions = false;

	File pointSFX = new File("Cleared.wav");

	String playerName;
	String worldBestScore;
	String difficultyOption;
	String colourOption;
	String[] difficulty = { "Easy", "Normal", "Hard" };
	String[] boxColour = { "Black", "Blue", "Green", "Grey", "Orange", "Pink",
			"Purple", "Red", "Yellow" };

	JFrame optionsFrame = new JFrame("Options");

	JPanel optionsPanel = new JPanel();

	JLabel difficultyLabel = new JLabel("Difficulty");
	JLabel colourLabel = new JLabel("Colour");

	JComboBox<String> difficultyBox = new JComboBox<>(difficulty);
	JComboBox<String> colourBox = new JComboBox<>(boxColour);

	JButton saveDifficulty = new JButton("Save");
	
	Color bgColour = new Color(48, 179, 255);

	public Main(String name) {
		playerName = name;

		for (int i = 0; i < 10; i += 2) { // Initialise y axis for pipes
			pipeY[i] = rand.nextInt(600) - 600;
			pipeY[i + 1] = pipeY[i] + pipeGap;
		}

		try { // see if HScore.txt exists on the computer

			BufferedReader br = new BufferedReader(new FileReader("HScore.txt"));

			String s = br.readLine();

			highScore = Integer.parseInt(s);

			System.out.println(highScore);
			br.close();

		} catch (Exception e) {
			highScore = 0;
		}
		
		try {
			BufferedReader or = new BufferedReader(new FileReader("Options.txt")); // see if Options.txt exists on the computer
			colourOption = or.readLine();
			System.out.println(colourOption + " lel");
			or.close();
			
		} catch (Exception e2) {
			colourBox.setSelectedItem("Yellow");
			colourOption = (String) colourBox.getSelectedItem();
			System.out.println(colourOption);
			
		}
		
		onlineCheck();
		
		difficultyBox.setSelectedItem("Normal"); // set default difficulty
		difficultyOption = (String) difficultyBox.getSelectedItem();
		System.out.println(difficultyOption);
		
		addKeyListener(this);
		setFocusable(true);
	}

	public void paintComponent(final Graphics g) {
		Rectangle[] pipe = new Rectangle[10];
		Rectangle o1 = new Rectangle((int) xPos, (int) yPos, 35, 35);

		Graphics2D g2d = (Graphics2D) g;							// Anti-aliasing the drawn objects (smoothening the edges)
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g2d);

		for (int i = 0; i < 10; i += 2) { // initialise pipe y axis
			while (pipeY[i] < -500) {
				pipeY[i] = rand.nextInt(600) - 600;
				pipeY[i + 1] = pipeY[i] + pipeGap;
			}
		}

		for (int i = 0; i < 10; i += 2) { // initialise pipes
			pipe[i] = new Rectangle(pipeX[i / 2], pipeY[i], 50, 500);
			pipe[i + 1] = new Rectangle(pipeX[i / 2], pipeY[i + 1], 50, 1000);
		}

		g.setColor(bgColour);
		g.fillRect(0, 0, 1280, 720);

		g.setColor(new Color(245, 251, 255));
		for (int i = 350; i <= 1400; i += 350) { // draw clouds
			paintOvals(g, cloudX, cloudY);
			paintOvals(g, cloudX + i, cloudY);
		}

		g.setColor(new Color(0, 212, 49));
		for (Rectangle p : pipe) { // draw pipes
			g.fillRect(p.x, p.y, p.width, p.height);
		}

		if (gameStarted) { // display box and score
			setColour(g);
			g.fillRect(o1.x, o1.y, o1.width, o1.height);
			g.setColor(Color.RED);
			g.setFont(new Font("Arial", Font.PLAIN, 35));
			g.drawString(Integer.toString(score), 635, 30);
		}

		displayMenu(g);

		for (int i = 0; i <= 9; i++) { // checks to see if box has collided with a pipe
			if (o1.intersects(pipe[i])) {
				gameOver = true;
				timer.stop();
			}
		}

		if (gamePaused) { // when paused
			g.setColor(Color.RED);
			g.setFont(new Font("Arial ", Font.PLAIN, 45));
			g.drawString("Paused", 545, 250);
			g.drawString("Press enter to resume", 420, 300);
			timer.stop();
		}

		if (gameOver) { // at game end

			onlineCheck();

			if (highScore < score) { // if new high score is achieved
				highScore = score;

				try { // save high score to HScore.txt

					String s = Integer.toString(highScore);

					Writer wr = new FileWriter("HScore.txt");
					wr.write(s);
					System.out.println(s);
					wr.close();

				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}

			g.setColor(Color.RED);
			g.setFont(new Font("Arial", Font.PLAIN, 45));
			g.drawString("You lose!", 545, 250);
			g.drawString("Local High Score: " + Integer.toString(highScore),
					450, 300);
			g.drawString("Press enter to retry or esc to exit", 323, 350);
			g.drawString("Press L to reveal the top scorer", 335, 400);
			g.drawString("Press O to view options", 402, 450);
		}

	}

	public void paintOvals(Graphics g, double cloudX2, double cloudY2) { // draw a cloud
		g.fillOval((int) cloudX2, (int) cloudY2 - 40, 150, 75);
		g.fillOval((int) cloudX2 + 30, (int) (cloudY2 - 70), 150, 75);
		g.fillOval((int) cloudX2 - 50, (int) (cloudY2 - 95), 150, 75);

	}

	public void onlineCheck() { // TODO online checking code

		final int s1 = score;
		ParseObject object = new ParseObject("Scores");
		object.put("User", playerName);
		object.put("Score", s1);
		object.saveInBackground(); // save score

		ParseQuery<ParseObject> q = ParseQuery.getQuery("Scores"); // Tests if
																	// score is
																	// high
																	// score

		q.getInBackground("PseztczTNY", new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject ob, ParseException e) {
				if (e == null) {
					if (ob.getInt("Score") < s1) {
						ob.put("User", playerName);
						System.out.println(score);
						ob.put("Score", s1);
						
						try {
							ob.save();
							JOptionPane.showMessageDialog(Frame.frame,
									"You have the best high score online!",
									"Congratulations!",
									JOptionPane.DEFAULT_OPTION);
							worldBestScore = "Score: " + ob.getInt("Score")
									+ "\n" + "User: " + ob.getString("User");

						} catch (ParseException e1) {

							e1.printStackTrace();
						}
					} else {
						worldBestScore = "Score: " + ob.getInt("Score") + "\n"
								+ "User: " + ob.getString("User");
						System.out.println(worldBestScore);
						onlineScore = ob.getInt("Score");
					}
				}
			}
		});
	}

	public void actionPerformed(ActionEvent e) {

		yVelocity += gravity;

		if (yVelocity >= terminalVelocity) {
			yVelocity = terminalVelocity;
		}
		if (yPos >= 670 || yPos <= -45) {
			gameOver = true;
			timer.stop();
		}
		yPos += yVelocity;

		if (clear <= 640) {
			score++;
			clear += 300;
			soundEffect();
		}

		// Pipe things moving etc.

		pipeX[0] += pipeSpeed;
		pipeX[1] += pipeSpeed;
		pipeX[2] += pipeSpeed;
		pipeX[3] += pipeSpeed;
		pipeX[4] += pipeSpeed;
		clear += pipeSpeed;
		cloudX -= cloudSpeed;

		for (int i = 0; i < 5; i++) {
			if (pipeX[i] <= -50) {
				if (i != 0) {
					pipeX[i] = pipeX[i - 1] + 300;
				} else {
					pipeX[0] = pipeX[4] + 300;
				}
				pipeY[2 * i] = rand.nextInt(600) - 600;
				pipeY[2 * i + 1] = pipeY[2 * i] + pipeGap;
			}
		}

		if (cloudX <= -185) {
			cloudX = 164.79999999999868;
		}

		repaint();
	}

	public void displayMenu(Graphics g) {
		if (!gameStarted && highScore != 0 && !gameOver) { // at start
			g.setColor(Color.RED);
			g.setFont(new Font("Arial ", Font.PLAIN, 45));
			g.drawString("Bouncy Box", 510, 100);
			g.drawString("Press space to begin", 420, 300);
			g.drawString("Press O to view options", 402, 350);

		} else if (!gameStarted && highScore == 0 && !gameOver) { // at start of first ever use
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

	public void soundEffect() { // method which is called to play a sound when scoring a point

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					AudioInputStream ais = AudioSystem
							.getAudioInputStream(pointSFX);
					Clip clip = AudioSystem.getClip();
					clip.open(ais);
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

	public void options() { // TODO Options Menu
			
		optionsPanel.setLayout(null);
		optionsPanel.setBackground(bgColour);

		optionsFrame.setTitle("Options");
		optionsFrame.setSize(230, 180);
		optionsFrame.setLocationRelativeTo(null);
		optionsFrame.setResizable(false);
		optionsFrame.setVisible(true);

		difficultyLabel.setBounds(20, 20, 70, 20);
		difficultyLabel.setForeground(Color.YELLOW);
		colourLabel.setBounds(20, 60, 70, 20);
		colourLabel.setForeground(Color.YELLOW);

		difficultyBox.setBounds(80, 20, 70, 20);
		difficultyBox.setBackground(Color.WHITE);
		difficultyBox.setForeground(Color.RED);
		colourBox.setBounds(80, 60, 70, 20);
		colourBox.setBackground(Color.WHITE);
		colourBox.setForeground(Color.RED);

		difficultyBox.setSelectedItem(difficultyOption);
		colourBox.setSelectedItem(colourOption);
		
		saveDifficulty.setBounds(80, 100, 70, 20);
		saveDifficulty.setBackground(Color.WHITE);
		saveDifficulty.setForeground(Color.RED);

		saveDifficulty.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) { // save the settings

				saveOptions = true;

				if (saveOptions) {
					difficultyOption = (String) difficultyBox.getSelectedItem();
					System.out.println(difficultyOption);

					switch (difficultyOption) {
					case "Easy":
						pipeGap = 650;
						break;
					case "Normal":
						pipeGap = 635;
						break;
					case "Hard":
						pipeGap = 620;
					}

					colourOption = (String) colourBox.getSelectedItem();
					System.out.println(colourOption);

					System.out.println(pipeGap);
					
					
					try {
						Writer owr = new FileWriter("Options.txt");
						owr.write(colourOption);
						owr.close();					
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				
					saveOptions = false;

				}
			}

		});

		optionsPanel.add(difficultyBox);
		optionsPanel.add(colourBox);

		optionsPanel.add(colourLabel);
		optionsPanel.add(difficultyLabel);

		optionsPanel.add(saveDifficulty);

		optionsFrame.add(optionsPanel);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		if (c == KeyEvent.VK_SPACE) { // bounce
			yVelocity = -7;

			if (yPos <= 50) {
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
		}

		if (c == KeyEvent.VK_O && !gameStarted) {
			options();
		}

		if (c == KeyEvent.VK_SPACE && !gameStarted) { // start the game animations
			timer.start();
			gameStarted = true;

		}
		if (gameOver) {

			if (c == KeyEvent.VK_ENTER) { // reset the positions of the pipes etc.
				timer.start();

				score = 0;
				gameOver = false;
				yVelocity = 0;
				yPos = 0;
				pipeX[0] = 1280;
				pipeX[1] = pipeX[0] + 300;
				pipeX[2] = pipeX[1] + 300;
				pipeX[3] = pipeX[2] + 300;
				pipeX[4] = pipeX[3] + 300;
				pipeY[0] = rand.nextInt(600) - 600;
				pipeY[2] = rand.nextInt(600) - 600;
				pipeY[4] = rand.nextInt(600) - 600;
				pipeY[6] = rand.nextInt(600) - 600;
				pipeY[8] = rand.nextInt(600) - 600;
				pipeY[1] = pipeY[0] + pipeGap;
				pipeY[3] = pipeY[2] + pipeGap;
				pipeY[5] = pipeY[4] + pipeGap;
				pipeY[7] = pipeY[6] + pipeGap;
				pipeY[9] = pipeY[8] + pipeGap;
				clear = 1305;
				clearIntersectPoint = 640;
			}
			if (c == KeyEvent.VK_ESCAPE) { // close the program
				System.exit(0);
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

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}