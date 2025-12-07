package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.game.ST1EGame;

public class EquipmentCard extends ST1EPhysicalCard implements CardWithCompatibility, ReportableCard {

    private AwayTeam _awayTeam;

    public EquipmentCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
    }


    public void leaveAwayTeam(ST1EGame cardGame) {
        _awayTeam.remove(cardGame, this);
        _awayTeam = null;
    }

    @JsonIgnore
    @Override
    public boolean isInAnAwayTeam() {
        return _awayTeam != null;
    }

    @Override
    public void addToAwayTeam(AwayTeam awayTeam) {
        _awayTeam = awayTeam;
        _awayTeam.add(this);
    }

    @JsonIgnore
    @Override
    public AwayTeam getAwayTeam() {
        return _awayTeam;
    }
}