package com.gempukku.stccg.league;

import com.gempukku.stccg.db.vo.CollectionType;
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
    public String getName() {
        return _name;
    }

    @Override
    public int getStart() {
        return _start;
    }

    @Override
    public int getEnd() {
        return _end;
    }

    @Override
    public int getMaxMatches() {
        return _maxMatches;
    }

    @Override
    public boolean isLimited() {
        return _limited;
    }

    @Override
    public GameFormat getFormat() {
        return _format;
    }

    @Override
    public CollectionType getCollectionType() {
        return _collectionType;
    }

    @Override
    public CardCollection getPrizeForLeagueMatchWinner(int winCountThisSeries) {
        return _leaguePrizes.getPrizeForLeagueMatchWinner(winCountThisSeries);
    }

}