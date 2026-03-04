package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardToDestinationAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.CanEnterPlayFilter;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.Collection;

public class SeedCardIntoPlayBlueprint extends DefaultActionBlueprint {
    private final FilterBlueprint _destinationBlueprint;
    private final FilterBlueprint _cardToSeedBlueprint;

    public SeedCardIntoPlayBlueprint(@JsonProperty(value = "destination", required = true)
                                     FilterBlueprint destinationBlueprint,
                                     @JsonProperty(value = "limit")
                                   UsageLimitBlueprint usageLimit,
                                     @JsonProperty(value = "seededCard", required = true) FilterBlueprint cardToSeedBlueprint
    ) {
        super(new YouPlayerSource());
        _cardToSeedBlueprint = cardToSeedBlueprint;
        _destinationBlueprint = destinationBlueprint;
        if (usageLimit != null) {
            usageLimit.applyLimitToActionBlueprint(this);
        }
    }

    public SeedCardAction createAction(DefaultGame cardGame, GameTextContext context) {
        CardFilter seedableCardFilter = _cardToSeedBlueprint.getFilterable(cardGame, context);
        CardFilter destinationFilter = _destinationBlueprint.getFilterable(cardGame, context);
        Collection<PhysicalCard> seedableCards = Filters.filter(cardGame, Zone.SEED_DECK, seedableCardFilter,
                new CanEnterPlayFilter(EnterPlayActionType.SEED),
                Filters.owner(context.yourName()));
        Collection<PhysicalCard> destinationOptions = Filters.filterCardsInPlay(cardGame, destinationFilter);

        if (seedableCards.isEmpty() || destinationOptions.isEmpty()) {
            return null;
        } else if (!(context.acceptsAllRequirements(cardGame, _requirements))) {
            return null;
        }

        SeedCardAction action = new SeedCardToDestinationAction(cardGame, context.yourName(),
                seedableCards, destinationOptions, context.card());
        appendSubActions(action);
        return action;
    }

}