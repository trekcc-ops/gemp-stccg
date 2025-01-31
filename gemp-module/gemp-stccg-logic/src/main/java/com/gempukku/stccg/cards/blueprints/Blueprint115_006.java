package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint115_006 extends CardBlueprint {
    Blueprint115_006() {
        super("115_006"); // Chula: The Chandra

    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {

        List<Action> result = new LinkedList<>();
        PersonnelCard randomCard = TextUtils.getRandomItemFromList(attemptingUnit.getAttemptingPersonnel());
        Collection<PhysicalCard> matchingCards = Filters.filter(attemptingUnit.getAttemptingPersonnel(),
                Filters.hasAttributeMatchingPersonnel(randomCard));

        Collection<PersonnelCard> cardsToStop = new LinkedList<>();
        for (PersonnelCard card : attemptingUnit.getAttemptingPersonnel()) {
            if (!matchingCards.contains(card)) {
                cardsToStop.add(card);
            }
        }

        result.add(new StopCardsAction(game, thisCard.getOwner(), cardsToStop));
        result.add(new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), thisCard));
        return result;
    }

}