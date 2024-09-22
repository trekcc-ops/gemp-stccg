package com.gempukku.stccg.common.filterable;

public enum Keyword implements Filterable {

    ;
    private final String _humanReadable;
    private final boolean _infoDisplayable;
    private final boolean _multiples;

    Keyword(String humanReadable, boolean infoDisplayable, boolean multiples) {
        _humanReadable = humanReadable;
        _infoDisplayable = infoDisplayable;
        _multiples = multiples;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

    public String getHumanReadableGeneric() {
        if(_multiples)
            return _humanReadable + " bonus";

        return _humanReadable;
    }

    public boolean isInfoDisplayable() {
        return _infoDisplayable;
    }

    public boolean isMultiples() {
        return _multiples;
    }

}
