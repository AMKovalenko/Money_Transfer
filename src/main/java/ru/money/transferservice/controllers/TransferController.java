package ru.money.transferservice.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.money.transferservice.entities.Xml;
import ru.money.transferservice.service.TransferService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class TransferController {

    private final static Logger logger = Logger.getLogger(TransferController.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final TransferService service;

    @Autowired
    public TransferController(TransferService service) {
        this.service = service;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String setupForm(Model model){
        Xml request = new Xml();
        model.addAttribute("xml", request);
        return "add-request";
    }

    @RequestMapping(value = "/executeMoneyTransferRequest", method = RequestMethod.POST)
    @ResponseBody
    public String executeMoneyTransferRequest(@ModelAttribute("xml") Xml xml) throws Exception {
        if (logger.isDebugEnabled()){
            logger.debug("Started method executeMoneyTransferRequest. Input params : \n" + xml.toString());
        }
        service.validateXML(xml);

        executorService.execute(() -> {
            service.executeXml(xml);
        });

        if (logger.isDebugEnabled()){
            logger.debug("Finished method executeMoneyTransferRequest.");
        }
        return handleOk();
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(Exception e) {
        if (logger.isDebugEnabled()){
            logger.debug("Exception has been catched : \n" + e);
        }
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.OK)
    private String handleOk() { return "OK";}

}
