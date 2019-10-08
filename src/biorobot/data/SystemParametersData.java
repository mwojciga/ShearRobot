package biorobot.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import biorobot.pack.SystemParameters;

/**
 * System parameters DTO.
 * @author maciej.wojciga
 */

public class SystemParametersData {

	public SystemParametersData() {
		Properties systemProperties = new Properties();
		try {
			FileInputStream propertiesFileIS = new FileInputStream(SystemParameters.SYSPROPERTIESFILE);
			systemProperties.load(propertiesFileIS);
			propertiesFileIS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
