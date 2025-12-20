package com.gempukku.stccg.league;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;

import java.time.ZonedDateTime;

public class LeagueSeries {

    private final int _maxMatches;
    private final ZonedDateTime _endDate;
    private final GameFormat _format;
    private final ZonedDateTime _startDate;
    private final String _name;

    @JsonCreator
    public LeagueSeries(@JsonProperty("maxMatches")
                        int maxMatches,
                        @JsonProperty("startDate")
                        ZonedDateTime startDate,
                        @JsonProperty("endDate")
                        ZonedDateTime endDate,
                        @JsonProperty("format")
                        String formatCode,
                        @JsonProperty("name")
                        String name,
                        @JacksonInject FormatLibrary formatLibrary
    ) {
        _maxMatches = maxMatches;
        _startDate = startDate;
        _endDate = endDate;
        _name = name;
        _format = formatLibrary.get(formatCode);
    }

    public LeagueSeries(int duration, int maxMatches, ZonedDateTime startDate, GameFormat format,
                        String name) {
        _maxMatches = maxMatches;
        _startDate = startDate;
        _endDate = startDate.plusDays(duration);
        _name = name;
        _format = format;
    }

    public ZonedDateTime getStart() {
        return _startDate;
    }

    public ZonedDateTime getEnd() {
        return _endDate;
    }

    public String getName() {
        return _name;
    }

    public GameFormat getFormat() {
        return _format;
    }

    public int getMaxMatches() {
        return _maxMatches;
    }
}