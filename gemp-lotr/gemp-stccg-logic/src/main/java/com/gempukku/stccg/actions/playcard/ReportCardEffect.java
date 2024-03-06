package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;

public class ReportCardEffect extends DefaultEffect {
    private final Zone _playedFrom;
    private final PhysicalReportableCard1E _cardPlayed;
    private final FacilityCard _reportingDestination;
    private final ST1EGame _game;

    public ReportCardEffect(String performingPlayerId, Zone playedFrom, PhysicalReportableCard1E cardPlayed,
                            FacilityCard reportingDestination) {
        super(performingPlayerId);
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _reportingDestination = reportingDestination;
        _game = cardPlayed.getGame();
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    @Override
    public String getText() {
        return "Play " + _cardPlayed.getFullName();
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        ST1EGameState gameState = _game.getGameState();

        _game.sendMessage(_cardPlayed.getOwnerName() + " played " + _cardPlayed.getCardLink());

        gameState.removeCardFromZone(_cardPlayed);
        _game.getGameState().getPlayer(_cardPlayed.getOwnerName()).addPlayedAffiliation(_cardPlayed.getCurrentAffiliation());
        _cardPlayed.reportToFacility(_reportingDestination);
        _game.getActionsEnvironment().emitEffectResult(
                new PlayCardResult(this, _playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}