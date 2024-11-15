package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.PlayerCannotSolveMissionModifier;

public class RevealSeedCardAction extends ActionyAction {
    private boolean _misSeedResolved;
    private final PhysicalCard _revealedCard;
    private final MissionCard _mission;

    public RevealSeedCardAction(Player revealingPlayer, PhysicalCard revealedCard, MissionCard mission) {
        super(revealingPlayer, "Reveal seed card", ActionType.REVEAL_SEED_CARD);
        _revealedCard = revealedCard;
        _mission = mission;
    }

    @Override
    public PhysicalCard getActionSource() {
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
            if (_revealedCard.isMisSeed(cardGame, _mission)) {
                if (_performingPlayerId.equals(_revealedCard.getOwnerName())) {
                    // TODO - Player also cannot solve objectives targeting the mission
                    Modifier modifier = new PlayerCannotSolveMissionModifier(_mission, _performingPlayerId);
                    cardGame.getModifiersEnvironment().addAlwaysOnModifier(modifier);
                }
                return new RemoveDilemmaFromGameAction(
                        cardGame.getPlayer(_performingPlayerId), _revealedCard, _mission);
            }
        }
        return getNextAction();
    }

    public PhysicalCard getRevealedCard() { return _revealedCard; }
}