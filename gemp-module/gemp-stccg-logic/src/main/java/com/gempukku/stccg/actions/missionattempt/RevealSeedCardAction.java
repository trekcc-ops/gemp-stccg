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
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.PlayerCannotSolveMissionModifier;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class RevealSeedCardAction extends ActionyAction {

    @JsonProperty("targetCardId")
    private final int _revealedCardId;

    private enum Progress { misSeedResolved }

    private final int _locationId;

    public RevealSeedCardAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard revealedCard,
                                MissionLocation mission) {
        super(cardGame, performingPlayerName, "Reveal seed card", ActionType.REVEAL_SEED_CARD, Progress.values());
        _revealedCardId = revealedCard.getCardId();
        _locationId = mission.getLocationId();
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
            if (cardGame.getCardFromCardId(_revealedCardId) instanceof ST1EPhysicalCard revealedCard &&
                    cardGame instanceof ST1EGame stGame) {
                setProgress(Progress.misSeedResolved);
                if (stGame.getGameState().getLocationById(_locationId) instanceof MissionLocation missionLocation) {
                    if (revealedCard.isMisSeed(cardGame, missionLocation)) {
                        if (_performingPlayerId.equals(revealedCard.getOwnerName())) {
                            // TODO - Player also cannot solve objectives targeting the mission
                            Modifier modifier =
                                    new PlayerCannotSolveMissionModifier(_locationId, _performingPlayerId);
                            cardGame.getModifiersEnvironment().addAlwaysOnModifier(modifier);
                        }
                        return new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, revealedCard);
                    }
                } else {
                    throw new InvalidGameLogicException("Unable to reveal seed card from location id " + _locationId);
                }
            } else {
                throw new InvalidGameLogicException("Tried to reveal a seed card in a non-1E game");
            }
        }
        setAsSuccessful();
        return getNextAction();
    }

    public int getRevealedCardId() { return _revealedCardId; }

}