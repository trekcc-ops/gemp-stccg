package com.gempukku.stccg.common.filterable;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public enum Species implements Filterable {
    ANDROID("android"),
    BAJORAN, BENZITE, BETAZOID, BOLIAN, BORG, BOSLIC,
    CAIRN, CARDASSIAN, DOPTERIAN,
    EL_AURIAN ("El-Aurian"),
    FERENGI, FLAXIAN, HEKARAN,
    HOLOGRAM("hologram"),
    HUMAN("human"),
    HUMANOID("humanoid"),
    KELLERUN, KLINGON, LETHEAN, MARKALIAN, NAPEAN, ROMULAN, RUTIAN,
    TAKARAN, TILONIAN, TRILL, VULCAN, YRIDIAN, ZALKONIAN;
    private final String _humanReadable;

    Species() { _humanReadable = StringUtils.capitalize(name().toLowerCase(Locale.ROOT)); }
    Species(String humanReadable) {
        _humanReadable = humanReadable;
    }

    @JsonValue
    private String getHumanReadable() {
        return _humanReadable;
    }

}