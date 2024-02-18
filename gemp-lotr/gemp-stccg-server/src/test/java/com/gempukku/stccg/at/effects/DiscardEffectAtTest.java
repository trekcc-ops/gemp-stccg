package com.gempukku.stccg.at.effects;

import com.gempukku.stccg.at.AbstractAtTest;
import com.gempukku.stccg.requirement.trigger.TriggerConditions;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalCardImpl;
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

        final PhysicalCardImpl merry = new PhysicalCardImpl(_game, 101, "1_303", P1, _cardLibrary.getCardBlueprint("1_303"));
        final PhysicalCardImpl hobbitSword = new PhysicalCardImpl(_game, 101, "1_299", P1, _cardLibrary.getCardBlueprint("1_299"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(_game, hobbitSword, merry);

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

        final PhysicalCardImpl merry = new PhysicalCardImpl(_game, 101, "1_303", P1, _cardLibrary.getCardBlueprint("1_303"));
        final PhysicalCardImpl alatar = new PhysicalCardImpl(_game, 101, "13_28", P1, _cardLibrary.getCardBlueprint("13_28"));
        final PhysicalCardImpl whisperInTheDark = new PhysicalCardImpl(_game, 101, "18_77", P1, _cardLibrary.getCardBlueprint("18_77"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(_game, alatar, merry);
        _game.getGameState().attachCard(_game, whisperInTheDark, alatar);

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

        final PhysicalCardImpl merry = new PhysicalCardImpl(_game, 101, "1_303", P1, _cardLibrary.getCardBlueprint("1_303"));
        final PhysicalCardImpl hobbitSword = new PhysicalCardImpl(_game, 101, "1_299", P1, _cardLibrary.getCardBlueprint("1_299"));

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().stackCard(_game, hobbitSword, merry);

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
