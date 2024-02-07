/*
 *    Copyright [2018] [Justin Nelson]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.audioplayer.GraphAndSound;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.MetalSliderUI;

import com.audioplayer.GraphAndSound.Decoder.BadFileFormatException;
import com.filesystem.handlers.FileExplorer;
import com.mp3.decoder.JavaLayerException;

public final class GUISynthesiszer extends JFrame implements MouseListener,
		KeyListener {
	/**
	 * Create pane bvnl for signal UI.
	 */
	public JPanel signalUI = new SignalPanel();
	/**
	 * Create ImageIcon fields for UI layout.
	 */
	private ImageIcon startIcon;
	private ImageIcon pauseIcon;
	private ImageIcon preIcon;
	private ImageIcon nextIcon;
	private ImageIcon stopIcon;
	private ImageIcon pandaIcon;
	private ImageIcon openIcon;
	/**
	 * Create JButton fields for UI layout.
	 */
	private JButton preButton;
	private JButton playButton;
	private JButton nextButton;
	private JButton stopButton;
	private JButton pandaButton;
	private JButton openButton;
	/**
	 * Create JLabel fields for UI layout.
	 */
	//private JLabel timeLabel;
	private static JLabel lyricsLabelPre;
	private static JLabel lyricsLabelMid;
	private static JLabel lyricsLabelNext;

	/**
	 * Create Box field to lay out components in a vertical manner.
	 */
	//private Box verticalBox;

	/**
	 * Create Container field for overall layout.
	 */
	private Container contentPane;

	/**
	 * Create Boolean fields for signaling or changing current player state.
	 */
	private boolean playNextSong;
	private boolean playPreviousSong;
	private boolean stopPlaying;
	private boolean playOrPause;

	/**
	 * Create final Strings for representing different requests.
	 */
	private final static String next = "playNextSong";
	private final static String open = "openFilesLocation";
	private final static String previous = "playPreviousSong";
	private final static String stop = "stopPlaying";
	private final static String playOrNot = "playOrPause";

	/**
	 * Create JSlider field and BasicSliderUI field for JSlider UI decoration.
	 */
	private static JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 0,
			10000, 0);
	private BasicSliderUI sliderUi;

	/**
	 * Create Font relevant fields for font style and pixels.
	 */
	private FontMetrics fontMetrics;
	private Font monospace;

	/**
	 * create String field for layout alignment.
	 */
	//private String alignment;

	/**
	 * Create Boolean fields for player state indication and control.
	 */
	private boolean stillParsingLyrics = true;
	private static boolean isPlaying = false;
	private boolean askForAChange = false;
	private boolean filesNotBeenInitialized = true;
	private static boolean wayPlaying = false;
	
	/**
	 * Create double field for signaling a request ranging from 0 to 1.
	 */
	private final static float defaultVal = 3.10f;
	private static double request = defaultVal;

	/**
	 * Create SourceDataLine field which decoded audio data would be written to.
	 */
	private static SourceDataLine line = null;

	/**
	 * Create Object field for thread synchronization.
	 */
	private static final Object lock = new Object();

	/**
	 * Create long field for indicating current time on slider and time label.
	 */
	private long clipStartTime;
	private long currentTimeInMicros;

	/**
	 * Create String field to represent current song name.
	 */
	private String songName = null;

	/**
	 * Create Path field to store current song path and song path of the most
	 * recently played one.
	 */
	private Path songToPlay = null;
	private Path lastPlayed = null;

	/**
	 * Create constant String array to store some quotes which would be randomly
	 * selected to show on the lyric label when the lyric is unavailable.
	 */
	private static final String[] quotes = {
			"When the solution is simple, God is answering.",
			"The greater the man, the more restrained his anger.",
			"If you do not learn to think when you are young, you may never learn.",
			"You can't move so fast that you try to change the mores faster than people can accept it. That doesn't mean you don't do anything, but it means that you do the things that need to be done according to priority.",
			"I was taught that the way of progress is neither swift nor easy." };

	// serial number
	private static final long serialVersionUID = 3313777002045451998L;

	/**
	 * Create ArrayList of Class Path to store song paths that have been played
	 * at least once.
	 */
	private ArrayList<Path> songsHavePlayed;

	/**
	 * Create constant String to represent the initial content of time label.
	 */
	private static final String timeLabelInit = " " + '\u264f' + " " + "00:00"
			+ " " + '\u264f' + " ";

	/**
	 * Create Logger field for logging.
	 */
	private static final Logger logger = Logger.getLogger(GUISynthesiszer.class
			.getName());

	/**
	 * Some fields used to ease loop calculation.
	 */
	private double sampleRateReciprocal;
	private double totalSampleReciprocal;
	private double oneInMillion;

	/**
	 * Create GUISynthesiszer field.
	 */
	private final static GUISynthesiszer synthesiszer = new GUISynthesiszer();

	/**
	 * Create a constant instance of <code>AudioParser</code> and
	 * <code>LyricsParsesr</code>.
	 */
	private final static AudioParser audioParser = synthesiszer.new AudioParser();
	private final static LyricsParser lyricsParser = synthesiszer.new LyricsParser();
	private static Thread audioExecutor;
	private static Thread lyricsExecutor;
	private static boolean requestConsumed = false;

	private GUISynthesiszer() {
		super();
		this.songsHavePlayed = new ArrayList<Path>();
		this.songToPlay = FileExplorer.songMenu(songsHavePlayed);
		if (songToPlay != null) {
			this.songName = FileExplorer.getSongName(songToPlay);
			if (!this.askForAChange) {
				this.askForAChange = !this.askForAChange;
			}
		} 
		this.lastPlayed = this.songToPlay;
		
		playNextSong = false;
		playPreviousSong = false;
		stopPlaying = false;
		this.setSize(new Dimension(629, 245));
		// Disable resizing.
		this.setResizable(false);
		
		// Initialize layout components.
		monospace = new Font("Monospace", Font.TRUETYPE_FONT, 13);
		sliderUi = new MetalSliderUI();
		sliderUi.installUI(slider);
		slider.setUI(sliderUi);
		
		slider.setEnabled(false);
		
		preIcon = new ImageIcon(getClass().getResource("/images/previous.png"));
		startIcon = new ImageIcon(getClass().getResource("/images/play.png"));
		nextIcon = new ImageIcon(getClass().getResource("/images/next.png"));
		pauseIcon = new ImageIcon(getClass().getResource("/images/pause.png"));
		stopIcon = new ImageIcon(getClass().getResource("/images/stop.png"));
		pandaIcon = new ImageIcon(getClass().getResource("/images/panda.png"));
		openIcon = new  ImageIcon(getClass().getResource("/images/open.png"));
		
		pandaButton = new JButton();
		preButton = new JButton();
		playButton = new JButton();
		nextButton = new JButton();
		stopButton = new JButton();
		openButton = new JButton();

		// set preferred dimensions for the buttons.
		pandaButton.setPreferredSize(new Dimension(60, 60));
		playButton.setPreferredSize(new Dimension(111, 38));
		preButton.setPreferredSize(new Dimension(111, 38));
		nextButton.setPreferredSize(new Dimension(111, 38));
		stopButton.setPreferredSize(new Dimension(111, 38));
		openButton.setPreferredSize(new Dimension(111, 38));
		preButton.setIcon(preIcon);
		playButton.setIcon(startIcon);
		nextButton.setIcon(nextIcon);
		stopButton.setIcon(stopIcon);
		pandaButton.setIcon(pandaIcon); 
		openButton.setIcon(openIcon);
		// to hinder the extra area excluded in the icon from being
		// highlighted when doing mouse click.
		int delta = 6;
		preButton.setContentAreaFilled(false);
		preButton.setBounds(15, 160, 111, 38);
		playButton.setContentAreaFilled(false);
		playButton.setBounds(126+delta, 160, 111, 38);
		nextButton.setContentAreaFilled(false);
		nextButton.setBounds(126+111+delta*2, 160, 111, 38);
		stopButton.setContentAreaFilled(false); 
		stopButton.setBounds(238+111+delta*3, 160, 111, 38);
		openButton.setContentAreaFilled(false);
		openButton.setBounds(238+111+111+delta*4, 160, 111, 38);
	    pandaButton.setContentAreaFilled(true);
		pandaButton.setBounds(10, 10, 60, 60);
		
		lyricsLabelPre = new JLabel();
		lyricsLabelPre.setBounds(80,10,510, 20);
		lyricsLabelPre.setBorder(BorderFactory.createDashedBorder(Color.red));
		lyricsLabelMid = new JLabel();
		lyricsLabelMid.setBounds(80,30,510, 20);
		lyricsLabelMid.setBorder(BorderFactory.createDashedBorder(Color.red));
		lyricsLabelNext = new JLabel();
		lyricsLabelNext.setBounds(80,50,510, 20);
		lyricsLabelNext.setBorder(BorderFactory.createDashedBorder(Color.red));
		fontMetrics = lyricsLabelMid.getFontMetrics(monospace);
		signalUI.setBounds(10, 70, 583, 60);
		slider.setBounds(10, 120, 583, 40);
		
		// Construct UI layout
		contentPane = getContentPane();
		contentPane.setLayout(null);
		contentPane.setBackground(Color.LIGHT_GRAY);
		playButton.setBorderPainted(false);
		playButton.setBackground(Color.LIGHT_GRAY);
		stopButton.setBorderPainted(false);
		stopButton.setBackground(Color.LIGHT_GRAY);
		preButton.setBorderPainted(false);
		preButton.setBackground(Color.LIGHT_GRAY);
		nextButton.setBorderPainted(false);
		nextButton.setBackground(Color.LIGHT_GRAY);
		openButton.setBorderPainted(false);
		openButton.setBackground(Color.LIGHT_GRAY);
	    pandaButton.setBackground(Color.LIGHT_GRAY);
		slider.setBackground(Color.LIGHT_GRAY);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lyricsLabelPre.setFont(monospace);
		lyricsLabelPre.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lyricsLabelMid.setFont(monospace);
		lyricsLabelMid.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lyricsLabelNext.setFont(monospace);
		lyricsLabelNext.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lyricsLabelMid.setForeground(new Color(0x6f5d92));
		lyricsLabelPre.setForeground(new Color(0x6f5d92));
		lyricsLabelNext.setForeground(new Color(0x6f5d92));
		
		setPrelableText(quotes[(int) (quotes.length * Math.random())]);
		setMidlableText(quotes[(int) (quotes.length * Math.random() )]);
		setNextlableText(quotes[(int) (quotes.length * Math.random())]);
		contentPane.add(lyricsLabelPre);
		contentPane.add(lyricsLabelMid);
		contentPane.add(lyricsLabelNext);
		contentPane.add(signalUI);
		contentPane.add(slider);
		contentPane.add(preButton);
		contentPane.add(playButton);
		contentPane.add(nextButton);
		contentPane.add(stopButton);
		contentPane.add(openButton);
		contentPane.add(pandaButton);

		// Add mouse listener and keyboard listener
		pandaButton.addMouseListener(this);
		preButton.addMouseListener(this);
		playButton.addMouseListener(this);
		nextButton.addMouseListener(this);
		stopButton.addMouseListener(this);
		openButton.addMouseListener(this);
		slider.addMouseListener(this);
		//contentPane.addKeyListener(this);
		//pandaButton.addKeyListener(this);
		preButton.addKeyListener(this);
		playButton.addKeyListener(this);
		nextButton.addKeyListener(this);
		stopButton.addKeyListener(this);
		openButton.addKeyListener(this);
		slider.addKeyListener(this);
		pandaButton.setFocusable(true);
		playButton.setFocusable(true);
		preButton.setFocusable(true);
		stopButton.setFocusable(true);
		nextButton.setFocusable(true);
		openButton.setFocusable(true);
		// SetsetFocusTraversalKeysEnabled for hot key settings.
		preButton.setFocusTraversalKeysEnabled(false);
		stopButton.setFocusTraversalKeysEnabled(false);
		nextButton.setFocusTraversalKeysEnabled(false);
		openButton.setFocusTraversalKeysEnabled(false);
		playButton.setFocusTraversalKeysEnabled(false);

		// Disable input methods
		contentPane.enableInputMethods(false);
		pandaButton.enableInputMethods(false);
		playButton.enableInputMethods(false);
		stopButton.enableInputMethods(false);
		preButton.enableInputMethods(false);
		nextButton.enableInputMethods(false);
		openButton.enableInputMethods(false);
		slider.enableInputMethods(false);
		
		// Set layout dimension and location on the screen
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		int screenWidth = gd.getDisplayMode().getWidth();
		int screenHeight = gd.getDisplayMode().getHeight();
		this.setLocation(screenWidth / 2 - 346, screenHeight / 2 - 256);
		contentPane.requestFocusInWindow();
		//Map<String, String> env = System.getenv();
		//for (String envName: env.keySet())
		//{
		//	System.out.format("%s=%s%n", envName, env.get(envName));
		//}
		((Frame)this).setIconImages(null);
		this.setVisible(true);
	}

	public void executor() {
		audioExecutor = new Thread(audioParser, "audioParser");
		lyricsExecutor = new Thread(lyricsParser, "lyricsParser");
	}

	/**
	 * Set the corresponding slider position based on the given double value.
	 * 
	 * @param t
	 */
	private static void setSliderPosition(final double t) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (isPlaying && !slider.getValueIsAdjusting())
					slider.setValue((int) Math.round(t * slider.getMaximum()));
			}
		});
	}

	/**
	 * Field used to ease loop calculation.
	 * 
	 * @return
	 */
	public static double getSampleRateReciprocal() {
		return synthesiszer.sampleRateReciprocal;
	}

	/**
	 * field used to ease loop calculation.
	 * 
	 * @return
	 */
	public static double getTotalSampleReciprocal() {
		return synthesiszer.totalSampleReciprocal;
	}

	/**
	 * field used to ease loop calculation.
	 * 
	 * @return
	 */
	public static double getOneInMillion() {
		return synthesiszer.oneInMillion;
	}

	/**
	 * Make GUISynthesiszer a singleton class.
	 * 
	 * @return
	 */
	public static GUISynthesiszer Initializer() {
		return synthesiszer;
	}

	public static void main(String[] args) throws IOException {
	 	logger.setLevel(Level.OFF);
		Initializer();
		Arrays.sort(new byte[]{1,34,5});
	}

	/**
	 * Check if line is playing.
	 * 
	 * @return
	 */
	public static Boolean wayPlaying() {
		return wayPlaying;
	}

	// Mouse click event handler.
	@Override
	public void mouseClicked(MouseEvent e) {
		try {
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			if (e.getSource().equals(this.nextButton)) {
				{
					Thread.sleep(50);
					playNextSong = true;
					requestHandler(next);
				}
			} else if (e.getSource().equals(this.preButton)) {
				{
					Thread.sleep(50);
					playPreviousSong = true;
					requestHandler(previous);
				}
			} else if (e.getSource().equals(this.stopButton)) {
				{
					Thread.sleep(50);
					stopPlaying = true;
					requestHandler(stop);
				}
			} else if (e.getSource().equals(this.openButton)) {
				{
					Thread.sleep(50);
					stopPlaying = true;
					requestHandler(open);
				}
			} else if (e.getSource().equals(this.playButton)) {
				{
					Thread.sleep(50);
					if (FormatParser.getInstance() == null) {
						askForAChange = true;
						if (isPlaying) {
							isPlaying = !isPlaying;
						}
						playOrPause = true;
						requestHandler(playOrNot);
						try {
							Thread.sleep(300);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					} else {
						if (askForAChange == true)
							playOrPause = true;
						else {
							playOrPause = !isPlaying;
						}
						requestHandler(playOrNot);
						try {
							Thread.sleep(300);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			} else if (e.getSource().equals(this.pandaButton)) {
				while (true) {
					String rawInput = JOptionPane.showInputDialog(pandaButton,
							"Type in what you want to hear...",
							"Song Explorer", JOptionPane.INFORMATION_MESSAGE);
					if (rawInput == null) {
						logger.log(Level.WARNING,
								"The input in not valid, please check again.");
						return;
					} else {
						Path searchRes = FileExplorer.fileSearcher(rawInput);
						Thread.sleep(100);
						if (searchRes == null) {
							int answer = JOptionPane
									.showConfirmDialog(
											pandaButton,
											"No matched song has been found. Want next search?",
											"Search Results",
											JOptionPane.YES_NO_OPTION,
											JOptionPane.ERROR_MESSAGE);
							if (answer == JOptionPane.YES_OPTION) {
								FileExplorer.refreshDirectory();
								Thread.sleep(10);
								continue;
							} else
								return;
						} else {
							int reply = JOptionPane
									.showConfirmDialog(
											pandaButton,
											"Matched song has been found: "
													+ FileExplorer
															.getSongName(searchRes)
													+ "\n \"YES\" to start play, \"NO\" to start next search, \"CANCEL\" to quit searching.",
											"Search Results",
											JOptionPane.YES_NO_CANCEL_OPTION,
											JOptionPane.QUESTION_MESSAGE);
							if (reply == JOptionPane.YES_OPTION) {
								if (!isPlaying) {
									isPlaying = !isPlaying;
								}
								stopPlaying = true;
								requestHandler(stop);
								Thread.sleep(300);
								songToPlay = searchRes.toAbsolutePath();
								lastPlayed = songToPlay;
								askForAChange = true;
								if (!songsHavePlayed.contains(songToPlay))
									songsHavePlayed.add(songToPlay);
								playOrPause = true;
								requestHandler(playOrNot);
								Thread.sleep(300);
								break;
							} else if (reply == JOptionPane.NO_OPTION) {
								Thread.sleep(100);
								continue;
							} else
								break;
						}
					}
				}
			} else if (e.getSource().equals(slider)) {
				if (slider.isEnabled()) {
					synchronized (lock) {
						double temp = (double) sliderUi.valueForXPosition(e
								.getX()) / slider.getMaximum();
						if (temp < 0)
							request = 0;
						else if (temp >= 1.0) {
							lock.notify();
							return;
						} else
							request = temp;
						lock.notify();
					}
					logger.log(Level.INFO, "Jumping to the selected position.");
				}
			}

		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		return;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getSource().equals(slider)) {
			if (slider.isEnabled()) {
				synchronized (lock) {
					if (isPlaying) {
						isPlaying = !isPlaying;
					}
					double temp = (double) sliderUi.valueForXPosition(e.getX())
							/ slider.getMaximum();
					if (temp < 0)
						request = 0;
					else if (temp >= 1.0) {
						lock.notify();
						return;
					} else
						request = temp;
					if (!isPlaying) {
						isPlaying = !isPlaying;
					}
					lock.notify();
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		return;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		return;
	}

	/**
	 * Create a method to package and handle different requests for code
	 * simplicity and reuse. There are five different requests, they are as
	 * follows: 1. play next song; 2. play previous song; 3. play or pause; 4.
	 * stop playing; 5. Open file directory.
	 * 
	 * @author JoySanctuary
	 *
	 */
	private void requestHandler(String option) {
		// The result of the constructor would depend on the given String
		// parameter.
		try {
			switch (option) {
			case open:
				FileExplorer.EXPLORER.OpenLocation();
				songsHavePlayed = new ArrayList<Path>();
				songToPlay = FileExplorer.songMenu(songsHavePlayed);
				
				if (songToPlay != null)  this.requestHandler(next);
				break;
			case next:
				if (songToPlay == null) return;
				logger.log(Level.INFO, "Switching to next song.");
				preConditionHandler();
				if (playNextSong) {
					playNextSong = !playNextSong;
				}
				if (songsHavePlayed.indexOf(lastPlayed) == (songsHavePlayed
						.size() - 1)) {
					songToPlay = FileExplorer.songMenu(songsHavePlayed);
					if (songToPlay == null) {
						songToPlay = songsHavePlayed.get(0);
					} 
				} else
					songToPlay = songsHavePlayed.get(songsHavePlayed
							.indexOf(lastPlayed) + 1);
				postConditionHandler();
				break;
			// handling play previous song
			case previous:
				
				logger.log(Level.INFO, "Start playing previous audio.");
				preConditionHandler();
				if (playPreviousSong) {
					playPreviousSong = !playPreviousSong;
				}
				if (songsHavePlayed.indexOf(lastPlayed) > 0) {
					songToPlay = songsHavePlayed.get(songsHavePlayed
							.indexOf(lastPlayed) - 1);
					lastPlayed = songToPlay;
				} else {
					songToPlay = lastPlayed;
					if (!stopPlaying) {
						stopPlaying = !stopPlaying;
					}
					playButton.setIcon(startIcon);
					requestHandler(stop);
					return;
				}
				postConditionHandler();
				break;
			case playOrNot:
				if (songToPlay == null) return;
				if (stopPlaying) {
					stopPlaying = !stopPlaying;
				}
				if (songToPlay != null)howToPlay();
				break;
			case stop:
				logger.log(Level.INFO, "Stop current audio.");
				askForAChange = true;
				setPrelableText(quotes[0]);
				setMidlableText(quotes[0]);
				setNextlableText(quotes[0]);
				synthesiszer.setTitle(quotes[0]);
				Thread.sleep(10);
				// reset GUI outlook.
				FormatParser.closeFile();
				slider.setValue(slider.getMinimum());
				slider.setEnabled(false);
				if (stopPlaying) {
					stopPlaying = !stopPlaying;
				}
				break;
			default:
				break;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pre-handler for requests.
	 */
	private void preConditionHandler() {
		try {
			logger.log(Level.INFO, "Preconditioning for switching songs.");
			askForAChange = true;
			if (stopPlaying) {
				stopPlaying = !stopPlaying;
			}
			if (line != null) {
				line.drain();
				line.close();
				line = null;
			}
			Thread.sleep(50);
			FormatParser.closeFile();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Post-condition handler for requests.
	 */
	private void postConditionHandler() {
		try {
			if (songToPlay != null) {
				FormatParser.createInstance(songToPlay);
				lastPlayed = songToPlay;
			} else {
				logger.log(Level.INFO,
						"New song path is invalid, use most recently played one instead.");
				songToPlay = lastPlayed;
				if (!stopPlaying) {
					stopPlaying = !stopPlaying;
				}
				playButton.setIcon(startIcon);
				logger.log(Level.INFO,
						"Stop playing when current song is the most recently played one by default.");
				requestHandler(stop);
				return;
			}
			if (!songsHavePlayed.contains(songToPlay)) {
				logger.log(Level.INFO, "Add new song path to songsHavePlayed.");
				songsHavePlayed.add(songToPlay);
			}
			logger.log(Level.INFO, "Reset the boolean state of isPlaying.");
			if (isPlaying) {
				isPlaying = !isPlaying;
			}
			if (!isPlaying) {
				isPlaying = !isPlaying;
				playButton.setIcon(pauseIcon);
				stillParsingLyrics = true;
				logger.log(Level.INFO,
						"Executing two threads, one for audio, the other lyrics.");
				executor();
				audioExecutor.start();
				lyricsExecutor.start();
			}
		} catch (IOException | BadFileFormatException | JavaLayerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Decide how to play the audio based on given file extension.
	 * 
	 * @param fileExtension
	 */
	private void howToPlay() {
		try {
			if (FormatParser.getInstance() == null) {
				askForAChange = true;
				//timeLabel.setText(alignment + timeLabelInit);
				logger.log(Level.INFO, "Creating an instance of decoder.");
				FormatParser.createInstance(songToPlay);
				logger.log(Level.INFO,
						"Set current song to play as the most recently played one.");
				if (lastPlayed != songToPlay) {
					lastPlayed = songToPlay;
				}
				Thread.sleep(200);
				if (playOrPause) {
					logger.log(Level.INFO, "Start playing audio.");
					executor();
					if (askForAChange) {
						stillParsingLyrics = true;
						audioExecutor.start();
						isPlaying = true;
					}
					isPlaying = playOrPause;
					lyricsExecutor.start();
					playButton.setIcon(pauseIcon);
				} else {
					logger.log(Level.INFO, "Pause current audio.");
					isPlaying = playOrPause;
					playButton.setIcon(startIcon);
				}
			} else {
				if (playOrPause) {
					if (askForAChange) {
						logger.log(Level.INFO, "Start playing audio.");
						//timeLabel.setText(alignment + timeLabelInit);
						Thread.sleep(200);
						stillParsingLyrics = true;
						logger.log(Level.INFO,
								"Executing two threads, one for audio, the other lyrics.");
						executor();
						audioExecutor.start();
						lyricsExecutor.start();
					}
					isPlaying = playOrPause;
					playButton.setIcon(pauseIcon);
				} else {
					logger.log(Level.INFO, "Pause current audio.");
					isPlaying = playOrPause;
					playButton.setIcon(startIcon);
				}
			}
		} catch (InterruptedException | BadFileFormatException | IOException
				| JavaLayerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize current SourceDataLine field with given parameters.
	 * 
	 * @param frequency
	 * @param sampleDepth
	 * @param channels
	 * @param signed
	 * @param bigEndian
	 * @throws LineUnavailableException
	 */
	protected static void sourceDataLineInitializer(int frequency,
			int sampleDepth, int channels, Boolean signed, Boolean bigEndian)
			throws LineUnavailableException {
		logger.log(Level.INFO, "Initializing SourceDataLine.");
		AudioFormat format = new AudioFormat(frequency, sampleDepth, channels,
				signed, bigEndian);
		line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(
				SourceDataLine.class, format));
		line.open(format);
		line.start();
	}

	public static SourceDataLine getSourceDataLine() {
		return line;
	}

	/**
	 * Change Icon whenever <code>isPlaying</code> state has been changed.
	 * 
	 * @param toPlay
	 * @throws InterruptedException
	 */
	public static void changeIcon(Boolean toPlay) throws InterruptedException {
		if (toPlay) {
			if (!synthesiszer.playButton.getIcon().equals(
					synthesiszer.pauseIcon)) {
				Thread.sleep(250);
				synthesiszer.playButton.setIcon(synthesiszer.pauseIcon);
			}
		} else {
			if (!synthesiszer.playButton.getIcon().equals(
					synthesiszer.startIcon)) {
				Thread.sleep(250);
				synthesiszer.playButton.setIcon(synthesiszer.startIcon);
			}
		}
	}

	/**
	 * Change clipStartTime to the proper position when any request occurs.
	 * 
	 * @param clipStartTime
	 */
	public static void setClipStartTime(long time) {
		synthesiszer.clipStartTime = time;
	}

	/**
	 * An audio handler for decoding and playing audio data.
	 * 
	 * @author JoySanctuary
	 *
	 */
	private class AudioParser implements Runnable {

		/**
		 * A field which is intended for cover the issue of inaccurate
		 * calculation of audio length. When hasNotReachedEnd turned false,
		 * streaming will end.
		 */
		private boolean hasNotReachedEnd = true;
		private double audioLength;

		protected AudioParser() {

		}

		@Override
		public void run() {
			logger.log(Level.INFO, "Current song to play is " + songToPlay);
			if (!FormatParser.isReady()) {
				logger.log(Level.INFO,
						"Currently decoder is not ready, thread is quitting.");
				stopPlaying = true;
				requestHandler(stop);
				return;
			}
			try {
				clipStartTime = 0;
				sourceDataLineInitializer(FormatParser.getSampleRateInHz(),
						FormatParser.getBitsPerSample(),
						FormatParser.getNumOfChannels(), true, false);
				audioLength = FormatParser.getAudioLength();
				if (filesNotBeenInitialized) {
					logger.log(Level.INFO,
							"Cancel wrong state since the stream has been initialized properly.");
					filesNotBeenInitialized = !filesNotBeenInitialized;
				}
			} catch (IllegalArgumentException ex) {
				JOptionPane.showMessageDialog(null, "Cannot play \""
						+ songToPlay.getFileName()
						+ "\" on your current OS.\nSample rate: "
						+ FormatParser.getSampleRateInHz() + ", sample depth: "
						+ FormatParser.getBitsPerSample() + ", channels: "
						+ FormatParser.getNumOfChannels(),
						"Initialization Failed", JOptionPane.ERROR_MESSAGE);
				filesNotBeenInitialized = true;
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			if (filesNotBeenInitialized) {
				logger.log(Level.WARNING,
						"Stream has failed to initialize, thread is quitting.");
				stopPlaying = true;
				requestHandler(stop);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				playButton.setIcon(startIcon);
				logger.log(Level.INFO,
						"Reset state of filesNotBeenInitialized for further use.");
				filesNotBeenInitialized = !filesNotBeenInitialized;
				return;
			}
			if (askForAChange) {
				logger.log(Level.INFO, "Cancel the signaling state.");
				askForAChange = !askForAChange;
			}
			try {
				oneInMillion = 1e0 / 1e6;
				logger.log(Level.INFO,
						"Prepare some parameters for decoding and playing streaming.");
				sampleRateReciprocal = 1e0 / FormatParser.getSampleRateInHz();
				totalSampleReciprocal = songToPlay.toString().endsWith(".flac") ? 1e0 / FormatParser
						.getInstance().getTotalSamplesInStream() : 0;
				if (FormatParser.getSampleRateInHz() == 0) {
					logger.log(Level.INFO,
							"Sampling rate is invalid for streaming, thread is quitting.");
					return;
				}
				if (FormatParser.getSampleRateInHz() > 44100) {
					Thread.sleep(500);
				}
				while (!askForAChange) {
					while (stillParsingLyrics) {
						Thread.sleep(1);
						// For reset the index of <code>FileExplorer</code>
					}
					if (FormatParser.getInstance() == null) {
						logger.log(Level.INFO,
								"Streaming instance is null ,thread is quitting.");
						break;
					}
					if (playNextSong) {
						if (!requestConsumed) {
							requestConsumed = !requestConsumed;
						}
						break;
					} else if (playPreviousSong) {
						if (!requestConsumed) {
							requestConsumed = !requestConsumed;
						}
						break;
					} else if (stopPlaying) {
						requestHandler(stop);
						break;
					} else if (!isPlaying) {
						Thread.sleep(1);
						continue;
					}
					hasNotReachedEnd = FormatParser.sampleProcessor(request);
					currentTimeInMicros = line.getMicrosecondPosition()
							- clipStartTime;
					if (FormatParser.getInstance() != null) {
						if (currentTimeInMicros * oneInMillion >= audioLength - 1.10f
								|| !hasNotReachedEnd) {
							int time = 1101;
							while (time > 0) {
								if (askForAChange) {
									line.drain();
									line.close();
									line = null;
									return;
								}
								Thread.sleep(1);
								time -= 1;
								currentTimeInMicros += 1e3;
							}
							playNextSong = true;
							requestHandler(next);
							return;
						}
						setSliderPosition(FormatParser.getCurrentPosition());
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Get current time in microseconds from SourceDataLine.
	 * 
	 * @return time in microseconds
	 */
	public static long getCurrentTimeInMicros() {
		return synthesiszer.currentTimeInMicros;
	}

	/**
	 * Consume request when request has been processed once.
	 */
	public static void consumeRequest() {
		if (request < defaultVal) {
			request = defaultVal;
			requestConsumed = true;
		}
	}

	/**
	 * Check if request has been consumed.
	 * 
	 * @return the state of <code>requestConsumed</code>
	 */
	public static Boolean hasRequestConsumed() {
		return requestConsumed;
	}

	/**
	 * Clean the state of <code>requestConsumed</code>
	 */
	public static void confirmRequestConsumed() {
		if (requestConsumed) {
			requestConsumed = !requestConsumed;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int id = e.getID();
		String keyString;
		if (id == KeyEvent.KEY_TYPED) {
			char c = e.getKeyChar();
			keyString = "Key character = '" + c + "'";
		} else {
			int keyCode = e.getKeyCode();
			keyString = "Key code = " + keyCode + "("
					+ KeyEvent.getKeyText(keyCode) + ")";
		}
		int modifierEx = e.getModifiersEx();
		String modString = "extended modifiers = " + modifierEx;
		String tmpString = KeyEvent.getModifiersExText(modifierEx);
		if (tmpString.length() > 0) {
			modString += "(" + tmpString + ")";
		} else {
			modString += "(no extended modifiers)";
		}

		String actionString = "action key?";
		if (e.isActionKey()) {
			actionString += "YES";
		} else {
			actionString += "NO";
		}

		String locationString = "key location: ";
		int location = e.getKeyLocation();
		if (location == KeyEvent.KEY_LOCATION_STANDARD) {
			locationString += "standard";
		} else if (location == KeyEvent.KEY_LOCATION_LEFT) {
			locationString += "left";
		} else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
			locationString += "right";
		} else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
			locationString += "numpad";
		} else {
			locationString += "unknown";
		}
		logger.log(Level.FINE, keyString + "\t" + modString + "\t"
				+ actionString + "\t" + locationString);

		if (e.getKeyCode() == 39 && e.getModifiersEx() == 128
				|| e.getKeyCode() == 39 && e.getModifiersEx() == 512
				|| e.getKeyCode() == 39 && e.getModifiersEx() == 64) {
			nextButton.requestFocusInWindow();
			playNextSong = true;
			requestHandler(next);
		} else if (e.getKeyCode() == 37 && e.getModifiersEx() == 128
				|| e.getKeyCode() == 37 && e.getModifiersEx() == 512
				|| e.getKeyCode() == 37 && e.getModifiersEx() == 64) {
			preButton.requestFocusInWindow();
			playPreviousSong = true;
			requestHandler(previous);
		} else if (e.getKeyCode() == 32) {
			playButton.requestFocusInWindow();
			if (FormatParser.getInstance() == null) {
				askForAChange = true;
				if (isPlaying) {
					isPlaying = !isPlaying;
				}
				playOrPause = true;
				requestHandler(playOrNot);
				try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} else {
				if (askForAChange == true)
					playOrPause = true;
				else {
					playOrPause = !isPlaying;
				}
				requestHandler(playOrNot);
				try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		} else if (e.getKeyCode() == 27 && e.getModifiersEx() == 0) {
			if (getState() != JFrame.ICONIFIED)
				this.setState(JFrame.ICONIFIED);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	/**
	 * Set text of <code>lyricsLabelPre</code>
	 * 
	 * @param text
	 */
	public static void setPrelableText(String text) {
		lyricsLabelPre.setText(text);
	}

	/**
	 * Set text of <code>lyricsLabelMid</code>
	 * 
	 * @param text
	 */
	public static void setMidlableText(String text) {
		lyricsLabelMid.setText(text);
	}

	/**
	 * Set text of <code>lyricsLabelNext</code>
	 * 
	 * @param text
	 */
	public static void setNextlableText(String text) {
		lyricsLabelNext.setText(text);
	}

	/**
	 * The LyricsParser mains has two functionalities: 1. to parse the lyrics if
	 * the given LRC file path exists; 2: Match what lyrics to show on the
	 * labels based on the current position in audio data of the SourceDataLine
	 * instance.
	 * 
	 * @author JoySanctuary
	 *
	 */
	private class LyricsParser implements Runnable {
		private TreeMap<Float, String> lyricDict = new TreeMap<Float, String>();
		private Float[] frameKeys;
		private FontMetrics fMetrics;
		private int timer;
		private String LastTimeFrame;
		private int idx;

		protected LyricsParser() {

		}

		@Override
		public void run() {
			lyricDict = null;
			logger.log(Level.INFO,
					"Constructing new thread for parsing lyrics.");
			Path lrcPath = FileExplorer.getLRCFilePath(songToPlay);
			songName = FileExplorer.getSongName(songToPlay);
			LastTimeFrame = timeLabelInit;
			fMetrics = lyricsLabelPre.getFontMetrics(monospace);
			if (fMetrics.stringWidth(songName) > 230) {
				synthesiszer.setTitle('\u264f' + " "
						+ songName.substring(songName.indexOf('-') + 1) + " "
						+ '\u264f');
			} else
				synthesiszer.setTitle('\u264f' + " " + songName + " "
						+ '\u264f');
			songName += "   ";
			if (lrcPath != null) {
				lyricDict = FileExplorer.lrcParser(lrcPath);
				// Resort time frames in ascending order.
				frameKeys = new Float[lyricDict.size()];
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lyricDict.navigableKeySet().toArray(frameKeys);
			} else {
				frameKeys = null;
				logger.log(Level.INFO,
						"Cannot find the lyrics for current song.");
				stillParsingLyrics = false;
				timer = 0;
				fMetrics = lyricsLabelPre.getFontMetrics(monospace);
				setPrelableText(quotes[(int) (quotes.length * Math.random())]);
				setMidlableText(quotes[(int) (quotes.length * Math.random())]);
				setNextlableText(quotes[(int) (quotes.length * Math.random())]);
			}
			slider.setEnabled(true);
			int unshownLenMid = 0;
			int unshownLenPrev = 0;
			int unshownLenNext = 0;
			String midText = null;
			String prevText = null;
			String nextText = null;
			int lenInPixelsMid = -1;
			int lenInPixelsPrev = -1;
			int lenInPixelsNext = -1;
			int midIdx = 0;
			int prevIdx = 0;
			int nextIdx = 0;
			midText = lyricsLabelMid.getText();
			prevText = lyricsLabelPre.getText();
			nextText = lyricsLabelNext.getText();
			lenInPixelsPrev = fontMetrics.stringWidth(prevText);
			lenInPixelsMid = fontMetrics.stringWidth(midText);
			lenInPixelsNext = fontMetrics.stringWidth(nextText);
			if (lenInPixelsMid > 582) {
				unshownLenMid = 1;
				midText += " ";
				midText += midText;
			}
			if (lenInPixelsPrev > 582) {
				unshownLenPrev = 1;
				prevText += " ";
				prevText += prevText;
			}
			if (lenInPixelsNext > 582) {
				unshownLenNext = 1;
				nextText += " ";
				nextText += nextText;
			}
			if (askForAChange) {
				askForAChange = !askForAChange;
			}
			stillParsingLyrics = false;
			if (!requestConsumed) {
				requestConsumed = !requestConsumed;
			}
			while (!playNextSong && !playPreviousSong && !stopPlaying
					&& !askForAChange) {
				try {
					if (FormatParser.getInstance() == null) {
						break;
					}
					if (getState() != Frame.ICONIFIED) {
						if (frameKeys == null) {
							Thread.sleep(50);
							if (isPlaying) {
								timer += 20;
								if (timer % 200 == 0) {
									if (unshownLenMid > 0) {
										setMidlableText('\u266A'
												+ " "
												+ midText.substring(midIdx,
														midIdx + 80) + " "
												+ '\u266A');
										midIdx += 1;
									}
									if (unshownLenPrev > 0) {
										setPrelableText('\u266A'
												+ " "
												+ prevText.substring(prevIdx,
														prevIdx + 80) + " "
												+ '\u266A');
										prevIdx += 1;
									}
									if (unshownLenNext > 0) {
										setNextlableText('\u266A'
												+ " "
												+ nextText.substring(nextIdx,
														nextIdx + 80) + " "
												+ '\u266A');
										nextIdx += 1;
									}
								}
								if (midIdx > (midText.length() - 1) / 2) {
									midText = midText.substring((midText
											.length() - 1) / 2 + 1);
									midText += midText;
									midIdx = 0;
								}
								if (prevIdx > (prevText.length() - 1) / 2) {
									prevText = prevText.substring((prevText
											.length() - 1) / 2 + 1);
									prevText += prevText;
									prevIdx = 0;
								}
								if (nextIdx > (nextText.length() - 1) / 2) {
									nextText = nextText.substring((nextText
											.length() - 1) / 2 + 1);
									nextText += nextText;
									nextIdx = 0;
								}
							}
						} else {
							// Pass by value has significantly avoided
							// NullPointerException.
							if (lyricDict == null) {
								break;
							}
							if (hasRequestConsumed()) {
								confirmRequestConsumed();
								idx = 0;
							}
							FileExplorer.lyricsReader(lyricDict, frameKeys,
									currentTimeInMicros, songName, idx);
						}
						// without any interruption
						long min = (long) (currentTimeInMicros / 1e6 / 60);
						long s = (long) (currentTimeInMicros / 1e6) % 60;
						if (s < 10) {
							if (min < 10) {
								if (!LastTimeFrame.equals("0" + min + ":0" + s)) {
									LastTimeFrame = "0" + min + ":0" + s;
									//timeLabel.setText(alignment + " "
										//	+ '\u264f' + " " + LastTimeFrame
											//+ " " + '\u264f' + " ");
								}
							} else {
								if (!LastTimeFrame.equals(min + ":0" + s)) {
									LastTimeFrame = min + ":0" + s;
									//timeLabel.setText(alignment + " "
										//	+ '\u264f' + " " + LastTimeFrame
											//+ " " + '\u264f' + " ");
								}
							}
						} else {
							if (min < 10) {
								if (!LastTimeFrame.equals("0" + min + ":" + s)) {
									LastTimeFrame = "0" + min + ":" + s;
									//timeLabel.setText(alignment + " "
										//	+ '\u264f' + " " + LastTimeFrame
											//+ " " + '\u264f' + " ");
								}
							} else {
								if (!LastTimeFrame.equals(min + ":" + s)) {
									LastTimeFrame = min + ":" + s;
									//timeLabel.setText(alignment + " "
										//	+ '\u264f' + " " + LastTimeFrame
											//+ " " + '\u264f' + " ");
								}
							}
						}
					} else {
						Thread.sleep(500);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			setPrelableText(quotes[0]);
			setMidlableText(quotes[0]);
			setNextlableText(quotes[0]);
			synthesiszer.setTitle(quotes[0]);
			playButton.setIcon(startIcon);
			slider.setValue(slider.getMinimum());
		}
	}
}
