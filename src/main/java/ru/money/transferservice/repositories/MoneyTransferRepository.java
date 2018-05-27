package ru.money.transferservice.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.money.transferservice.entities.MoneyTransferRequest;

public interface MoneyTransferRepository extends CrudRepository<MoneyTransferRequest, Long> {

}
