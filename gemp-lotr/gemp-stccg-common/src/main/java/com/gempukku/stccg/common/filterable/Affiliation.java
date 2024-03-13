package com.gempukku.stccg.common.filterable;

import java.util.Arrays;

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
    VIDIIAN("Vidiian",""),
    VULCAN("Vulcan", "https://www.trekcc.org/images/icons/1e/1E-VUL.gif");

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

    public static Affiliation findAffiliation(String name) {
            // TODO - Copied this logic from the LotR Culture class. May not be appropriate for this one.
        return Arrays.stream(values()).filter(
                affiliation -> affiliation.getHumanReadable().equalsIgnoreCase(name) ||
                        affiliation.toString().equals(
                                name.toUpperCase().replace(' ', '_').replace('-', '_')))
                .findFirst().orElse(null);
    }

}