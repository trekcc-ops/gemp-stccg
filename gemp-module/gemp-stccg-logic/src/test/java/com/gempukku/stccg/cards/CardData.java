package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.filterable.CardType;

import java.util.List;

@JsonIgnoreProperties({ "ImageFile", "Set id", "Info", "Mission/ Dilemma Type", "Points", "Span", "Characteristics/ Keywords",
"Requires", "Persona", "Command", "Reports", "Names" })
public class CardData {

    @JsonProperty("Staff")
    String _staffing;

    @JsonProperty("Property")
    String _property;

    @JsonProperty("Name")
    String _lackeyTitle;

    @JsonProperty("Text")
    String _rawGameText;

    @JsonProperty("Type")
    CardType _type;

    @JsonProperty("Release")
    String _release;

    @JsonProperty("Set")
    String _formats;

    @JsonProperty("Blueprint ID")
    String _blueprintId;
    @JsonProperty("Int/Rng")
    String _attribute1;

    @JsonProperty("Cun/Wpn")
    String _attribute2;

    @JsonProperty("Str/Shd")
    String _attribute3;

    @JsonProperty("Uniqueness")
    String _uniqueness;

    @JsonProperty("Class")
    String _class;

    @JsonProperty("Icons")
    String _icons;

    @JsonProperty("Affil")
    String _affiliation;

    @JsonProperty("Quadrant")
    String _quadrant;

    @JsonProperty("Lore")
    String _lore;

    @JsonProperty("Region")
    String _region;

    List<String> getAttributes() {
        return List.of(_attribute1, _attribute2, _attribute3);
    }


}