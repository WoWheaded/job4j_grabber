package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HabrCareerDateTimeParserTest {
    @Test
    void parse() {
        String datetime = "2023-09-26T11:41:32+03:00";
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        LocalDateTime result = dateTimeParser.parse(datetime);
        assertEquals("2023-09-26T11:41:32", result.toString());
    }
}