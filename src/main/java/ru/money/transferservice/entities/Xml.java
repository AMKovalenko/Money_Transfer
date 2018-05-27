package ru.money.transferservice.entities;

public class Xml {

    private String request;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    @Override
    public String toString() {
        return "Xml{" +
                "request='" + request + '\'' +
                '}';
    }
}
