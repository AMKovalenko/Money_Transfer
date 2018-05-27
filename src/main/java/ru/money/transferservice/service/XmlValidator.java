package ru.money.transferservice.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.xml.sax.SAXException;
import ru.money.transferservice.entities.Xml;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;

@Component
public class XmlValidator implements org.springframework.validation.Validator{

    @Value("${xsdPath}")
    private String xsdPath;
    @Override
    public boolean supports(Class<?> clazz) {
        return Xml.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "request", "error.request", "Запрос не должен быть пустым или состоять из пробелов.");
        String request = ((Xml) o).getRequest();

        if (!validateXMLByXSD(xsdPath, request)){
            errors.rejectValue("request", "error.request", (Object[])null, "Ваш запрос не прошел валидацию. Проверьте корректность XML");
        }
    }

    public static boolean validateXMLByXSD(String xsdPath, String xml){
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Resource resource = new ClassPathResource(xsdPath);
            Schema schema = factory.newSchema(new StreamSource(resource.getInputStream()));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xml)));
        } catch (IOException e){
            System.out.println("Exception: "+e.getMessage());
            return false;
        }catch(SAXException e1){
            System.out.println("SAX Exception: "+e1.getMessage());
            return false;
        }
        return true;
    }
}