package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;

import java.util.List;
import java.util.Stack;

public class PersonnelCard extends AffiliatedCard implements CardWithCompatibility, ReportableCard {

    private AwayTeam _awayTeam;

    public PersonnelCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }

    public Integer getIntegrity(DefaultGame cardGame) {
        return getAttribute(CardAttribute.INTEGRITY, cardGame);
    }

    public Integer getCunning(DefaultGame cardGame) {
        return getAttribute(CardAttribute.CUNNING, cardGame);
    }

    public Integer getStrength(DefaultGame cardGame) {
        return getAttribute(CardAttribute.STRENGTH, cardGame);
    }


    public Integer getAttribute(CardAttribute attribute, DefaultGame cardGame) {
        return (int) cardGame.getGameState().getModifiersQuerying().getAttribute(this, attribute);
    }

    public Integer getSkillLevel(DefaultGame cardGame, SkillName skillName) {
        return cardGame.getGameState().getModifiersQuerying().getSkillLevel(this, skillName);
    }


    public void leaveAwayTeam(ST1EGame cardGame) {
        _awayTeam.remove(cardGame, this);
        _awayTeam = null;
    }

    @Override
    public boolean isInAnAwayTeam() {
        return _awayTeam != null;
    }

    public boolean isDisabled() {
        return false; }
    public boolean isInStasis() { // TODO - Eventually will need setter functions for these
        return false; }

    @Override
    public boolean hasSkill(SkillName skillName, DefaultGame cardGame) { return getSkillLevel(cardGame, skillName) >= 1; }

    public int getTotalAttributes(DefaultGame cardGame) {
        return getIntegrity(cardGame) + getCunning(cardGame) + getStrength(cardGame);
    }


    public boolean isFacingADilemma(DefaultGame cardGame) {
        boolean result = false;
        Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
        for (Action action : actionStack) {
            if (action instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard().getCardType() == CardType.DILEMMA &&
                    encounterAction.getAttemptingUnit().getAttemptingPersonnel(cardGame).contains(this)) {
                result = true;
            }
        }
        return result;
    }

    @JsonIgnore
    public int getSkillDotCount() {
        return _blueprint.getSkillDotCount();
    }

    public List<Skill> getSkills(DefaultGame cardGame) {
        return _blueprint.getSkills(cardGame, this);
    }


    @JsonIgnore
    public SkillName getClassification() { return _blueprint.getClassification(); }

    public void addToAwayTeam(AwayTeam awayTeam) {
        _awayTeam = awayTeam;
        _awayTeam.add(this);
    }

    @JsonIgnore
    @Override
    public AwayTeam getAwayTeam() {
        return _awayTeam;
    }

}