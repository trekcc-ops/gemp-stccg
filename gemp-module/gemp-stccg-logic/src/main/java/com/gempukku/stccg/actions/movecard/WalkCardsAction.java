package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class WalkCardsAction extends BeamOrWalkAction {

    public WalkCardsAction(DefaultGame cardGame, Player player, ST1EPhysicalCard cardWalkingFrom) {
        super(cardGame, player, cardWalkingFrom, ActionType.WALK_CARDS);
    }

    @Override
    protected Collection<PhysicalCard> getDestinationOptions(ST1EGame game) {
        Collection<PhysicalCard> result = new LinkedList<>();
        if (_cardSource instanceof ShipCard ship && ship.isDocked())
            result.add(ship.getDockedAtCard(game));
        else if (_cardSource instanceof FacilityCard)
            result.addAll(Filters.filter(
                    _cardSource.getAttachedCards(game), game, Filters.ship, Filters.your(_performingPlayerId)));
        return result;
    }

    @Override
    public List<PhysicalCard> getValidFromCards(DefaultGame game) {
        List<PhysicalCard> cards = new ArrayList<>();
        cards.add(_cardSource);
        return cards;
    }

    protected String actionVerb() { return "walk"; }

    public boolean requirementsAreMet(DefaultGame cardGame) {
            // TODO - No compatibility check
        return !_destinationOptions.isEmpty() &&
                !Filters.filter(_cardSource.getAttachedCards(cardGame), cardGame, Filters.personnel).isEmpty();
    }

}