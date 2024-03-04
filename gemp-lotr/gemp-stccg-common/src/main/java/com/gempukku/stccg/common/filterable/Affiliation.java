package com.gempukku.stccg.common.filterable;

public enum Affiliation implements Filterable {
    BAJORAN("Bajoran",""),
    BORG("Borg",""),
    CARDASSIAN("Cardassian",""),
    DOMINION("Dominion",""),
    FEDERATION("Federation", "https://www.trekcc.org/images/icons/1e/1E-FED.gif"),
    FERENGI("Ferengi",""),
    HIROGEN("Hirogen",""),
    KAZON("Kazon",""),
    KLINGON("Klingon","https://www.trekcc.org/images/icons/1e/1E-KLG.gif"),
    NEUTRAL("Neutral","https://www.trekcc.org/images/icons/1e/1E-NEU.gif"),
    NON_ALIGNED("Non-Aligned","https://www.trekcc.org/images/icons/1e/1E-NON.gif"),
    ROMULAN("Romulan","https://www.trekcc.org/images/icons/1e/1E-ROM.gif"),
    STARFLEET("Starfleet",""),
    VIDIIAN("Vidiian","");

    private final String _humanReadable, _iconURL;
    Affiliation(String humanReadable, String iconURL) {
        _humanReadable = humanReadable;
        _iconURL = iconURL;
    }
    public String getHumanReadable() { return _humanReadable; }
    public String toHTML() {
        if (_iconURL.isEmpty())
            return _humanReadable;
        else
            return "<img src='" + _iconURL + "' class='inline-icon' title='" + _humanReadable + "'>"; }
}