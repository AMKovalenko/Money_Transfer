package ru.money.transferservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import ru.money.transferservice.entities.MoneyTransferRequest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MoneyTransferCallable implements Callable<MoneyTransferRequest> {

    private String uploadPath;
    private TransferService<MoneyTransferRequest> service;
    private ConcurrentLinkedQueue<MoneyTransferRequest> requests;
    private Charset charset;

    private static final Logger logger = Logger.getLogger(MoneyTransferCallable.class);

    MoneyTransferCallable(ConcurrentLinkedQueue<MoneyTransferRequest> requests, TransferService<MoneyTransferRequest> service, String uploadPath, Charset charset) {
        this.requests = requests;
        this.service = service;
        this.uploadPath = uploadPath;
        this.charset = charset;
    }

    @Override
    public MoneyTransferRequest call() throws Exception {

        MoneyTransferRequest requestItem = null;

        while (requests.size() > 0){
            if ((requestItem = requests.poll()) == null){
                break;
            }
            try{
                if (service.isMoneyTransferRequestExist(requestItem)){
                    logger.error(requestItem.toString() + " already exist.");
                    continue;
                }
            }catch (Exception e){
                logger.error("Failed attempt to check existing." + e);
            }

            service.saveMoneyTransferRequest(requestItem);

            String result = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(requestItem);

            Path uploadFilePath = Paths.get(uploadPath.concat(requestItem.getId().toString()).concat(".json"));

            try (BufferedWriter fileWriter = Files.newBufferedWriter(uploadFilePath, charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE)){
                fileWriter.write(result);
                fileWriter.flush();
            }catch (IOException e){
                logger.error("FAILED attempt to cteate file " + requestItem.getId() + ".json " + e.getLocalizedMessage());
            }
        }
        return requestItem;
    }
}
