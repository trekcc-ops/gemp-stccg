package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.blueprints.resolver.YouPlayerResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

public class WhereYouControlCardsLocationFilter implements LocationFilter {

    YouPlayerResolver _youResolver;
    CardFilter _filter;

    public WhereYouControlCardsLocationFilter(YouPlayerResolver you, CardFilter filter) {
        _youResolver = you;
        _filter = filter;
    }

    public boolean accepts(ST1EGame game, MissionLocation location) throws InvalidGameLogicException {
        for (PhysicalCard card : game.getGameState().getAllCardsInPlay()) {
            if (card.getLocation() == location && card.isControlledBy(_youResolver.getPlayer()) &&
                    _filter.accepts(game, card)) {
                return true;
            }
        }
        return false;
    }
}