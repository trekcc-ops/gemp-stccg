package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

public class PersonnelCard extends PhysicalReportableCard1E implements AffiliatedCard {

    public PersonnelCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
    }

    public Integer getAttribute(CardAttribute attribute) {
        if (attribute == CardAttribute.STRENGTH)
            return _game.getModifiersQuerying().getStrength(this);
            // TODO - Does not apply modifiers for any attribute other than strength
        else
            return _blueprint.getAttribute(attribute);
    }

    public Integer getSkillLevel(SkillName skill) {
        int level = 0;
        for (RegularSkill blueprintRegularSkill : _blueprint.getRegularSkills()) {
            if (blueprintRegularSkill.getRegularSkill() == skill) {
                level += blueprintRegularSkill.getLevel();
            }
        }
        if (_blueprint.getClassification() == skill)
            level += 1;
        return level;
    }

    @Override
    public String getCardInfoHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getCardInfoHTML());
        sb.append("<br><b>Affiliation:</b> ");
        for (Affiliation affiliation : Affiliation.values())
            if (isAffiliation(affiliation))
                sb.append(affiliation.toHTML());

        sb.append("<br><b>Icons:</b> ");
        for (CardIcon icon : CardIcon.values())
            if (hasIcon(icon))
                sb.append(icon.toHTML());

        return sb.toString();
    }
    public boolean isStopped() {
        return false; }
    public boolean isDisabled() {
        return false; }
    public boolean isInStasis() { // TODO - Eventually will need setter functions for these
        return false; }

    @Override
    public boolean hasSkill(SkillName skillName) { return getSkillLevel(skillName) >= 1; }
}