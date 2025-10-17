package com.microservico.sales;

import org.springframework.boot.SpringApplication;

public class TestSalesApplication {

	public static void main(String[] args) {
		SpringApplication.from(SalesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
