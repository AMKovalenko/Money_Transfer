package ru.money.transferservice.service;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ru.money.transferservice.repositories.MoneyTransferRepository;
import ru.money.transferservice.entities.MoneyTransferRequest;
import ru.money.transferservice.entities.Xml;
import ru.money.transferservice.exceptions.InvalidXmlException;
import ru.money.transferservice.service.parser.XmlParser;
import ru.money.transferservice.service.validator.XmlValidator;


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


@Service
public class MoneyTransferService implements TransferService<MoneyTransferRequest>  {

    @Value("${upload.path}")
    private String uploadPath;
    @Value("${upload.charset.name}")
    private String charsetName;
    @Value("${pool.size}")
    private String poolSizeStringValue;
    private final XmlValidator validator;
    private final MoneyTransferRepository repository;
    private final XmlParser<MoneyTransferRequest> xmlParser;
    private final static Logger logger = Logger.getLogger(MoneyTransferService.class);

    @Autowired
    public MoneyTransferService(XmlValidator validator, MoneyTransferRepository repository, XmlParser<MoneyTransferRequest> xmlParser) {
        this.validator = validator;
        this.repository = repository;
        this.xmlParser = xmlParser;
    }

    @Override
    public void executeXml(Xml xml) {
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
            }
        }
        // если всё в норме - парсим XML и передаём данные в пул потоков
        ConcurrentLinkedQueue<MoneyTransferRequest> request = xmlParser.parseXML(xml.getRequest());

        List<Callable<MoneyTransferRequest>> callables = new ArrayList<>();

        for (int i = 0; i < poolSize; i++){
            callables.add(new MoneyTransferCallable(request, this, uploadPath, uploadingCharset));
        }

        ExecutorService executor = PoolExecutor.getExecutorServiceInstance(poolSize);

        try {
            executor.invokeAll(callables);
        } catch (InterruptedException e) {
            logger.error("Occured exception during processing request : \n", e);
        }
    }

    @Override
    public void validateXML(Xml xml) throws InvalidXmlException {
        try {
            validator.validate(xml.getRequest());
        }catch (InvalidXmlException e){
            logger.error(e.getMessage());
            throw e;
        }
        if (logger.isDebugEnabled()){
            logger.debug("Xml has been validated successfully.");
        }
    }

    @Override
    public boolean isMoneyTransferRequestExist(MoneyTransferRequest request){
        return repository.existsById(request.getId());
    }

    @Override
    public void saveMoneyTransferRequest(MoneyTransferRequest request){
        repository.save(request);
    }

}
