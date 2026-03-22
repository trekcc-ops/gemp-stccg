package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.TrueCondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AddAffiliationIconToMissionModifier extends AbstractModifier {

    @JsonProperty("affiliationsToAdd")
    private final List<Affiliation> _affiliationsToAdd;
    @JsonProperty("addToBothSides")
    private final boolean _addToBothSides;

    public AddAffiliationIconToMissionModifier(GameTextContext actionContext, CardFilter affectedCardsFilter,
                                               List<Affiliation> affiliationsToAdd, boolean addToBothSides) {
        super(actionContext.card(), affectedCardsFilter, new TrueCondition(), ModifierEffect.ADD_ICON_TO_MISSION, false);
        _affiliationsToAdd = affiliationsToAdd;
        _addToBothSides = addToBothSides;
    }

    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return null;
    }

    public boolean addsAffiliationToMissionForPlayer(Affiliation affiliation, MissionCard mission, DefaultGame cardGame,
                                                     String playerName) {
        return _affectedCardsFilter.accepts(cardGame, mission) && _affiliationsToAdd.contains(affiliation) &&
                (_addToBothSides || _cardSource.isControlledBy(playerName));
    }

    public Collection<Affiliation> getAffiliationsAddedForPlayer(MissionCard missionCard, DefaultGame game, String playerName) {
        List<Affiliation> result = new ArrayList<>();
        if (_affectedCardsFilter.accepts(game, missionCard) && (_addToBothSides || _cardSource.isControlledBy(playerName))) {
            result.addAll(_affiliationsToAdd);
        }
        return result;
    }
}