package com.gempukku.stccg.serializing;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampTest {

    @Test
    public void test() {
        DateTimeFormatter timeStampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//        Timestamp sqlTimestamp = Timestamp.from(Instant.now().atOffset(ZoneOffset.UTC).toInstant());
        System.out.println("Current SQL Timestamp: " + ZonedDateTime.now(ZoneOffset.UTC).format(timeStampFormatter));
    }

}