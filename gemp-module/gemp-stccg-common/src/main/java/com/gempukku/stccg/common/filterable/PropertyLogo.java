package com.gempukku.stccg.common.filterable;

public enum PropertyLogo implements Filterable {
    // TV shows
    TOS_LOGO("Star Trek"),
    TNG_LOGO("Star Trek: The Next Generation"),
    DS9_LOGO("Star Trek: Deep Space Nine"),
    VOY_LOGO("Star Trek: Voyager"),
    ENT_LOGO("Star Trek: Enterprise"),
    TAS_LOGO("Star Trek: The Animated Series"),

    // Movies
    TMP_LOGO("Star Trek: The Motion Picture"),
    ST2_LOGO("Star Trek II: The Wrath of Khan"),
    ST3_LOGO("Star Trek III: The Search for Spock"),
    ST4_LOGO("Star Trek IV: The Voyage Home"),
    ST5_LOGO("Star Trek V: The Final Frontier"),
    ST6_LOGO("Star Trek VI: The Undiscovered Country"),
    GENERATIONS_LOGO("Star Trek: Generations"),
    FIRST_CONTACT_LOGO("Star Trek: First Contact"),
    INSURRECTION_LOGO("Star Trek: Insurrection"),
    NEMESIS_LOGO("Star Trek: Nemesis"),

    // Other products
    ARMADA_LOGO("Star Trek: Armada"),
    KLINGON_CHALLENGE_LOGO("Star Trek: A Klingon Challenge"),
    CCG_LOGO("Star Trek: Customizable Card Game");

    private final String _humanReadable;
    PropertyLogo(String humanReadable) {
        _humanReadable = humanReadable;
    }

}