package com.gempukku.stccg.cards.blueprints.trigger;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.PreventableCardEffect;
import com.gempukku.stccg.actions.discard.DiscardCardFromDeckResult;
import com.gempukku.stccg.actions.discard.DiscardCardFromHandResult;
import com.gempukku.stccg.actions.discard.DiscardCardFromPlayResult;
import com.gempukku.stccg.actions.discard.DiscardCardsFromPlayEffect;
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

    public static boolean startOfPhase(DefaultGame game, EffectResult effectResult, Phase phase) {
        return (effectResult.getType() == EffectResult.Type.START_OF_PHASE
                && game.getGameState().getCurrentPhase() == phase);
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

    public static boolean forEachCardDrawn(EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
            DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
            return drawResult.isDraw() && drawResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachDiscardedFromPlay(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY)
            return Filters.and(filters).accepts(game, ((DiscardCardFromPlayResult) effectResult).getDiscardedCard());
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

    public static boolean forEachReturnedToHand(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_RETURNED_TO_HAND) {
            ReturnCardsToHandResult result = (ReturnCardsToHandResult) effectResult;
            return Filters.and(filters).accepts(game, result.getReturnedCard());
        }
        return false;
    }

    public static boolean isGettingDiscardedBy(Effect effect, DefaultGame game, Filterable sourceFilter, Filterable... filters) {
        if (effect instanceof DiscardCardsFromPlayEffect) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && Filters.and(sourceFilter).accepts(game, effect.getSource()))
                return !Filters.filter(preventableEffect.getAffectedCardsMinusPrevented(), game, filters).isEmpty();
        }
        return false;
    }

    public static boolean isGettingDiscardedByOpponent(Effect effect, DefaultGame game, String playerId, Filterable... filters) {
        if (effect instanceof DiscardCardsFromPlayEffect) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && !effect.getPerformingPlayerId().equals(playerId))
                return !Filters.filter(preventableEffect.getAffectedCardsMinusPrevented(), game, filters).isEmpty();
        }
        return false;
    }

    public static boolean isGettingDiscarded(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect instanceof DiscardCardsFromPlayEffect) {
            PreventableCardEffect discardEffect = (PreventableCardEffect) effect;
            return !Filters.filter(discardEffect.getAffectedCardsMinusPrevented(), game, filters).isEmpty();
        }
        return false;
    }

    public static boolean played(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY_CARD) {
            PhysicalCard playedCard = ((PlayCardResult) effectResult).getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard);
        }
        return false;
    }

    public static boolean played(Player player, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY_CARD) {
            if (effectResult.getPerformingPlayerId().equals(player.getPlayerId())) {
                PhysicalCard playedCard = ((PlayCardResult) effectResult).getPlayedCard();
                return Filters.and(filters).accepts(player.getGame(), playedCard);
            }
        }
        return false;
    }


    public static boolean playedFromZone(DefaultGame game, EffectResult effectResult, Zone zone, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY_CARD) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return (playResult.getPlayedFrom() == zone && Filters.and(filters).accepts(game, playedCard));
        }
        return false;
    }

    public static boolean playedOn(DefaultGame game, EffectResult effectResult,
                                   Filterable targetFilter, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY_CARD) {
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


}