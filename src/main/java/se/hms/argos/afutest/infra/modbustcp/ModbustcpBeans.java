package se.hms.argos.afutest.infra.modbustcp;

/**
* ModbustcpBeans.
* 
* <P>This is the ModbustcpBeans class that set the configuration of Modbus server and create object to start the server.
*  
* @author HMS
* @version 1.0
*/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.hms.argos.common.owner.utils.OwnerUtils;

@Configuration
public class ModbustcpBeans {
	private static ModbustcpServer mModbustcpServer;

	@Bean
	public ModbustcpServer modbustcpServer() throws Exception {
		ModbustcpConfig config = OwnerUtils.getOrCreate(ModbustcpConfig.class);
		return mModbustcpServer = new ModbustcpServer(config);
	}

	public static <T> T getBean() {
		return (T) mModbustcpServer;
	}

}
