package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Icon1E;
import com.gempukku.stccg.common.filterable.RegularSkill;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

public class PersonnelCard extends PhysicalReportableCard1E implements AffiliatedCard {

        // TODO - Eventually will need setter functions for these
    private boolean _inStasis = false;
    private boolean _stopped = false;
    private boolean _disabled = false;
    public PersonnelCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
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