package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.TrueCondition;

public class PlayerCannotSolveMissionModifier extends AbstractModifier {

    @JsonProperty("playerName")
    private final String _playerName;
    @JsonProperty("locationId")
    private final int _locationId;

    @JsonCreator
    private PlayerCannotSolveMissionModifier(@JsonProperty("performingCard") PhysicalCard performingCard,
                                            @JsonProperty("affectedCards") CardFilter affectFilter,
                                             @JsonProperty("condition") Condition condition,
                                             @JsonProperty("effectType") ModifierEffect effectType,
                                             @JsonProperty("playerName") String playerName,
                                             @JsonProperty("locationId") int locationId) {
        super(performingCard, affectFilter, condition, effectType);
        _playerName = playerName;
        _locationId = locationId;
    }


    public PlayerCannotSolveMissionModifier(int locationId, String playerId) {
        this(null, Filters.any, new TrueCondition(), ModifierEffect.SOLVE_MISSION_MODIFIER, playerId, locationId);
    }


    public boolean cannotSolveMission(MissionLocation mission, String playerId) {
        return (_playerName.equals(playerId) && _locationId == mission.getLocationId());
    }


    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return "Player cannot solve mission";
    }
}