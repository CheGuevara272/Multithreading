package com.parshin.port.entity;

import com.parshin.port.exception.CustomException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Ship extends Thread {
    private static final Logger log = LogManager.getLogger();
    private final String shipName;
    private int actualNumberOfContainers;
    private final int maxNumberOfContainers;
    private double minimumShipLoadToLeavePort;
    private Pier pier;
    private final Port port;
    private final WaterArea waterArea;

    public Ship(@NotNull String name, int maxNumberOfContainers, int actualNumberOfContainers, Port port, double minimumShipLoadToLeavePort) {
        super("Ship - " + name);
        this.shipName = name;
        this.port = port;
        this.maxNumberOfContainers = maxNumberOfContainers;
        this.actualNumberOfContainers = actualNumberOfContainers;
        this.waterArea = WaterArea.getInstance();
        this.minimumShipLoadToLeavePort = minimumShipLoadToLeavePort;
    }

    public double getMinimumShipLoadToLeavePort() {
        return minimumShipLoadToLeavePort;
    }

    public int getActualNumberOfContainers() {
        return actualNumberOfContainers;
    }

    public int getMaxNumberOfContainers() {
        return maxNumberOfContainers;
    }

    public boolean areThereContainers() {
        return actualNumberOfContainers > 0;
    }

    public boolean isFreeSpaceForContainers() {
        return maxNumberOfContainers > actualNumberOfContainers;
    }

    public void decrementNumberOfContainers() {
        actualNumberOfContainers -= 5;
    }

    public void incrementNumberOfContainers() {
        actualNumberOfContainers += 5;
    }

    @Override
    public void run() {
        port.incrementShipCounter();
        Thread.currentThread().setName(shipName);
        try {
            waterArea.enterTheWaterArea();
            pier = port.popDockPool();
        } catch (CustomException e) {
            log.log(Level.ERROR, "Can't get pier", e);
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Thread {} was interrupted", Thread.currentThread().getName(), e);
            Thread.currentThread().interrupt();
        }
        pier.setShip(this);

        if (actualNumberOfContainers != 0) {
            pier.unLoadShip();
            try {
                int shipMaintenanceTime = 3;
                TimeUnit.SECONDS.sleep(shipMaintenanceTime);
            } catch (InterruptedException e) {
                log.log(Level.ERROR, "Thread {} was interrupted", Thread.currentThread().getName(), e);
                Thread.currentThread().interrupt();
            }
            pier.loadShip();
        } else {
            pier.loadShip();
        }
        try {
            port.pushDockPool(pier);
            waterArea.getOutOfTheWaterArea();
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Thread {} was interrupted", Thread.currentThread().getName(), e);
            Thread.currentThread().interrupt();
        }
        port.decrementShipCounter();
    }
}
