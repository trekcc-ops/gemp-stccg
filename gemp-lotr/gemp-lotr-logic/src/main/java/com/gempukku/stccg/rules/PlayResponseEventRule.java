package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.PlayEventAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.common.filterable.Side;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.EffectResult;
import com.gempukku.stccg.rules.lotronly.LotroGameUtils;
import com.gempukku.stccg.rules.lotronly.LotroPlayUtils;

import java.util.LinkedList;
import java.util.List;

public class PlayResponseEventRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public PlayResponseEventRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getOptionalAfterActions(String playerId, DefaultGame game, EffectResult effectResult) {
                        List<Action> result = new LinkedList<>();
                        final Side side = LotroGameUtils.getSide(game, playerId);
                        for (PhysicalCard responseEvent : Filters.filter(game.getGameState().getHand(playerId), game, side, CardType.EVENT, Keyword.RESPONSE)) {
                            if (LotroPlayUtils.checkPlayRequirements(game, responseEvent, Filters.any, 0, 0, false, false, false)) {
                                final List<PlayEventAction> actions = responseEvent.getBlueprint().getPlayResponseEventAfterActions(playerId, game, effectResult, responseEvent);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }
                        return result;
                    }

                    @Override
                    public List<? extends Action> getOptionalBeforeActions(String playerId, DefaultGame game, Effect effect) {
                        List<Action> result = new LinkedList<>();
                        final Side side = LotroGameUtils.getSide(game, playerId);
                        for (PhysicalCard responseEvent : Filters.filter(game.getGameState().getHand(playerId), game, side, CardType.EVENT, Keyword.RESPONSE)) {
                            if (LotroPlayUtils.checkPlayRequirements(game, responseEvent, Filters.any, 0, 0, false, false, false)) {
                                final List<PlayEventAction> actions = responseEvent.getBlueprint().getPlayResponseEventBeforeActions(playerId, game, effect, responseEvent);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }
                        return result;
                    }
                }
        );
    }
}
