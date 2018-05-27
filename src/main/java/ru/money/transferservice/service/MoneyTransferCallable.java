package ru.money.transferservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.money.transferservice.repositories.MoneyTransferRepository;
import ru.money.transferservice.entities.MoneyTransferRequest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MoneyTransferCallable implements Callable<MoneyTransferRequest> {

    private String uploadPath;
    private MoneyTransferRepository repository;
    private ConcurrentLinkedQueue<MoneyTransferRequest> requests;
    private Charset charset;

    private static final Logger logger = Logger.getLogger(MoneyTransferCallable.class);

    @Autowired
    public MoneyTransferCallable(ConcurrentLinkedQueue<MoneyTransferRequest> requests, MoneyTransferRepository repository, String uploadPath, Charset charset) {
        this.requests = requests;
        this.repository = repository;
        this.uploadPath = uploadPath;
        this.charset = charset;
    }

    @Override
    public MoneyTransferRequest call() throws Exception {

        MoneyTransferRequest item = null;
        while (requests.size() > 0){
            if ((item = requests.poll()) == null){
                break;
            }
            if (repository.existsById(item.getId())){
                logger.error(item.toString() + " already exist.");
                continue;
            }

            repository.save(item);

            String result = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(item);

            Path uploadFilePath = Paths.get(uploadPath.concat(item.getId().toString()).concat(".json"));

            try (BufferedWriter fileWriter = Files.newBufferedWriter(uploadFilePath, charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE)){
                fileWriter.write(result);
                fileWriter.flush();
            }catch (IOException e){
                logger.error("FAILED attempt to cteate file " + item.getId() + ".json " + e.getLocalizedMessage());
            }
        }
        return item;
    }
}
