package com.parshin.port.entity;

import com.parshin.port.exception.CustomException;
import com.parshin.port.parser.CustomParser;
import com.parshin.port.parser.impl.CustomParserImpl;
import com.parshin.port.reader.impl.CustomReaderImpl;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class ShipCreator {
    private static final Logger log = LogManager.getLogger();
    private static final AtomicBoolean isInstanceCreated = new AtomicBoolean(false);
    private static ShipCreator instance;
    private static final ReentrantLock lock = new ReentrantLock();

    private static final String SMALL_SHIP_MAX_CONTAINERS = "small_ship_max_containers";
    private static final String MEDIUM_SHIP_MAX_CONTAINERS = "medium_ship_max_containers";
    private static final String LARGE_SHIP_MAX_CONTAINERS = "large_ship_max_containers";
    private static final String MINIMUM_SHIP_LOAD = "minimum_ship_load";
    private static final String SHIP_IS_EMPTY_PROBABILITY = "ship_is_empty_probability";
    private static final String SHIPS_NUMBER = "ships_number";

    private double smallShipMaxContainers;
    private double mediumShipMaxContainers;
    private double largeShipMaxContainers;
    private final double[] shipTypes = {smallShipMaxContainers, mediumShipMaxContainers, largeShipMaxContainers};
    private double minimumShipLoad; // From 0.0 to 1.0
    private double shipIsEmptyProbability; // From 0.0 to 1.0
    private Double numberOfShips;
    private List<Ship> shipList;

    private ShipCreator() {
    }

    public static ShipCreator getInstance() {
        if (!isInstanceCreated.get()) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new ShipCreator();
                    isInstanceCreated.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public List<Ship> getShips(Port port, String initFileName) throws CustomException {
        setShipParameters(initFileName);
        shipList = fillShipList(port);
        return shipList;
    }

    private List<Ship> fillShipList(Port port) {
        Random random = new Random();
        Ship ship;
        shipList = new ArrayList<>();

        for (int i = 0; i < numberOfShips; i++) {
            boolean shipIsEmpty = random.nextFloat() < shipIsEmptyProbability;
            int type = random.nextInt(shipTypes.length);
            int actualNumberOfContainers = shipIsEmpty ? random.nextInt((int) (shipTypes[type] * minimumShipLoad), (int) shipTypes[type]) : 0;
            ship = new Ship("Ship number " + i, (int) shipTypes[type], actualNumberOfContainers, port);
            shipList.add(ship);
        }
        return shipList;
    }

    private void setShipParameters(String initFileName) throws CustomException {
        CustomReaderImpl reader = CustomReaderImpl.getInstance();
        String initData = reader.readFile(initFileName);
        CustomParserImpl parser = CustomParserImpl.getInstance();
        Map<String, Double> initMap = parser.parse(initData);

        initMap.forEach((key, value) -> {
            switch (key) {
                case SMALL_SHIP_MAX_CONTAINERS -> {
                    smallShipMaxContainers = value;
                    log.log(Level.INFO, "{} has been set = {}", key, value);
                }
                case MEDIUM_SHIP_MAX_CONTAINERS -> {
                    mediumShipMaxContainers = value;

                    log.log(Level.INFO, "{} has been set = {}", key, value);
                }
                case LARGE_SHIP_MAX_CONTAINERS -> {
                    largeShipMaxContainers = value;
                    log.log(Level.INFO, "{} has been set = {}", key, value);
                }
                case MINIMUM_SHIP_LOAD -> {
                    minimumShipLoad = value;
                    log.log(Level.INFO, "{} has been set = {}", key, value);
                }
                case SHIP_IS_EMPTY_PROBABILITY -> {
                    shipIsEmptyProbability = value;
                    log.log(Level.INFO, "{} has been set = {}", key, value);
                }
                case SHIPS_NUMBER -> {
                    numberOfShips = value;
                    log.log(Level.INFO, "{} has been set = {}", key, value);
                }
                default -> log.log(Level.ERROR, "Unsupported type - {}", key);
            }
        });
    }
}
