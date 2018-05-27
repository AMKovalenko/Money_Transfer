package ru.money.transferservice.service.parser;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import ru.money.transferservice.entities.MoneyTransferRequest;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class XmlParserSAXImpl implements XmlParser<MoneyTransferRequest> {

    private final static Logger logger = Logger.getLogger(XmlParserSAXImpl.class);

    public XmlParserSAXImpl() {
    }

    @Override
    public ConcurrentLinkedQueue<MoneyTransferRequest> parseXML(String xmlStringValue) {

        InputStream xmlStream = new ByteArrayInputStream(StandardCharsets.UTF_8.encode(xmlStringValue).array());

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(false);
        XMLParseHandler handler = new XMLParseHandler();

        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(xmlStream, handler);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.error("Exception occured during the parsing XML.\n" + e);
        }

        return handler.getRequest();
    }
}
