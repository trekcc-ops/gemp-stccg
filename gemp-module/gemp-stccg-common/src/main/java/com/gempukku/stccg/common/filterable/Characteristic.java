package com.gempukku.stccg.common.filterable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public enum Characteristic implements Filterable {
    ADMIRAL("admiral"),
    CAPTAINS_ORDER("Captain's Order"),
    COOK("cook"),
    GENERAL("general"),

    @SuppressWarnings("SpellCheckingInspection") K_EHLEYR("K'Ehleyr"),
    MAJE("Maje"),
    NOR("Nor"),
    SCOTTY("Scotty"),
    PADD("PADD");

    private String _humanReadable;

    Characteristic(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }
    
    public static Characteristic findCharacteristic(String name) {
        Stream<Characteristic> characteristicValues = Arrays.stream(values());
        return characteristicValues.filter(
                        characteristic -> characteristic.getHumanReadable().equalsIgnoreCase(name) ||
                                characteristic.toString().equals(
                                        name.toUpperCase(Locale.ROOT)
                                                .replace(' ', '_')
                                                .replace("'", "_")
                                                .replace('-', '_')))
                .findFirst().orElse(null);
    }

}