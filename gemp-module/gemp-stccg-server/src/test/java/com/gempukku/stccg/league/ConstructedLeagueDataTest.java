package com.gempukku.stccg.league;

import com.gempukku.stccg.AbstractServerTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstructedLeagueDataTest extends AbstractServerTest {
    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void testParameters() {
        final List<LeagueSeriesData> series = getLeagueSeriesData();
        assertEquals(3, series.size());
        assertEquals(20120312, series.getFirst().getStart());
        assertEquals(20120318, series.get(0).getEnd());
        assertEquals("st1emoderncomplete", series.get(0).getFormat().getCode());
        assertEquals(20120319, series.get(1).getStart());
        assertEquals(20120325, series.get(1).getEnd());
        assertEquals("st1emoderncomplete", series.get(1).getFormat().getCode());
        assertEquals(20120326, series.get(2).getStart());
        assertEquals(20120401, series.get(2).getEnd());
        assertEquals("st2e", series.get(2).getFormat().getCode());
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static List<LeagueSeriesData> getLeagueSeriesData() {
        String params = "20120312"; // start date
        params = params + ",st1emoderncomplete"; // league prize pool
        params = params + ",0.7"; // prize multiplier
        params = params + ",default"; // collection type
        params = params + ",All cards"; // collection name
        params = params + ",7"; // series length
        params = params + ",10"; // series match count
        params = params + ",3"; // number of series in league
        params = params + ",st1emoderncomplete,st1emoderncomplete"; // format and prize pool for series 1
        params = params + ",st1emoderncomplete,st1emoderncomplete"; // format and prize pool for series 2
        params = params + ",st2e,st1emoderncomplete"; // format and prize pool for series 3

        return new ConstructedLeagueData(_cardLibrary, _formatLibrary, params).getSeries();
    }
}