package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.*;
import java.util.stream.Stream;

public class ST1EMission {
    private final ST1ELocation _location;
    private final Map<String, MissionCard> _missionCardsByPlayer = new HashMap<>();
    private final ST1EGame _game;
    private final List<MissionCard> _missionCards = new LinkedList<>();

    public enum SharedStatus { SHARED, NOT_SHARED }
    private boolean _completed;

    private final MissionCard _initialMissionCard; // TODO - used to keep Away Teams attached to a single card

    public ST1EMission(MissionCard card, ST1ELocation location) {
        _game = card.getGame();
        Collection<String> players = _game.getPlayerIds();
        for (String playerId : players)
            _missionCardsByPlayer.put(playerId, card);
        _initialMissionCard = card;
        _location = location;
        _missionCards.add(card);
    }

    public void addMissionCard(MissionCard newCard) throws InvalidGameLogicException {
        String newCardOwnerName = newCard.getOwnerName();
        MissionCard currentCardForOwner = _missionCardsByPlayer.get(newCardOwnerName);
        if (!Objects.equals(newCard, currentCardForOwner)) {
            if (Objects.equals(currentCardForOwner.getOwnerName(), newCardOwnerName)) {
                throw new InvalidGameLogicException(
                        "Player " + newCardOwnerName + " already has a mission at location " +
                                _location.getLocationName());
            } else {
                newCard.setMission(this);
                _missionCardsByPlayer.put(newCardOwnerName, newCard);
                _missionCards.add(newCard);
            }
        }
    }

    public ST1ELocation getLocation() { return _location; }

    public Set<Affiliation> getAffiliationIcons(Player player) {
        MissionCard playerCard = getMissionCardForPlayer(player);
        return playerCard.getAffiliationIconsForPlayer(player);
    }

    public boolean isHomeworldForPlayer(Player player) {
        MissionCard playerCard = getMissionCardForPlayer(player);
        return playerCard.isHomeworld();
    }

    public MissionCard getMissionCardForPlayer(Player player) {
        return _missionCardsByPlayer.get(player.getPlayerId());
    }

    // TODO - see comment in declarations
    public MissionCard getInitialMissionCard() {
        return _initialMissionCard;
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(Player player) {
        return getAwayTeamsOnSurface().filter(awayTeam -> awayTeam.getPlayer() == player);
    }
    public Stream<AwayTeam> getAwayTeamsOnSurface() {
        ST1EGameState gameState = _game.getGameState();
        Set<AwayTeam> allAwayTeams = gameState.getAwayTeams();
        return allAwayTeams.stream().filter(awayTeam -> awayTeam.isOnSurface(this.getInitialMissionCard()));
    }

    public MissionType getMissionType() {
        // TODO - Does not account for mission types changing due to other cards, or possibly different types at the
        //  same location? Should do a card blueprint factory check for that one
        return _initialMissionCard.getBlueprint().getMissionType();
    }

    public ST1EGame getGame() { return _game; }

    public int getSpan(Player player) {
        MissionCard missionForPlayer = getMissionCardForPlayer(player);
        if (Objects.equals(player, missionForPlayer.getOwner())) {
            return missionForPlayer.getBlueprint().getOwnerSpan();
        } else {
            return missionForPlayer.getBlueprint().getOpponentSpan();
        }
    }
    public List<? extends PhysicalCard> getAllMissionCards() { return _missionCards; }

    public MissionRequirement getMissionRequirements(String playerId) {
        Player player = _game.getGameState().getPlayer(playerId);
        MissionCard missionCardForPlayer = getMissionCardForPlayer(player);
        return missionCardForPlayer.getBlueprint().getMissionRequirements();
    }

    public void isSolvedByPlayer(String playerId) {
        Player player = _game.getGameState().getPlayer(playerId);
        int pointsShown = getMissionCardForPlayer(player).getBlueprint().getPointsShown();
        player.scorePoints(pointsShown);
        _completed = true;
        _game.getGameState().checkVictoryConditions();
    }

}