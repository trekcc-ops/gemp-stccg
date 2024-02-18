package com.gempukku.stccg.common.filterable;

public enum RegularSkill implements Filterable {
    // Classifications
    ANIMAL("ANIMAL"), CIVILIAN("CIVILIAN"), ENGINEER("ENGINEER"),
    MEDICAL("MEDICAL"), OFFICER("OFFICER"), SCIENCE("SCIENCE"),
    SECURITY("SECURITY"), VIP("V.I.P."),

    // Other skills
    ANTHROPOLOGY("Anthropology"), ACQUISITION("Acquisition"),
    ARCHAEOLOGY("Archaeology"), ASTROPHYSICS("Astrophysics"),
    BARBERING("Barbering"), BIOLOGY("Biology"),
    CANTANKEROUSNESS("Cantankerousness"), COMPUTER_SKILL("Computer Skill"),
    CYBERNETICS("Cybernetics"), DIPLOMACY("Diplomacy"),
    EMPATHY("Empathy"), EXOBIOLOGY("Exobiology"),
    FCA("FCA", true),
    GEOLOGY("Geology"), GREED("Greed"),
    GURAMBA("Guramba"), HONOR("Honor"),
    KLINGON_INTELLIGENCE("Klingon Intelligence", true),
    LAW("Law"), LEADERSHIP("Leadership"),
    MEMORY_OMEGA("Memory Omega", true),
    MINDMELD("Mindmeld"), MIRACLE_WORKER("Miracle Worker"),
    MUSIC("Music"), NAVIGATION("Navigation"),
    OBSIDIAN_ORDER("Obsidian Order", true),
    ORION_SYNDICATE("Orion Syndicate", true),
    PHYSICS("Physics"), RESISTANCE("Resistance"),
    SECTION_31("Section 31", true),
    SMUGGLING("Smuggling"), STELLAR_CARTOGRAPHY("Stellar Cartography"),
    TAL_SHIAR("Tal Shiar", true),
    TRANSPORTER_SKILL("Transporter Skill"), TREACHERY("Treachery"),
    V_SHAR("V'Shar", true),
    YOUTH("Youth", false);

    private final String _humanReadable;
    private final boolean _isIntelligence;

    RegularSkill(String humanReadable) {
        this(humanReadable, false);
    }

    RegularSkill(String humanReadable, boolean isIntelligence) {
        _humanReadable = humanReadable;
        _isIntelligence = true;
    }

    public String get_humanReadable() { return _humanReadable; }
}