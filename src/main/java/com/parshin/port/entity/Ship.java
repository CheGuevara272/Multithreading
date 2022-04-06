package com.parshin.port.entity;

import com.parshin.port.exception.CustomException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Ship extends Thread {
    private static final Logger log = LogManager.getLogger();
    private final String shipName;
    private final int maxNumberOfContainers;
    private int actualNumberOfContainers;
    private Dock dock;
    private Port port;
    private WaterArea waterArea;

    public Ship(@NotNull String name, Port port, int maxNumberOfContainers, int actualNumberOfContainers) {
        super("Ship - " + name);
        this.shipName = name;
        this.port = port;
        this.maxNumberOfContainers = maxNumberOfContainers;
        this.actualNumberOfContainers = actualNumberOfContainers;
        this.waterArea = WaterArea.getInstance();
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

        try {
            waterArea.enterTheWaterArea();
            dock = port.popDockPool();
        } catch (CustomException e) {
            log.log(Level.ERROR, "Can't get dock", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Process was interrupted {}", Thread.currentThread().getName(), e);
        }
        dock.setShip(this);

        if (actualNumberOfContainers != 0) {
            dock.unLoadShip();
        } else {
            dock.loadShip();
        }
        try {
            dock.unLoadShip();
            port.pushDockPool(dock);
            waterArea.getOutOfTheWaterArea();
        } catch (CustomException e) {
            log.log(Level.ERROR, "Can't get dock", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Process was interrupted {}", Thread.currentThread().getName(), e);
        }
        port.decrementShipCounter();
    }
}
