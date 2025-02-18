package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.cardgroup.CardPile;
import com.gempukku.stccg.cards.cardgroup.MissionCardPile;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Stream;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="locationId")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "quadrant", "region", "locationName", "locationId", "isCompleted", "isHomeworld",
        "missionCardIds", "seedCardCount", "seedCardIds" })
@JsonPropertyOrder({ "quadrant", "region", "locationName", "locationId", "isCompleted", "isHomeworld",
        "missionCardIds", "seedCardCount", "seedCardIds" })
@JsonFilter("missionLocationSerializerFilter")
public class MissionLocation implements GameLocation {

    private static final Logger LOGGER = LogManager.getLogger(MissionLocation.class);
    @JsonProperty("quadrant")
    private final Quadrant _quadrant;
    @JsonProperty("region")
    private final Region _region;
    @JsonProperty("locationName")
    private final String _locationName;
    @JsonProperty("isCompleted")
    private boolean _isCompleted;
    @JsonProperty("locationId")
    private final int _locationId;
    private final MissionCardPile _missionCards = new MissionCardPile(Zone.SPACELINE);
    private Map<Player, CardPile> _preSeedCards = new HashMap<>();
    private final CardPile _seedCards;
    public MissionLocation(MissionCard mission, int locationId) {
        this(mission.getBlueprint().getQuadrant(), mission.getBlueprint().getRegion(),
                mission.getBlueprint().getLocation(), locationId);
        _missionCards.addCard(mission);
        mission.setLocation(this);
    }

    public MissionLocation(Quadrant quadrant, Region region, String locationName, int locationId) {
        _quadrant = quadrant;
        _region = region;
        _locationName = locationName;
        _locationId = locationId;
        _seedCards = new CardPile();
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(ST1EGame game, Player player) {
        Stream<AwayTeam> teamsOnSurface =
                game.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
        return teamsOnSurface.filter(awayTeam -> awayTeam.getPlayer() == player);
    }


    public Stream<AwayTeam> getAwayTeamsOnSurface(ST1EGame cardGame) {
        return cardGame.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
    }


    @JsonProperty("missionCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    public List<MissionCard> getMissionCards() {
        return _missionCards.getCards();
    }

    @JsonProperty("seedCardCount")
    private int getSeedCardCount() { return _seedCards.size(); }

    public boolean isInQuadrant(Quadrant quadrant) { return _quadrant == quadrant; }
    public Quadrant getQuadrant() { return _quadrant; }
    public String getLocationName() { return _locationName; }
    public Region getRegion() { return _region; }

    public MissionCard getCardForActionSelection(Player performingPlayer) throws InvalidGameLogicException {
        return getMissionForPlayer(performingPlayer.getPlayerId());
    }

    public MissionCard getMissionForPlayer(String playerId) throws InvalidGameLogicException {
        PhysicalCard result = null;
        Collection<? extends PhysicalCard> missionCards = getMissionCards();
        if (missionCards.size() == 1) {
            result = Iterables.getOnlyElement(missionCards);
        }
        else if (missionCards.size() == 2) {
            for (PhysicalCard missionCard : missionCards) {
                if (Objects.equals(missionCard.getOwnerName(), playerId))
                    result = missionCard;
            }
        }
        if (result instanceof MissionCard missionCard)
            return missionCard;
        throw new InvalidGameLogicException("Could not find valid mission properties for player " + playerId + " at " + _locationName);
    }

    @JsonProperty("seedCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    public List<PhysicalCard> getSeedCards() {
        return _seedCards.getCards();
    }


    public int getDistanceToLocation(ST1EGame cardGame, GameLocation location, Player player)
            throws InvalidGameLogicException
    {
        if (!location.isInQuadrant(_quadrant)) {
            // TODO - Not correct if you're calculating inter-quadrant distance (e.g., Bajoran Wormhole)
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Tried to calculate span between quadrants for ");
            errorMessage.append(_locationName);
            errorMessage.append(" (").append(_quadrant).append(") and ");
            errorMessage.append(location.getLocationName());
            LOGGER.error(errorMessage);
            throw new InvalidGameLogicException("Tried to calculate span between quadrants");
        } else {
            List<MissionLocation> spaceline = cardGame.getGameState().getSpacelineLocations();
            int startingIndex = spaceline.indexOf(this);
            int endingIndex = spaceline.indexOf(location);
            int distance = 0;
            int loopStart;
            int loopEnd;
            if (startingIndex < endingIndex) {
                loopStart = startingIndex + 1;
                loopEnd = endingIndex;
            } else {
                loopStart = endingIndex;
                loopEnd = startingIndex - 1;
            }
            for (int i = loopStart; i <= loopEnd; i++) {
                distance += spaceline.get(i).getSpan(player);
            }
            return distance;
        }
    }

    public int getLocationZoneIndex(ST1EGame cardGame) {
        return cardGame.getGameState().getSpacelineLocations().indexOf(this);
    }

    private int getSpan(Player player) throws InvalidGameLogicException {
        MissionCard card = getMissionForPlayer(player.getPlayerId());
        if (card.getOwner() == player)
            return card.getBlueprint().getOwnerSpan();
        else return card.getBlueprint().getOpponentSpan();
    }

    @Override
    public boolean mayBeAttemptedByPlayer(Player player, ST1EGame cardGame) throws InvalidGameLogicException {
        // Rule 7.2.1, Paragraph 1
        // TODO - Does not address shared missions, multiple copies of universal missions, or dual missions
        MissionCard missionCard = getMissionForPlayer(player.getPlayerId());
        MissionType missionType = missionCard.getBlueprint().getMissionType();
        if (missionCard.getBlueprint().hasNoPointBox())
            return false;
        if (_isCompleted)
            return false;
        if (missionCard.wasSeededBy(player) || missionCard.getPointsShown() >= 40) {
            if (missionType == MissionType.PLANET)
                return getYourAwayTeamsOnSurface(cardGame, player).anyMatch(
                        awayTeam -> awayTeam.canAttemptMission(cardGame, this));
            if (missionType == MissionType.SPACE)
                return Filters.filterYourActive(cardGame, player, Filters.ship, Filters.atLocation(this))
                        .stream().anyMatch(ship -> ((PhysicalShipCard) ship).canAttemptMission(this));
        }
        return false;
    }

    public Set<Affiliation> getAffiliationIcons(String playerId) {
        try {
            MissionCard card = getMissionForPlayer(playerId);
            CardBlueprint blueprint = card.getBlueprint();
            if (Objects.equals(playerId, card.getOwnerName())) {
                return blueprint.getOwnerAffiliationIcons();
            } else if (blueprint.getOpponentAffiliationIcons() == null) {
                return blueprint.getOwnerAffiliationIcons();
            } else {
                return blueprint.getOwnerAffiliationIcons();
            }
        } catch(InvalidGameLogicException exp) {
            return new HashSet<>();
        }
    }

    public Set<Affiliation> getAffiliationIconsForPlayer(Player player) {
        return getAffiliationIcons(player.getPlayerId());
    }

    public MissionType getMissionType() {
        PhysicalCard missionCard = getMissionCards().getFirst();
        return missionCard.getBlueprint().getMissionType();
    }

    public MissionRequirement getRequirements(String playerId) throws InvalidGameLogicException {
        MissionCard card = getMissionForPlayer(playerId);
        return card.getBlueprint().getMissionRequirements();
    }

    public void complete(String completingPlayerId, DefaultGame cardGame)
            throws InvalidGameLogicException, PlayerNotFoundException {
        MissionCard missionCard = getMissionForPlayer(completingPlayerId);
        _isCompleted = true;
        cardGame.getGameState().getPlayer(completingPlayerId).scorePoints(missionCard.getPoints());
        cardGame.getGameState().checkVictoryConditions(cardGame);
    }

    public void removeSeedCard(PhysicalCard cardToRemove) {
        removeSeedCardFromMission(cardToRemove);
    }

    public int getPreSeedCardCountForPlayer(Player player) {
        CardPile preSeeds = _preSeedCards.get(player);
        if (preSeeds == null) {
            return 0;
        } else {
            return preSeeds.getCards().size();
        }
    }

    public boolean hasCardsPreSeededByPlayer(Player player) {
        return getPreSeedCardCountForPlayer(player) > 0;
    }

    public Collection<PhysicalCard> getPreSeedCardsForPlayer(Player player) {
        CardPile preSeeds = _preSeedCards.get(player);
        if (preSeeds == null) {
            return new LinkedList<>();
        } else {
            return preSeeds.getCards();
        }
    }

    public void seedPreSeedsForSharedMissions(ST1EGame stGame) {
        Player firstPlayer = _missionCards.getBottomCard().getOwner();
        List<Player> players = new LinkedList<>(stGame.getPlayers());
        int currentIndex = players.indexOf(firstPlayer);

        while (!_preSeedCards.isEmpty()) {
            if (currentIndex == players.size())
                currentIndex = 0;
            Player currentPlayer = players.get(currentIndex);
            CardPile currentPreSeedPile = _preSeedCards.get(currentPlayer);
            if (currentPreSeedPile == null) {
                currentIndex++;
            } else if (currentPreSeedPile.isEmpty()) {
                _preSeedCards.remove(currentPlayer);
                currentIndex++;
            } else {
                PhysicalCard card = currentPreSeedPile.getBottomCard();
                currentPreSeedPile.removeCard(card);
                seedCardOnTopOfMissionSeedCards(card);
            }
        }
    }

    public void seedPreSeedsForYourMissions() {
        Set<Player> playersWithSeeds = _preSeedCards.keySet();
        for (Player player : playersWithSeeds) {
            seedCardPileOnTopOfSeedCards(_preSeedCards.get(player));
        }
    }



    public void seedPreSeedsForOpponentsMissions() {
        Set<Player> playersWithSeeds = _preSeedCards.keySet();
        for (Player player : playersWithSeeds) {
            seedCardPileOnBottomOfSeedCards(_preSeedCards.get(player), this);
        }
    }

    public void addCardToTopOfPreSeedPile(PhysicalCard card, Player player) {
        if (_preSeedCards.get(player) == null) {
            _preSeedCards.put(player, new CardPile());
        }
        CardPile preSeeds = _preSeedCards.get(player);
        preSeeds.addCardToTop(card);
    }

    public void removePreSeedCard(PhysicalCard card, Player player) {
        CardPile preSeeds = _preSeedCards.get(player);
        if (preSeeds != null) {
            preSeeds.removeCard(card);
        }
    }

    public boolean isCompleted() {
        return _isCompleted;
    }

    public void setCompleted(boolean completed) {
        _isCompleted = completed;
    }

    @JsonProperty("isHomeworld")
    public boolean isHomeworld() {
        boolean result = false;
        for (MissionCard card : getMissionCards()) {
            if (card.isHomeworld())
                result = true;
        }
        return result;
    }

    public boolean isPlanet() { return getMissionType() == MissionType.PLANET || getMissionType() == MissionType.DUAL; }
    public boolean isSpace() { return getMissionType() == MissionType.SPACE || getMissionType() == MissionType.DUAL; }

    public MissionCard getTopMissionCard() { return getMissionCards().getLast(); }

    public void addMission(MissionCard newMission) {
        newMission.stackOn(_missionCards.getCards().getFirst());
        _missionCards.addCard(newMission);
        newMission.setLocation(this);
    }

    public boolean hasMatchingAffiliationIcon(Player contextPlayer, Collection<Affiliation> affiliationOptions) {
        for (Affiliation affiliation : getAffiliationIconsForPlayer(contextPlayer)) {
            if (affiliationOptions.contains(affiliation))
                return true;
        }
        return false;
    }

    public void seedCardUnderMission(MissionLocation location, PhysicalCard card) {
        _seedCards.addCardToBottom(card);
        card.setLocation(this);
    }

    public void seedCardOnTopOfMissionSeedCards(PhysicalCard card) {
        _seedCards.addCardToTop(card);
        card.setLocation(this);
    }

    public void removeSeedCardFromMission(PhysicalCard card) {
        _seedCards.removeCard(card);
    }

    public void seedCardPileOnBottomOfSeedCards(CardPile cardPile, MissionLocation missionLocation) {
        while (!cardPile.isEmpty()) {
            PhysicalCard card = cardPile.getTopCard();
            cardPile.removeCard(card);
            seedCardUnderMission(missionLocation, card);
        }
    }

    public void seedCardPileOnTopOfSeedCards(CardPile cardPile) {
        while (!cardPile.isEmpty()) {
            PhysicalCard card = cardPile.getBottomCard();
            cardPile.removeCard(card);
            seedCardOnTopOfMissionSeedCards(card);
        }
    }

    public void preSeedCardsUnder(DefaultGame cardGame, Collection<PhysicalCard> cards, Player player)
            throws InvalidGameLogicException {
        GameState gameState = cardGame.getGameState();
        for (PhysicalCard card : cards) {
            gameState.removeCardFromZone(card);
            gameState.addCardToZone(card, Zone.VOID);
            addCardToTopOfPreSeedPile(card, player);
        }
    }


    public int getLocationId() {
        return _locationId;
    }

    public boolean isInSameQuadrantAs(GameLocation currentLocation) {
        return currentLocation.isInQuadrant(_quadrant);
    }
}