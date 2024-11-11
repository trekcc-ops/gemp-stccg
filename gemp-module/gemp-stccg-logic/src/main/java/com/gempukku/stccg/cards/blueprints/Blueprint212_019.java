package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.PassThroughEffectAction;
import com.gempukku.stccg.actions.playcard.ChooseAndPlayCardFromZoneEffect;
import com.gempukku.stccg.cards.blueprints.actionsource.SeedCardActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;

import java.util.ArrayList;
import java.util.List;

public class Blueprint212_019 extends CardBlueprint {
    Blueprint212_019() {
        super("212_019"); // Risk is Our Business
    }

    // TODO - None of this is correct for the actual card's gametext. Using it as a test case for response actions

    @Override
    public SeedCardActionSource getSeedCardActionSource() {
        SeedCardActionSource actionSource = new SeedCardActionSource();
        actionSource.setSeedZone(Zone.TABLE);
        return actionSource;
    }

    public List<Action> getValidResponses(PhysicalCard card, Player player, EffectResult effectResult) {
        List<Action> actions = new ArrayList<>();
        if (effectResult.getType() == EffectResult.Type.START_OF_MISSION_ATTEMPT) {
            Effect chooseCardEffect = new ChooseAndPlayCardFromZoneEffect(Zone.DRAW_DECK, player, CardType.PERSONNEL);
            Action action = new PassThroughEffectAction(card, chooseCardEffect);
            actions.add(action);
        }
        return actions;
    }

}