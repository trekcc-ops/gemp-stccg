package com.gempukku.stccg.common.filterable;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public enum Species implements Filterable {
    AENAR,
    ANDROID("android"),
    BAJORAN, BENZITE, BETAZOID, BOLIAN, BORG, BOSLIC,
    CAIRN, CARDASSIAN, DOPTERIAN, DOSI,
    EL_AURIAN ("El-Aurian"),
    FERENGI, FLAXIAN, HEKARAN,
    HOLOGRAM("hologram"),
    HUMAN("human"),
    HUMANOID("humanoid"),
    JEM_HADAR ("Jem'Hadar"),
    KELLERUN, KLINGON, LETHEAN, MARKALIAN, NAPEAN, RAMATIN, REMAN, ROMULAN, RUTIAN,
    TAKARAN, TAMARIAN, TILONIAN, TRILL, VORTA, VULCAN, YRIDIAN, ZALKONIAN, ZAKDORN;
    private final String _humanReadable;

    Species() { _humanReadable = StringUtils.capitalize(name().toLowerCase(Locale.ROOT)); }
    Species(String humanReadable) {
        _humanReadable = humanReadable;
    }

    @JsonValue
    public String getHumanReadable() {
        return _humanReadable;
    }

}