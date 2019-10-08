package gui.pack;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import biorobot.data.SystemParametersData;
import biorobot.pack.ActionProcessor;
import biorobot.pack.OperationProcessor;
import biorobot.pack.SystemParameters;

/**
 * The main class of the software.
 * Initializes the GUI and starts the software.
 * @author maciej.wojciga
 */

public class MainGUI extends JFrame {

	private static final long serialVersionUID = 1L;

	public static Logger logger = Logger.getLogger(MainGUI.class);

	public static MainGUI mainGUIfrm;
	public JPanel mainPane;

	// Visible fields.
	public JLabel lblTotaltimelbl;
	public JLabel lblLoadedroutelbl;
	public JLabel lblConnectedtolbl;
	public JLabel lblActionlbl;
	public JTextArea routeTextArea;

	// Other
	File loadedFile = null;
	Properties confProperties;

	OperationProcessor operationProcessor = null;
	ActionProcessor actionProcessor = null;
	SystemParametersData systemParametersData = new SystemParametersData();

	private void createObjects() {
		operationProcessor = new OperationProcessor(this);
		actionProcessor = new ActionProcessor(this);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainGUIfrm = new MainGUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public MainGUI() {
		configureProperties();
		initialize();
		setVisible(true);
		createObjects();
		operationProcessor.searchForPorts();
		checkIfFirstLaunch();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws ParseException 
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		Image icon = Toolkit.getDefaultToolkit().getImage("./img/imim_logo.gif");
		setIconImage(icon);
		//setTitle("ShearRobot v." + confProperties.getProperty("version"));
		setBounds(01000, 01000, 750, 318);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		/* MENU */
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmLoad = new JMenuItem("Load...");
		mnFile.add(mntmLoad);

		JMenuItem mntmConnect = new JMenuItem("Connect...");
		mnFile.add(mntmConnect);

		JMenuItem mntmDisconnect = new JMenuItem("Disconnect");
		mnFile.add(mntmDisconnect);

		JMenuItem mntmPreferences = new JMenuItem("Preferences");
		mntmPreferences.setEnabled(false);
		mnFile.add(mntmPreferences);

		JMenuItem mntmAbout = new JMenuItem("About");
		mnFile.add(mntmAbout);

		JSeparator separator = new JSeparator();
		mnFile.add(separator);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);

		/* MAIN FRAME */
		mainPane = new JPanel();
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		mainPane.setLayout(null);
		mainPane.setEnabled(false);

		JButton btnStart = new JButton("Start");
		btnStart.setBounds(645, 238, 89, 23);
		mainPane.add(btnStart);

		JButton btnStop = new JButton("Stop");
		btnStop.setBounds(546, 238, 89, 23);
		mainPane.add(btnStop);

		routeTextArea = new JTextArea();
		routeTextArea.setEditable(false);
		routeTextArea.setBounds(372, 11, 362, 216);
		mainPane.add(routeTextArea);

		JLabel lblConnectedTo = new JLabel("Connected to:");
		lblConnectedTo.setBounds(10, 11, 69, 14);
		mainPane.add(lblConnectedTo);

		JLabel lblLoadedRoute = new JLabel("Loaded route:");
		lblLoadedRoute.setBounds(10, 36, 69, 14);
		mainPane.add(lblLoadedRoute);

		JLabel lblTotalTime = new JLabel("Total time:");
		lblTotalTime.setBounds(10, 61, 69, 14);
		mainPane.add(lblTotalTime);

		lblTotaltimelbl = new JLabel("");
		lblTotaltimelbl.setEnabled(false);
		lblTotaltimelbl.setBounds(89, 61, 112, 14);
		mainPane.add(lblTotaltimelbl);

		lblLoadedroutelbl = new JLabel("");
		lblLoadedroutelbl.setEnabled(false);
		lblLoadedroutelbl.setBounds(89, 36, 112, 14);
		mainPane.add(lblLoadedroutelbl);

		lblConnectedtolbl = new JLabel("");
		lblConnectedtolbl.setEnabled(false);
		lblConnectedtolbl.setBounds(89, 11, 112, 14);
		mainPane.add(lblConnectedtolbl);

		lblActionlbl = new JLabel("");
		lblActionlbl.setBounds(10, 242, 352, 14);
		mainPane.add(lblActionlbl);

		/* Actions */
		mntmConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				mntmConnectActionPerformed(event);
			}
		});

		mntmDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				mntmDisconnectActionPerformed(event);
			}
		});
		
		mntmLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				mntmLoadActionPerformed(event);
			}
		});
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				btnStartActionPerformed(event);
			}
		});
		
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				btnStopActionPerformed(event);
			}
		});
		
		mntmAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(mainGUIfrm, "ShearRobot v" + confProperties.getProperty("version") + "\nWritten by: " + confProperties.getProperty("author") + "\n\nCooperator: dr. Roman Major\nInstitute of Metallurgy and Materials Science\nPolish Academy of Sciences", "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	/* Action methods */
	private void mntmDisconnectActionPerformed(ActionEvent event) {
		logger.trace("[C]: mntmDisconnect");
		operationProcessor.disconnect();
	}
	
	private void btnStartActionPerformed(ActionEvent event) {
		operationProcessor.useLoadedFile(loadedFile);
	}
	
	private void btnStopActionPerformed(ActionEvent event) {
		operationProcessor.writeData(0, 0, 0, 1);
	}

	private void mntmLoadActionPerformed(ActionEvent event) {
		logger.trace("[C]: mntmLoad");
		if (loadedFile == null) {
			logger.debug("There wasn't any route loaded before.");
			loadedFile = actionProcessor.loadFile();
		} else {
			int confirmNewRoute = JOptionPane.showConfirmDialog(mainGUIfrm, "Another route was loaded before.\nDo you want to replace it with a new one?", "Note", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (confirmNewRoute == 0) {
				logger.debug("There was a route file loaded before: " + loadedFile.getAbsolutePath());
				loadedFile = null;
				loadedFile = actionProcessor.loadFile();
				logger.debug("Replaced route with a new one: " + loadedFile.getAbsolutePath());
			}
		}
	}

	private void mntmConnectActionPerformed(ActionEvent event) {
		logger.trace("[C]: mntmConnect");
		ArrayList<String> availableCommPorts = operationProcessor.searchForPorts();
		String[] possibilities = availableCommPorts.toArray(new String[availableCommPorts.size()]);
		String selectedPort = (String)JOptionPane.showInputDialog(mainGUIfrm, "Connect to:", "Connect", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);
		if (selectedPort != null) {
			operationProcessor.connect(selectedPort);
			if (operationProcessor.isConnectedToPort() == true)
			{
				if (operationProcessor.initIOStream() == true)
				{
					operationProcessor.initListener();
				}
			}
		}
	}

	/* Other methods */
	public void configureProperties() {
		confProperties = new Properties();
		try {
			FileInputStream propertiesFileIS =  new FileInputStream(SystemParameters.PROPERTYFILE);
			confProperties.load(propertiesFileIS);
			propertiesFileIS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkIfFirstLaunch() {
		try {
			if (confProperties.getProperty("first.time").equals("1")) {
				File changeLogFile = new File(SystemParameters.CHANGELOGFILE);
				String changelog = actionProcessor.takeFileAndWriteToString(changeLogFile, "[" + confProperties.getProperty("version") + "]");
				JOptionPane.showMessageDialog(mainGUIfrm, changelog + "\n\nNote that you can view this information later in the changelog.txt file in \"conf\" directory.", "What is new in version " + confProperties.getProperty("version") + "?", JOptionPane.INFORMATION_MESSAGE);
			}
			confProperties.setProperty("first.time", "0");
			confProperties.store(new FileOutputStream(new File(SystemParameters.PROPERTYFILE)), "Changed.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
