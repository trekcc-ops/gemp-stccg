package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.PlayerCannotSolveMissionModifier;

public class RevealSeedCardAction extends ActionyAction {
    private boolean _misSeedResolved;
    private final PhysicalCard _revealedCard;
    private final MissionLocation _missionLocation;

    public RevealSeedCardAction(Player revealingPlayer, PhysicalCard revealedCard, MissionLocation mission) {
        super(revealingPlayer, "Reveal seed card", ActionType.REVEAL_SEED_CARD);
        _revealedCard = revealedCard;
        _missionLocation = mission;
    }


    @Override
    public PhysicalCard getPerformingCard() {
        return _revealedCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _revealedCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_misSeedResolved) {
            _misSeedResolved = true;
            if (_revealedCard.isMisSeed(cardGame, _missionLocation)) {
                if (_performingPlayerId.equals(_revealedCard.getOwnerName())) {
                    // TODO - Player also cannot solve objectives targeting the mission
                    Modifier modifier = new PlayerCannotSolveMissionModifier(cardGame, _missionLocation,
                            _performingPlayerId);
                    cardGame.getModifiersEnvironment().addAlwaysOnModifier(modifier);
                }
                if (_revealedCard instanceof ST1EPhysicalCard stCard) {
                    return new RemoveDilemmaFromGameAction(
                            cardGame.getPlayer(_performingPlayerId), stCard, _missionLocation);
                } else {
                    throw new InvalidGameLogicException("Tried to reveal a seed card in a non-1E game");
                }
            }
        }
        return getNextAction();
    }

    public PhysicalCard getRevealedCard() { return _revealedCard; }
}