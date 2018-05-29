package ru.money.transferservice.service.parser;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface XmlParser<T> {

    ConcurrentLinkedQueue<T> parseXML(String xmlStringValue);
}
