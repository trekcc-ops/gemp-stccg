package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalNounCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.GameStateListener;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.UserFeedback;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.ST1EPlayerOrderProcess;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.ST1ERuleSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class AwayTeam {
    private Player _player;
    private PhysicalCard _parentCard;
    private final Collection<PhysicalNounCard1E> _cardsInAwayTeam;

    public AwayTeam(Player player, PhysicalCard parentCard, Collection<PhysicalNounCard1E> cards) {
        _player = player;
        _parentCard = parentCard;
        _cardsInAwayTeam = cards;
    }

    public boolean hasAffiliation(Affiliation affiliation) {
        for (PhysicalNounCard1E card : _cardsInAwayTeam) {
            if (card.getCurrentAffiliation() == affiliation)
                return true;
        }
        return false;
    }

    public boolean isOnSurface(PhysicalCard planet) {
        return _parentCard == planet;
    }

    public String getPlayerId() { return _player.getPlayerId(); }
    public Collection<PhysicalNounCard1E> getCards() { return _cardsInAwayTeam; }
}
