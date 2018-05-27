package ru.money.transferservice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import ru.money.transferservice.entities.Xml;
import ru.money.transferservice.exceptions.InvalidXmlException;
import ru.money.transferservice.service.TransferService;

@Controller
public class TransferController {

    private final TransferService service;

    @Autowired
    public TransferController(TransferService service) {
        this.service = service;
    }


    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String setupForm(Model model){
        Xml request = new Xml();
        model.addAttribute("xml", request);
        return "hello-world";
    }

    @RequestMapping(value = "/addMoneyTransferRequest", method = RequestMethod.POST)
    @ResponseBody
    public String addMoneyTransferRequest(@ModelAttribute("xml") Xml xml, BindingResult result, SessionStatus status) throws Exception {

        if (service.isValidXML(xml, result)){
            handleOk();
        }
        service.executeXml(xml);
        status.setComplete();
        return "OK";
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(Exception e) {
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.OK)
    private void handleOk() {}


}
