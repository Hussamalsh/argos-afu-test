package se.hms.argos.afutest.infra.modbustcp;

import java.io.PrintStream;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

public class LoggedParameterData {
	private final String accessKey;
	private final PrintStream out = java.lang.System.out;
	private final Client client = Client.create();
	private String [] names;

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
	public String[] getParametersID(String systemID) {
		String []parameterID = null;
		int nextIndex = 0;
		ClientResponse clientResponse = client.resource(SYSTEM_RESOURCE_URL).path(systemID).path("/log/config")
				.queryParam("accesskey", accessKey)
				.get(ClientResponse.class);

		String strResponse = clientResponse.getEntity(String.class);

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {
			// Deserialize the JSON data.
			List<ParameterID> systems = new Gson().fromJson(strResponse, new TypeToken<List<ParameterID>>() {
			}.getType());
			parameterID = new String [systems.size()];
			names = new String [systems.size()];
			// Print information about the systems.
			for (ParameterID system : systems) {
				out.print(String.format("\t    * DeviceName: %s  ===>",  system.deviceName));
				out.print(String.format("\t\t    * Name: %s     *",  names[nextIndex]  = system.name));
				out.println(String.format("\t    * Parameter ID: %s     *", parameterID[nextIndex++] = system.id));
				
			}
		} else {
			printErrorInformation(strResponse);
		}

		return parameterID;
	}
	
	public String getName(int index){
		return names[index];
	}

	/*
	 * Get the inputregister loged value from the argos REST api.
	 */
	public String[][] getInputRegister(String systemId, String parameterID1,String parameterID2,String parameterID3) {
		String [][]inputRegister = null;
		out.println("Lets see...........getInputRegister from API............");
		int row = 0;
		int col =0;
		ClientResponse clientResponse = client.resource(SYSTEM_RESOURCE_URL).path(systemId).path("/live")
				.queryParam("accesskey", accessKey)
				.queryParam("id", parameterID1)
				.queryParam("id", parameterID2)
				.queryParam("id", parameterID3)
				.get(ClientResponse.class);

		String strResponse = clientResponse.getEntity(String.class);

		// Do we have a valid request.
		if (clientResponse.getClientResponseStatus() == Status.OK) {

			// Deserialize the JSON data.
			List<LoggedData> systems = new Gson().fromJson(strResponse, new TypeToken<List<LoggedData>>() {
			}.getType());
			// out.println("\n");
			// Print information about the systems.
			inputRegister = new String [systems.size()][systems.size()];
			for (LoggedData system : systems) {
				col=0;
				// out.println(String.format("timestamp: %s",system.timestamp));
				// out.println(String.format("value: %s", system.value));
				// apivalue = system.value;
				out.println(inputRegister[row][col++] = system.id);
				out.print( "\t" + (inputRegister[row++][col] = system.value));
			}

			// Print information about the system.

		} else {
			printErrorInformation(strResponse);
		}

		return inputRegister;
	}

	private class System {

		public boolean activated, suspended;
		public String id, name, projectName;
		public int projectId;
	}

	private class ParameterID extends System {
		public String deviceName, /*id, name,*/ pointType, unit,logInterval;
	}

	private class LoggedData extends System {
		public String value;
	}

	private class Error {
		public String code;
		public String message;
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

	public static void main(String[] args) {
		String accessKey = "51F2531794288EBA64764B38D2516890";
		/* old code to test everything is working
		// String systemId = "003011FAE2BA";                            
		//String parameterID1 = "66261.9269.172526";
		//String parameterID2 = "66261.9269.173394";
		//String parameterID3 = "66261.9269.173393";
		*/

		LoggedParameterData account = new LoggedParameterData(accessKey);
		String systemId = account.getSystemID();
		String []parameterId = account.getParametersID(systemId);
		/* Print logged data for a given parameter           == oldcode
		//account.getInputRegister(parameterID1);
		//account.getInputRegister(parameterID2);
		//account.getInputRegister(parameterID3);*/
		//for(String pID: parameterId){
			account.getInputRegister(systemId,parameterId[0],parameterId[1], parameterId[2]);
		//}
		

	}

}
