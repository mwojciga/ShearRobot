package biorobot.pack;

import gui.pack.MainGUI;

/**
 * Writes logs and messages.
 * @author maciej.wojciga
 */
public class Log {

	MainGUI mainGUI;

	public Log(MainGUI mainGUI) {
		this.mainGUI = mainGUI;
	}

	public void log(String messages, String logs) {
		if (messages != null) {
			// Write to action messages.
			//mainGUI.lblActionlbl.setText(messages);
		}
		if (logs != null) {
			// Write to logs.
		}
	}
}
