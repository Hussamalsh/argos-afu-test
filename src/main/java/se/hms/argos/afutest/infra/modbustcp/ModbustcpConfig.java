package se.hms.argos.afutest.infra.modbustcp;

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
