package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;
import java.util.Stack;

@JsonIgnoreProperties(value = { "cardType", "hasUniversalIcon", "imageUrl", "isInPlay", "title", "uniqueness" },
        allowGetters = true)
public class PersonnelCard extends AffiliatedCard implements CardWithCompatibility, ReportableCard, CardWithStrength {

    @JsonCreator
    public PersonnelCard(
            @JsonProperty("cardId")
            int cardId,
            @JsonProperty("owner")
            String ownerName,
            @JsonProperty("blueprintId")
            String blueprintId,
            @JacksonInject
            CardBlueprintLibrary blueprintLibrary) throws CardNotFoundException {
        super(cardId, ownerName, blueprintLibrary.getCardBlueprint(blueprintId));
    }

    public PersonnelCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
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
        return (int) cardGame.getAttribute(this, attribute);
    }

    public Integer getSkillLevel(DefaultGame cardGame, SkillName skillName) {
        return cardGame.getSkillLevel(this, skillName);
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

    public float getPrintedStrength() {
        return _blueprint.getStrength();
    }

    public int getPrintedCunning() {
        return _blueprint.getCunning();
    }

    public int getPrintedIntegrity() {
        return _blueprint.getIntegrity();
    }


}