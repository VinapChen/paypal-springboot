package com.masasdani.paypal;

import com.masasdani.paypal.service.DBHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration
@Configuration
@ComponentScan
public class Application {

	public static void main(String[] args) {
        DBHelper.init();
		SpringApplication.run(Application.class, args);
	}
}