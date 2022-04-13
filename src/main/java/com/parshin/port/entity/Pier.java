package com.parshin.port.entity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Pier {
    private static final Logger log = LogManager.getLogger();
    private final int dockId;
    private final Port port;
    private Ship ship;

    public Pier(int dockId, Port port) {
        this.port = port;
        this.dockId = dockId;
    }

    public int getDockId() {
        return dockId;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public void unLoadShip() {
        while (ship.areThereContainers()) {
            if(port.getNumberOfContainers().doubleValue() < port.getMaxNumberOfContainers()) {
                ship.decrementNumberOfContainers();
                port.incrementNumberOfContainers();
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    log.log(Level.ERROR, "Thread {} was interrupted", Thread.currentThread().getName(), e);
                    Thread.currentThread().interrupt();
                }
            } else {
                Thread.yield();
            }
        }
    }

    public void loadShip() {
        Random random = new Random();
        int numberOfContainersToLeavePort = random.nextInt((int) (ship.getMaxNumberOfContainers() * ship.getMinimumShipLoadToLeavePort()), (int) ship.getMaxNumberOfContainers());
        while (ship.isFreeSpaceForContainers() && ship.getActualNumberOfContainers() < numberOfContainersToLeavePort) {
            if (port.getNumberOfContainers().doubleValue() > 0) {
                port.decrementNumberOfContainers();
                ship.incrementNumberOfContainers();
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    log.log(Level.ERROR, "Thread {} was interrupted", Thread.currentThread().getName(), e);
                    Thread.currentThread().interrupt();
                }
            } else {
                Thread.yield();
            }
        }
    }
}
