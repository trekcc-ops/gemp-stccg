package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.tribblepower.*;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;

public class ActivateTribblePowerEffectBlueprint implements SubActionBlueprint {

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context)
            throws InvalidCardDefinitionException, PlayerNotFoundException {

        List<Action> result = new LinkedList<>();
        PhysicalCard performingCard = context.card();
        TribblePower tribblePower = performingCard.getBlueprint().getTribblePower();
        if (cardGame instanceof TribblesGame tribblesGame) {

            Action activateAction = switch (tribblePower) {
                case AVALANCHE -> new ActivateAvalancheTribblePowerAction(tribblesGame, performingCard, context);
                case CONVERT -> new ActivateConvertTribblePowerAction(tribblesGame, performingCard, context);
                case CYCLE -> new ActivateCycleTribblePowerAction(tribblesGame, performingCard, context);
                case DISCARD -> new ActivateDiscardTribblePowerAction(tribblesGame, performingCard, context);
                case DRAW -> new ActivateDrawTribblePowerAction(tribblesGame, performingCard, context);
                case EVOLVE -> new ActivateEvolveTribblePowerAction(tribblesGame, performingCard, context);
                case FAMINE -> new ActivateFamineTribblePowerAction(tribblesGame, performingCard, context);
                case GENEROSITY -> new ActivateGenerosityTribblePowerAction(tribblesGame, performingCard, context);
                case KILL -> new ActivateKillTribblePowerAction(tribblesGame, performingCard, context);
                case KINDNESS -> new ActivateKindnessTribblePowerAction(tribblesGame, performingCard, context);
                case LAUGHTER -> new ActivateLaughterTribblePowerAction(tribblesGame, performingCard, context);
                case MASAKA -> new ActivateMasakaTribblePowerAction(tribblesGame, performingCard, context);
                case MUTATE -> new ActivateMutateTribblePowerAction(tribblesGame, performingCard, context);
                case POISON -> new ActivatePoisonTribblePowerAction(tribblesGame, performingCard, context);
                case PROCESS -> new ActivateProcessTribblePowerAction(tribblesGame, performingCard, context);
                case RECYCLE -> new ActivateRecycleTribblePowerAction(tribblesGame, performingCard, context);
                case REVERSE -> new ActivateReverseTribblePowerAction(tribblesGame, performingCard, context);
                default -> throw new InvalidCardDefinitionException(
                        "Code not yet implemented for Tribble power " + tribblePower.getHumanReadable());
            };
            result.add(activateAction);
            return result;
        } else throw new InvalidCardDefinitionException(
                "Could not create Tribbles power effect for a non-Tribbles game");
    }
}