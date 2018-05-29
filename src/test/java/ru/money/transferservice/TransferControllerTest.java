package ru.money.transferservice;



import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.money.transferservice.controllers.TransferController;
import ru.money.transferservice.entities.MoneyTransferRequest;
import ru.money.transferservice.entities.Xml;
import ru.money.transferservice.exceptions.InvalidXmlException;
import ru.money.transferservice.repositories.MoneyTransferRepository;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:testdb;INIT=runscript from 'classpath:/script.sql';DB_CLOSE_ON_EXIT=FALSE"})
public class TransferControllerTest {

    @Autowired
    ApplicationContext context;
    @Autowired
    TransferController controller;
    @Autowired
    MoneyTransferRepository repository;

    @Test
    public void moneyTransferServiceTest() throws Exception {
        controller.executeMoneyTransferRequest(getXmlByName("request-3-items.xml"));
        // ждем секунду, пока метод выполнится асинхронно
        TimeUnit.SECONDS.sleep(1);
        Optional<MoneyTransferRequest> request1 = repository.findById(1L);
        Assert.assertTrue(request1.isPresent());
        Assert.assertNotNull(request1.get().getXmlBody());
        Assert.assertEquals(1L,request1.get().getId().longValue());
        Assert.assertNotNull(request1.get().getCreationDate());

        Optional<MoneyTransferRequest> request2 = repository.findById(2L);
        Assert.assertTrue(request2.isPresent());
        Assert.assertNotNull(request2.get().getXmlBody());
        Assert.assertEquals(2L,request2.get().getId().longValue());
        Assert.assertNotNull(request2.get().getCreationDate());

        Optional<MoneyTransferRequest> request3 = repository.findById(3L);
        Assert.assertTrue(request3.isPresent());
        Assert.assertNotNull(request3.get().getXmlBody());
        Assert.assertEquals(3L,request3.get().getId().longValue());
        Assert.assertNotNull(request3.get().getCreationDate());
        // запроса с идентификатором 4 еще не должно быть
        Optional<MoneyTransferRequest> request4 = repository.findById(4L);
        Assert.assertFalse(request4.isPresent());
        // получим даты создания для сравнения
        Date creationDate1 = request1.get().getCreationDate();
        Date creationDate2 = request2.get().getCreationDate();
        Date creationDate3 = request3.get().getCreationDate();

        // попробуем обработать еще раз те же самые запросы + 1 новый. Ждем 5 секунд, чтобы проверить, изменилась ли дата создания у записей в БД
        TimeUnit.SECONDS.sleep(5);
        // Дата должна остаться прежней, т.к. записи не должны перезаписаться.
        controller.executeMoneyTransferRequest(getXmlByName("request-4-items.xml"));
        TimeUnit.SECONDS.sleep(1);
        Optional<MoneyTransferRequest> request1New = repository.findById(1L);
        Assert.assertTrue(request1New.isPresent());
        Assert.assertEquals(creationDate1, request1New.get().getCreationDate());

        Optional<MoneyTransferRequest> request2New = repository.findById(2L);
        Assert.assertTrue(request2New.isPresent());
        Assert.assertEquals(creationDate2, request2New.get().getCreationDate());

        Optional<MoneyTransferRequest> request3New = repository.findById(3L);
        Assert.assertTrue(request3New.isPresent());
        Assert.assertEquals(creationDate3, request3New.get().getCreationDate());
        // должен появиться новый запрос с идентификатором 4
        Optional<MoneyTransferRequest> request4New = repository.findById(4L);
        Assert.assertTrue(request4New.isPresent());
        Assert.assertNotNull(request4New.get().getXmlBody());
        Assert.assertEquals(4L, request4New.get().getId().longValue());
        Assert.assertNotNull(request4New.get().getCreationDate());
    }

    @Test(expected = InvalidXmlException.class)
    public void moneyTransferServiceInvalidXmlTest() throws Exception {
        Xml wrongXML = getXmlByName("wrong-request.xml");
        controller.executeMoneyTransferRequest(wrongXML);
    }

    @Test(expected = InvalidXmlException.class)
    public void moneyTransferServiceEmptyXmlTest() throws Exception {
        Xml wrong = new Xml();
        wrong.setRequest("");
        controller.executeMoneyTransferRequest(wrong);
    }

    @Test(expected = InvalidXmlException.class)
    public void moneyTransferServiceNullXmlTest() throws Exception {
        controller.executeMoneyTransferRequest(new Xml());
    }

    public Xml getXmlByName(String name) throws IOException {
        InputStream inputStream = context.getResource("classpath:".concat(name)).getInputStream();
        Xml xml = new Xml();
        xml.setRequest(new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n")));
        return xml;
    }
}
