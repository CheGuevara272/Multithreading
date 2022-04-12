package com.parshin.port.parser;

import java.util.Map;

public interface CustomParser {
    Map<String, Double> parse(String portInit);
}
