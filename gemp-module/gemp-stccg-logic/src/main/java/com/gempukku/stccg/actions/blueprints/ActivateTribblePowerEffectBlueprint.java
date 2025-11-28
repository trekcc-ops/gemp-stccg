package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.tribblepower.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;

public class ActivateTribblePowerEffectBlueprint implements SubActionBlueprint {

    @Override
    public List<Action> createActions(DefaultGame cardGame, CardPerformedAction action, ActionContext context)
            throws InvalidCardDefinitionException, InvalidGameLogicException, PlayerNotFoundException {

        List<Action> result = new LinkedList<>();
        TribblePower tribblePower = context.getSource().getBlueprint().getTribblePower();
        if (context instanceof TribblesActionContext tribblesContext && cardGame instanceof TribblesGame tribblesGame) {

            Action activateAction = switch (tribblePower) {
                case AVALANCHE -> new ActivateAvalancheTribblePowerAction(tribblesGame, tribblesContext);
                case CONVERT -> new ActivateConvertTribblePowerAction(tribblesGame, tribblesContext);
                case CYCLE -> new ActivateCycleTribblePowerAction(tribblesGame, tribblesContext);
                case DISCARD -> new ActivateDiscardTribblePowerAction(tribblesGame, tribblesContext);
                case DRAW -> new ActivateDrawTribblePowerAction(tribblesGame, tribblesContext);
                case EVOLVE -> new ActivateEvolveTribblePowerAction(tribblesGame, tribblesContext);
                case FAMINE -> new ActivateFamineTribblePowerAction(tribblesGame, tribblesContext);
                case GENEROSITY -> new ActivateGenerosityTribblePowerAction(tribblesGame, tribblesContext);
                case KILL -> new ActivateKillTribblePowerAction(tribblesGame, tribblesContext);
                case KINDNESS -> new ActivateKindnessTribblePowerAction(tribblesGame, tribblesContext);
                case LAUGHTER -> new ActivateLaughterTribblePowerAction(tribblesGame, tribblesContext);
                case MASAKA -> new ActivateMasakaTribblePowerAction(tribblesGame, tribblesContext);
                case MUTATE -> new ActivateMutateTribblePowerAction(tribblesGame, tribblesContext);
                case POISON -> new ActivatePoisonTribblePowerAction(tribblesGame, tribblesContext);
                case PROCESS -> new ActivateProcessTribblePowerAction(tribblesGame, tribblesContext);
                case RECYCLE -> new ActivateRecycleTribblePowerAction(tribblesGame, tribblesContext);
                case REVERSE -> new ActivateReverseTribblePowerAction(tribblesGame, tribblesContext);
                default -> throw new InvalidCardDefinitionException(
                        "Code not yet implemented for Tribble power " + tribblePower.getHumanReadable());
            };
            result.add(activateAction);
            return result;
        } else throw new InvalidCardDefinitionException(
                "Could not create Tribbles power effect for a non-Tribbles game");
    }
}