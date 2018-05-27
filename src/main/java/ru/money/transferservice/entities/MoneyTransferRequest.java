package ru.money.transferservice.entities;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@JsonAutoDetect
@Table(name = "MONEY_TRANSFERS")
public class MoneyTransferRequest {

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATIONDATE", nullable = false)
    private Date creationDate;

    @JsonIgnore
    @Column(name = "REQUEST", nullable = false)
    private String xmlBody;

    @Transient
    private String payerAccount;
    @Transient
    private String payeeAccount;
    @JsonIgnore
    @Transient
    private BigDecimal amount;
    @JsonIgnore
    @Transient
    private BigDecimal comission;
    @Transient
    private BigDecimal sum;



    public MoneyTransferRequest() {
        this.creationDate = new Date();
    }

    public MoneyTransferRequest(Long id, Date creationDate, String xmlBody) {
        this.id = id;
        this.creationDate = creationDate;
        this.xmlBody = xmlBody;
    }

    public BigDecimal getSum() {
        return (comission == null) ? amount : amount.subtract(comission);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getXmlBody() {
        return xmlBody;
    }

    public void setXmlBody(String xmlBody) {
        this.xmlBody = xmlBody;
    }

    public String getPayerAccount() {
        return payerAccount;
    }

    public void setPayerAccount(String payerAccount) {
        this.payerAccount = payerAccount;
    }

    public String getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(String payeeAccount) {
        this.payeeAccount = payeeAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getComission() {
        return comission;
    }


    public void setComission(BigDecimal comission) {
        this.comission = comission;
    }

    @Override
    public String toString() {
        return "MoneyTransferRequest{" +
                "id='" + id + '\'' +
                ", creationDate=" + creationDate +
                ", xmlBody='" + xmlBody + '\'' +
                '}';
    }
}
