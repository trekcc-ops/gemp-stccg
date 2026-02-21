package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.choose.SelectNumberAction;
import com.gempukku.stccg.actions.choose.SelectPlayerAction;
import com.gempukku.stccg.actions.choose.SelectTribblePowerAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.evaluator.ConstantValueSource;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChooseStuffSubActionBlueprint implements SubActionBlueprint {

    private final EffectType _effectType;
    private final String _saveToMemoryId;
    private final String _choiceText;
    private final ValueSource _valueSource;
    private final PlayerSource _excludePlayerSource;

    private enum EffectType {
        CHOOSEANUMBER, CHOOSEOPPONENT, CHOOSEPLAYER, CHOOSEPLAYEREXCEPT, CHOOSETRIBBLEPOWER
    }

    ChooseStuffSubActionBlueprint(
            @JsonProperty(value = "type", required = true)
            EffectType effectType,
            @JsonProperty(value = "memorize", required = true)
            String memorize,
            @JsonProperty("amount")
            ValueSource amount,
            @JsonProperty("text")
            String text,
            @JsonProperty("exclude")
            String excludedPlayer
    ) throws InvalidCardDefinitionException {
        _effectType = effectType;
        _saveToMemoryId = memorize;
        _choiceText = Objects.requireNonNullElse(text, getDefaultText(effectType));
        _valueSource = Objects.requireNonNullElse(amount, new ConstantValueSource(0));
        _excludePlayerSource = PlayerResolver.resolvePlayer(Objects.requireNonNullElse(excludedPlayer, "you"));
    }

    private static String getDefaultText(EffectType effectType) {
        return switch (effectType) {
            case CHOOSEANUMBER -> "Choose a number";
            case CHOOSEOPPONENT, CHOOSEPLAYER, CHOOSEPLAYEREXCEPT -> "Choose a player";
            case CHOOSETRIBBLEPOWER -> "Choose a tribble power";
        };

    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction, GameTextContext context)
            throws PlayerNotFoundException, InvalidGameLogicException {
        Action action = switch (_effectType) {
            case CHOOSEANUMBER -> new SelectNumberAction(cardGame, context, _choiceText, _valueSource, _saveToMemoryId);
            case CHOOSEOPPONENT -> {
                List<String> playerIds = Arrays.asList(cardGame.getAllPlayerIds());
                playerIds.remove(context.yourName());
                yield new SelectPlayerAction(cardGame, context, _saveToMemoryId, playerIds);
            }
            case CHOOSEPLAYER ->
                    new SelectPlayerAction(cardGame, context, _saveToMemoryId, Arrays.asList(cardGame.getAllPlayerIds()));
            case CHOOSEPLAYEREXCEPT -> {
                List<String> playerIds = Arrays.asList(cardGame.getAllPlayerIds());
                playerIds.removeIf(playerName -> _excludePlayerSource.isPlayer(playerName, cardGame, context));
                yield new SelectPlayerAction(cardGame, context, _saveToMemoryId, playerIds);
            }
            case CHOOSETRIBBLEPOWER -> new SelectTribblePowerAction(cardGame, context, _saveToMemoryId);
        };
        return List.of(action);
    }


}