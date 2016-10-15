package se.hms.argos.afutest.infra.simulator;

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
