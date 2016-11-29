package se.hms.argos.afutest.boot;

/**
* ArgosTestConfig.
* 
* <P>This is the configuration class that has the properties of the Argos server.
*  
* @author HMS
* @version 1.0
*/

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.springframework.context.annotation.Description;

import se.hms.argos.common.server.ArgosServerConfig;

@LoadPolicy(LoadType.MERGE)
@Sources({"file:${onejar.expand.dir}/build.properties","classpath:build.properties"
})
public interface ArgosTestConfig extends ArgosServerConfig
{
   /*
    * Server properties
    */
   @Key("server_internal_port")
   @DefaultValue("44041")
   public int serverInternalPort();

   @Key("server_internal_operation_port")
   @DefaultValue("44042")
   public int serverInternalOperationPort();

   @Key("server_internal_resource_base")
   @DefaultValue("${resource.base}/webapp/internal")
   public String serverInternalResourceBase();


   /*
    * Service properties
    */
   @Key("service.name")
   @DefaultValue("Unknown")
   public String serviceName();

   @Key("service.version")
   @DefaultValue("Unknown")
   public String serviceVersion();

   /*
    * Onejar properties
    */
   @Key("onejar.expand.dir")
   @DefaultValue("target/classes")
   @Description("This property is only used to read the expand dir set by the onejar plugin, if applicable.")
   public String onejarExpandDir();

   @Key("resource_base")
   @DefaultValue("${onejar.expand.dir}")
   public String resourceBase();
}
