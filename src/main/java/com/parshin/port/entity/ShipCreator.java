package com.parshin.port.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ShipCreator {
    private static final Logger log = LogManager.getLogger();
    private static final AtomicReference<ShipCreator> instance = new AtomicReference<>();
    private static final String SHIP_MAX_CONTAINERS = "ship_max_containers";
    private static final String SHIP_IS_EMPTY_PROBABILITY = "ship_is_empty_probability";
    private static final String SHIPS_NUMBER = "ships_number";

    private double smallShipMaxContainers;
    private double mediumShipMaxContainers;
    private double largeShipMaxContainers;
    private double shipIsEmptyProbability;
    private double numberOfShips;
    private List<Ship> shipList;

    private ShipCreator() {
    }

    public static ShipCreator getInstance() {
        while (true) {
            ShipCreator shipCreator = instance.get();
            if (shipCreator != null) {
                return shipCreator;
            }

            shipCreator = new ShipCreator();
            if (instance.compareAndSet(null, shipCreator)) {
                return shipCreator;
            }
        }
    }

    public List<Ship> getShips(Port port, String initFileName) {
        shipList = fillShipList(port);
        return shipList;
    }

    private List<Ship> fillShipList(Port port) {  //TODO

    }
}
