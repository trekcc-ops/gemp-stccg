package com.gempukku.stccg.actions.missionattempt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.PlayerCannotSolveMissionModifier;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class RevealSeedCardAction extends ActionyAction {

    @JsonProperty("targetCardId")
    private final int _revealedCardId;
    private final int _missionAttemptActionId;
    private enum Progress { misSeedResolved }
    private final MissionLocation _missionLocation;

    public RevealSeedCardAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard revealedCard,
                                AttemptMissionAction attemptAction, MissionLocation mission) {
        super(cardGame, performingPlayerName, "Reveal seed card", ActionType.REVEAL_SEED_CARD, Progress.values());
        _revealedCardId = revealedCard.getCardId();
        _missionAttemptActionId = attemptAction.getActionId();
        _missionLocation = mission;
        revealedCard.reveal();
    }



    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, CardNotFoundException,
            PlayerNotFoundException {
        if (!getProgress(Progress.misSeedResolved)) {
            PhysicalCard revealedCard = cardGame.getCardFromCardId(_revealedCardId);
            Action attemptAction = cardGame.getActionById(_missionAttemptActionId);
            if (attemptAction instanceof AttemptMissionAction missionAction) {
                setProgress(Progress.misSeedResolved);
                if (revealedCard.isMisSeed(cardGame, _missionLocation)) {
                    if (_performingPlayerId.equals(revealedCard.getOwnerName())) {
                        // TODO - Player also cannot solve objectives targeting the mission
                        Modifier modifier =
                                new PlayerCannotSolveMissionModifier(_missionLocation, _performingPlayerId);
                        cardGame.getModifiersEnvironment().addAlwaysOnModifier(modifier);
                    }
                    if (revealedCard instanceof ST1EPhysicalCard stCard) {
                        return new RemoveDilemmaFromGameAction(
                                cardGame.getPlayer(_performingPlayerId), stCard);
                    } else {
                        throw new InvalidGameLogicException("Tried to reveal a seed card in a non-1E game");
                    }
                }
            } else {
                throw new InvalidGameLogicException("No valid action found for mission attempt");
            }
        }
        setAsSuccessful();
        return getNextAction();
    }

    public int getRevealedCardId() { return _revealedCardId; }

}