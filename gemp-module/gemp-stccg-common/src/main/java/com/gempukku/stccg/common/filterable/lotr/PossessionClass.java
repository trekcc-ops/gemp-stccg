package com.gempukku.stccg.common.filterable.lotr;

import com.gempukku.stccg.common.filterable.Filterable;

public enum PossessionClass implements Filterable {
    HAND_WEAPON("Hand Weapon"), ARMOR("Armor"), HELM("Helm"),
    MOUNT("Mount"), RANGED_WEAPON("Ranged Weapon"),
    CLOAK("Cloak"), PIPE("Pipe"),
    BRACERS("Bracers"), RING("Ring"),
    BROOCH("Brooch"), GAUNTLETS("Gauntlets"), BOX("Box"),
    PALANTIR("Palantir"), PHIAL("Phial"), HORN("Horn"),
    CLASSLESS("Classless"),

    //PC Classes
    PONY("Pony")

    ;
    
    private final String _humanReadable;

    PossessionClass(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }
}
