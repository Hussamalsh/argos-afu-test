package se.hms.argos.afutest.infra.simulator;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jayway.restassured.response.Response;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import se.hms.argos.afutest.infra.modbustcp.ModbustcpServer;

import com.sun.jersey.api.client.WebResource;

public class LoggedParameterData {
	private final String accessKey;
	private final static PrintStream out = java.lang.System.out;
	private final Client client = Client.create();

	private final static String BASEURL = "https://api.netbiter.net/operation/v1/rest/json";
	private final static String SYSTEM_RESOURCE_URL = BASEURL + "/system";

	public LoggedParameterData(String accessKey) {
		this.accessKey = accessKey;
	}

	/*
	 * Get system id that the access key give access to.
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

	/*
	 * Get system parameters id that the access key give access to.
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

	/*
	 * Get the inputregister loged value from the argos REST api.
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
			// out.println("\n");
			// Print information about the systems.
			inputRegister = new String[systems.size()][systems.size()];
			for (liveData system : systems) {
				col = 0;
				// out.println(String.format("timestamp: %s",system.timestamp));
				// out.println(String.format("value: %s", system.value));
				out.println((inputRegister[row][col++] = system.id));
				out.println("\t" + (inputRegister[row++][col] = system.value));
			}

			// Print information about the system.

		} else {
			printErrorInformation(strResponse);
		}

		return inputRegister;
	}

	/*
	 * Get the inputregister loged value from the argos REST api.
	 */
	public int[] getLoggedData(String systemId, String parameterID, String startdate, String enddate) {
		int[] loggedData = null;
		out.println("Lets see...........LoggedData from API............");
		int row = 0, col = 0;
		ClientResponse clientResponse = client.resource(SYSTEM_RESOURCE_URL).path(systemId).path("log")
				.path(parameterID).queryParam("accesskey", accessKey).queryParam("startdate", startdate)//
				.queryParam("enddate", enddate) // "2016-10-14T14:00:0020"
				.queryParam("limitrows", "2").get(ClientResponse.class);
		// 66261.9269.173394?accesskey=51F2531794288EBA64764B38D2516890&startdate=2016-10-14T08:00:0020UTC&enddate=2016-10-14T13:30:0020
		String strResponse = clientResponse.getEntity(String.class);

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {

			// Deserialize the JSON data.
			List<LoggedData> systems = new Gson().fromJson(strResponse, new TypeToken<List<LoggedData>>() {
			}.getType());
			// out.println("\n");
			// Print information about the systems.
			loggedData = new int[systems.size()];
			out.println(systems.size());
			for (LoggedData system : systems) {
				// col = 0;
				// out.println(String.format("timestamp: %s",system.timestamp));
				// out.println(String.format("value: %s", system.value));
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

	public String[][] getalarmData(String startdate) {
		String[][] alarmData = null;
		out.println("Lets see...........AlarmData from API............");
		int row = 0, col = 0;
		// https://api.netbiter.net/operation/v1/rest/json/updated/alarms?accesskey=747C441E03934628AF2C13B730E4CCDD&startdate=2016-10-24T08:35:00Z

		ClientResponse clientResponse = client
				.resource("https://api.netbiter.net/operation/v1/rest/json/updated/alarms")
				.queryParam("accesskey", accessKey).queryParam("startdate", "2016-10-24T08:35:00Z")
				.get(ClientResponse.class);

		String strResponse = clientResponse.getEntity(String.class);

		/*
		 * JsonDeserializationContext jdc = null; JsonElement je = null;
		 * AlarmData p = jdc.deserialize(je, AlarmData.class); List<AlarmData>
		 * pList = new ArrayList<AlarmData>(1); // pList.add(p);
		 */

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {

			// Deserialize the JSON data.
			List<AlarmData> systems = new Gson().fromJson(strResponse, new TypeToken<List<AlarmData>>() {
			}.getType());
			// out.println("\n");
			// Print information about the systems.
			alarmData = new String[systems.size()][systems.size()];
			out.println(systems.size());
			for (AlarmData system : systems) {
				col = 0;
				// out.println(String.format("timestamp: %s",system.timestamp));
				// out.println(String.format("value: %s", system.value));
				out.println((system.acked));
				out.print((alarmData[row++][col++] = system.active + "\n"));
				out.print((alarmData[row][col++] = system.id + "\n"));
				out.print((alarmData[row][col] = system.info + "\n"));
				out.print((system.timestamp + "\n"));
			}

			// Print information about the system.

		} else {
			printErrorInformation(strResponse);
		}

		return alarmData;
	}

	// seeeeeeeeeeeeeeeeeeeeeeee
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

	protected String[][] alarmItems() {
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
			// out.println(String.format("Alarm class: %s", alarm.alarmClass));
			// out.println(String.format("Device name: %s", alarm.deviceName));
			out.println(String.format("Acked: %s", alarm.acked));
			out.println(String.format("Active: %s", itemsArr[row++][col++] = "" + alarm.active));

			out.println("\n \n \n");
		}

		return itemsArr;

	}

	protected String[] writeLiveValue(String systemId, String id, String value) {
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

	private class System {

		public boolean activated, suspended;
		public String id, name, projectName;
		public int projectId;
	}

	private class ParameterID extends System {
		public String deviceName, pointType, unit, logInterval;
	}

	private class liveData extends System {
		public String value;
	}

	private class LoggedData extends System {
		public String timestamp;
		public String value;
	}

	private class Alarm {
		ArrayList<AlarmData> alarms;
	}

	private class AlarmData extends System {
		public String timestamp, severity, triggerOrigin, info;
		public boolean acked, active;
	}

	private class Error {
		public String code;
		public String message;
	}

	private class Synch {
		ArrayList<SynchData> alarms;
	}

	private class SynchData extends liveData {

	}

	/*
	 * Print information about occurred error.
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

		LoggedParameterData account = new LoggedParameterData(accessKey);
		String systemId = account.getSystemID();
		String[] parameterTag = account.getParametersTag(systemId);

		// Print logged data for a given parameter == oldcode

		// account.getInputRegister(systemId, parameterTag [0], parameterTag
		// [1], parameterTag[2]);
		// startdate = Ex="2016-10-14T10:54:0020UTC" ,,, enddate
		// Ex="2016-10-14T10:54:0020UTC"
		String StartTime = "2016-10-26T08:15:00ZUTC";// CurrentTime.getCurretTime(true);
		String endTime = "2016-10-26T09:20:00Z";
		// int temparr[]= account.getLoggedData(systemId, "TEST1",StartTime ,
		// endTime);
		// account.getalarmData(systemId, "TEST1ALARM");
		// startdate=2016-10-24T08:35:00Z
		// account.getalarmData("2016-10-24T08:35:00Z");
		/*
		 * String[][] elm = account.alarmItems();
		 * 
		 * for(int i = 0; i<elm.length; i++) { for(int j = 0; j<3; j++)
		 * out.println(elm[i][j]); }
		 */

		/*
		 * 
		 * PUT
		 * https://api.netbiter.net/operation/v1/rest/json/system/003011FB1234/
		 * live/async?accesskey=1234567890ABCDEFCGHIJ&id=967.0.1111&id=967.0.
		 * 1112
		 * 
		 * Data: [ { "id": "967.0.1111", "value": "32" }, { "id": "967.0.1112",
		 * "value": "0" } ]
		 * 
		 */

		// account.getAsynchStart2(systemId, parameterTag[0]);
		// account.assItems(systemId);

		/*
		 * 
		 * Test write value to live parameter
		 * 
		 */

		out.println("Lets see...........Write value to live parameter............");

		account.writeLiveValue(systemId, "HOLDINGT", "257");
		Thread.sleep(10000);
		out.println(ModbustcpServer.holdingReg.getValue());

	}

}
