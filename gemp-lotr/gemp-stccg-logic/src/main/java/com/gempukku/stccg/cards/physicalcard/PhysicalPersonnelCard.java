package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ChangeAffiliationAction;
import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.*;

public class PhysicalPersonnelCard extends PhysicalReportableCard1E {

        // TODO - Eventually will need setter functions for these
    private boolean _inStasis = false;
    private boolean _stopped = false;
    private boolean _disabled = false;
    public PhysicalPersonnelCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }

    public Integer getAttribute(CardAttribute attribute) {
        return _blueprint.getAttribute(attribute);
    }

    public Integer getSkillLevel(RegularSkill skill) {
        int level = 0;
        for (Skill blueprintSkill : _blueprint.getSkills()) {
            if (blueprintSkill.getRegularSkill() == skill) {
                level += blueprintSkill.getLevel();
            }
        }
        if (_blueprint.getClassification() == skill)
            level += 1;
        return level;
    }

    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        List<Action> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            // TODO - Implement land, take off, cloak
            if (isControlledBy(player.getPlayerId())) {
                if (_affiliationOptions.size() > 1) {
                    // TODO - This action should be available to other card types as well. Can be performed at any time.
                    actions.add(new ChangeAffiliationAction(player, this));
                }
            }
        }
        actions.removeIf(action -> !action.canBeInitiated());
        return actions;
    }

    @Override
    public String getTypeSpecificCardInfoHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<br><b>Affiliation:</b> ");
        for (Affiliation affiliation : Affiliation.values())
            if (isAffiliation(affiliation))
                sb.append(affiliation.toHTML());

        sb.append("<br><b>Icons:</b> ");
        for (Icon1E icon : Icon1E.values())
            if (hasIcon(icon))
                sb.append(icon.toHTML());

        return sb.toString();
    }

    public boolean isStopped() { return _stopped; }
    public boolean isDisabled() { return _disabled; }
    public boolean isInStasis() { return _inStasis; }
}