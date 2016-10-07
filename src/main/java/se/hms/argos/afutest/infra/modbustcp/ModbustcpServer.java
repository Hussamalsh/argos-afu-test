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
public class ModbustcpServer implements InitializingBean, DisposableBean
{

   private static final Logger logger = LoggerFactory.getLogger(ModbustcpServer.class);

   private final ModbustcpConfig config;

   public ModbusTCPListener listener = null;
   public SimpleProcessImage spi = null;

   // TODO: Fix how to control registers from other part of the code
   public SimpleRegister publicRegister = new SimpleRegister(252);
   
   
   SimpleInputRegister inputRegister1;
   SimpleInputRegister inputRegister2;
   SimpleInputRegister inputRegister3;
   
   

   public ModbustcpServer(ModbustcpConfig config) throws Exception
   {
      this.config = config;
   }

   @Override
   public void afterPropertiesSet()
   {
      start();
   }

   protected boolean start()
   {
	   
	   /** Value - {@value}, timer object to count the time of the game.*/
 		Timer timer;
 		Timer timer2;
	   
	   
      try
      {
         logger.info("ModbustcpServer starting");
         
       inputRegister1 = new SimpleInputRegister(30);
  	   inputRegister2 = new SimpleInputRegister(40);
  	   inputRegister3 = new SimpleInputRegister(50);
  	   
  		
  		
  		timer= new Timer(60000, new MyTimerActionListener());
  		timer2= new Timer(90000, new MyTimerActionListener2());

         // TODO: Check if this is a good way starting the Modbus/TCP Server

         // 2. Prepare a process image
         spi = new SimpleProcessImage();
         spi.addRegister(new SimpleRegister(251));
         spi.addInputRegister(inputRegister1);

         // 3. Set the image on the coupler
         ModbusCoupler.getReference().setProcessImage(spi);
         ModbusCoupler.getReference().setMaster(false);
         ModbusCoupler.getReference().setUnitID(15);

         listener = new ModbusTCPListener(3);
         listener.setAddress(InetAddress.getByName("0.0.0.0"));
         listener.setPort(config.modbusTcpServerPort());
         listener.start();

         // Register more after start.
         // TODO: Test if this works
         // TODO: How can registers be controlled in a better way?
         spi.addRegister(publicRegister);
         spi.addInputRegister(inputRegister2);
         spi.addRegister(new SimpleRegister(253));
         spi.addInputRegister(inputRegister3);
         
         timer.start();
         timer2.start();

         // TODO: Is it possible to know when a client read values?

         return true;
      }
      catch (Exception e)
      {
         logger.error("Modbus problem", e);
         return false;
      }
   }

   @Override
   public void destroy() throws Exception
   {
      listener.stop();
   }
   
   int i =0;
	private void changeInputRegister()
	{
        //for (int i =0; i < 30 ; i++){
       	 /// TODO: KÖr varje timme så värdet (inputregister) ändras hela tiden varje timme för alla de 3 register 
		i++;
       	 inputRegister1.setValue(i+(inputRegister1).getValue());
       	 inputRegister2.setValue(i+(inputRegister2).getValue());
       	 inputRegister3.setValue(i+(inputRegister3).getValue());
       	 //TimeUnit.SECONDS.sleep(40);
       	 System.out.println("new one = " + i);
       // }
		
	}
	
	/**
	 * This is an ActionListener that invoked every second to update the time
	 * to pdate inputregister. The listener is added by the timer object.
	 */
	class MyTimerActionListener implements ActionListener {
		/**
		 * This method runs every second and updates the timer in the calling board
		 * object.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
			changeInputRegister();
		}
	}
	
	class MyTimerActionListener2 implements ActionListener {
		/**
		 * This method runs every second and updates the timer in the calling board
		 * object.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
			valueValidation();
		}


	}
	
	private void valueValidation() 
	{
		System.out.println("New Check Of Argos and Modbus values");
	      String accessKey = "51F2531794288EBA64764B38D2516890";
	     /* String systemId = "003011FAE2BA";               used to test purposes only
	      String parameterID1 = "66261.9269.172526";
	      String parameterID2 = "66261.9269.173394";
	      String parameterID3 = "66261.9269.173393";*/
		
		LoggedParameterData account = new LoggedParameterData(accessKey); 
		String systemId = account.getSystemID();
		String []parameterId = account.getParametersID(systemId);
		int index = 0;

		String [][]inputRegisterId  = account.getInputRegister(systemId,parameterId[0],parameterId[1], parameterId[2]);


		
		/*for(String[] arr2: inputRegisterId)
		{
			System.out.println("out array");
		    for(String val: arr2){
		    	System.out.println("inner array");
		        System.out.println(val);
		    }
		}*/
		
		for(int i = 0; i < inputRegisterId.length; i++)
		{
			// TODO: fix the array parameterID in the class LoggedParamData by adding two dimensions array [parametid][name]  H
			//		so we can compare by names
			for(int j = 0; j < 1; j++)
			{
				//System.out.println(inputRegisterId[i][j] + " " + parameterId[i]);
				if (inputRegisterId[i][j].equals(parameterId[i]))
				{
					
					System.out.println(inputRegisterId[i][j] + " T " + parameterId[i]);
					int val = Integer.parseInt(inputRegisterId[i][j+1]);
					
					if(account.getName(i).equals("test1"))
					{
						System.out.println(val + " ==  " + inputRegister1.getValue() +" ===> "+ (val == inputRegister1.getValue() ) );
					}else if(account.getName(i).equals("test2"))
					{
						System.out.println(val + " ==  " + inputRegister2.getValue() +" ===> "+ (val == inputRegister2.getValue() ) );
					} else if(account.getName(i).equals("test3"))
					{
						System.out.println(val + " ==  " + inputRegister3.getValue() +" ===> "+ (val == inputRegister3.getValue() ) );
					} 
			
						
				}
					
			}
			
		}
		
		
	}
	
	
	
}
