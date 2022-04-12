package com.parshin.port.parser.impl;

import com.parshin.port.entity.ShipCreator;
import com.parshin.port.parser.CustomParser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class CustomParserImpl implements CustomParser {
    private static final Logger log = LogManager.getLogger();
    private static final AtomicBoolean isInstanceCreated = new AtomicBoolean(false);
    private static final ReentrantLock lock = new ReentrantLock();
    private static CustomParserImpl instance;

    private static final String REGEXP_STRING_SPLITTER = "\\s+";
    private static final String REGEXP_MAP_SPLITTER = ":";

    private CustomParserImpl() {
    }

    public static CustomParserImpl getInstance() {
        if (!isInstanceCreated.get()) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new CustomParserImpl();
                    isInstanceCreated.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    @Override
    public Map<String, Double> parse(String initData) {
        String[] strings = initData.split(REGEXP_STRING_SPLITTER);
        Map<String, Double> result = new HashMap<>();

        for (String string : strings) {
            String[] line = string.strip().split(REGEXP_MAP_SPLITTER);
            result.put(line[0], Double.valueOf(line[1]));
        }
        log.log(Level.INFO, "String has been parsed");
        return result;
    }
}
