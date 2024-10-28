package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.LinkedList;
import java.util.List;

public class PersonnelCard extends PhysicalReportableCard1E implements AffiliatedCard {

    private final List<Skill> _skills;

    public PersonnelCard(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
        _skills = new LinkedList<>(blueprint.getSkills());
    }

    public Integer getAttribute(CardAttribute attribute) {
        return _game.getModifiersQuerying().getAttribute(this, attribute);
    }

    public Integer getSkillLevel(SkillName skillName) {
        return _game.getModifiersQuerying().getSkillLevel(this, skillName);
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

    public void addSkill(SkillName skill) { _skills.add(new RegularSkill(skill)); }

}