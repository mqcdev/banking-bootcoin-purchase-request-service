package com.nttdata.banking.bootcoinpurchaserequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Class MsBootcoinMovementApplication Main.
 * BootcoinMovement microservice class MsBootcoinMovementApplication.
 */
@SpringBootApplication
@EnableEurekaClient
public class BootcoinPurchaseRequestApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootcoinPurchaseRequestApplication.class, args);
	}

}
