package com.gempukku.stccg.league;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
                        @JsonProperty("start")
                        ZonedDateTime startDate,
                        @JsonProperty("end")
                        ZonedDateTime endDate,
                        @JsonProperty("formatCode")
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

    @JsonProperty("start")
    public ZonedDateTime getStart() {
        return _startDate;
    }

    @JsonProperty("end")
    public ZonedDateTime getEnd() {
        return _endDate;
    }

    @JsonProperty("name")
    public String getName() {
        return _name;
    }

    @JsonIgnore
    public GameFormat getFormat() {
        return _format;
    }

    @JsonProperty("formatCode")
    private String getFormatCode() {
        return _format.getCode();
    }

    @JsonProperty("maxMatches")
    public int getMaxMatches() {
        return _maxMatches;
    }
}