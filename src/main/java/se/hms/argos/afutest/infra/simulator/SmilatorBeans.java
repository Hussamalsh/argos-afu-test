package se.hms.argos.afutest.infra.simulator;

/**
* SmilatorBeans.
* 
* <P>This is the SmilatorBeans class that set the configuration of the simulator class and create an object to start the simulator.
*  
* @author HMS
* @version 1.0
*/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import se.hms.argos.common.owner.utils.OwnerUtils;

@Configuration
public class SmilatorBeans
{
   @Bean
   public Simulator simulator() throws Exception
   {
      ModbustcpConfig config = OwnerUtils.getOrCreate(ModbustcpConfig.class);
      return new Simulator(config);
   }
}
