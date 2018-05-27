package ru.money.transferservice.service.parser;

import ru.money.transferservice.entities.MoneyTransferRequest;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface XmlParser<T> {

    ConcurrentLinkedQueue<T> parseXML(String xmlStringValue);
}
