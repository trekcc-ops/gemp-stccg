package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.FailDilemmaAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_054 extends CardBlueprint {

    // Wind Dancer
    Blueprint101_054() {
        super("101_054");
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {

        List<Action> result = new LinkedList<>();
        Collection<PersonnelCard> attemptingPersonnel = attemptingUnit.getAttemptingPersonnel(game);

        boolean meetsConditions = false;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.getTitle().equals("Lwaxana Troi") || personnel.hasSkill(SkillName.YOUTH, game) ||
                    personnel.hasSkill(SkillName.MUSIC, game) || personnel.getStrength(game) > 9) {
                meetsConditions = true;
            }
        }

        if (meetsConditions) {
            result.add(new FailDilemmaAction(game, attemptingUnit, thisCard, action));
        } else {
            result.add(new RemoveDilemmaFromGameAction(game, attemptingUnit.getControllerName(), thisCard));
        }

        return result;
    }

}