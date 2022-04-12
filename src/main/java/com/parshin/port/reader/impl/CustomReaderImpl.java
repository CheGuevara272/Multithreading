package com.parshin.port.reader.impl;

import com.parshin.port.exception.CustomException;
import com.parshin.port.reader.CustomReader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

public class CustomReaderImpl implements CustomReader {
    private static final Logger log = LogManager.getLogger();
    private static final AtomicReference<CustomReaderImpl> instance = new AtomicReference<>();

    private CustomReaderImpl() {
    }

    public static CustomReaderImpl getInstance() {
        while (true) {
            CustomReaderImpl current = instance.get();
            if (current != null) {
                return current;
            }
            current = new CustomReaderImpl();
            if (instance.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    @Override
    public String readFile(String fileName) throws CustomException{
        File file = new File(fileName);
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            log.log(Level.ERROR, "File wasn't read", e); //TODO Как правильно сделать? Записывать лог тут, или когда будем ловить брошеный ниже exception
            throw new CustomException("File was not read", e);
        }
    }
}
