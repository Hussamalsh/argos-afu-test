package se.hms.argos.api.client.rest;
import java.io.PrintStream;
import java.util.Date;
 
import javax.ws.rs.core.MediaType;
 
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
 
/**
* The User Class
* 
* <P>This is the User class which is responsible to get access key that will make it possible 
* to get access to projects an systems set up for this user.
* <P>By implementing the suitable REST API, we can get the write values.
*  
* @author Hussam Alshammari
* @author Lolita Mageramova
* @version 1.0
*/

public class User
{
   private final PrintStream out = java.lang.System.out;
   private final Client client = Client.create();
   private UserAccess userAccess;
 
   private final static String BASEURL = "https://api.netbiter.net/operation/v1/rest/json";
   private final static String USER_RESOURCE_URL = BASEURL + "/user";
 
   /** 
    * Return a user access key.
    *
    * @param userName = A valid Netbiter Argos username. 
    * @param password = The corresponding Netbiter Argos account password. 
    * @return a valid user access key or null if the function fails.
    */
   public String getAccessKey(String userName, String password)
   {
      // Do we have a valid user access key or do we need to request a new?
      if (userAccess == null || userAccess.expires.getTime() >= System.currentTimeMillis())
      {
         UserCredentials userCredentials = new UserCredentials();
         userCredentials.userName = userName;
         userCredentials.password = password;
 
         ClientResponse clientResponse = client.resource(USER_RESOURCE_URL).path("authenticate").
               type(MediaType.APPLICATION_JSON).post(ClientResponse.class, new Gson().toJson(userCredentials));
 
         String strResponse = clientResponse.getEntity(String.class);
         if (clientResponse.getClientResponseStatus() == Status.OK)
         {
            // Deserialize the JSON data.
            UserAccess userAccess = new Gson().fromJson(strResponse, UserAccess.class);
            return userAccess.accessKey;
         }
         else
         {
            printErrorInformation(strResponse);
         }
      }
      else
      {
         return userAccess.accessKey;
      }
 
      return null;
   }
 
   /* Print information about occurred error.
    */
   private void printErrorInformation(String errorResponse)
   {
      if (errorResponse != null)
      {
         // Deserialize the error JSON data.
         Error error = new Gson().fromJson(errorResponse, Error.class);
         out.println(String.format("An error occured when communication with the Argos REST API. Message:%s Code:%s",
               error.message, error.code));
      }
      else
      {
         out.println("An error occured when communication with the Argos REST API. No detailed info is available.");
      }
   }
 
   private class UserCredentials
   {
      public String userName, password;
   }
 
   private class UserAccess
   {
      public String accessKey;
      public Date expires;
   }
 
   private class Error
   {
      public String code;
      public String message;
   }
 
   public static void main(String[] args)
   {
      String userName = "hussam";
      String password = "nyt777RA";

      // Get a valid user access key.
      String accessKey = new User().getAccessKey(userName, password);
 
      // Print the user access key.
      System.out.printf("Access key %s is ready to use.", accessKey);
   }
}