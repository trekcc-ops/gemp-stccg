package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionStatus;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.blueprints.UseNormalCardPlayBlueprint;
import com.gempukku.stccg.actions.targetresolver.ActionTargetResolver;
import com.gempukku.stccg.actions.targetresolver.ReportCardResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.List;

public class PlayCardAction extends ActionWithSubActions implements TopLevelSelectableAction {
    private final PhysicalCard _performingCard;
    protected PhysicalCard _cardEnteringPlay;
    private final EnterPlayActionType _type;
    @JsonProperty("destinationZone")
    protected Zone _destinationZone;
    private boolean _played;

    public PlayCardAction(DefaultGame cardGame, PhysicalCard actionSource, PhysicalCard cardEnteringPlay,
                          String performingPlayerName, Zone toZone, ActionType actionType, GameTextContext context) {
        super(cardGame, performingPlayerName, actionType, context);
        _performingCard = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _destinationZone = toZone;
        _type = switch(actionType) {
            case DOWNLOAD_CARD -> EnterPlayActionType.DOWNLOAD;
            case PLAY_CARD -> EnterPlayActionType.PLAY;
            case SEED_CARD -> EnterPlayActionType.SEED;
            default -> null;
        };
    }

    protected PlayCardAction(int actionId, PhysicalCard actionSource, PhysicalCard cardEnteringPlay,
                             String performingPlayerName, Zone toZone, ActionType actionType,
                             ActionStatus status) {
        super(actionId, actionType, performingPlayerName, status, new GameTextContext(actionSource, performingPlayerName));
        _performingCard = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _destinationZone = toZone;
        _type = switch(actionType) {
            case DOWNLOAD_CARD -> EnterPlayActionType.DOWNLOAD;
            case PLAY_CARD -> EnterPlayActionType.PLAY;
            case SEED_CARD -> EnterPlayActionType.SEED;
            default -> null;
        };
    }

    public PlayCardAction(DefaultGame cardGame, PhysicalCard actionSource, PhysicalCard cardEnteringPlay,
                          String performingPlayerName, Zone toZone, ActionType actionType) {
        super(cardGame, performingPlayerName, actionType, new GameTextContext(actionSource, performingPlayerName));
        _performingCard = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _destinationZone = toZone;
        _type = switch(actionType) {
            case DOWNLOAD_CARD -> EnterPlayActionType.DOWNLOAD;
            case PLAY_CARD -> EnterPlayActionType.PLAY;
            case SEED_CARD -> EnterPlayActionType.SEED;
            default -> null;
        };
    }


    public boolean requirementsAreMet(DefaultGame cardGame) {
        if (_cardEnteringPlay != null) {
            return cardGame.getRules().cardCanEnterPlay(cardGame, _cardEnteringPlay, _type);
        } else {
            for (ActionTargetResolver resolver : _cardTargets) {
                if (resolver.cannotBeResolved(cardGame)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    public PhysicalCard getCardEnteringPlay() { return _cardEnteringPlay; }

    @JsonIgnore
    public Collection<? extends PhysicalCard> getSelectableCardsToPlayForTesting(DefaultGame cardGame) {
        if (_cardEnteringPlay != null) {
            return List.of(_cardEnteringPlay);
        } else if (_cardTargets.size() == 1 && _cardTargets.getFirst() instanceof ReportCardResolver resolver) {
            return resolver.getSelectableCardsToEnterPlay();
        } else {
            return null;
        }
    }

    protected void putCardIntoPlay(DefaultGame cardGame) throws PlayerNotFoundException {
        _cardEnteringPlay.removeFromCardGroup(cardGame);
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        if (performingPlayer.getCardsInDrawDeck().contains(_cardEnteringPlay)) {
            cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
            performingPlayer.shuffleDrawDeck(cardGame);
        }
        GameState gameState = cardGame.getGameState();
        cardGame.removeCardsFromZone(List.of(_cardEnteringPlay));
        gameState.addCardToZone(cardGame, _cardEnteringPlay, _destinationZone, _actionContext);
        saveResult(new PlayCardResult(cardGame,this, _cardEnteringPlay), cardGame);
        _played = true;
    }

    protected void processEffect(DefaultGame cardGame) {
        if (!_played) {
            try {
                putCardIntoPlay(cardGame);
            } catch (PlayerNotFoundException exp) {
                cardGame.sendErrorMessage(exp);
                setAsFailed();
            }
        } else {
            super.processEffect(cardGame);
        }
    }

    public void removeNormalCardPlayCost() {
        _queuedCosts.removeIf(cost -> cost instanceof UseNormalCardPlayBlueprint);
    }

}