package se.hms.argos.afutest.infra.http.internal;
/**
* InternalWebMvcSetup.
* 
* <P>This is the InternalWebMvcSetup class that set up the beans to the Spring framework
*  
* @author HMS
* @version 1.0
*/
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import se.hms.argos.common.server.spring.mvc.HttpMessageConverterUtils;

@Configuration
@EnableWebMvc
public class InternalWebMvcSetup extends WebMvcConfigurerAdapter
{
   // Use default servlet for static content.
   @Override
   public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer)
   {
      configurer.enable();
   }

   // Basic setup for Freemarker views
   @Bean
   public FreeMarkerConfigurer freeMarkerConfigurer()
   {
      FreeMarkerConfigurer conf = new FreeMarkerConfigurer();
      conf.setTemplateLoaderPath("/WEB-INF/views/");
      conf.setDefaultEncoding("UTF-8");
      return conf;
   }

   @Bean
   public FreeMarkerViewResolver configureInternalFreemarkerResourceViewResolver()
   {
      FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
      resolver.setPrefix("");
      resolver.setSuffix(".ftl");
      resolver.setContentType("text/html; charset=UTF-8");
      return resolver;
   }

   @Override
   public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
   {
      HttpMessageConverterUtils.registerDefaultMessageConverters(converters);
   }
}
