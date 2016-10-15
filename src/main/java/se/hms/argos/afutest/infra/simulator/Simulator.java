package se.hms.argos.afutest.infra.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import se.hms.argos.afutest.infra.modbustcp.ModbustcpServer;
import se.hms.argos.afutest.infra.simulator.LoggedParameterData;

import se.hms.argos.afutest.infra.simulator.ModbustcpConfig;
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
	private String[][] parameterId;
	private int savedInputReg1, savedInputReg2, savedInputReg3;

	/** Value - {@value}, timer object to count the time of the game. */
	private Timer inputRegtimer;
	private Timer apiValtimer;
	private Timer loggedValtimer;

	private final int DELAY1 = 120000; // 3600 000 for one hour
	private final int DELAY2 = 130000;
	private final int DELAY3 = 190000;//7200000; // two hours

	private LoggedParameterData account;
	private String timeShifting;

	public Simulator(ModbustcpConfig c) throws Exception {
		config = c;
		inputRegtimer = new Timer(DELAY1, new MyTimerActionListener());
		apiValtimer = new Timer(DELAY2, new MyTimerActionListener2());
		loggedValtimer = new Timer(DELAY3, new MyTimerActionListener3());
		savedInputReg1 = (ModbustcpServer.inputRegister1).getValue();
		savedInputReg2 = (ModbustcpServer.inputRegister2).getValue();
		savedInputReg3 = (ModbustcpServer.inputRegister3).getValue();
		timeShifting = CurrentTime.getCurretTime();
		System.out.print("Simulator .........................");
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
		loggedValtimer.start();

	}

	@Override
	public void destroy() throws Exception {

	}

	private int i = 0;

	private void changeInputRegister() {
		// for (int i =0; i < 30 ; i++){
		/// TODO: Run every hour so that inputregister will change everytime

		i++;
		int iR1 = (ModbustcpServer.inputRegister1).getValue();
		int iR2 = (ModbustcpServer.inputRegister2).getValue();
		int iR3 = (ModbustcpServer.inputRegister3).getValue();

		ModbustcpServer.inputRegister1.setValue(++iR1);
		ModbustcpServer.inputRegister2.setValue(++iR2);
		ModbustcpServer.inputRegister3.setValue(++iR3);

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
				valueValidation();
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
			} catch (NullPointerException e) {
				System.out.println("Caught the NullPointerException");
			}

		}

	}

	protected boolean valueValidation() {

		logger.info("Simulator starting");

		System.out.println("New Test Of Argos And Modbus Values");
		String accessKey = "51F2531794288EBA64764B38D2516890";
		/*
		 * String systemId = "003011FAE2BA"; used to test purposes only String
		 * parameterID1 = "66261.9269.172526"; String parameterID2 =
		 * "66261.9269.173394"; String parameterID3 = "66261.9269.173393";
		 */
		account = new LoggedParameterData(accessKey);
		if (i == 1) {

			systemId = account.getSystemID();
			parameterId = account.getParametersID(systemId);
		}

		String[][] inputRegisterId = account.getInputRegister(systemId, parameterId[0][1], parameterId[1][1],
				parameterId[2][1]);
		boolean testStatus = false;
		for (int i = 0; i < inputRegisterId.length; i++) {
			for (int j = 0; j < 1; j++) {
				// System.out.println(inputRegisterId[i][j] + "
				// "+parameterId[i]);
				if (inputRegisterId[i][j].equals(parameterId[i][j + 1])) {

					System.out.println(inputRegisterId[i][j] + " == " + parameterId[i][j + 1]);
					int val = Integer.parseInt(inputRegisterId[i][j + 1]);

					if (parameterId[i][j].equals("test1")) {
						System.out.println(
								parameterId[i][j] + " " + val + " ==  " + ModbustcpServer.inputRegister1.getValue()
										+ " ===> " + (testStatus = val == ModbustcpServer.inputRegister1.getValue()));
					} else if (parameterId[i][j].equals("test2")) {
						System.out.println(
								parameterId[i][j] + " " + val + " == " + ModbustcpServer.inputRegister2.getValue()
										+ " ===> " + (testStatus = val == ModbustcpServer.inputRegister2.getValue()));
					} else if (parameterId[i][j].equals("test3")) {
						System.out.println(
								parameterId[i][j] + " " + val + " ==  " + ModbustcpServer.inputRegister3.getValue()
										+ " ===> " + (testStatus = val == ModbustcpServer.inputRegister3.getValue()));
					}

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

	protected void loggedDataCheck() {
		String []loggeddata = new String [3];
		String [][] temparr;
		for (int i = 0; i < parameterId.length; i++)  
		{
			temparr= account.getLoggedData(systemId, parameterId[i][i],"2016-10-14T13:00:0020UTC","2016-10-14T15:54:0020");
			loggeddata[i]  =  temparr[i][++i];
			System.out.println(loggeddata[i] );
		}
	}

}
