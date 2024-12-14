package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DynamicDownloadAction extends ActionyAction {
    private final String _playerId;
    private final int _maxCardCount;
    private Action _playCardAction;
    private final Zone _fromZone;
    private final DefaultGame _game;
    private final PhysicalCard _actionSource;
    private final Map<PersonnelCard, List<PersonnelCard>> _validCombinations;

    public DynamicDownloadAction(Zone fromZone, Player player, PhysicalCard actionSource,
                                 Map<PersonnelCard, List<PersonnelCard>> validCombinations,
                                 int maxCardCount) {
        super(player, "Download card from " + fromZone.getHumanReadable(),
                ActionType.DOWNLOAD_CARD);
        _playerId = player.getPlayerId();
        _actionSource = actionSource;
        _validCombinations = validCombinations;
        _fromZone = fromZone;
        _game = player.getGame();
        _maxCardCount = maxCardCount;
    }

    protected Collection<PhysicalCard> getPlayableCards() {
        return new LinkedList<>(_validCombinations.keySet());
    }

    protected void playCard(final PhysicalCard selectedCard) {
        _playCardAction = selectedCard.getPlayCardAction(true);
        _playCardAction.appendEffect(
                new UnrespondableEffect(_game) {
                    @Override
                    protected void doPlayEffect() {
                        afterCardPlayed(selectedCard);
                    }
                });
        _game.getActionsEnvironment().addActionToStack(_playCardAction);
    }

    private void afterCardPlayed(PhysicalCard cardPlayed) {
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayCardAction)
            return _playCardAction.wasCarriedOut();
        return true;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK)
            return !_game.getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK) &&
                    !getPlayableCards().isEmpty();
        else
            return !getPlayableCards().isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Player performingPlayer = cardGame.getPlayer(_playerId);
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK) {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ArbitraryCardsSelectionDecision(performingPlayer, "Choose card(s) to download",
                            new LinkedList<>(getPlayableCards()), _validCombinations, 0, _maxCardCount) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            if (!selectedCards.isEmpty()) {
                                final PhysicalCard selectedCard = selectedCards.getFirst();
                                playCard(selectedCard);
                            }
                        }
                    });
        }
        return null;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _actionSource;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _actionSource;
    }

    protected Action getPlayCardAction() { return _playCardAction; }
    protected void setPlayCardAction(Action action) { _playCardAction = action; }

    public String getPerformingPlayerId() { return _playerId; }

    public DefaultGame getGame() { return _game; }
}