package se.hms.argos.afutest.infra.http.internal.api.rest.controller;

/**
* InternalWebMvcSetup.
* 
* <P>This is the DummyController class that have autowired Metric register to count how many time the dummy function was called
*  
* @author HMS
* @version 1.0
*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import se.hms.argos.common.server.spring.mvc.MediaTypeConstants;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

@RestController    ///will intercept GET request to "/" to wi
@RequestMapping(produces = MediaTypeConstants.APPLICATION_JSON)
public class DummyController implements InitializingBean
{
   @Autowired
   MetricRegistry metricRegistry;

   private static final Logger logger = LoggerFactory.getLogger(DummyController.class);

   // Metric counters
   private final Counter dummyCounter = new Counter();


   @Override
   public void afterPropertiesSet() throws Exception
   {
      // Register metrics
      if (metricRegistry != null)
      {
         metricRegistry.register("example.dummy", dummyCounter);
         
      }
   }

   @RequestMapping(value = "dummy", method = RequestMethod.GET)
   public String getDummy()
   {
      // Log information
      logger.info("Dummy function call");
      // Increase a metric counter
      dummyCounter.inc();
      // Return some information
      return "DUMMY";
   }
}
