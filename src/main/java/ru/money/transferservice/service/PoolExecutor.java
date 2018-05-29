package ru.money.transferservice.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class PoolExecutor {

    private static volatile ExecutorService instance;

    private PoolExecutor() {
    }

    static ExecutorService getExecutorServiceInstance(int size){
        ExecutorService executor = instance;
        if (instance == null){
            synchronized (PoolExecutor.class){
                executor = instance;
                if (executor == null){
                    instance = executor = Executors.newFixedThreadPool(size);
                }
            }
        }
        return executor;
    }
}
