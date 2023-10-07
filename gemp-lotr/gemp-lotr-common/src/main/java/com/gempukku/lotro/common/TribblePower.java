package com.gempukku.lotro.common;

public enum TribblePower implements Filterable {
    ACQUIRE("Acquire",true), ADVANCE("Advance",false),
    ANTE("Ante",true), ANTIDOTE("Antidote",false),
    ASSIMILATE("Assimilate",true),
    AVALANCHE("Avalanche",true), BAH("BaH!",false),
    BATTLE("Battle",true), BEAM("Beam",false),
    BIJ("BiJ",false),
    BONUS("Bonus",false), CHEAT("Cheat",false),
    CLONE("Clone",false), CONFRONT("Confront",true),
    CONVERT("Convert",true),
    COPY("Copy",true), CYCLE("Cycle",true),
    DABO("Dabo",false), DANCE("Dance",true),
    DECLOAK("Decloak",false),
    DISCARD("Discard",true), DOUBLE("Double",false),
    DRAW("Draw",true), EVADE("Evade",false),
    EVOLVE("Evolve",true),
    EXCHANGE("Exchange",true), FAMINE("Famine",true),
    FIZZBIN("Fizzbin",false), FLASH("Flash",true),
    FOLD("Fold",false),
    FREEZE("Freeze",true), GENEROSITY("Generosity",true),
    GO("Go",true), HONESTY("Honesty",true),
    IDIC("IDIC",false), KILL("Kill",true),
    KINDNESS("Kindness",true), LAUGHTER("Laughter",true),
    LOYALTY("Loyalty",false), MAGIC("Magic", true),
    MASAKA("Masaka", true), MIRROR("Mirror", false),
    MUTATE("Mutate", true),
    PARTY("Party", true), POISON("Poison", true),
    PROCESS("Process", true),
    QAPLA("Qapla'", true), QUADRUPLE("Quadruple", false),
    RECYCLE("Recycle", true),
    REPLAY("Replay", true), REPLICATE("Replicate", false),
    RESCUE("Rescue", true),
    RESET("Reset", true), RETREAT("Retreat", true),
    REVEAL("Reveal", true),
    REVERSE("Reverse", true), RIVAL("Rival", true),
    ROLL("Roll", false),
    SABOTAGE("Sabotage", true), SAFETY("Safety", false),
    SCAN("Scan", true), SCORE("Score", true),
    SHIFT("Shift",true), SKIP("Skip", true),
    STAMPEDE("Stampede", true), TALLY("Tally", false),
    TIME_WARP("Time Warp", false), TOXIN("Toxin", true),
    TRICK("Trick", false), UTILIZE("Utilize", true);

    private final boolean _isActive;
    private final String _humanReadable;
    TribblePower(String humanReadable, boolean isActive) {
        _humanReadable = humanReadable;
        _isActive = isActive;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

    public boolean isActive() { return _isActive; }

    public static String[] names() {
        TribblePower[] tribblePowers = values();
        String[] names = new String[values().length];

        for (int i = 0; i < tribblePowers.length; i++) {
            names[i] = tribblePowers[i].getHumanReadable();
        }
        return names;
    }
}