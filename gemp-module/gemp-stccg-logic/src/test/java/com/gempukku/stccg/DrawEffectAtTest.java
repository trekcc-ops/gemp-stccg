package com.gempukku.stccg;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerConditions;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.Preventable;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("MultipleExceptionsDeclaredOnTestMethod")
public class DrawEffectAtTest extends AbstractAtTest {

    @Test
    public void drawOneSuccess() throws DecisionResultInvalidException, CardNotFoundException {
        drawEffectTest(1,30, false);
    }

    @Test
    public void drawTwoSuccess() throws DecisionResultInvalidException, CardNotFoundException {
        drawEffectTest(2,30, false);
    }

    @Test
    public void drawOnePrevented() throws DecisionResultInvalidException, CardNotFoundException {
        drawEffectTest(1,30, true);
    }

    @Test
    public void drawElevenFailure() throws DecisionResultInvalidException, CardNotFoundException {
        drawEffectTest(11,9, false);
    }


    public void drawEffectTest(int cardsToDraw, int cardsInDeck, boolean prevented)
            throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimple1EGame(cardsInDeck);
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) skipDilemma();
        String playerId = _game.getCurrentPlayerId();
        int initialDeckSize = _game.getGameState().getDrawDeck(playerId).size();
        int initialHandSize = _game.getGameState().getHand(playerId).size();
        int expectedCardsDrawn = prevented ? 0 : Math.min(cardsToDraw, initialDeckSize+1);
        assertEquals(cardsInDeck,initialDeckSize + initialHandSize);

        final PhysicalCardGeneric picard =
                new PhysicalCardGeneric(_game, 101, playerId, _cardLibrary.getCardBlueprint("101_215"));

        _game.getGameState().putCardOnTopOfDeck(picard);

        final AtomicInteger triggerCount = new AtomicInteger(0);
        final AtomicInteger preventCount = new AtomicInteger(0);

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
                        if (TriggerConditions.forEachCardDrawn(effectResult, playerId)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(picard);
                            action.appendEffect(
                                    new IncrementEffectForTesting(_game, triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }

                    @Override
                    public List<? extends Action> getRequiredBeforeTriggers(Effect effect) {
                        if (prevented && TriggerConditions.isDrawingACard(effect, _game, playerId)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(picard);
                            action.appendEffect(
                                    new PreventEffect(_game, (Preventable) effect));
                            action.appendEffect(
                                    new IncrementEffectForTesting(_game, preventCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        DrawCardsEffect drawEffect = new DrawCardsEffect(_game, new SystemQueueAction(_game), playerId, cardsToDraw);

        carryOutEffectInPhaseActionByPlayer(playerId, drawEffect);

        assertEquals(initialHandSize + expectedCardsDrawn, _game.getGameState().getHand(playerId).size());
        assertEquals(initialDeckSize + 1 - expectedCardsDrawn, _game.getGameState().getDrawDeck(playerId).size());
        assertEquals(!prevented, _game.getGameState().getHand(playerId).contains(picard));
        assertEquals((cardsToDraw <= initialDeckSize + 1) && !prevented, drawEffect.wasCarriedOut());
        assertEquals(expectedCardsDrawn, triggerCount.get());
        assertEquals(prevented ? cardsToDraw : 0, preventCount.get());
    }
}