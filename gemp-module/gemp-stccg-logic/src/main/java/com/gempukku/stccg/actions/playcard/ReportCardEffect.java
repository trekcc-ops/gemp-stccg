package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.GameState;

public class ReportCardEffect extends DefaultEffect {
    private final Zone _playedFrom;
    private final PhysicalReportableCard1E _cardPlayed;
    private final FacilityCard _reportingDestination;

    public ReportCardEffect(String performingPlayerId, Zone playedFrom, PhysicalReportableCard1E cardPlayed,
                            FacilityCard reportingDestination) {
        super(cardPlayed.getGame(), performingPlayerId);
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _reportingDestination = reportingDestination;
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
        GameState gameState = _game.getGameState();

        _game.sendMessage(_cardPlayed.getOwnerName() + " played " + _cardPlayed.getCardLink());

        gameState.removeCardFromZone(_cardPlayed);
        _game.getGameState().getPlayer(_cardPlayed.getOwnerName()).addPlayedAffiliation(_cardPlayed.getAffiliation());
        _cardPlayed.reportToFacility(_reportingDestination);
        _game.getActionsEnvironment().emitEffectResult(
                new PlayCardResult(this, _playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}