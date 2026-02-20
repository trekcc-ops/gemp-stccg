package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;
import com.google.common.collect.Iterables;

import java.util.Collection;

public class DockAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final ShipCard _cardToDock;
    private final ActionCardResolver _dockingTargetResolver;

    public DockAction(Player player, ShipCard cardToDock, ST1EGame cardGame) {
        super(cardGame, player, ActionType.DOCK_SHIP);
        _cardToDock = cardToDock;
        Collection<FacilityCard> dockingTargetOptions = Filters.yourFacilitiesInPlay(cardGame, player).stream()
                .filter(card -> card.isCompatibleWith(cardGame, _cardToDock) &&
                        card.isAtSameLocationAsCard(_cardToDock))
                .toList();
        _dockingTargetResolver = new SelectCardsResolver(new SelectVisibleCardAction(cardGame, _performingPlayerId,
                "Choose facility to dock at", dockingTargetOptions));
        _cardTargets.add(_dockingTargetResolver);
    }

    @Override
    public PhysicalCard getPerformingCard() { return _cardToDock; }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Collection<PhysicalCard> selectedCards = _dockingTargetResolver.getCards(cardGame);
        if (selectedCards.size() == 1 && Iterables.getOnlyElement(selectedCards) instanceof FacilityCard facility) {
            setAsSuccessful();
            _cardToDock.setAsDockedAt(facility);
        } else {
            cardGame.sendErrorMessage("Unable to dock");
            setAsFailed();
        }
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardToDock.isDocked() && !_dockingTargetResolver.cannotBeResolved(cardGame);
    }

}