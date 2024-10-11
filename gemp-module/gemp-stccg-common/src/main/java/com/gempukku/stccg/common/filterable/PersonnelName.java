package com.gempukku.stccg.common.filterable;

@SuppressWarnings("SpellCheckingInspection")
public enum PersonnelName implements Filterable {
    GEORDI_LA_FORGE("Geordi La Forge"),
    GOWRON("Gowron"),
    JAMES_T_KIRK("James T. Kirk"),
    JEAN_LUC_PICARD ("Jean-Luc Picard"),
    MAROUK("Marouk"),
    TEBOK("Tebok");

    private final String _humanReadable;

    PersonnelName(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() { return _humanReadable; }

}