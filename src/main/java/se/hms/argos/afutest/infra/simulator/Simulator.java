package se.hms.argos.afutest.infra.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import ch.qos.logback.core.net.SyslogOutputStream;
import se.hms.argos.afutest.infra.modbustcp.ModbustcpServer;
import se.hms.argos.afutest.infra.simulator.LoggedParameterData;

import se.hms.argos.common.server.spring.mvc.MediaTypeConstants;

@RequestMapping(produces = MediaTypeConstants.APPLICATION_JSON)
public class Simulator implements InitializingBean, DisposableBean {

	@Autowired
	MetricRegistry metricRegistry;

	// Metric counters for how many test is running
	private final Counter counterTest = new Counter();
	// Metric counters for successful test
	private final Counter counterSuccess = new Counter();
	// Metric counters for unsuccessful test
	private final Counter counterFail = new Counter();

	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);
	private final ModbustcpConfig config;

	private String systemId;
	private String[] parameterTag;
	private int savedInputReg1, savedInputReg2, savedInputReg3;

	/** Value - {@value}, timer object to count the time of the game. */
	private Timer inputRegtimer;
	private Timer apiValtimer;
	private Timer loggedValAtimer;
	private Timer loggedValBtimer;
	private Timer alarmtimer;
						
	private final int DELAY1 = 37890000; // 3600 000 for one hour
	private final int DELAY2 = 3899999;
	private final int DELAY3A = 7200000;// 7499 099;//7200 000; // two hours
										// //10 800 000 tre hours
	private final int DELAY3B = 7200000;// 7200 000; // two hours //10 800 000
										// tre hours

	private final int DELAY3 = 300000 ; //5min
	
	
	private LoggedParameterData account;
	private String startTime;
	private String endTime;
	private int[][] SavedInputReg = new int[2][3];

	public Simulator(ModbustcpConfig c) throws Exception {
		config = c;
		inputRegtimer = new Timer(DELAY1, new MyTimerActionListener());
		apiValtimer = new Timer(DELAY2, new MyTimerActionListener2());
		loggedValAtimer = new Timer(DELAY3A, new MyTimerActionListener3());
		loggedValBtimer = new Timer(DELAY3B, new MyTimerActionListener3());
		alarmtimer = new Timer(DELAY3, new MyTimerActionListeneralarm());
		savedInputReg1 = (ModbustcpServer.inputRegister1).getValue();
		savedInputReg2 = (ModbustcpServer.inputRegister2).getValue();
		savedInputReg3 = (ModbustcpServer.inputRegister3).getValue();
		startTime = CurrentTime.getCurretTime(true);
		System.out.print("Simulator cons...........0000.............." + savedInputReg1);
		String accessKey = "51F2531794288EBA64764B38D2516890";
		account = new LoggedParameterData(accessKey);
		systemId = account.getSystemID();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("EfterpropertiesSet..........................");
		// Register metrics
		if (metricRegistry != null) {
			metricRegistry.register("example.test", counterTest);
			metricRegistry.register("example.success", counterSuccess);
			metricRegistry.register("example.fail", counterFail);

		}

		inputRegtimer.start();
		apiValtimer.start();
		loggedValAtimer.start();
		alarmtimer.start();

	}

	@Override
	public void destroy() throws Exception {
		// inputRegtimer.stop();
		// apiValtimer.stop();
		// loggedValAtimer.stop();
	}

	private int i = 0;

	private void changeInputRegister() {
		// for (int i =0; i < 30 ; i++){
		/// TODO: Run every hour so that inputregister will change everytime

		int iR1 = (ModbustcpServer.inputRegister1).getValue();
		int iR2 = (ModbustcpServer.inputRegister2).getValue();
		int iR3 = (ModbustcpServer.inputRegister3).getValue();
		// SavedInputReg[1] = SavedInputReg[0].clone();
		i++;
		ModbustcpServer.inputRegister1.setValue((++iR1));
		ModbustcpServer.inputRegister2.setValue((++iR2));
		ModbustcpServer.inputRegister3.setValue((++iR3));

		// System.out.println(Arrays.deepToString(SavedInputReg));

		counterTest.inc();
		System.out.println("increment by one the inputregisters of the modbus.  Number of test  = " + i);

	}

	/**
	 * This is an ActionListener that invoked every second to update the time to
	 * update inputregister. The listener is added by the timer object.
	 */
	class MyTimerActionListener implements ActionListener {
		/**
		 * This method runs every second and updates the timer in the calling
		 * board object.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println();
			changeInputRegister();
		}
	}

	class MyTimerActionListener2 implements ActionListener {
		/**
		 * This method runs every second and updates the timer in the calling
		 * board object.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			try {
				liveValValidation();
			} catch (NullPointerException e) {
				System.out.println("Caught the NullPointerException");
			}

		}

	}

	class MyTimerActionListener3 implements ActionListener {
		/**
		 * This method runs every second and updates the timer in the calling
		 * board object.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			try {

				loggedDataCheck();

				// 1==50 2==51 3==52 here we call
				savedInputReg1 = (ModbustcpServer.inputRegister1).getValue();
				savedInputReg2 = (ModbustcpServer.inputRegister2).getValue();
				savedInputReg3 = (ModbustcpServer.inputRegister3).getValue();

			} catch (NullPointerException e) {
				System.out.println("Caught the NullPointerException");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	class MyTimerActionListeneralarm implements ActionListener {
		/**
		 * This method runs every second and updates the timer in the calling
		 * board object.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			try {

				alarmDataCheck();
				try {
					Thread.sleep(50000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				writeValLiveCheck();
			} catch (NullPointerException e) {
				System.out.println("Caught the NullPointerException");
			}

		}

	}

	protected boolean liveValValidation() {

		logger.info("Simulator starting");

		System.out.println("New Test Of Argos And Modbus Values");
		String accessKey2 = "747C441E03934628AF2C13B730E4CCDD";
		
		if (i == 1) {

			systemId = account.getSystemID();
			parameterTag = account.getParametersTag(systemId);
			loggedValBtimer.start();
		}

		String[][] inputRegisterVal = account.getInputRegister(systemId, parameterTag[0], parameterTag[1],
				parameterTag[2]);
		boolean testStatus = false;

		for (int i = 0; i < inputRegisterVal.length; i++) {
			for (int j = 0; j < 1; j++) {

				System.out.println(inputRegisterVal[i][j] + " == " + inputRegisterVal[i][j + 1]);
				int val;

				if (inputRegisterVal[i][j].equals("TEST1")) {
					val = Integer.parseInt(inputRegisterVal[i][j + 1]);
					System.out.println(
							inputRegisterVal[i][j] + " " + val + " ==  " + ModbustcpServer.inputRegister1.getValue()
									+ " ===> " + (testStatus = val == ModbustcpServer.inputRegister1.getValue()));
				} else if (inputRegisterVal[i][j].equals("TEST2")) {
					val = Integer.parseInt(inputRegisterVal[i][j + 1]);
					System.out.println(
							inputRegisterVal[i][j] + " " + val + " ==  " + ModbustcpServer.inputRegister2.getValue()
									+ " ===> " + (testStatus = val == ModbustcpServer.inputRegister2.getValue()));
				} else if (inputRegisterVal[i][j].equals("TEST3")) {
					val = Integer.parseInt(inputRegisterVal[i][j + 1]);
					System.out.println(
							inputRegisterVal[i][j] + " " + val + " ==  " + ModbustcpServer.inputRegister3.getValue()
									+ " ===> " + (testStatus = val == ModbustcpServer.inputRegister3.getValue()));
				} else {
					System.out.println("Something went wrong.....");
				}

			}

		}

		if (testStatus) {
			counterSuccess.inc();
			System.out.println("***********************  Test " + i + " is Successful ***********************");
		} else {
			counterFail.inc();
			System.out.println("***********************  Test " + i + " Failed      ***********************");

		}

		return true;
	}

	protected void loggedDataCheck() throws InterruptedException {

		// inputRegtimer.wait(60000); error fix needed
		// apiValtimer.wait(90000);
		endTime = CurrentTime.getOneHourBack();
		boolean test;
		System.out.println("loggeddatacheck running....\t" + startTime + "\t" + endTime);
		// savedInputReg1 Testa de med den loggade värde från API
		// savedInputReg2
		// savedInputReg3
		// "2016-10-14T13:00:0020UTC"
		int temparr[] = account.getLoggedData(systemId, "TEST1", startTime, endTime);

		// test if there is values..
		System.out.println("Size of temparr...." + temparr.length + "savedInputReg1 = " + savedInputReg1);

		if (temparr[1] == savedInputReg1)
			test = true;
		else
			test = false;

		temparr = account.getLoggedData(systemId, "TEST2", startTime, endTime);
		if (temparr[1] == savedInputReg2)
			test = true;
		else
			test = false;

		temparr = account.getLoggedData(systemId, "TEST3", startTime, endTime);
		if (temparr[1] == savedInputReg3)
			test = true;
		else
			test = false;

		if (test) {
			// counterSuccess.inc();
			System.out.println("***********************  LogTest " + (i) + " is Successful ***********************");
		} else {
			// counterFail.inc();
			System.out.println("***********************  LogTest " + (i) + " Failed      ***********************");

		}

	}

	protected void alarmDataCheck() {
		boolean test1 = false, test2 = false, test3 = false;

		String[][] elm = account.alarmItems();

		for (int i = 0; i < elm.length; i++) {
			for (int j = 0; j < 1; j++) {
				//TODO: Add test for info	+ metric
				if (elm[i][j].equals("TEST1ALARM") 
												   && elm[i][j + 2].equals("true")) {
					test1 = true;
				} else if (elm[i][j].equals("TEST2ALARM") 
														  && elm[i][j + 2].equals("true")) {
					test2 = true;
				} else if (elm[i][j].equals("TEST3ALARM") 
														  && elm[i][j + 2].equals("false")) {
					test3 = true;
				}

			}
			
		}
		
		if (test1 && test2 && test3) 
		{
			// counterSuccess.inc();
			System.out.println("***********************  AlarmTest " + (i) + " is Successful ***********************");
		} else {
			// counterFail.inc();
			System.out.println("***********************  AlarmTest " + (i) + " Failed      ***********************");

		}


	}
	
	
	
	public void writeValLiveCheck()
	{
		Random randomObj = new Random();
		System.out.println("Lets see...........Write value to live parameter............");
		System.out.println("modbus = "+ModbustcpServer.holdingReg.getValue());
		String holdingAdress = "HOLDINGT";
		int val = randomObj.ints(251, 300).findFirst().getAsInt();
		String [] liveValArr = 	account.writeLiveValue (systemId,holdingAdress,""+val);
		boolean test1 = false;
		ModbustcpServer.holdingReg.getValue();
		for (int i = 0; i < liveValArr.length; i++) 
		{
				if (liveValArr[i].equals(holdingAdress)) 
				{
					System.out.println("live id = "+liveValArr[i+1]);
					if (liveValArr[i+1].equals((""+ModbustcpServer.holdingReg.getValue())))
							test1 = true;
				}
		}
		
		
		if (test1) 
		{
			// counterSuccess.inc();
			System.out.println("***********************  WriteLiveValTest " + (i) + " is Successful ***********************");
		} else {
			// counterFail.inc();
			System.out.println("***********************  WriteLiveValTest " + (i) + " Failed      ***********************");

		}//fix id with error code
		System.out.println(ModbustcpServer.holdingReg.getValue() + "   after");

		
	}
	

}
