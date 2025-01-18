package com.gempukku.stccg.cards.blueprints.trigger;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.discard.DiscardCardFromDeckResult;
import com.gempukku.stccg.actions.discard.DiscardCardFromHandResult;
import com.gempukku.stccg.actions.discard.DiscardCardFromPlayResult;
import com.gempukku.stccg.actions.draw.DrawCardOrPutIntoHandResult;
import com.gempukku.stccg.actions.placecard.ReturnCardsToHandResult;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.actions.revealcards.RevealCardFromTopOfDeckResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Objects;

public class TriggerConditions {

    public static boolean startOfPhase(DefaultGame game, ActionResult actionResult, Phase phase) {
        return (actionResult.getType() == ActionResult.Type.START_OF_PHASE
                && game.getGameState().getCurrentPhase() == phase);
    }

    public static boolean startOfTurn(ActionResult actionResult) {
        return actionResult.getType() == ActionResult.Type.START_OF_TURN;
    }

    public static boolean endOfTurn(ActionResult actionResult) {
        return actionResult.getType() == ActionResult.Type.END_OF_TURN;
    }

    public static boolean playerGoesOut(ActionResult actionResult, String playerId) {
        return (actionResult.getType() == ActionResult.Type.PLAYER_WENT_OUT &&
                Objects.equals(actionResult.getPlayer(), playerId));
    }

    public static boolean forEachCardDrawn(ActionResult actionResult, String playerId) {
        if (actionResult.getType() == ActionResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
            DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) actionResult;
            return drawResult.isDraw() && drawResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachDiscardedFromPlay(DefaultGame game, ActionResult actionResult, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.FOR_EACH_DISCARDED_FROM_PLAY)
            return Filters.and(filters).accepts(game, ((DiscardCardFromPlayResult) actionResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromHand(DefaultGame game, ActionResult actionResult, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.FOR_EACH_DISCARDED_FROM_HAND)
            return Filters.and(filters).accepts(game, ((DiscardCardFromHandResult) actionResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromDeck(DefaultGame game, ActionResult actionResult, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.FOR_EACH_DISCARDED_FROM_DECK)
            return Filters.and(filters).accepts(game, ((DiscardCardFromDeckResult) actionResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromHandBy(DefaultGame game, ActionResult actionResult, Filterable discardedBy, Filterable... discarded) {
        if (actionResult.getType() == ActionResult.Type.FOR_EACH_DISCARDED_FROM_HAND) {
            DiscardCardFromHandResult discardResult = (DiscardCardFromHandResult) actionResult;
            if (discardResult.getSource() != null 
                    && Filters.and(discardedBy).accepts(game, discardResult.getSource()))
                return Filters.and(discarded).accepts(game, discardResult.getDiscardedCard());
        }
        return false;
    }

    public static boolean revealedCardsFromTopOfDeck(ActionResult actionResult, String playerId) {
        if (actionResult.getType() == ActionResult.Type.FOR_EACH_REVEALED_FROM_TOP_OF_DECK) {
            RevealCardFromTopOfDeckResult revealCardFromTopOfDeckResult = (RevealCardFromTopOfDeckResult) actionResult;
            return revealCardFromTopOfDeckResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachReturnedToHand(DefaultGame game, ActionResult actionResult, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.FOR_EACH_RETURNED_TO_HAND) {
            ReturnCardsToHandResult result = (ReturnCardsToHandResult) actionResult;
            return Filters.and(filters).accepts(game, result.getReturnedCard());
        }
        return false;
    }

    public static boolean played(DefaultGame game, ActionResult actionResult, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.PLAY_CARD) {
            PhysicalCard playedCard = ((PlayCardResult) actionResult).getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard);
        }
        return false;
    }

    public static boolean played(Player player, ActionResult actionResult, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.PLAY_CARD) {
            if (actionResult.getPerformingPlayerId().equals(player.getPlayerId())) {
                PhysicalCard playedCard = ((PlayCardResult) actionResult).getPlayedCard();
                return Filters.and(filters).accepts(player.getGame(), playedCard);
            }
        }
        return false;
    }


    public static boolean playedFromZone(DefaultGame game, ActionResult actionResult, Zone zone, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.PLAY_CARD) {
            final PlayCardResult playResult = (PlayCardResult) actionResult;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return (playResult.getPlayedFrom() == zone && Filters.and(filters).accepts(game, playedCard));
        }
        return false;
    }

    public static boolean playedOn(DefaultGame game, ActionResult actionResult,
                                   Filterable targetFilter, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.PLAY_CARD) {
            final PlayCardResult playResult = (PlayCardResult) actionResult;
            final PhysicalCard attachedTo = playResult.getAttachedTo();
            if (attachedTo == null)
                return false;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard)
                    && Filters.and(targetFilter).accepts(game, attachedTo);
        }
        return false;
    }


}