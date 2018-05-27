package ru.money.transferservice.service.parser;


import org.thymeleaf.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.money.transferservice.entities.MoneyTransferRequest;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentLinkedQueue;

public class XMLParseHandler extends DefaultHandler {

    private ConcurrentLinkedQueue<MoneyTransferRequest> request;
    private MoneyTransferRequest item = null;
    private String currentQName = "";
    StringBuffer xml = new StringBuffer();

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        currentQName = qName;
        switch (qName) {
            case "money-transfer-request":
                item = new MoneyTransferRequest();
                item.setId(Long.parseLong(attributes.getValue("id")));
                xml.append("<money-transfer-request id=\"").append(attributes.getValue("id")).append("\">");
                break;
            case "sender":
                item.setPayerAccount(attributes.getValue("account"));
                xml.append("<sender name=\"").append(attributes.getValue("name")).append("\" account=\"").append(attributes.getValue("account")).append("\"/>");
                break;
            case "recipient":
                item.setPayeeAccount(attributes.getValue("account"));
                xml.append("<recipient name=\"").append(attributes.getValue("name")).append("\" account=\"").append(attributes.getValue("account")).append("\"/>");
                break;
        }
    }



    @Override
    public void characters(char[] c, int start, int length) throws SAXException {

        String value = new String(c, start, length).trim();
        if (StringUtils.isEmpty(value)){
            return;
        }
        if (value.equals("\n")){
            xml.append(value);
            return;
        }
        switch (currentQName) {
            case "amount":
                xml.append("<amount>").append(value).append("</amount>");
                item.setAmount(new BigDecimal(value));
                break;
            case "commission":
                xml.append("<commission>").append(value).append("</commission>");
                item.setComission(new BigDecimal(value));
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equals("money-transfer-request")){
            xml.append("</money-transfer-request>");
            item.setXmlBody(xml.toString());
            request.add(item);
            xml = new StringBuffer();
        }
    }

    @Override
    public void startDocument() throws SAXException {
        this.request = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();

    }

    public ConcurrentLinkedQueue<MoneyTransferRequest> getRequest() {
        return request;
    }
}

