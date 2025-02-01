package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Uniqueness;

import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties({ "ImageFile", "Set id", "Info", "Mission/ Dilemma Type", "Points", "Span", "Characteristics/ Keywords",
"Requires", "Persona", "Command", "Lore", "Reports", "Names" })
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
    String _set;

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


    @JsonProperty("Region")
    String _region;


}