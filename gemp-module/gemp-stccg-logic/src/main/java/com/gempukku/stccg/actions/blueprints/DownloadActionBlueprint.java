package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.actions.targetresolver.EnterPlayAtDestinationResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.AndFilterBlueprint;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.YouCanDownloadFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public class DownloadActionBlueprint implements SubActionBlueprint {

    private final FilterBlueprint _cardToDownloadFilter;

    DownloadActionBlueprint(@JsonProperty(value = "filter") FilterBlueprint cardToDownloadFilter) {
        _cardToDownloadFilter = new AndFilterBlueprint(cardToDownloadFilter, new YouCanDownloadFilterBlueprint());
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext actionContext) {
        List<Action> result = new ArrayList<>();
        String performingPlayerName = actionContext.yourName();
        Collection<PhysicalCard> downloadableCards =
                Filters.filter(cardGame, _cardToDownloadFilter.getFilterable(cardGame, actionContext));
        Map<PhysicalCard, Collection<PhysicalCard>> destinationMap = new HashMap<>();
        for (PhysicalCard downloadableCard : downloadableCards) {
            Collection<PhysicalCard> destinationOptions =
                    downloadableCard.getDestinationOptionsFromGameText(actionContext, cardGame);
            if (!destinationOptions.isEmpty()) {
                destinationMap.put(downloadableCard, destinationOptions);
            }
        }
        if (!destinationMap.isEmpty()) {
            EnterPlayAtDestinationResolver resolver =
                    new EnterPlayAtDestinationResolver(performingPlayerName, destinationMap);
            Action downloadAction = new DownloadCardAction(cardGame, performingPlayerName, resolver, actionContext.card());
            result.add(downloadAction);
        }
        return result;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext actionContext) {
        return !Filters.filter(cardGame, _cardToDownloadFilter.getFilterable(cardGame, actionContext)).isEmpty();
    }
}