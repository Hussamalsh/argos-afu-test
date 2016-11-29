package se.hms.argos.afutest.boot;

/**
* ArgosTestMain.
* 
* <P>This is the main class that start the Argos server with the chosen configuration.
*  
* @author HMS
* @version 1.0
*/


import org.slf4j.bridge.SLF4JBridgeHandler;

import se.hms.argos.afutest.boot.ArgosTestConfig;
import se.hms.argos.afutest.boot.ArgosTestServer;
import se.hms.argos.common.owner.utils.OwnerUtils;

public class ArgosTestMain
{
   public static void main(String... args)
   {
      // Setup SLF4J bridge so that anyone using JUL will be redirected to SLF4J
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();

      // Create configuration
      ArgosTestConfig config = OwnerUtils.getOrCreate(ArgosTestConfig.class);

      // Create and start server
      ArgosTestServer server = new ArgosTestServer(config);
      if (!server.start() || !server.join())
      {
         System.exit(1);  
      }
   }
}
