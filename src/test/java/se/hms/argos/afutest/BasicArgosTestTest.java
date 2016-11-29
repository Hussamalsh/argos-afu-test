/*
 * FIXME: This is an example, remove after usage.
 */

package se.hms.argos.afutest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.annotation.Configuration;

import se.hms.argos.afutest.boot.ArgosTestConfig;
import se.hms.argos.afutest.boot.ArgosTestServer;
import se.hms.argos.afutest.infra.modbustcp.ModbustcpBeans;
import se.hms.argos.afutest.infra.modbustcp.ModbustcpConfig;
import se.hms.argos.afutest.infra.modbustcp.ModbustcpServer;
import se.hms.argos.common.owner.utils.OwnerUtils;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

@Configuration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicArgosTestTest {
	private static final Log logger = LogFactory.getLog(BasicArgosTestTest.class);
	private ModbustcpServer mModbustcpServer = ModbustcpBeans.getBean();

	private static ArgosTestServer server;
	private static ArgosTestConfig config;
	private static String internalBase;
	private static String operationBase;

	private static TCPMasterConnection m_Connection;
	private static InetAddress m_SlaveAddress;
	private static ModbusTCPTransaction m_Transaction;
	private static ReadInputRegistersRequest m_ReadInputRegistersRequest;
	private static ReadMultipleRegistersRequest m_ReadMultipleRegistersRequest;
	private static WriteSingleRegisterRequest m_WriteSingleRegisterRequest;
	private boolean m_Reconnecting = false;
	private static ModbustcpConfig configTCP;

	// This is how we can override the default configuration. This class
	// could be extracted and used in other test classes as well. If we
	// needed to run tests with varying property values, we could
	// also extend "Mutable", and update the property within the tests.
	public interface TestArgosTestConfig extends ArgosTestConfig {
		@Override
		@DefaultValue("4")
		public int serverThreadpoolSizeMax();
	}

	@BeforeClass
	public static void setup() throws Exception {
		logger.info("\n\nStarting service for tests\n\n");

		// Setup SLF4J bridge so that anyone using JUL will be redirected to
		// SLF4
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		// Create default configuration and update it with system properties
		config = OwnerUtils.getOrCreate(TestArgosTestConfig.class);
		configTCP = OwnerUtils.getOrCreate(ModbustcpConfig.class);

		internalBase = String.format("http://127.0.0.1:%s/afutest/internal", config.serverInternalPort());
		operationBase = String.format("http://127.0.0.1:%s/afutest/operation", config.serverInternalOperationPort());

		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

		try {
			m_SlaveAddress = InetAddress.getByName("0.0.0.0");
			m_Connection = new TCPMasterConnection(m_SlaveAddress);
			m_ReadInputRegistersRequest = new ReadInputRegistersRequest();
			m_ReadMultipleRegistersRequest = new ReadMultipleRegistersRequest();
			m_WriteSingleRegisterRequest = new WriteSingleRegisterRequest();
			m_Connection.setPort(configTCP.modbusTcpServerPort());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e.getMessage());
		}

		// Create and start server
		server = new ArgosTestServer(config);
		server.start();
	}

	@AfterClass
	public static void teardown() throws Exception {
		if (m_Connection != null && m_Connection.isConnected()) {
			m_Connection.close();
			m_Transaction = null;
		}

		logger.info("\n\nStopping service after tests\n\n");
		server.stop();
	}

	@Test
	public void serviceInfo() {
		given().baseUri(operationBase).when().get("info").then().statusCode(200).contentType(ContentType.JSON)
				.body("version", equalTo(config.serviceVersion())).body("name", equalTo(config.serviceName()));
	}

	@Test
	public void serviceConfig() {
		given().baseUri(operationBase).when().get("info/config").then().statusCode(200).body(
				"\"server_internal_operation_port\"", equalTo(Integer.toString(config.serverInternalOperationPort())));
	}

	@Test
	public void internalDummy() {
		String responseText = given().baseUri(internalBase).when().get("dummy").then().statusCode(200)
				.contentType(ContentType.JSON).extract().asString();
		Assert.assertEquals("DUMMY", responseText);
	}

	@Test
	public void metricTest() {
		given().baseUri(internalBase).when().get("dummy").then().statusCode(200);
		given().baseUri(operationBase).when().get("diagnostic/metrics").then().statusCode(200)
				.contentType(ContentType.JSON).body("counters.\"example.dummy\".count", greaterThanOrEqualTo(1));
	}

	@Test
	public void operationPingTest() {
		given().baseUri(operationBase).when().get("diagnostic/ping").then().statusCode(200)
				.contentType(ContentType.TEXT).body(notNullValue());
	}

	@Test
	public void operationDiagnosticMetrics() {
		given().baseUri(operationBase).when().get("diagnostic/metrics").then().statusCode(200)
				.contentType(ContentType.JSON).body("version", equalTo("3.0.0"));
	}

	@Test
	public void operationDiagnosticHealthcheck() {
		given().baseUri(operationBase).when().get("diagnostic/healthcheck").then().contentType(ContentType.JSON)
				.body("\"jvm.dead_lock\".healthy", equalTo(true));
	}

	@Test
	public void holdingRegisterTest() {
		System.out.println("H1" + mModbustcpServer.getHoldingReg().getValue());
		assertEquals("Holding register in startup must be 251", 251, mModbustcpServer.getHoldingReg().getValue());
		mModbustcpServer.getHoldingReg().setValue(252);
		assertTrue(mModbustcpServer.getHoldingReg().getValue() == 252);

	}

	@Test
	public void connectModbusTCPMasterWithSlaveTest() throws Exception {

		if (m_Connection != null && !m_Connection.isConnected()) {
			m_Connection.connect();
			m_Transaction = new ModbusTCPTransaction(m_Connection);
			m_Transaction.setReconnecting(m_Reconnecting);

		}

		assertTrue(m_Connection.isConnected() == true);
	}

	@Test
	public void readInputRegistersTest() throws ModbusException, Exception {
		if (m_Connection != null && !m_Connection.isConnected()) {
			m_Connection.connect();
			m_Transaction = new ModbusTCPTransaction(m_Connection);
			m_Transaction.setReconnecting(m_Reconnecting);

		}

		m_ReadInputRegistersRequest.setUnitID(15);
		m_ReadInputRegistersRequest.setReference(0);
		m_ReadInputRegistersRequest.setWordCount(3);
		// m_Transaction = new ModbusTCPTransaction(m_Connection);
		m_Transaction.setRequest(m_ReadInputRegistersRequest);
		m_Transaction.execute();

		InputRegister ir[] = ((ReadInputRegistersResponse) m_Transaction.getResponse()).getRegisters();

		for (InputRegister elm : ir) {
			if (mModbustcpServer.getInputRegister1().getValue() == elm.getValue()) {
				assertTrue(mModbustcpServer.getInputRegister1().getValue() == elm.getValue());
			} else if (mModbustcpServer.getInputRegister2().getValue() == elm.getValue()) {
				assertTrue(mModbustcpServer.getInputRegister2().getValue() == elm.getValue());
			} else if (mModbustcpServer.getInputRegister3().getValue() == elm.getValue()) {
				assertTrue(mModbustcpServer.getInputRegister3().getValue() == elm.getValue());
			}
		}

	}

	@Test
	public void readMultipleRegistersTest() throws ModbusException, Exception {

		if (m_Connection != null && !m_Connection.isConnected()) {
			m_Connection.connect();
			m_Transaction = new ModbusTCPTransaction(m_Connection);
			m_Transaction.setReconnecting(m_Reconnecting);

		}

		m_ReadMultipleRegistersRequest.setUnitID(15);
		m_ReadMultipleRegistersRequest.setReference(0);
		m_ReadMultipleRegistersRequest.setWordCount(1);
		m_Transaction.setRequest(m_ReadMultipleRegistersRequest);
		m_Transaction.execute();
		Register[] rArr = ((ReadMultipleRegistersResponse) m_Transaction.getResponse()).getRegisters();

		for (Register elm : rArr) {
			assertTrue(mModbustcpServer.getHoldingReg().getValue() == elm.getValue());
		}

	}

	@Test
	public void writeSingleRegisterTest() throws ModbusException, Exception {
		if (m_Connection != null && !m_Connection.isConnected()) {
			m_Connection.connect();
			m_Transaction = new ModbusTCPTransaction(m_Connection);
			m_Transaction.setReconnecting(m_Reconnecting);
		}

		m_WriteSingleRegisterRequest.setUnitID(15);
		m_WriteSingleRegisterRequest.setReference(0);
		m_WriteSingleRegisterRequest.setRegister(new SimpleRegister(255));
		m_Transaction.setRequest(m_WriteSingleRegisterRequest);
		m_Transaction.execute();
		assertTrue(mModbustcpServer.getHoldingReg().getValue() == 255);

	}

}
