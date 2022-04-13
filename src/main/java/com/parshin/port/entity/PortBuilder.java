package com.parshin.port.entity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PortBuilder {
    private static final Logger log = LogManager.getLogger();
    private static final String MAX_NUMBER_OF_CONTAINERS = "max_number_containers";
    private static final String NUMBER_OF_PIERS = "number_of_piers";
    private static final String INIT_NUMBER_OF_CONTAINERS = "init_number_of_containers";

    public Port getPort(Map<String, Double> portInitData) {
        Port port = Port.getInstance();
        for (Map.Entry<String, Double> entry : portInitData.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            switch (key) {
                case MAX_NUMBER_OF_CONTAINERS -> {
                    log.log(Level.INFO, "{} has been set = {}", key, value);
                    port.setMaxNumberOfContainers(value);
                }
                case NUMBER_OF_PIERS -> {
                    log.log(Level.INFO, "{} has been set = {}", key, value);
                    port.setNumberOfPiers(value);
                }
                case INIT_NUMBER_OF_CONTAINERS -> {
                    AtomicInteger initNumberOfContainers = new AtomicInteger(value.intValue());
                    log.log(Level.INFO, "{} has been set = {}", key, value);
                    port.setNumberOfContainers(initNumberOfContainers);
                }
                default -> log.log(Level.ERROR, "unsupported type - {}", key);
            }
        }
        port.initialise();
        return port;
    }
}
