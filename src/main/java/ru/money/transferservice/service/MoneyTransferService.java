package ru.money.transferservice.service;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import ru.money.transferservice.repositories.MoneyTransferRepository;
import ru.money.transferservice.entities.MoneyTransferRequest;
import ru.money.transferservice.entities.Xml;
import ru.money.transferservice.exceptions.InvalidXmlException;
import ru.money.transferservice.service.parser.XmlParser;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class MoneyTransferService implements TransferService {

    private final static Logger logger = Logger.getLogger(MoneyTransferService.class);

    @Value("${upload.path}")
    private String uploadPath;
    @Value("${upload.charset.name}")
    private String charsetName;
    @Value("${pool.size}")
    private String poolSizeStringValue;
    private final Validator validator;
    private final MoneyTransferRepository repository;
    private final XmlParser xmlParser;

    @Autowired
    public MoneyTransferService(@Qualifier("xmlValidator") Validator validator, MoneyTransferRepository repository, XmlParser xmlParser) {
        this.validator = validator;
        this.repository = repository;
        this.xmlParser = xmlParser;
    }

    @Override
    public void executeXml(Xml xml) throws Exception {
        // Проверим, существует ли переданная кодировка.
        Charset uploadingCharset = null;
        try {
            uploadingCharset = Charset.forName(charsetName);
        }catch (UnsupportedCharsetException e){
            logger.error("Unsupported charset name [" + charsetName + "] has been transmitted. Check up field upload.charset.name.");
            throw new UnsupportedCharsetException("Unsupported charset name [" + charsetName + "] has been transmitted. Check up field upload.charset.name.");
        }
        // Проверим, сужествует ли папка для сохранения json файлов. Если нет - попробуем создать
        if (Files.notExists(Paths.get(uploadPath))){
            try {
                Files.createDirectory(Paths.get(uploadPath));
            } catch (IOException e) {
                logger.error("Uploading directory is absent and impossible to create." + e.getLocalizedMessage());
                throw new IOException("Uploading directory is absent and impossible to create." + e.getLocalizedMessage());
            }
        }
        // проверим корректность величины пула потоков. Если менее 1 или неадекватно большая - бросаем исключение
        int poolSize;
        try {
            poolSize = Integer.parseInt(poolSizeStringValue);
            if (poolSize < 1 || poolSize > 30)
                throw new IllegalArgumentException();
        }catch (IllegalArgumentException e){
            logger.error("Incorrect field pool.size. Check it up!");
            throw new IllegalArgumentException("Incorrect field pool.size. Check it up!");
        }
        // если всё в норме - парсим XML.
        ConcurrentLinkedQueue<MoneyTransferRequest> request = xmlParser.parseXML(xml.getRequest());
        // и передаём данные в пул потоков
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        List<Callable<MoneyTransferRequest>> callables = new ArrayList<>();

        for (int i = 0; i < poolSize; i++){
            callables.add(new MoneyTransferCallable(request, repository, uploadPath, uploadingCharset));
        }

        try {
            executor.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValidXML(Xml xml, Errors errors) throws InvalidXmlException {
        validator.validate(xml, errors);
        if (errors.hasErrors()) {
            logger.error("Invalid XML. " + xml.getRequest());
            throw new InvalidXmlException("Invalid XML. Check it up!");
        }
        return true;
    }
}
