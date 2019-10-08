package biorobot.pack;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gui.pack.MainGUI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

import org.apache.log4j.Logger;

import biorobot.data.InputMessageData;
import biorobot.data.OutputMessageData;
import biorobot.data.SystemParametersData;
import biorobot.threads.StartRouteFromFile;

/**
 * Operations class.
 * @author maciej.wojciga
 */

public class OperationProcessor implements SerialPortEventListener {

	/* THREADS */
	StartRouteFromFile startRouteFromFileThread;

	/* LOG */
	Logger logger = Logger.getLogger(OperationProcessor.class);
	Log log;

	/* OTHER */
	MainGUI mainGUI;
	private Enumeration availablePorts = null;
	private HashMap portMap = new HashMap();
	private CommPortIdentifier selectedPortIdentifier = null;
	private SerialPort openedSerialPort = null;
	private boolean connectedToPort = false;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;

	boolean actualMS = false;
	boolean errorMS = false;
	boolean compareMS = false;

	Calculations calculations = new Calculations();
	SystemParametersData systemParametersData = new SystemParametersData();

	private boolean globalCalibrated = false;

	private String sendedMessage = "initialMessage";

	public boolean calibrateDone = false;
	public boolean shouldWait = false;

	byte[] buffer = new byte[1024];
	int bytes;
	String end = "E";
	StringBuilder curMsg = new StringBuilder();
	public String inputMessage = "";


	public OperationProcessor(MainGUI mainGUI) {
		this.mainGUI = mainGUI;
		log = new Log(mainGUI);
	}

	public ArrayList<String> searchForPorts() {
		ArrayList<String> availableCommPorts = new ArrayList<String>();;
		availablePorts = CommPortIdentifier.getPortIdentifiers();
		while (availablePorts.hasMoreElements()) {
			CommPortIdentifier currentPort = (CommPortIdentifier) availablePorts.nextElement();
			logger.info("Found port: " + currentPort.getName());
			if (currentPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				availableCommPorts.add(currentPort.getName());
				portMap.put(currentPort.getName(), currentPort);
				logger.info(currentPort.getName() + " is a serial port. Added.");
			}
		}
		return availableCommPorts;
	}

	public void connect(String selectedPort) {
		logger.info("Connecting to " + selectedPort);
		selectedPortIdentifier = (CommPortIdentifier) portMap.get(selectedPort);
		CommPort commPort = null;
		try {
			commPort = selectedPortIdentifier.open("ShearRobot", SystemParameters.TIMEOUT);
			openedSerialPort = (SerialPort) commPort;
			openedSerialPort.setSerialPortParams(SystemParameters.DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			connectedToPort = true;
			mainGUI.lblConnectedtolbl.setText(selectedPort);
			log.log("Successfully connected to " + commPort.getName() + ".", null);
			logger.info("Successfully connected to " + commPort.getName());
		} catch (PortInUseException e) {
			log.log("Could not connect: port is already in use.", null);
			logger.info("Could not connect: port is already in use.");
		} catch (Exception e) {
			log.log("Could not connect: " + e.toString(), null);
			logger.info("Could not connect: " + e.toString());
		}
	}

	public boolean initIOStream() {
		log.log("Opening IOStream...", null);
		logger.info("Opening IOStream.");
		boolean ioStreamOpened = false;
		try {
			inputStream = openedSerialPort.getInputStream();
			outputStream = openedSerialPort.getOutputStream();
			ioStreamOpened = true;
			logger.info("IOStream successfully opened.");
		} catch (IOException e) {
			log.log("Could not open IOStream.", null);
			logger.info("Could not open IOStream." + e.toString());
		}
		return ioStreamOpened;
	}

	public void initListener() {
		try {
			log.log("Initializing listener...", null);
			logger.info("Initializing listener.");
			openedSerialPort.addEventListener(this);
			openedSerialPort.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e) {
			log.log("Could not add event listener.", null);
			logger.info("Could not add event listener. " + e.toString());
		}
	}

	public void disconnect() {
		if (connectedToPort == true) {
			openedSerialPort.removeEventListener();
			openedSerialPort.close();
			logger.info("Disconnected from " + openedSerialPort.getName());
			try {
				inputStream.close();
				outputStream.close();
				connectedToPort = false;
				logger.info("IOStream closed.");
			} catch (IOException e) {
				logger.info("Could not close IOStream." + e.toString());
			}
		} else {
			logger.info("Tried to disconnect, but no port was opened.");
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				// Input message:  
				inputMessage = "";
				bytes = inputStream.read(buffer);
				curMsg.append(new String(buffer, 0, bytes, Charset.forName("UTF-8")));
				int endIdx = curMsg.indexOf(end);
				if (endIdx != -1) {
					inputMessage = curMsg.substring(0, endIdx + end.length()).trim();
					curMsg.delete(0, endIdx + end.length());
					log.log(null, "Received: " + inputMessage);
					logger.info("Received: " + inputMessage);
					System.out.println("[R]: " + inputMessage);
					// Check if there is an error in the message.
					errorMS = false;
					errorMS = checkIfError(inputMessage);
					// Compare input and output messages if they are the same.
					compareMS = false;
					compareMS = compareMessages(inputMessage, sendedMessage);
					if (errorMS) {
						log.log("Received a message with an error from " + openedSerialPort.getName(), "Received a message with an error from " + openedSerialPort.getName());
						logger.info("Received a message with an error from " + openedSerialPort.getName());
					}
					if (compareMS && !errorMS && !actualMS) {
						startRouteFromFileThread.resume();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.log("An exception occured. See more in log file.", "An exception occured. See more in log file.");
				logger.error(e.toString());
			}
		}

	}

	/**
	 * Writes data to uC.
	 * @param fastVelocity
	 * @param slowVelocity
	 * @param t
	 * @param error
	 */
	public void writeData(int fastVelocity, int slowVelocity, int t, int error) {
		try {
			String fastStr = String.format("%03d", fastVelocity);
			String slowStr = String.format("%03d", slowVelocity);
			String tStr = String.format("%03d", t);
			// Sf000s000t000e0E
			// Send the message.
			outputStream.flush();
			String toSend = "S".concat("f" + fastStr + "s" + slowStr + "t" + tStr + "e" + error + "E");
			sendedMessage = toSend;
			System.out.println("[S]: " + toSend);
			outputStream.write(toSend.getBytes());
			outputStream.flush();
			// Log errors.
			if (error == 0) {
				logger.info("Sent: " + toSend);
			} else {
				log.log("Robot stopped by the user!", "Robot stopped by the user!");
				logger.info("Robot stopped by the user!");
			}
		} catch (Exception e) {
			log.log("Could not write data: " + e.toString(), "Could not write data: " + e.toString());
			logger.info("Could not write data: " + e.toString());
		}
	}

	/**
	 * Gets the loaded route file and sends to uC message after message.
	 * Firstly it checks if actual coordinates are [10,10,10].
	 * @param loadedFile
	 */
	public synchronized void useLoadedFile(File loadedFile) {
		log.log("Starting route...", null);
		System.out.println("Starting route...");
		startRouteFromFileThread = new StartRouteFromFile(mainGUI, this, loadedFile);
	}

	/**
	 * Sends a message to uC with error flag set to "1"
	 */
	public void stopRobotMovement() {
		/* Send the stop message to uC */
		writeData(0, 0, 0, 1);
	}

	/**
	 * Resets all threads.
	 */
	public void resetThreads() {
		if (startRouteFromFileThread != null) {
			if (!startRouteFromFileThread.startRouteFromFileThread.getState().equals(Thread.State.TERMINATED)) {
				startRouteFromFileThread.suspend();
				startRouteFromFileThread.reset();
			}
		}
	}

	/**
	 * Checks if there has been an error in input message.
	 * @param inputMessage
	 * @return
	 */
	public boolean checkIfError(String inputMessage) {
		boolean errorInMessage = false;
		InputMessageData inputMessageData = new InputMessageData();
		inputMessageData = calculations.processInputMessage(inputMessage, inputMessageData);
		if (inputMessageData.geteIM() == 1) {
			errorInMessage = true;
		}
		return errorInMessage;
	}

	/**
	 * Compares 
	 * @param inputMessage
	 * @param outputMessage
	 * @return
	 */
	public boolean compareMessages(String inputMessage, String outputMessage) {
		boolean theSame = false;
		// Check if messages are the same.
		InputMessageData inputMessageData = new InputMessageData();
		OutputMessageData outputMessageData = new OutputMessageData();
		inputMessageData = calculations.processInputMessage(inputMessage, inputMessageData);
		outputMessageData = calculations.processOutputMessage(outputMessage, outputMessageData);
		if (inputMessageData.getfIM() == outputMessageData.getfIM() && inputMessageData.getsIM() == outputMessageData.getsIM() && inputMessageData.gettIM() == outputMessageData.gettIM()) {
			theSame = true;
		}
		logger.info("IM: " + inputMessage + "; SM: " + sendedMessage);
		return theSame;
	}

	/* GETTERS & SETTERS */

	public boolean isConnectedToPort() {
		return connectedToPort;
	}

	public void setConnectedToPort(boolean connectedToPort) {
		this.connectedToPort = connectedToPort;
	}

	public String getSendedMessage() {
		return sendedMessage;
	}

	public void setSendedMessage(String sendedMessage) {
		this.sendedMessage = sendedMessage;
	}

	public String getInputMessage() {
		return inputMessage;
	}

	public void setInputMessage(String inputMessage) {
		this.inputMessage = inputMessage;
	}

	public boolean isGlobalCalibrated() {
		return globalCalibrated;
	}

	public void setGlobalCalibrated(boolean globalCalibrated) {
		this.globalCalibrated = globalCalibrated;
	}
}