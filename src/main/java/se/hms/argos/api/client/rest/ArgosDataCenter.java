package se.hms.argos.api.client.rest;

/**
* ArgosDataCenter
* 
* <P>This is the ArgosDataCenter class which is responsible to get right collected values from Argos server
*  by implementing the suitable REST API. This class handles data from connected field systems on the Argos server
*  
* @author Hussam Alshammari
* @author Lolita Mageramova
* @version 1.0
*/

import java.io.PrintStream;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

public class ArgosDataCenter {

	private final String accessKey;
	private final static PrintStream out = java.lang.System.out;
	private final Client client = Client.create();

	private final static String BASEURL = "https://api.netbiter.net/operation/v1/rest/json";
	private final static String SYSTEM_RESOURCE_URL = BASEURL + "/system";
	
	/**
	 * The default constructor.  
	 * Initializes accessKey variable of the simulator.
	 */
	public ArgosDataCenter() {
		this.accessKey = setup();
	}
	
	/**
	 * The setup method  
	 * Initializes accessKey variable of the simulator by creating the user object
	 * @return ak = the method return the accesskey of the user.
	 */
	private String setup() {
		// create a scanner so we can read the command-line input
		Scanner scanner = new Scanner(java.lang.System.in);
		// prompt for the user's name
		out.print("\nEnter your username: ");
		// get their input as a String
		String userName = scanner.next();

		// prompt for their password
		out.print("\nEnter your password: ");
		// get the password
		String password = scanner.next();
		String ak;
		out.println("Your accessKey is = " + (ak= new User().getAccessKey(userName, password)));
		scanner.close();
		return ak;
	}

	/**
	 * the getSystemID method
	 * Get system id that the access key give access to.
	 * @return systemID = the method return the systemID of the user.
	 */
	public String getSystemID() {
		String systemID = "";
		ClientResponse clientResponse = client.resource(SYSTEM_RESOURCE_URL).queryParam("accesskey", accessKey)
				.get(ClientResponse.class);

		String strResponse = clientResponse.getEntity(String.class);

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {
			// Deserialize the JSON data.
			List<System> systems = new Gson().fromJson(strResponse, new TypeToken<List<System>>() {
			}.getType());

			// Print information about the systems.
			for (System system : systems) {
				out.println(String.format("    * System ID: %s     *", systemID = system.id));
			}
		} else {
			printErrorInformation(strResponse);
		}

		return systemID;
	}

	/**
	 * the getParametersTag method
	 * Get system parameters id that the access key give access to.
	 * @param systemID - system id of the system
	 * @return getParametersTag = the method return all yhr parameters of the system as an array.
	 */
	public String[] getParametersTag(String systemID) {
		String[] parameterID = null;
		int i = 0;
		ClientResponse clientResponse = client.resource(SYSTEM_RESOURCE_URL).path(systemID).path("/live/config")
				.queryParam("accesskey", accessKey).get(ClientResponse.class);

		String strResponse = clientResponse.getEntity(String.class);

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {
			// Deserialize the JSON data.
			List<ParameterID> systems = new Gson().fromJson(strResponse, new TypeToken<List<ParameterID>>() {
			}.getType());
			parameterID = new String[systems.size()];
			// Print information about the systems.
			for (ParameterID system : systems) {
				out.print(String.format("\t    * DeviceName: %s  ===>", system.deviceName));
				out.print(String.format("\t\t    * Name: %s     ", system.name));
				out.println(String.format("\t    * Parameter ID: %s     *", parameterID[i++] = system.id));

			}
		} else {
			printErrorInformation(strResponse);
		}

		return parameterID;
	}

	/**
	 * getInputRegister method
	 * Return a list of all systems for which the configuration has been updated since last request. 
	 * @param systemID - system id of the system
	 * @param parameterID1 - The first parameter of the system
	 * @param parameterID2 - The second parameter of the system
	 * @param parameterID2 - The third parameter of the system
	 * @return inputRegister = The array holding the input registers as element
	 */
	public String[][] getInputRegister(String systemId, String parameterID1, String parameterID2, String parameterID3) {
		String[][] inputRegister = null;
		out.println("Lets see...........getInputRegister from API............");
		int row = 0, col = 0;
		ClientResponse clientResponse = client.resource(SYSTEM_RESOURCE_URL).path(systemId).path("/live")
				.queryParam("accesskey", accessKey).queryParam("id", parameterID1).queryParam("id", parameterID2)
				.queryParam("id", parameterID3).get(ClientResponse.class);

		String strResponse = clientResponse.getEntity(String.class);

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {

			// Deserialize the JSON data.
			List<liveData> systems = new Gson().fromJson(strResponse, new TypeToken<List<liveData>>() {
			}.getType());
			// Print information about the systems.
			inputRegister = new String[systems.size()][systems.size()];
			for (liveData system : systems) {
				col = 0;
				out.println((inputRegister[row][col++] = system.id));
				out.println("\t" + (inputRegister[row++][col] = system.value));
			}

			// Print information about the system.

		} else {
			printErrorInformation(strResponse);
		}

		return inputRegister;
	}

	/**
	 * The getLoggedData method
	 * Return a list of historical logged parameter data for a given parameter within a system.
	 * @param systemID - system id of the system
	 * @param parameterID - The parameter of the system
	 * @param startdate - The UTC start date and time for the list
	 * @param enddate - The UTC end date and time for the list
	 * @return getLoggedData = The array holding the logged values as element
	 */
	public int[] getLoggedData(String systemId, String parameterID, String startdate, String enddate) {
		int[] loggedData = null;
		out.println("Lets see...........LoggedData from API............");
		int row = 0, col = 0;
		ClientResponse clientResponse = client.resource(SYSTEM_RESOURCE_URL).path(systemId).path("log")
				.path(parameterID).queryParam("accesskey", accessKey).queryParam("startdate", startdate)//
				.queryParam("enddate", enddate) // "2016-10-14T14:00:0020"
				.queryParam("limitrows", "2").get(ClientResponse.class);
		String strResponse = clientResponse.getEntity(String.class);

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {

			// Deserialize the JSON data.
			List<LoggedData> systems = new Gson().fromJson(strResponse, new TypeToken<List<LoggedData>>() {
			}.getType());
			// Print information about the systems.
			loggedData = new int[systems.size()];
			out.println(systems.size());
			for (LoggedData system : systems) {

				out.println((system.timestamp));
				int val = Integer.parseInt(system.value);
				out.print((loggedData[row++] = val) + "\n");
			}

			// Print information about the system.

		} else {
			printErrorInformation(strResponse);
		}

		return loggedData;
	}
	
	/**
	 * The printCurrentAlarmStatus method  
	 * This method gets  all the existed alarms from the system.
	 * @return alarms = The array holding the alarm information
	 */
	public ArrayList<AlarmData> printCurrentAlarmStatus() {
		ClientResponse clientResponse = client.resource(BASEURL).path("updated").path("alarms")
				.queryParam("accesskey", accessKey).queryParam("startdate", "2016-10-24T08:35:00Z")
				.get(ClientResponse.class);

		String strResponse = clientResponse.getEntity(String.class);

		Alarm alarm = null;

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {
			// Deserialize the JSON data.
			alarm = new Gson().fromJson(strResponse, Alarm.class);
			// Deserialize the JSON data
		} else {
			printErrorInformation(strResponse);
		}
		return alarm.alarms;
	}

	/**
	 * The alarmItems method  
	 * Return a list of all alarms generated from a given start date until current time,
	 * using the access key requested by this method. 
	 * @return alarms = The array holding the alarm information
	 */
	public String[][] alarmItems() {
		ArrayList<AlarmData> aList = printCurrentAlarmStatus();
		String[][] itemsArr = new String[aList.size()][3];

		int row = 0, col;

		for (AlarmData alarm : aList) {
			col = 0;
			out.println(String.format("    * Alarm ID: %s     *", itemsArr[row][col++] = alarm.id));
			out.println(String.format("Name: %s", alarm.name));
			out.println(String.format("Info: %s", itemsArr[row][col++] = alarm.info));
			out.println(String.format("Severity: %s", alarm.severity));
			out.println(String.format("Last modified: %s", alarm.timestamp));
			out.println(String.format("TriggerOrigin: %s", alarm.triggerOrigin));
			out.println(String.format("Acked: %s", alarm.acked));
			out.println(String.format("Active: %s", itemsArr[row++][col++] = "" + alarm.active));

			out.println("\n \n \n");
		}

		return itemsArr;

	}

	/**
	 * The writeLiveValue method  
	 * Write a value to a connected device with set enabled for a register.
	 * @param systemId - system id of the system
	 * @param id - The parameter id’s that will be written to. This could be tags if it is used
	 * @param value - The value that should be written to the parameter. 
	 * @return liveValueArr = The array holding the response parameters
	 */
	public String[] writeLiveValue(String systemId, String id, String value) {
		String[] liveValueArr = null;
		int row = 0, col;
		JsonObject input2 = new JsonObject();
		input2.addProperty("id", id);
		input2.addProperty("value", value);

		ClientResponse clientResponse = client.resource(SYSTEM_RESOURCE_URL).path(systemId).path("live")
				.queryParam("accesskey", accessKey).type("application/json")
				.put(ClientResponse.class, input2.toString());

		String strResponse = clientResponse.getEntity(String.class);

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {

			// Deserialize the JSON data.
			List<liveData> systems = new Gson().fromJson(strResponse, new TypeToken<List<liveData>>() {
			}.getType());
			// Print information about the systems.
			out.println(systems.size() + 1);
			liveValueArr = new String[systems.size() + 1];
			for (liveData system : systems) {
				out.println((liveValueArr[row++] = system.id));
				out.println((liveValueArr[row] = system.value));
			}

			// Print information about the system.

		} else {
			printErrorInformation(strResponse);
		}

		return liveValueArr;
	}

	/**
	 * The System CLass 
	 * This is a class that have all the variables that can be used to get the write values from Argos server
	 */
	private class System {

		public boolean activated, suspended;
		public String id, name, projectName;
		public int projectId;
	}

	/**
	 * The ParameterID CLass 
	 * ParameterID inherits the class System and have more variables to get more information about the system.
	 */
	private class ParameterID extends System {
		public String deviceName, pointType, unit, logInterval;
	}

	/**
	 * The ParameterID CLass 
	 * ParameterID inherits the class System and have more variables to get more information about the system.
	 */
	private class liveData extends System {
		public String value;
	}

	/**
	 * The ParameterID CLass 
	 * ParameterID inherits the class System and have more variables to get more information about the system.
	 */
	private class LoggedData extends System {
		public String timestamp;
		public String value;
	}

	/**
	 * The Alarm CLass 
	 * Alarm inherits the class System and have more variables to get more information about the alarms on the system.
	 */
	private class Alarm {
		ArrayList<AlarmData> alarms;
	}

	/**
	 * The AlarmData CLass 
	 * AlarmData inherits the class System and have more variables to get more information about the alarms on the system.
	 */
	private class AlarmData extends System {
		public String timestamp, severity, triggerOrigin, info;
		public boolean acked, active;
	}

	/**
	 * The Error CLass 
	 * Error class have both the code of the error and the error message variables, 
	 * to show the code and the message of the error
	 */
	private class Error {
		public String code;
		public String message;
	}

	/**
	 * Print information about occurred error.
	 * @param errorResponse - the response of the error from the Argos server. 
	 */
	private void printErrorInformation(String errorResponse) {
		if (errorResponse != null) {
			// Deserialize the error JSON data.
			Error error = new Gson().fromJson(errorResponse, Error.class);
			out.println(String.format("An error occured when communication with the Argos REST API. Message:%s Code:%s",
					error.message, error.code));
		} else {
			out.println("An error occured when communication with the Argos REST API. No detailed info is available.");
		}
	}

	/**
	 * The main method 
	 * This method is used for test purposes only.
	 */
	public static void main(String[] args) throws InterruptedException {
		String accessKey = "51F2531794288EBA64764B38D2516890";
		String accessKey2 = "747C441E03934628AF2C13B730E4CCDD";
		// 003011FAE2B0
		/*
		 * old code to test everything is working // String systemId
		 * ="003011FAE2BA"; //String parameterID1 = "66261.9269.172526";
		 * //String parameterID2 = "66261.9269.173394"; //String parameterID3 =
		 * "66261.9269.173393";
		 */

		ArgosDataCenter account = new ArgosDataCenter();
		String systemId = account.getSystemID();
		String[] parameterTag = account.getParametersTag(systemId);



		out.println("Lets see...........Write value to live parameter............");

		account.writeLiveValue(systemId, "HOLDINGT", "257");
		Thread.sleep(10000);
		// out.println(mModbustcpServer.holdingReg.getValue());

	}

}
