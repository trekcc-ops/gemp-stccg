package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.EffectAppender;
import com.gempukku.lotro.effectappender.EffectAppenderProducer;
import com.gempukku.lotro.effectappender.DelayedAppender;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.processes.lotronly.assign.Assignment;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CancelAllAssignments implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        List<Assignment> assignments = new LinkedList<>(game.getGameState().getAssignments());
                        for (Assignment assignment : assignments)
                            game.getGameState().removeAssignment(assignment);

                        game.getGameState().finishSkirmish();
                    }
                };
            }
        };
    }
}
