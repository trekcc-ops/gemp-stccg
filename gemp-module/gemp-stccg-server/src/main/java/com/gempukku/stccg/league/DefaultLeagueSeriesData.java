package com.gempukku.stccg.league;

import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.formats.GameFormat;

public class DefaultLeagueSeriesData implements LeagueSeriesData {
    private final LeaguePrizes _leaguePrizes;
    private final boolean _limited;
    private final String _name;
    private final int _start;
    private final int _end;
    private final int _maxMatches;
    private final GameFormat _format;
    private final CollectionType _collectionType;

    public DefaultLeagueSeriesData(LeaguePrizes leaguePrizes, boolean limited, String name, int start, int end, int maxMatches, GameFormat format, CollectionType collectionType) {
        _leaguePrizes = leaguePrizes;
        _limited = limited;
        _name = name;
        _start = start;
        _end = end;
        _maxMatches = maxMatches;
        _format = format;
        _collectionType = collectionType;
    }

    @Override
    public final String getName() {
        return _name;
    }

    @Override
    public final int getStart() {
        return _start;
    }

    @Override
    public final int getEnd() {
        return _end;
    }

    @Override
    public final int getMaxMatches() {
        return _maxMatches;
    }

    @Override
    public final boolean isLimited() {
        return _limited;
    }

    @Override
    public final GameFormat getFormat() {
        return _format;
    }

    @Override
    public final CollectionType getCollectionType() {
        return _collectionType;
    }

    @Override
    public final CardCollection getPrizeForLeagueMatchWinner(int winCount) {
        return _leaguePrizes.getPrizeForLeagueMatchWinner(winCount);
    }

}