package com.gcn.etl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.SpringServletContainerInitializer;

@SpringBootApplication	
@ComponentScan(basePackages = "com.gcn.etl")
@EnableAsync	
@EnableJpaRepositories	
@PropertySource({"file:config/application.properties" })
public class ETLMain extends SpringServletContainerInitializer {
	
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ETLMain.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ETLMain.class, args);
	}
	
	@Bean
	public TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory() {
	    return new TomcatEmbeddedServletContainerFactory();
	}
}