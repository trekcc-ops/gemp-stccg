package com.gempukku.stccg.rules.lotronly;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.choose.ChooseActiveCardEffect;
import com.gempukku.stccg.effects.TransferPermanentEffect;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.effects.EffectResult;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.game.PlayConditions;
import com.gempukku.stccg.requirement.trigger.TriggerConditions;

import java.util.LinkedList;
import java.util.List;

public class FollowerRule {
    private final DefaultActionsEnvironment defaultActionsEnvironment;

    public FollowerRule(DefaultActionsEnvironment defaultActionsEnvironment) {
        this.defaultActionsEnvironment = defaultActionsEnvironment;
    }

    public void applyRule() {
        defaultActionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, DefaultGame game, EffectResult effectResult) {
                        if (TriggerConditions.startOfPhase(game, effectResult, Phase.MANEUVER)) {
                            final Filter followerTarget = Filters.and(Filters.owner(playerId), Filters.or(CardType.COMPANION, CardType.MINION));

                            List<OptionalTriggerAction> optionalTriggerActions = new LinkedList<>();
                            for (final PhysicalCard follower : Filters.filterActive(game, CardType.FOLLOWER, Filters.owner(playerId))) {
                                if (follower.getBlueprint().canPayAidCost(game, follower)
                                        && PlayConditions.isActive(game, followerTarget)) {
                                    final OptionalTriggerAction action = new OptionalTriggerAction(follower);
                                    action.setText("Use " + GameUtils.getCardLink(follower) + " Aid");
                                    follower.getBlueprint().appendAidCosts(game, action, follower);
                                    action.appendCost(
                                            new ChooseActiveCardEffect(follower, playerId, "Choose character to transfer follower to", followerTarget, Filters.not(Filters.hasAttached(follower))) {
                                                @Override
                                                protected void cardSelected(DefaultGame game, PhysicalCard card) {
                                                    action.appendEffect(
                                                            new TransferPermanentEffect(follower, card));
                                                }
                                            });
                                    optionalTriggerActions.add(action);
                                }
                            }
                            return optionalTriggerActions;
                        }
                        return null;
                    }
                });
    }
}
