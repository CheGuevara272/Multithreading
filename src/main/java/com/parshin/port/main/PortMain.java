package com.parshin.port.main;

import com.parshin.port.entity.Port;
import com.parshin.port.entity.PortBuilder;
import com.parshin.port.entity.Ship;
import com.parshin.port.entity.ShipCreator;
import com.parshin.port.exception.CustomException;
import com.parshin.port.parser.impl.CustomParserImpl;
import com.parshin.port.reader.impl.CustomReaderImpl;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PortMain {
    private static final Logger log = LogManager.getLogger();
    private static final String PORT_INIT_FILE_NAME = "data/port_init.txt";
    private static final String SHIPS_INIT_FILE_NAME = "data/ships_init.txt";

    private Port port;

    public static void main(String[] args) throws CustomException {
        PortMain portRunner = new PortMain();
        portRunner.run();
    }

    private void run() throws CustomException {
        ShipCreator shipGenerator = ShipCreator.getInstance();
        List<Ship> ships;

        initialisePort();
        ships = shipGenerator.getShips(port, SHIPS_INIT_FILE_NAME);

        ExecutorService executor;
        executor = Executors.newFixedThreadPool(10);
        log.log(Level.INFO, "\n\n <<<<<<<<<< START >>>>>>>>>>\n");

        ships.forEach(executor::execute);
        executor.shutdown();

        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            log.log(Level.ERROR, "Sleep if thread {} was interrupted", Thread.currentThread().getName(), e);
            Thread.currentThread().interrupt();
        }

        while (port.getShipCounter().intValue() > 0) {
            Thread.yield();
        }

        log.log(Level.INFO, "\n\n <<<<<<<<<< PortRunner finished work >>>>>>>>>>\n");
        log.log(Level.INFO, "\n{} - docks were gotten, {} - docks were returned\n", port.getDockGetCount(), port.getDockReturnCount());

        log.log(Level.INFO, "\n\nAvailable containers in port value - {} \nContainers income value - {} \nContainers outcome value - {}",
                port.getNumberOfContainers().intValue(), port.getDebit().intValue(), port.getCredit().intValue());
    }

    private void initialisePort() throws CustomException {
        CustomReaderImpl reader = CustomReaderImpl.getInstance();
        String initDataText = reader.readFile(PORT_INIT_FILE_NAME);
        CustomParserImpl parser = CustomParserImpl.getInstance();
        Map<String, Double> initMap = parser.parse(initDataText);
        PortBuilder builder = new PortBuilder();
        port = builder.getPort(initMap);
    }
}