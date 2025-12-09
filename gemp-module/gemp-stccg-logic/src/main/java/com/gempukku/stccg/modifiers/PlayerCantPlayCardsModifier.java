package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Condition;

import java.util.Objects;

public class PlayerCantPlayCardsModifier extends AbstractModifier {

    @JsonProperty("playerName")
    private final String _playerName;

    @JsonCreator
    private PlayerCantPlayCardsModifier(@JsonProperty("performingCard") PhysicalCard performingCard,
                                            @JsonProperty("affectedCards") CardFilter affectFilter,
                                             @JsonProperty("condition") Condition condition,
                                             @JsonProperty("effectType") ModifierEffect effectType,
                                             @JsonProperty("playerName") String playerName) {
        super(performingCard, affectFilter, condition, effectType);
        _playerName = playerName;
    }

    public PlayerCantPlayCardsModifier(PhysicalCard source, Condition condition, String playerId) {
        this(source, Filters.any, condition, ModifierEffect.ACTION_MODIFIER, playerId);
    }

    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return "Player cannot play cards";
    }

    @Override
    public boolean canPerformAction(DefaultGame game, String performingPlayer, Action action) {
        return action.getActionType() != ActionType.PLAY_CARD
                || !Objects.equals(_playerName, performingPlayer);
    }
}