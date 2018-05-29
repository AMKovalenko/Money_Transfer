package ru.money.transferservice.service.validator;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;
import org.xml.sax.SAXException;
import ru.money.transferservice.exceptions.InvalidXmlException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;

@Component
public class XmlValidatorImpl implements XmlValidator{

    private final static Logger logger = Logger.getLogger(XmlValidatorImpl.class);

    @Value("${xsdPath}")
    private String xsdPath;

    @Override
    public void validate(String xml) {

        if (StringUtils.isEmptyOrWhitespace(xml)){
            throw new InvalidXmlException("Request shouldn't be empty or consist of whitespaces.");
        }

        if (!validateXMLByXSD(xsdPath, xml)){
            throw new InvalidXmlException("Request has not been passed validation by XSD. Check up XML.");
        }
    }

    private static boolean validateXMLByXSD(String xsdPath, String xml){
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Resource resource = new ClassPathResource(xsdPath);
            Schema schema = factory.newSchema(new StreamSource(resource.getInputStream()));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xml)));
        } catch (IOException | SAXException e){
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }


}