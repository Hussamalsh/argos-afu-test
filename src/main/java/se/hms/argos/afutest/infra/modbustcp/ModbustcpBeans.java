package se.hms.argos.afutest.infra.modbustcp;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import se.hms.argos.common.owner.utils.OwnerUtils;

@Configuration
public class ModbustcpBeans /*implements ApplicationContextAware*/
{
	private static ModbustcpServer mModbustcpServer;
   @Bean
   public ModbustcpServer modbustcpServer() throws Exception
   {
      ModbustcpConfig config = OwnerUtils.getOrCreate(ModbustcpConfig.class);
      return mModbustcpServer = new ModbustcpServer(config);
   }
   
	public  static <T> T getBean()
	{
	        return (T) mModbustcpServer;
	}
   
	/*private static ApplicationContext context;
	
	public  static <T> T getBean(String name,Class<T> aClass)
	{
	        return context.getBean(name,aClass);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException 
	{
		context = ctx;
		
	}*/
   

	
}
