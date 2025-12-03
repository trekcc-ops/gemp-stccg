package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.processes.GameProcess;
import com.google.common.collect.Iterables;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@JsonTypeName("DoorwaySeedPhaseProcess")
public class DoorwaySeedPhaseProcess extends SimultaneousGameProcess {

    @ConstructorProperties({"playersParticipating"})
    public DoorwaySeedPhaseProcess(Collection<String> playersSelecting) {
        super(playersSelecting);
    }

    @Override
    public void process(DefaultGame cardGame) throws InvalidGameLogicException {
        for (Player player : cardGame.getPlayers()) {
            Collection<PhysicalCard> doorwaySeeds = new LinkedList<>();
            for (PhysicalCard seedCard : player.getCardsInGroup(Zone.SEED_DECK)) {
                if (seedCard.getCardType() == CardType.DOORWAY)
                    doorwaySeeds.add(seedCard);
            }
            if (!doorwaySeeds.isEmpty()) {
                String message = "Select cards to seed during doorway phase";
                cardGame.getUserFeedback().sendAwaitingDecision(
                        new ArbitraryCardsSelectionDecision(player, message, doorwaySeeds,
                                cardGame) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                try {
                                    List<PhysicalCard> cards = getSelectedCardsByResponse(result);
                                    for (PhysicalCard card : cards) {
                                        List<TopLevelSelectableAction> seedActions =
                                                card.createSeedCardActions(cardGame);
                                        if (seedActions.size() != 1) {
                                            throw new InvalidGameLogicException("Could not create a seed action");
                                        } else {
                                            TopLevelSelectableAction action = Iterables.getOnlyElement(seedActions);
                                            cardGame.getActionsEnvironment().addActionToStack(action);
                                        }
                                    }
                                } catch(InvalidGameLogicException exp) {
                                    throw new DecisionResultInvalidException(exp.getMessage());
                                }
                            }
                        });
            }
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        GameState cardGameState = cardGame.getGameState();
        cardGame.setCurrentPhase(Phase.SEED_MISSION);
        for (Player player : cardGame.getPlayers()) {
            List<PhysicalCard> missionSeeds = new LinkedList<>(cardGameState.getZoneCards(player, Zone.MISSIONS_PILE));
            if (!cardGame.getFormat().isNoShuffle())
                Collections.shuffle(missionSeeds);
        }
        return new ST1EMissionSeedPhaseProcess();
    }
}