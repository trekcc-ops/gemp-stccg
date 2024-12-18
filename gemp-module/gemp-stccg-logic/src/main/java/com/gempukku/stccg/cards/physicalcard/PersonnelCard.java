package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.SnapshotData;

import java.util.List;
import java.util.Stack;

public class PersonnelCard extends PhysicalReportableCard1E implements AffiliatedCard {

    public PersonnelCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }

    public Integer getAttribute(CardAttribute attribute) {
        return _game.getModifiersQuerying().getAttribute(this, attribute);
    }

    public Integer getSkillLevel(SkillName skillName) {
        return _game.getModifiersQuerying().getSkillLevel(this, skillName);
    }

    public boolean isDisabled() {
        return false; }
    public boolean isInStasis() { // TODO - Eventually will need setter functions for these
        return false; }

    @Override
    public boolean hasSkill(SkillName skillName) { return getSkillLevel(skillName) >= 1; }

    @Override
    public ST1EPhysicalCard generateSnapshot(SnapshotData snapshotData) {

        // TODO - A lot of repetition here between the various PhysicalCard classes

        PersonnelCard newCard = new PersonnelCard(_game, _cardId, snapshotData.getDataForSnapshot(_owner), _blueprint);
        newCard.setZone(_zone);
        newCard.attachTo(snapshotData.getDataForSnapshot(_attachedTo));
        newCard.stackOn(snapshotData.getDataForSnapshot(_stackedOn));
        newCard._currentLocation = snapshotData.getDataForSnapshot(_currentLocation);

        newCard._currentAffiliation = _currentAffiliation;

        return newCard;

    }


    public int getTotalAttributes() {
        return getAttribute(CardAttribute.INTEGRITY) + getAttribute(CardAttribute.CUNNING) +
                getAttribute(CardAttribute.STRENGTH);
    }

    public boolean isFacingADilemma() {
        boolean result = false;
        Stack<Action> actionStack = _game.getActionsEnvironment().getActionStack();
        for (Action action : actionStack) {
            if (action instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard().getCardType() == CardType.DILEMMA &&
                    encounterAction.getAttemptingUnit().getAttemptingPersonnel().contains(this)) {
                result = true;
            }
        }
        return result;
    }

    public List<Skill> getSkills() {
        return _blueprint.getSkills(_game, this);
    }
}