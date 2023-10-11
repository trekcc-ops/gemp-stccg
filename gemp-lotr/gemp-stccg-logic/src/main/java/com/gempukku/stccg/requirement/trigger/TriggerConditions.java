package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.*;
import com.gempukku.stccg.effects.defaulteffect.DrawOneCardEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.*;

import java.util.Objects;

public class TriggerConditions {

    public static boolean startOfPhase(DefaultGame game, EffectResult effectResult, Phase phase) {
        return (effectResult.getType() == EffectResult.Type.START_OF_PHASE
                && game.getGameState().getCurrentPhase() == phase);
    }

    public static boolean endOfPhase(DefaultGame game, EffectResult effectResult, Phase phase) {
        return (effectResult.getType() == EffectResult.Type.END_OF_PHASE
                && (game.getGameState().getCurrentPhase() == phase || phase == null));
    }

    public static boolean startOfTurn(EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.START_OF_TURN;
    }

    public static boolean endOfTurn(EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.END_OF_TURN;
    }

    public static boolean playerGoesOut(EffectResult effectResult, String playerId) {
        return (effectResult.getType() == EffectResult.Type.PLAYER_WENT_OUT &&
                Objects.equals(effectResult.getPlayer(), playerId));
    }

    public static boolean reconciles(EffectResult effectResult, String playerId) {
        return effectResult.getType() == EffectResult.Type.RECONCILE && (playerId == null || ((ReconcileResult) effectResult).getPlayerId().equals(playerId));
    }

    public static boolean forEachCardDrawn(EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
            DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
            return drawResult.isDraw() && drawResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachDiscardedFromPlay(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY)
            return Filters.and(filters).accepts(game, ((DiscardCardsFromPlayResult) effectResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromHand(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_HAND)
            return Filters.and(filters).accepts(game, ((DiscardCardFromHandResult) effectResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromDeck(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_DECK)
            return Filters.and(filters).accepts(game, ((DiscardCardFromDeckResult) effectResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromHandBy(DefaultGame game, EffectResult effectResult, Filterable discardedBy, Filterable... discarded) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_HAND) {
            DiscardCardFromHandResult discardResult = (DiscardCardFromHandResult) effectResult;
            if (discardResult.getSource() != null 
                    && Filters.and(discardedBy).accepts(game, discardResult.getSource()))
                return Filters.and(discarded).accepts(game, discardResult.getDiscardedCard());
        }
        return false;
    }

    public static boolean revealedCardsFromTopOfDeck(EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_REVEALED_FROM_TOP_OF_DECK) {
            RevealCardFromTopOfDeckResult revealCardFromTopOfDeckResult = (RevealCardFromTopOfDeckResult) effectResult;
            return revealCardFromTopOfDeckResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean isDrawingACard(Effect effect, DefaultGame game, String playerId) {
        if (effect.getType() == EffectType.BEFORE_DRAW_CARD) {
            DrawOneCardEffect drawEffect = (DrawOneCardEffect) effect;
            return effect.getPerformingPlayer().equals(playerId) && drawEffect.canDrawCard(game);
        }
        return false;
    }

    public static boolean forEachReturnedToHand(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_RETURNED_TO_HAND) {
            ReturnCardsToHandResult result = (ReturnCardsToHandResult) effectResult;
            return Filters.and(filters).accepts(game, result.getReturnedCard());
        }
        return false;
    }

    public static boolean isGettingDiscardedBy(Effect effect, DefaultGame game, Filterable sourceFilter, Filterable... filters) {
        if (effect.getType() == EffectType.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && Filters.and(sourceFilter).accepts(game, effect.getSource()))
                return Filters.filter(preventableEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingDiscardedByOpponent(Effect effect, DefaultGame game, String playerId, Filterable... filters) {
        if (effect.getType() == EffectType.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && !effect.getPerformingPlayer().equals(playerId))
                return Filters.filter(preventableEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingDiscarded(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect.getType() == EffectType.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect discardEffect = (PreventableCardEffect) effect;
            return Filters.filter(discardEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean activated(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.ACTIVATE) {
            PhysicalCard source = ((ActivateCardResult) effectResult).getSource();
            return Filters.and(filters).accepts(game, source);
        }
        return false;
    }

    public static boolean played(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            PhysicalCard playedCard = ((PlayCardResult) effectResult).getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard);
        }
        return false;
    }

    public static boolean playedFromZone(DefaultGame game, EffectResult effectResult, Zone zone, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return (playResult.getPlayedFrom() == zone && Filters.and(filters).accepts(game, playedCard));
        }
        return false;
    }

    public static boolean playedFromStacked(DefaultGame game, EffectResult effectResult, Filterable stackedOnFilter, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return (playResult.getPlayedFrom() == Zone.STACKED
                    && Filters.and(stackedOnFilter).accepts(game, playResult.getAttachedOrStackedPlayedFrom())
                    && Filters.and(filters).accepts(game, playedCard));
        }
        return false;
    }

    public static boolean playedOn(DefaultGame game, EffectResult effectResult, Filterable targetFilter, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            final PhysicalCard attachedTo = playResult.getAttachedTo();
            if (attachedTo == null)
                return false;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard)
                    && Filters.and(targetFilter).accepts(game, attachedTo);
        }
        return false;
    }

    public static boolean movesFrom(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        return effectResult.getType() == EffectResult.Type.WHEN_MOVE_FROM
                && Filters.and(filters).accepts(game, ((WhenMoveFromResult) effectResult).getSite());
    }

    public static boolean moves(EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.WHEN_FELLOWSHIP_MOVES;
    }

    public static boolean transferredCard(DefaultGame game, EffectResult effectResult, Filterable transferredCard, Filterable transferredFrom, Filterable transferredTo) {
        if (effectResult.getType() == EffectResult.Type.CARD_TRANSFERRED) {
            CardTransferredResult transferResult = (CardTransferredResult) effectResult;
            return (Filters.and(transferredCard).accepts(game, transferResult.getTransferredCard())
                    && (transferredFrom == null || (transferResult.getTransferredFrom() != null && Filters.and(transferredFrom).accepts(game, transferResult.getTransferredFrom())))
                    && (transferredTo == null || (transferResult.getTransferredTo() != null && Filters.and(transferredTo).accepts(game, transferResult.getTransferredTo()))));
        }
        return false;
    }

}
