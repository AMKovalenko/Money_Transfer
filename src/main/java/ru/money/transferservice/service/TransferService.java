package ru.money.transferservice.service;

import org.springframework.validation.Errors;
import ru.money.transferservice.entities.MoneyTransferRequest;
import ru.money.transferservice.entities.Xml;
import ru.money.transferservice.exceptions.InvalidXmlException;

import javax.xml.bind.JAXBException;

public interface TransferService {

    void executeXml(Xml xml) throws  Exception;

    boolean isValidXML(Xml xml, Errors errors) throws InvalidXmlException;

}
