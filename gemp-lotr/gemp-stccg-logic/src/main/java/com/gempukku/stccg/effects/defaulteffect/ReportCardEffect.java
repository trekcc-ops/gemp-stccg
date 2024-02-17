package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalFacilityCard;
import com.gempukku.stccg.cards.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.results.PlayCardResult;
import com.gempukku.stccg.rules.GameUtils;

public class ReportCardEffect extends DefaultEffect {
    private final Zone _playedFrom;
    private final PhysicalReportableCard1E _cardPlayed;
    private final PhysicalFacilityCard _reportingDestination;
    private final ST1EGame _game;

    public ReportCardEffect(ST1EGame game, Zone playedFrom, PhysicalReportableCard1E cardPlayed, PhysicalFacilityCard reportingDestination) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _reportingDestination = reportingDestination;
        _game = game;
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

        _game.getGameState().sendMessage(_cardPlayed.getOwnerName() + " played " +
                GameUtils.getCardLink(_cardPlayed));

        gameState.removeCardFromZone(_cardPlayed);
        _game.getGameState().getPlayer(_cardPlayed.getOwnerName()).addPlayedAffiliation(_cardPlayed.getCurrentAffiliation());
        _game.getGameState().sendMessage(
                "DEBUG: " + _cardPlayed.getOwnerName() + " now playing: " +
                        _cardPlayed.getCurrentAffiliation().getHumanReadable());
        _cardPlayed.reportToFacility(_reportingDestination);
        _game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}