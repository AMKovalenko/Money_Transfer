package ru.money.transferservice.service;

import ru.money.transferservice.entities.Xml;
import ru.money.transferservice.exceptions.InvalidXmlException;




public interface TransferService<T> {

    void executeXml(Xml xml);

    void validateXML(Xml xml) throws InvalidXmlException;

    boolean isMoneyTransferRequestExist(T request);

    void saveMoneyTransferRequest(T request);

}
