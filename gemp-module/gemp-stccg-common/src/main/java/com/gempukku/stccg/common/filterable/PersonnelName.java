package com.gempukku.stccg.common.filterable;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public enum PersonnelName implements Filterable {
    DAIMON_BOK("DaiMon Bok"),
    GEORDI_LA_FORGE("Geordi La Forge"),
    GOWRON("Gowron"),
    JAMES_T_KIRK("James T. Kirk"),
    JEAN_LUC_PICARD ("Jean-Luc Picard"),
    MAROUK("Marouk"),
    MORN("Morn"),
    NOG("Nog"),
    QUARK_SON_OF_KELDAR("Quark Son of Keldar"),
    SIRNA_KOLRAMI("Sirna Kolrami"),
    TEBOK("Tebok");

    private final String _humanReadable;

    PersonnelName(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() { return _humanReadable; }

}