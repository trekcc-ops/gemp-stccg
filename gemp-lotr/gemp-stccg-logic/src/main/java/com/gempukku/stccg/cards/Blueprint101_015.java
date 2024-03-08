package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.PropertyLogo;

public class Blueprint101_015 extends CardBlueprint {
        // TODO - Very much not complete
    Blueprint101_015() {
        super("101_015");
        setTitle("Armus - Skin of Evil");
        setCardType(CardType.DILEMMA);
        setMissionType(MissionType.PLANET);
        setPropertyLogo(PropertyLogo.TNG_LOGO);
        setLore("A malevolent being was formed when the inhabitants of Vagra II rid themselves of all the evil they had inside.");
//        setGameText("Kills one Away Team member (random selection). Discard dilemma.");
        setImageUrl("https://www.trekcc.org/1e/cardimages/errata/Armus-Skin-of-Evil.jpg");
    }

/*    public void encounter(EncounterSeedCardAction action, DilemmaCard self) {
        PersonnelCard personnel =
                TextUtils.getRandomFromList(action.getAttemptingUnit().getAllPersonnel().stream().toList());
        personnel.kill();
        self.discard();
    } */
}