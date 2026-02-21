package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Collection;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AtLocationDestinationBlueprint.class, name = "atLocation"),
        @JsonSubTypes.Type(value = ToCardDestinationBlueprint.class, name = "toCard")
})
public interface DestinationBlueprint {

    Collection<PhysicalCard> getDestinationOptions(ST1EGame stGame, String performingPlayerName,
                                                          PhysicalCard cardArriving, GameTextContext context);

}