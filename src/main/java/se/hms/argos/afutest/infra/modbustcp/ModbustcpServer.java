package se.hms.argos.afutest.infra.modbustcp;

/**
* ModbustcpServer.
* 
* <P>This is the ModbustcpServer class that have 3 inputregisters and 1 holding register to use them for testing with argos.
* <P>This class start the ModbusListener to listen to the client.
* @author Hussam Alshammari
* @author Lolita Mageramova
* @version 1.0
*/

import java.net.InetAddress;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * Wrapper for managing an embedded ActiveMQ broker.
 *
 */
@Service
public class ModbustcpServer implements InitializingBean, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(ModbustcpServer.class);

	private final ModbustcpConfig config;
	public ModbusTCPListener listener = null;
	public SimpleProcessImage spi = null;
	
	private  SimpleInputRegister inputRegister1;
	private  SimpleInputRegister inputRegister2;
	private  SimpleInputRegister inputRegister3;
	private  SimpleRegister holdingReg;
		
	
	/**
	 * The default constructor.  
	 * Initializes config variable of the ModbustcpServer.
	 * @param config - Modbus/tcpconfig object
	 */
	public ModbustcpServer(ModbustcpConfig config) throws Exception
	{
		this.config = config;
	}

	/**
	 * AfterPropertiesSet Method
	 * AfterPropertiesSet start the simulator by calling the start() method
	 */
	@Override
	public void afterPropertiesSet() 
	{
		start();
	}
	
	/**
	 * start Method
	 * This method start the Modbuslistener and Initializes the 3 inputregisters and one holding register.
	 * @return boolean value - the method return boolean value to test if it started with errors or not.
	 */
	protected boolean start() {



		try {
			logger.info("ModbustcpServer starting");
			holdingReg     = new SimpleRegister(251); 
			inputRegister1 = new SimpleInputRegister(30);
			inputRegister2 = new SimpleInputRegister(40);
			inputRegister3 = new SimpleInputRegister(50);

			// 2. Prepare a process image
			spi = new SimpleProcessImage();
			//spi.addRegister(new SimpleRegister(251));
			spi.addRegister(holdingReg);///holding
			
			spi.addInputRegister(inputRegister1); 
			spi.addInputRegister(inputRegister2);
			spi.addInputRegister(inputRegister3);
			
			// 3. Set the image on the coupler
			ModbusCoupler.getReference().setProcessImage(spi);
			ModbusCoupler.getReference().setMaster(false);
			ModbusCoupler.getReference().setUnitID(15);

			listener = new ModbusTCPListener(5);
			listener.setAddress(InetAddress.getByName("0.0.0.0"));
			listener.setPort(config.modbusTcpServerPort());
			listener.start();
			
			
			
			logger.info("This Slave have   " + spi.getRegisterCount() + " register");
			logger.info("This Slave have " + spi.getInputRegisterCount() + " inputregisters");
			

			return true;
		} catch (Exception e) {
			logger.error("Modbus problem", e);
			return false;
		}
	}

	@Override
	public void destroy() throws Exception {
		listener.stop();
	}

	public SimpleInputRegister getInputRegister1() {
		return inputRegister1;
	}

	public SimpleInputRegister getInputRegister2() {
		return inputRegister2;
	}

	public SimpleInputRegister getInputRegister3() {
		return inputRegister3;
	}

	public SimpleRegister getHoldingReg() {
		return holdingReg;
	}


}
