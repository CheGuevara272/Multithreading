package com.parshin.port.entity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WaterArea {
    private static final Logger log = LogManager.getLogger();
    private final Semaphore semaphore = new Semaphore(10);
    private static final AtomicReference<WaterArea> instance = new AtomicReference<>();

    private WaterArea() {
    }

    public static WaterArea getInstance() {
        while (true) {
            WaterArea waterArea = instance.get();
            if (waterArea != null) {
                return waterArea;
            }

            waterArea = new WaterArea();
            if (instance.compareAndSet(null, waterArea)) {
                return waterArea;
            }
        }
    }

    public void enterTheWaterArea() throws InterruptedException {
        semaphore.acquire();
        log.log(Level.INFO, "Ship {} entered the water area", Thread.currentThread().getName());
    }

    public void getOutOfTheWaterArea() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500);
        semaphore.release();
        log.log(Level.INFO, "Ship {} left the water area", Thread.currentThread().getName());
    }
}
