package com.gempukku.stccg.db.vo;

import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.*;

import java.util.Objects;

public class League {
    private final int _cost;
    private final String _name;
    private final String _type;
    private final String _clazz;
    private final String _parameters;
    private final int _status;
    private LeagueData _leagueData;

    public League(int cost, String name, String type, String clazz, String parameters, int status) {
        _cost = cost;
        _name = name;
        _type = type;
        _clazz = clazz;
        _parameters = parameters;
        _status = status;
    }

    public int getCost() {
        return _cost;
    }

    public String getName() {
        return _name;
    }

    public String getType() {
        return _type;
    }

    public synchronized LeagueData getLeagueData(CardBlueprintLibrary bpLibrary, FormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions) {
        if (_leagueData == null) {
            try {
                if(_clazz.equals(ConstructedLeagueData.class.getName())) {
                    _leagueData = new ConstructedLeagueData(bpLibrary, formatLibrary, _parameters);
                }
                else if(_clazz.equals(NewConstructedLeagueData.class.getName())) {
                    _leagueData = new NewConstructedLeagueData(bpLibrary, formatLibrary, _parameters);
                }
                else if(_clazz.equals(SealedLeagueData.class.getName())) {
                    _leagueData = new SealedLeagueData(bpLibrary, formatLibrary, _parameters);
                }
                else if(_clazz.equals(NewSealedLeagueData.class.getName())) {
                    _leagueData = new NewSealedLeagueData(bpLibrary, formatLibrary, _parameters);
                }
                else if(_clazz.equals(SoloDraftLeagueData.class.getName())) {
                    _leagueData = new SoloDraftLeagueData(bpLibrary,  formatLibrary, soloDraftDefinitions, _parameters);
                }
                else {
                    throw new IllegalArgumentException("Class '" + _clazz + "' does not have a constructor registered.");
                }
            } catch (Exception exp) {
                throw new RuntimeException("Unable to create LeagueData", exp);
            }
        }
        return _leagueData;
    }

    public int getStatus() {
        return _status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        League league = (League) o;

        return Objects.equals(_type, league._type);
    }

    @Override
    public int hashCode() {
        return _type != null ? _type.hashCode() : 0;
    }
}
