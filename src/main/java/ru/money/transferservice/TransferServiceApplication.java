package ru.money.transferservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.xml.bind.JAXBException;



@SpringBootApplication
public class TransferServiceApplication {

	public static void main(String[] args) throws JAXBException {
		SpringApplication.run(TransferServiceApplication.class, args);
	}
}
