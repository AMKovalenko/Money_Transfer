package ru.money.transferservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.money.transferservice.entities.Xml;
import ru.money.transferservice.service.TransferService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"pool.size = 500"})
public class TransferServiceFailedPoolSizeTest {

    @Autowired
    ApplicationContext context;
    @Autowired
    TransferService service;

    private Xml xml = new Xml();

    @Before
    public void setXML() throws IOException {

        InputStream inputStream = context.getResource("classpath:request-3-items.xml").getInputStream();

        xml.setRequest(new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void moneyTransferServiceEnormousPoolSizeTest() throws Exception {
        service.executeXml(xml);
    }


}
