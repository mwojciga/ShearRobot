package biorobot.pack;

import biorobot.data.InputMessageData;
import biorobot.data.OutputMessageData;
import biorobot.data.SystemParametersData;

/**
 * Calculates things.
 * @author maciej.wojciga
 */

public class Calculations {

	static SystemParametersData systemParametersData = new SystemParametersData();

	/**
	 * Takes input message and outputs useful data.
	 * @param inputMessage
	 * @return
	 */
	public InputMessageData processInputMessage(String inputMessage, InputMessageData inputMessageData) {
		// Sf10s20t3e0E
		int fStartIM = inputMessage.indexOf("f") + 1;
		int fEndIM = inputMessage.indexOf("s");
		int sStartIM = inputMessage.indexOf("s") + 1;
		int sEndIM = inputMessage.indexOf("t");
		int tStartIM = inputMessage.indexOf("t") + 1;
		int tEndIM = inputMessage.indexOf("e");
		int eStartIM = inputMessage.indexOf("e") + 1;
		int eEndIM = inputMessage.indexOf("E");
		// Fill DTO with new values.
		inputMessageData.setfIM(Integer.parseInt(inputMessage.substring(fStartIM, fEndIM)));
		inputMessageData.setsIM(Integer.parseInt(inputMessage.substring(sStartIM, sEndIM)));
		inputMessageData.settIM(Integer.parseInt(inputMessage.substring(tStartIM, tEndIM)));
		inputMessageData.seteIM(Integer.parseInt(inputMessage.substring(eStartIM, eEndIM)));
		return inputMessageData;
	}

	/**
	 * Takes output message and outputs useful data.
	 * @param outputMessage
	 * @param outputMessageData
	 * @return
	 */
	public OutputMessageData processOutputMessage(String outputMessage, OutputMessageData outputMessageData) {
		// Sf10s20t3e0E
		int fStartIM = outputMessage.indexOf("f") + 1;
		int fEndIM = outputMessage.indexOf("s");
		int sStartIM = outputMessage.indexOf("s") + 1;
		int sEndIM = outputMessage.indexOf("t");
		int tStartIM = outputMessage.indexOf("t") + 1;
		int tEndIM = outputMessage.indexOf("e");
		int eStartIM = outputMessage.indexOf("e") + 1;
		int eEndIM = outputMessage.indexOf("E");
		// Fill DTO with new values.
		outputMessageData.setfIM(Integer.parseInt(outputMessage.substring(fStartIM, fEndIM)));
		outputMessageData.setsIM(Integer.parseInt(outputMessage.substring(sStartIM, sEndIM)));
		outputMessageData.settIM(Integer.parseInt(outputMessage.substring(tStartIM, tEndIM)));
		outputMessageData.seteIM(Integer.parseInt(outputMessage.substring(eStartIM, eEndIM)));
		return outputMessageData;
	}

	/**
	 * Generates a single message from the given parameters.
	 * @param fastVelocity
	 * @param slowVelocity
	 * @param t
	 * @param error
	 * @return
	 */
	public String generateSingleMessage(int fastVelocity, int slowVelocity, int t, int error) {
		// Sf000s000t000e0E
		String singleMessage = "";
		// Concatenate message.
		singleMessage = "S" +
						"f" + String.format("%03d", fastVelocity) + 
						"s" + String.format("%03d", slowVelocity) + 
						"t" + String.format("%03d", t) +
						"e" + error +
						"E";
		return singleMessage;
	}
}
