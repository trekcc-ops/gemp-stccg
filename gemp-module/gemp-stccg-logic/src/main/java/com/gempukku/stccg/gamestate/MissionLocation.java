package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.cardgroup.CardPile;
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

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="locationId",
        scope = MissionLocation.class)
@JsonIgnoreProperties(value = { "isHomeworld", "seedCardCount" }, allowGetters = true)
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
    private final CardPile<MissionCard> _missionCards = new CardPile<>();
    private final Map<String, CardPile<PhysicalCard>> _preSeedCards = new HashMap<>();
    private final CardPile<PhysicalCard> _seedCards;

    public MissionLocation(@JsonProperty("quadrant") Quadrant quadrant,
                           @JsonProperty("region") Region region,
                           @JsonProperty("locationName") String locationName,
                           @JsonProperty("isCompleted") boolean isCompleted,
                           @JsonProperty("locationId") Integer locationId,
                           @JsonProperty("missionCardIds") @JsonIdentityReference(alwaysAsId = true)
                           List<MissionCard> missionCards,
                           // preseeds
                            @JsonProperty("seedCardIds") @JsonIdentityReference(alwaysAsId = true)
                           List<PhysicalCard> seedCards) {
        _quadrant = quadrant;
        _region = region;
        _locationName = locationName;
        _isCompleted = isCompleted;
        _locationId = locationId;
        _missionCards.setCards(missionCards);
        _seedCards = new CardPile<>();
        _seedCards.setCards(seedCards);
    }



    public MissionLocation(DefaultGame cardGame, MissionCard mission, int locationId) {
        this(mission.getBlueprint().getQuadrant(), mission.getBlueprint().getRegion(),
                mission.getBlueprint().getLocation(), locationId);
        _missionCards.addCardToTop(mission);
        mission.setLocation(cardGame, this);
    }

    public MissionLocation(Quadrant quadrant, Region region, String locationName, int locationId) {
        _quadrant = quadrant;
        _region = region;
        _locationName = locationName;
        _locationId = locationId;
        _seedCards = new CardPile<>();
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(ST1EGame game, Player player) {
        Stream<AwayTeam> teamsOnSurface =
                game.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
        return teamsOnSurface.filter(awayTeam -> Objects.equals(awayTeam.getControllerName(), player.getPlayerId()));
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(ST1EGame game, String playerName) {
        Stream<AwayTeam> teamsOnSurface =
                game.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
        return teamsOnSurface.filter(awayTeam -> Objects.equals(awayTeam.getControllerName(), playerName));
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
        if (isSharedMission()) {
            for (PhysicalCard missionCard : missionCards) {
                if (Objects.equals(missionCard.getOwnerName(), playerId))
                    result = missionCard;
            }
        } else {
            result = Iterables.getOnlyElement(missionCards);
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
        if (card.isOwnedBy(player.getPlayerId()))
            return card.getBlueprint().getOwnerSpan();
        else return card.getBlueprint().getOpponentSpan();
    }

    @Override
    public boolean mayBeAttemptedByPlayer(Player player, ST1EGame cardGame) throws InvalidGameLogicException {
        // Rule 7.2.1, Paragraph 1
        String playerName = player.getPlayerId();
        // TODO - Does not address shared missions, multiple copies of universal missions, or dual missions
        MissionCard missionCard = getMissionForPlayer(playerName);
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
                return Filters.filterYourCardsInPlay(cardGame, player, Filters.ship, Filters.atLocation(this))
                        .stream().anyMatch(ship -> (cardGame.getRules().canShipAttemptMission((ShipCard) ship, _locationId,
                                cardGame, playerName)));
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

    public Set<Affiliation> getAffiliationIconsForPlayer(String playerName) {
        return getAffiliationIcons(playerName);
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
        CardPile<PhysicalCard> preSeeds = _preSeedCards.get(player.getPlayerId());
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
        CardPile<PhysicalCard> preSeeds = _preSeedCards.get(player.getPlayerId());
        if (preSeeds == null) {
            return new LinkedList<>();
        } else {
            return preSeeds.getCards();
        }
    }

    public void seedPreSeedsForSharedMissions(ST1EGame stGame) {
        String firstPlayerName = _missionCards.getBottomCard().getOwnerName();
        List<Player> players = new LinkedList<>(stGame.getPlayers());
        List<String> playerNames = new LinkedList<>(stGame.getPlayerIds());
        int currentIndex = playerNames.indexOf(firstPlayerName);

        while (!_preSeedCards.isEmpty()) {
            if (currentIndex == players.size())
                currentIndex = 0;
            Player currentPlayer = players.get(currentIndex);
            CardPile<PhysicalCard> currentPreSeedPile = _preSeedCards.get(currentPlayer.getPlayerId());
            if (currentPreSeedPile == null) {
                currentIndex++;
            } else if (currentPreSeedPile.isEmpty()) {
                _preSeedCards.remove(currentPlayer.getPlayerId());
                currentIndex++;
            } else {
                PhysicalCard card = currentPreSeedPile.getBottomCard();
                currentPreSeedPile.removeCard(card);
                seedCardOnTopOfMissionSeedCards(stGame, card);
            }
        }
    }

    public void seedPreSeedsForYourMissions(DefaultGame cardGame) {
        Set<String> playersWithSeeds = _preSeedCards.keySet();
        for (String player : playersWithSeeds) {
            seedCardPileOnTopOfSeedCards(cardGame, _preSeedCards.get(player));
        }
    }



    public void seedPreSeedsForOpponentsMissions(DefaultGame cardGame) {
        Set<String> playersWithSeeds = _preSeedCards.keySet();
        for (String player : playersWithSeeds) {
            seedCardPileOnBottomOfSeedCards(cardGame, _preSeedCards.get(player));
        }
    }

    public void addCardToTopOfPreSeedPile(PhysicalCard card, String playerName) {
        if (_preSeedCards.get(playerName) == null) {
            _preSeedCards.put(playerName, new CardPile<>());
        }
        CardPile<PhysicalCard> preSeeds = _preSeedCards.get(playerName);
        preSeeds.addCardToTop(card);
    }


    public void removePreSeedCard(PhysicalCard card, String playerName) {
        CardPile<PhysicalCard> preSeeds = _preSeedCards.get(playerName);
        if (preSeeds != null) {
            preSeeds.removeCard(card);
        }
    }


    public void removePreSeedCard(PhysicalCard card, Player player) {
        CardPile<PhysicalCard> preSeeds = _preSeedCards.get(player.getPlayerId());
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

    public void addMission(DefaultGame cardGame, MissionCard newMission) {
        newMission.stackOn(_missionCards.getCards().getFirst());
        _missionCards.addCardToTop(newMission);
        newMission.setLocation(cardGame, this);
    }

    public boolean hasMatchingAffiliationIcon(Player contextPlayer, Collection<Affiliation> affiliationOptions) {
        for (Affiliation affiliation : getAffiliationIconsForPlayer(contextPlayer)) {
            if (affiliationOptions.contains(affiliation))
                return true;
        }
        return false;
    }

    public void seedCardUnderMission(DefaultGame cardGame, PhysicalCard card) {
        _seedCards.addCardToBottom(card);
        card.setLocation(cardGame, this);
    }

    public void seedCardOnTopOfMissionSeedCards(DefaultGame cardGame, PhysicalCard card) {
        _seedCards.addCardToTop(card);
        card.setLocation(cardGame, this);
    }

    public void removeSeedCardFromMission(PhysicalCard card) {
        _seedCards.removeCard(card);
    }

    public void seedCardPileOnBottomOfSeedCards(DefaultGame cardGame, CardPile<PhysicalCard> cardPile) {
        while (!cardPile.isEmpty()) {
            PhysicalCard card = cardPile.getTopCard();
            cardPile.removeCard(card);
            seedCardUnderMission(cardGame, card);
        }
    }

    public void seedCardPileOnTopOfSeedCards(DefaultGame cardGame, CardPile<PhysicalCard> cardPile) {
        while (!cardPile.isEmpty()) {
            PhysicalCard card = cardPile.getBottomCard();
            cardPile.removeCard(card);
            seedCardOnTopOfMissionSeedCards(cardGame, card);
        }
    }

    public boolean hasCardSeededUnderneath(PhysicalCard card) {
        return _seedCards.contains(card);
    }


    public int getLocationId() {
        return _locationId;
    }

    public boolean isSharedMission() {
        return _missionCards.size() == 2;
    }

    public boolean isInSameQuadrantAs(GameLocation currentLocation) {
        return currentLocation.isInQuadrant(_quadrant);
    }

    public boolean wasSeededBy(String playerName) {
        for (MissionCard mission : _missionCards.getCards()) {
            if (mission.isOwnedBy(playerName)) {
                return true;
            }
        }
        return false;
    }
}