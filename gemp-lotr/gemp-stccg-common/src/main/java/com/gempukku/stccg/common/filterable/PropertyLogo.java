package com.gempukku.stccg.common.filterable;

public enum PropertyLogo implements Filterable {
    // TV shows
    TOS("Star Trek"),
    TNG("Star Trek: The Next Generation"),
    DS9("Star Trek: Deep Space Nine"),
    VOY("Star Trek: Voyager"),
    ENT("Star Trek: Enterprise"),
    TAS("Star Trek: The Animated Series"),

    // Movies
    TMP("Star Trek: The Motion Picture"),
    ST2("Star Trek II: The Wrath of Khan"),
    ST3("Star Trek III: The Search for Spock"),
    ST4("Star Trek IV: The Voyage Home"),
    ST5("Star Trek V: The Final Frontier"),
    ST6("Star Trek VI: The Undiscovered Country"),
    GENERATIONS("Star Trek: Generations"),
    FIRST_CONTACT("Star Trek: First Contact"),
    INSURRECTION("Star Trek: Insurrection"),
    NEMESIS("Star Trek: Nemesis"),

    // Other products
    ARMADA("Star Trek: Armada"),
    KLINGON_CHALLENGE("Star Trek: A Klingon Challenge"),
    CCG("Star Trek: Customizable Card Game");

    private String _humanReadable;
    PropertyLogo(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }
}