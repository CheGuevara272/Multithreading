package com.parshin.port.reader;

import com.parshin.port.exception.CustomException;

public interface CustomReader {
    String readFile(String stringFileName) throws CustomException;
}
