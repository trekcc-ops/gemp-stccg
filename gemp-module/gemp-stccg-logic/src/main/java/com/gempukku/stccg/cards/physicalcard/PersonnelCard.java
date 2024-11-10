package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.cards.RegularSkill;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.SnapshotData;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public boolean isStopped() {
        return false; }
    public boolean isDisabled() {
        return false; }
    public boolean isInStasis() { // TODO - Eventually will need setter functions for these
        return false; }

    @Override
    public boolean hasSkill(SkillName skillName) { return getSkillLevel(skillName) >= 1; }

    public void addSkill(SkillName skill) { _skills.add(new RegularSkill(skill)); }

    @Override
    public ST1EPhysicalCard generateSnapshot(SnapshotData snapshotData) {

        // TODO - A lot of repetition here between the various PhysicalCard classes

        PersonnelCard newCard = new PersonnelCard(_game, _cardId, snapshotData.getDataForSnapshot(_owner), _blueprint);
        newCard.setZone(_zone);
        newCard.attachTo(snapshotData.getDataForSnapshot(_attachedTo));
        newCard.stackOn(snapshotData.getDataForSnapshot(_stackedOn));
        newCard._currentLocation = snapshotData.getDataForSnapshot(_currentLocation);

        for (PhysicalCard card : _cardsSeededUnderneath)
            newCard.addCardToSeededUnder(snapshotData.getDataForSnapshot(card));

        for (Map.Entry<Player, List<PhysicalCard>> entry : _cardsPreSeededUnderneath.entrySet())
            for (PhysicalCard card : entry.getValue())
                newCard.addCardToPreSeeds(snapshotData.getDataForSnapshot(card), entry.getKey());

        newCard._currentAffiliation = _currentAffiliation;
        newCard._skills.clear();
        newCard._skills.addAll(_skills);

        return newCard;

    }


}