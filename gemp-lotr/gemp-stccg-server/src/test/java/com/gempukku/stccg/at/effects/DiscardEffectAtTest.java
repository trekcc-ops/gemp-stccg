package com.gempukku.stccg.at.effects;

import com.gempukku.stccg.at.AbstractAtTest;
import com.gempukku.stccg.requirement.trigger.TriggerConditions;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalCardGeneric;
import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.effects.defaulteffect.DiscardCardsFromPlayEffect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiscardEffectAtTest extends AbstractAtTest {
    @Test
    public void attachedCardGetsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardGeneric merry = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_303"));
        final PhysicalCardGeneric hobbitSword = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_299"));

        _game.getGameState().addCardToZone(merry, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(hobbitSword, merry);

        final Set<PhysicalCard> discardedFromPlay = new HashSet<>();

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
                        if (TriggerConditions.forEachDiscardedFromPlay(_game, effectResult, Filters.any)) {
                            DiscardCardsFromPlayResult discardResult = (DiscardCardsFromPlayResult) effectResult;
                            discardedFromPlay.add(discardResult.getDiscardedCard());
                            return null;
                        }
                        return null;
                    }
                });

        DiscardCardsFromPlayEffect discardEffect = new DiscardCardsFromPlayEffect(_game, merry.getOwnerName(), merry, merry);

        carryOutEffectInPhaseActionByPlayer(P1, discardEffect);

        assertTrue(discardEffect.wasCarriedOut());

        assertEquals(2, discardedFromPlay.size());
        assertTrue(discardedFromPlay.contains(merry));
        assertTrue(discardedFromPlay.contains(hobbitSword));

        assertEquals(Zone.DISCARD, merry.getZone());
        assertEquals(Zone.DISCARD, hobbitSword.getZone());
    }

    @Test
    public void attachedCardToAttachedCardGetsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardGeneric merry = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_303"));
        final PhysicalCardGeneric alatar = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("13_28"));
        final PhysicalCardGeneric whisperInTheDark = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("18_77"));

        _game.getGameState().addCardToZone(merry, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(alatar, merry);
        _game.getGameState().attachCard(whisperInTheDark, alatar);

        final Set<PhysicalCard> discardedFromPlay = new HashSet<>();

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
                        if (TriggerConditions.forEachDiscardedFromPlay(_game, effectResult, Filters.any)) {
                            DiscardCardsFromPlayResult discardResult = (DiscardCardsFromPlayResult) effectResult;
                            discardedFromPlay.add(discardResult.getDiscardedCard());
                            return null;
                        }
                        return null;
                    }
                });

        DiscardCardsFromPlayEffect discardEffect = new DiscardCardsFromPlayEffect(_game, merry.getOwnerName(), merry, merry);

        carryOutEffectInPhaseActionByPlayer(P1, discardEffect);

        assertTrue(discardEffect.wasCarriedOut());

        assertEquals(3, discardedFromPlay.size());
        assertTrue(discardedFromPlay.contains(merry));
        assertTrue(discardedFromPlay.contains(alatar));
        assertTrue(discardedFromPlay.contains(whisperInTheDark));

        assertEquals(Zone.DISCARD, merry.getZone());
        assertEquals(Zone.DISCARD, alatar.getZone());
        assertEquals(Zone.DISCARD, whisperInTheDark.getZone());
    }

    @Test
    public void stackedCardGetsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardGeneric merry = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_303"));
        final PhysicalCardGeneric hobbitSword = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_299"));

        _game.getGameState().addCardToZone(merry, Zone.FREE_CHARACTERS);
        _game.getGameState().stackCard(hobbitSword, merry);

        final Set<PhysicalCard> discardedFromPlay = new HashSet<>();

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
                        if (TriggerConditions.forEachDiscardedFromPlay(_game, effectResult, Filters.any)) {
                            DiscardCardsFromPlayResult discardResult = (DiscardCardsFromPlayResult) effectResult;
                            discardedFromPlay.add(discardResult.getDiscardedCard());
                            return null;
                        }
                        return null;
                    }
                });

        DiscardCardsFromPlayEffect discardEffect = new DiscardCardsFromPlayEffect(_game, merry.getOwnerName(), merry, merry);

        carryOutEffectInPhaseActionByPlayer(P1, discardEffect);

        assertTrue(discardEffect.wasCarriedOut());

        assertEquals(1, discardedFromPlay.size());
        assertTrue(discardedFromPlay.contains(merry));

        assertEquals(Zone.DISCARD, merry.getZone());
        assertEquals(Zone.DISCARD, hobbitSword.getZone());
    }

}
