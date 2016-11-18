package se.hms.argos.afutest.boot;

import org.eclipse.jetty.server.Connector;

import se.hms.argos.afutest.infra.http.internal.InternalWebMvcSetup;
import se.hms.argos.afutest.infra.http.internal.api.rest.controller.DummyController;

import se.hms.argos.afutest.infra.modbustcp.ModbustcpBeans;
import se.hms.argos.afutest.infra.simulator.SmilatorBeans;
import se.hms.argos.common.server.ArgosServer;

public class ArgosTestServer extends ArgosServer
{
   public ArgosTestServer(ArgosTestConfig config)
   {
      this(config, ModbustcpBeans.class,SmilatorBeans.class);
   }

   public ArgosTestServer(ArgosTestConfig config, Class<?>... springAnnotatedClasses)
   {
      init(config, springAnnotatedClasses);
      /*
       * Create internal webapp handler
       */
      Connector internalConnector = createAndRegisterHttpConnector(config.serverInternalPort(), "INTERNAL");
      createAndRegisterWebApp(internalConnector, "/afutest/internal/", config.serverInternalResourceBase(),
            InternalWebMvcSetup.class, DummyController.class);

      /*
       * Create the operation handler.
       */
      createAndRegisterOperationServletHandler(config.serverInternalOperationPort(), "/afutest/", config.serviceName(),
            config.serviceVersion());
   }
   
   
}
