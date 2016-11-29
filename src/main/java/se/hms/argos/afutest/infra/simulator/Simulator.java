package se.hms.argos.afutest.infra.simulator;

/**
* The Simulator Class
* 
* <P>This is the Simulator class where the all actions happen, because this class test the combination of Netbiter and Argos server.
* <P>It provides efficient methods for testing and have a MetricRegistry object which counts the successful and failed tests. 
* <P>This class control values and validate thats everything is working as it should between the Modbus, Netbiter and Argos server
* @author Hussam Alshammari
* @author Lolita Mageramova
* @version 1.0
*/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Random;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import se.hms.argos.afutest.infra.modbustcp.ModbustcpBeans;
import se.hms.argos.afutest.infra.modbustcp.ModbustcpServer;
import se.hms.argos.api.client.rest.ArgosDataCenter;
import se.hms.argos.common.server.spring.mvc.MediaTypeConstants;

@RequestMapping(produces = MediaTypeConstants.APPLICATION_JSON)
public class Simulator implements InitializingBean, DisposableBean {

	@Autowired
	MetricRegistry metricRegistry; // MetricRegistry object used later to have counters of the test

	private ModbustcpServer mModbustcpServer = ModbustcpBeans.getBean(); // Gets the Modbus server object

	// Metric counters for how many test is running
	private final Counter counterTest = new Counter();
	// Metric counters for successful test
	private final Counter counterSuccess = new Counter();
	// Metric counters for unsuccessful test
	private final Counter counterFail = new Counter();

	// Metric counters for successful test for log
	private final Counter counterSuccessLog = new Counter();
	// Metric counters for unsuccessful test for log
	private final Counter counterFailLog = new Counter();

	// Metric counters for successful test for alarm
	private final Counter counterSuccessAlarm = new Counter();
	// Metric counters for unsuccessful test for alarm
	private final Counter counterFailAlarm = new Counter();

	// Metric counters for successful test for live value
	private final Counter counterSuccessLiveVal = new Counter();
	// Metric counters for unsuccessful test for live value
	private final Counter counterFailLiveVal = new Counter();

	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);
	private final ModbustcpConfig config;

	private String systemId; // The unique id of a system. 
	//Array that have all the parameters of the system
	private String[] parameterTag;
	private int savedInputReg1, savedInputReg2, savedInputReg3; // all the three variabels will hold the inputregister of the slave

	/** Value - {@value}, timer object to count the time of the test. */
	private Timer inputRegtimer;
	private Timer loggedValAtimer;
	private Timer loggedValBtimer;
	private Timer alarmtimer;

	private final int DELAY1 = 3789000; // 3600 000 = for one hour
	private final int DELAY3A = 7200000;// 7200 000 = two hours
	private final int DELAY3B = 7200000;// 7200 000 = two hours 
	private final int DELAY3 = 300000; // 5min

	private ArgosDataCenter account; 
	private String startTime;  //The start date and time in UTC. 
	private String endTime;  //The end date and time in UTC. Can be used as start date in next call to this method. 

	private int counter1, counter2 = 0;  // Count how many time the test was running

	
	/**
	 * The default constructor.  
	 * Initializes config variable of the simulator and other important variables and objects.
	 * 
	 * @param config - Modbus/tcpconfig object
	 */
	public Simulator(ModbustcpConfig c) throws Exception {
		config = c;
		inputRegtimer = new Timer(DELAY1, new MyTimerActionListener());
		loggedValAtimer = new Timer(DELAY3A, new MyTimerActionListener3());
		loggedValBtimer = new Timer(DELAY3B, new MyTimerActionListener3());
		alarmtimer = new Timer(DELAY3, new MyTimerActionListeneralarm());

		savedInputReg1 = (mModbustcpServer.getInputRegister1()).getValue();
		savedInputReg2 = (mModbustcpServer.getInputRegister2()).getValue();
		savedInputReg3 = (mModbustcpServer.getInputRegister3()).getValue();
		startTime = CurrentTime.getCurretTime(true);
		account = new ArgosDataCenter();
		systemId = account.getSystemID();
	}

	/**
	 * AfterPropertiesSet method.  
	 * Initializes metricRegistry variables and it starts the timers. 
	 * 
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// Register metrics
		if (metricRegistry != null) {
			metricRegistry.register("example.testcounter", counterTest);

			metricRegistry.register("example.successLiveVal", counterSuccess);
			metricRegistry.register("example.failLiveVal", counterFail);
			metricRegistry.register("example.successLogValTest", counterSuccessLog);
			metricRegistry.register("example.failLogValTest", counterFailLog);
			metricRegistry.register("example.successAlarmTest", counterSuccessAlarm);
			metricRegistry.register("example.failAlarmTest", counterFailAlarm);
			metricRegistry.register("example.successWLivevalueTest", counterSuccessLiveVal);
			metricRegistry.register("example.failWLivevalueTest", counterFailLiveVal);

		}

		inputRegtimer.start();
		loggedValAtimer.start();
		alarmtimer.start();
	}


	@Override
	public void destroy() throws Exception {

	}
	
	/**
	 * ChangeInputRegister method.  
	 * This metheod increase the 3 inputregisters of the Modbus slave and increase the counter of the metricregister.
	 * 
	 */
	private void changeInputRegister() {

		int iR1 = (mModbustcpServer.getInputRegister1()).getValue();
		int iR2 = (mModbustcpServer.getInputRegister2()).getValue();
		int iR3 = (mModbustcpServer.getInputRegister3()).getValue();
		System.out.println(" Old Values == iR1 = " + iR1 + " iR2 = " + iR2 + "iR3 = " + iR3);
		counter1++;
		mModbustcpServer.getInputRegister1().setValue((++iR1));
		mModbustcpServer.getInputRegister2().setValue((++iR2));
		mModbustcpServer.getInputRegister3().setValue((++iR3));

		System.out.println(" New Values == iR1 = " + mModbustcpServer.getInputRegister1().getValue() + " iR2 = "
				+ mModbustcpServer.getInputRegister2().getValue() + " iR3 = "
				+ mModbustcpServer.getInputRegister1().getValue());

		counterTest.inc();
		System.out.println("increment by one the inputregisters of the modbus.  Number of test  = " + counter1);

	}

	/**
	 * This is an ActionListener that invoked every second to update the time to
	 * update inputregister. The listener is added by the timer object.
	 */
	class MyTimerActionListener implements ActionListener {
		/**
		 * This method runs every second and updates the timer in the calling
		 * object.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("changeInputRegister(); RUNNING");
			changeInputRegister();
			try {
				Thread.sleep(60000);
				liveValValidation();
			} catch (NullPointerException e) {
				System.out.println("Caught the NullPointerException");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This is an ActionListener3 that invoked every second to update the time to
	 * update inputregister. The listener is added by the timer object.
	 */
	class MyTimerActionListener3 implements ActionListener {
		/**
		 * This method runs every second and updates the timer in the calling
		 * board object.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {

				loggedDataCheck();

				
				savedInputReg1 = (mModbustcpServer.getInputRegister1()).getValue();
				savedInputReg2 = (mModbustcpServer.getInputRegister2()).getValue();
				savedInputReg3 = (mModbustcpServer.getInputRegister3()).getValue();

			} catch (NullPointerException e) {
				System.out.println("Caught the NullPointerException");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}
	
	/**
	 * This is an ActionListeneralarm that invoked every second to update the time to
	 * update inputregister. The listener is added by the timer object.
	 */
	class MyTimerActionListeneralarm implements ActionListener {
		/**
		 * This method runs every second and updates the timer in the calling
		 * board object.
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {

				
				 alarmDataCheck();
				 try {
					  Thread.sleep(50000); 
					 }
				 catch(InterruptedException e) 
				 { e.printStackTrace(); }
				 writeValLiveCheck(); counter2++;
				 
			} catch (NullPointerException e) {
				System.out.println("Caught the NullPointerException");
			}

		}

	}
	
	/**
	 * This is an liveValValidation method.
	 * It compares values from Argos server with  Modbus slave.
	 * @return boolean - return boolean value true/false if test fails or succeed
	 * 
	 */
	protected boolean liveValValidation() {

		logger.info("Simulator starting");

		System.out.println("New Test Of Argos And Modbus Values");

		if (counter1 == 1) {
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
					System.out.println(inputRegisterVal[i][j] + " " + val + " ==  "
							+ mModbustcpServer.getInputRegister1().getValue() + " ===> "
							+ (testStatus = val == mModbustcpServer.getInputRegister1().getValue()));
				} else if (inputRegisterVal[i][j].equals("TEST2")) {
					val = Integer.parseInt(inputRegisterVal[i][j + 1]);
					System.out.println(inputRegisterVal[i][j] + " " + val + " ==  "
							+ mModbustcpServer.getInputRegister2().getValue() + " ===> "
							+ (testStatus = val == mModbustcpServer.getInputRegister2().getValue()));
				} else if (inputRegisterVal[i][j].equals("TEST3")) {
					val = Integer.parseInt(inputRegisterVal[i][j + 1]);
					System.out.println(inputRegisterVal[i][j] + " " + val + " ==  "
							+ mModbustcpServer.getInputRegister3().getValue() + " ===> "
							+ (testStatus = val == mModbustcpServer.getInputRegister3().getValue()));
				} else {
					System.out.println("Something went wrong.....");
				}

			}

		}

		if (testStatus) {
			counterSuccess.inc();
			System.out.println(
					"***********************  TestliveValue " + counter1 + " is Successful ***********************");
		} else {
			counterFail.inc();
			System.out.println(
					"***********************  TestliveValue " + counter1 + " Failed      ***********************");

		}

		return true;
	}
	/**
	 * LoggedDataCheck method.
	 * It compares logged values from Argos server with  Modbus slave.
	 * 
	 */
	protected void loggedDataCheck() throws InterruptedException {

		endTime = CurrentTime.getOneHourBack();
		boolean test;
		System.out.println("loggeddatacheck running....\t" + startTime + "\t" + endTime);

		int temparr[] = account.getLoggedData(systemId, "TEST1", startTime, endTime);

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
			counterSuccessLog.inc();
			System.out.println(
					"***********************  LogTest " + (counter1) + " is Successful ***********************");
		} else {
			counterFailLog.inc();
			System.out
					.println("***********************  LogTest " + (counter1) + " Failed      ***********************");

		}

	}
	/**
	 * AlarmDataCheck method.
	 * It checks if the alarm was triggered or not and concludes if it was successful or not.
	 * 
	 */
	protected void alarmDataCheck() {

		boolean test1 = false, test2 = false, test3 = false;

		String[][] elm = account.alarmItems();

		for (int i = 0; i < elm.length; i++) {
			for (int j = 0; j < 1; j++) {
				if (elm[i][j].equals("TEST1ALARM") && elm[i][j + 2].equals("true")) {
					test1 = true;
				} else if (elm[i][j].equals("TEST2ALARM") && elm[i][j + 2].equals("true")) {
					test2 = true;
				} else if (elm[i][j].equals("TEST3ALARM") && elm[i][j + 2].equals("false")) {
					test3 = true;
				}

			}

		}

		if (test1 && test2 && test3) {
			counterSuccessAlarm.inc();
			System.out.println(
					"***********************  AlarmTest " + (counter2) + " is Successful ***********************");
		} else {
			counterFailAlarm.inc();
			System.out.println(
					"***********************  AlarmTest " + (counter2) + " Failed      ***********************");

		}

	}
	/**
	 * WriteValLiveCheck method.
	 * A value is written in order to change holding register value. 
	 * It tests if a value was changed correctly or not.
	 * 
	 */
	public void writeValLiveCheck() {
		Random randomObj = new Random();
		System.out.println("Lets see...........Write value to live parameter............");
		System.out.println("modbus = " + mModbustcpServer.getHoldingReg().getValue());
		String holdingAdress = "HOLDINGT";
		int val = randomObj.ints(251, 300).findFirst().getAsInt();
		String[] liveValArr = account.writeLiveValue(systemId, holdingAdress, "" + val);
		boolean test1 = false;
		mModbustcpServer.getHoldingReg().getValue();
		for (int i = 0; i < liveValArr.length; i++) {
			if (liveValArr[i].equals(holdingAdress)) {
				System.out.println("live id = " + liveValArr[i + 1]);
				if (liveValArr[i + 1].equals(("" + mModbustcpServer.getHoldingReg().getValue())))
					test1 = true;
			}
		}
		if (test1) {
			counterSuccessLiveVal.inc();
			System.out.println("***********************  WriteLiveValTest " + (counter2)
					+ " is Successful ***********************");
		} else {
			counterFailLiveVal.inc();
			System.out.println(
					"***********************  WriteLiveValTest " + (counter2) + " Failed      ***********************");

		} 
		System.out.println(mModbustcpServer.getHoldingReg().getValue() + "   after");
	}

}
