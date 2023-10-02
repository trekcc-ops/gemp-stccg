package com.gempukku.lotro.gamestate;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.common.Quadrant;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.GameFormat;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.PlayerOrder;

import java.util.*;

public class ST1EGameState extends GameState {
    protected final Map<String, List<PhysicalCardImpl>> _drawDecks = new HashMap<>();
    protected final Map<String, List<PhysicalCardImpl>> _seedDecks = new HashMap<>();
    protected final Map<String, List<PhysicalCardImpl>> _missionPiles = new HashMap<>();
    private Map<Quadrant, List<PhysicalCardImpl>> _spacelines = new HashMap<>();

    public ST1EGameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library, GameFormat format) {
        super(players, decks, library, format);
    }

    @Override
    public void init(PlayerOrder playerOrder, String firstPlayer) {
        _playerOrder = playerOrder;
        setCurrentPlayerId(firstPlayer);
        for (String player : playerOrder.getAllPlayers()) {
            _players.put(player, new Player(player));
        }
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializeBoard(playerOrder.getAllPlayers(), _format.discardPileIsPublic());
        }
    }

    @Override
    public List<PhysicalCardImpl> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DECK)
            return _decks.get(playerId);
        else if (zone == Zone.ADVENTURE_DECK)
            return _adventureDecks.get(playerId);
        else if (zone == Zone.DISCARD)
            return _discards.get(playerId);
        else if (zone == Zone.HAND)
            return _hands.get(playerId);
        else if (zone == Zone.REMOVED)
            return _removed.get(playerId);
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else // This should never be accessed
            return _inPlay;
    }

    @Override
    protected void addPlayerCards(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library) throws CardNotFoundException {
        int cardId = 1;
        for (String playerId : players) {
            for (Map.Entry<String,List<String>> entry : decks.get(playerId).getSubDecks().entrySet()) {
                List<PhysicalCardImpl> subDeck = new LinkedList<>();
                for (String blueprintId : entry.getValue()) {
                    subDeck.add(new PhysicalCardImpl(cardId, blueprintId, playerId, library.getLotroCardBlueprint(blueprintId)));
                }
                if (Objects.equals(entry.getKey(), "DRAW_DECK")) {
                    _drawDecks.put(playerId, subDeck);
                } else if (Objects.equals(entry.getKey(), "SEED_DECK")) {
                    _seedDecks.put(playerId, subDeck);
                } else if (Objects.equals(entry.getKey(), "MISSIONS")) {
                    _missionPiles.put(playerId, subDeck);
                }
            }
        }
    }

    public List<LotroPhysicalCard> getMissionPile(String playerId) {
        return Collections.unmodifiableList(_missionPiles.get(playerId));
    }

    public List<LotroPhysicalCard> getSpaceline(Quadrant quadrant) {
        return Collections.unmodifiableList(_spacelines.get(quadrant));
    }

    public void createNewSpaceline(Quadrant quadrant) {
        _spacelines.put(quadrant, new ArrayList<>());
    }

    public void addToSpaceline(LotroPhysicalCard newMission, Quadrant quadrant, int indexNumber) {
        // TODO - define method
//        _spacelines.get(quadrant).add(indexNumber, newMission);
    }

    public boolean spacelineExists(Quadrant quadrant) { return _spacelines.containsKey(quadrant); }

    @Override
    public void playEffectReturningResult(LotroPhysicalCard cardPlayed) { }

}