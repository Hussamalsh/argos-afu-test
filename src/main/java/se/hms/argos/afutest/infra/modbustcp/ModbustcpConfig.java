package se.hms.argos.afutest.infra.modbustcp;

/**
* ModbustcpConfig
* 
* <P>This is the ModbustcpConfig class that allows a Config object to access the contents of the properties,
*  providing utility methods to perform consequent operations.
*   
* @author HMS
* @version 1.0
*/
import org.aeonbits.owner.Accessible;

public interface ModbustcpConfig extends Accessible
{
   /*
    * Modbus TCP Config
    */
   /** The JDBC driver that is used for connecting to the database */
   @Key("modbus_tcp_server_port")
   @DefaultValue("502")
   public int modbusTcpServerPort();
   
}
