package se.hms.argos.afutest.infra.modbustcp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Wrapper for managing an embedded ActiveMQ broker.
 *
 */
public class ModbustcpServer implements InitializingBean, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(ModbustcpServer.class);

	private final ModbustcpConfig config;

	public ModbusTCPListener listener = null;
	public SimpleProcessImage spi = null;

	// TODO: Fix how to control registers from other part of the code
	public SimpleRegister publicRegister = new SimpleRegister(252);

	public static SimpleInputRegister inputRegister1;
	public static SimpleInputRegister inputRegister2;
	public static SimpleInputRegister inputRegister3;
	
	public static SimpleRegister holdingReg;
		
	
	
	public ModbustcpServer(ModbustcpConfig config) throws Exception
	{
		this.config = config;
	}

	@Override
	public void afterPropertiesSet() 
	{
		start();
	}

	protected boolean start() {



		try {
			logger.info("ModbustcpServer starting");
			holdingReg =	 new SimpleRegister(251); 
			inputRegister1 = new SimpleInputRegister(30);
			inputRegister2 = new SimpleInputRegister(40);
			inputRegister3 = new SimpleInputRegister(50);

			// TODO: Check if this is a good way starting the Modbus/TCP Server

			// 2. Prepare a process image
			spi = new SimpleProcessImage();
			//spi.addRegister(new SimpleRegister(251));
			spi.addRegister(holdingReg);///holding
			spi.addInputRegister(inputRegister1); 


			// 3. Set the image on the coupler
			ModbusCoupler.getReference().setProcessImage(spi);
			ModbusCoupler.getReference().setMaster(false);
			ModbusCoupler.getReference().setUnitID(15);

			listener = new ModbusTCPListener(5);
			listener.setAddress(InetAddress.getByName("0.0.0.0"));
			listener.setPort(config.modbusTcpServerPort());
			listener.start();
		
			spi.addInputRegister(inputRegister2);
			spi.addInputRegister(inputRegister3);
			



			// TODO: Is it possible to know when a client read values?

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



}
