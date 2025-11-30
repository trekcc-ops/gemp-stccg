package com.gempukku.stccg.filters;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.cards.CompletePhysicalCardVisitor;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

public class Filters {
        // TODO - There is almost certainly a cleaner way of implementing this

    private static final Map<CardType, CardFilter> _cardTypeFilterMap = new HashMap<>();
    private static final Map<SkillName, CardFilter> _skillNameFilterMap = new HashMap<>();
    private static final Map<FacilityType, CardFilter> _facilityTypeFilterMap = new HashMap<>();
    private static final Map<PropertyLogo, CardFilter> _propertyLogoFilterMap = new HashMap<>();
    private static final Map<Species, CardFilter> _speciesFilterMap = new HashMap<>();
    private static final Map<Affiliation, CardFilter> _affiliationFilterMap = new HashMap<>();
    private static final Map<Uniqueness, CardFilter> _uniquenessFilterMap = new HashMap<>();
    private static final Map<Zone, CardFilter> _zoneFilterMap = new HashMap<>();
    private static final Map<Characteristic, CardFilter> _characteristicFilterMap = new HashMap<>();

    static {
        for (Zone zone : Zone.values())
            _zoneFilterMap.put(zone, zone(zone));
        for (CardType cardType : CardType.values())
            _cardTypeFilterMap.put(cardType, cardType(cardType));
        for (SkillName skillName : SkillName.values())
            _skillNameFilterMap.put(skillName, skillName(skillName));
        for (PropertyLogo propertyLogo : PropertyLogo.values())
            _propertyLogoFilterMap.put(propertyLogo, propertyLogo(propertyLogo));
        for (FacilityType facilityType : FacilityType.values())
            _facilityTypeFilterMap.put(facilityType, facilityType(facilityType));
        for (Affiliation affiliation : Affiliation.values())
            _affiliationFilterMap.put(affiliation, affiliation(affiliation));
        for (Uniqueness uniqueness : Uniqueness.values())
            _uniquenessFilterMap.put(uniqueness, uniqueness(uniqueness));
        for (Species species : Species.values())
            _speciesFilterMap.put(species, species(species));
        for (Characteristic characteristic : Characteristic.values())
            _characteristicFilterMap.put(characteristic, characteristic(characteristic));
    }

    public static Collection<PhysicalCard> filterYourActive(DefaultGame cardGame, String playerName,
                                                            Filterable... filters) {
        return filterActive(cardGame, your(playerName), and(filters));
    }


    public static Collection<PhysicalCard> filterYourActive(DefaultGame cardGame, Player player,
                                                            Filterable... filters) {
        return filterActive(cardGame, your(player.getPlayerId()), and(filters));
    }

    public static Collection<PhysicalCard> filteredCardsYouOwnInPlay(DefaultGame cardGame, Player player,
                                                            Filterable... filters) {
        return filterActive(cardGame, your(player.getPlayerId()), and(filters));
    }

    public static Collection<PhysicalCard> filterYourCardsPresentWith(DefaultGame cardGame,
                                                                      String playerName, PhysicalCard card,
                                                                      Filterable... filters) {
        return filterYourActive(cardGame, playerName, presentWith(card), and(filters));
    }

    public static List<FacilityCard> yourFacilitiesInPlay(DefaultGame cardGame, Player player) {
        List<FacilityCard> result = new LinkedList<>();
        Collection<PhysicalCard> facilities = filterYourActive(cardGame, player, CardType.FACILITY);
        for (PhysicalCard facility : facilities) {
            if (facility instanceof FacilityCard && facility.isInPlay()) {
                result.add((FacilityCard) facility);
            }
        }
        return result;
    }



    public static Collection<PersonnelCard> highestTotalAttributes(Collection<PersonnelCard> personnelCards) {
        List<PersonnelCard> highestCards = new LinkedList<>();
        int highestTotal = 0;
        for (PersonnelCard personnel : personnelCards) {
            if (personnel.getTotalAttributes() > highestTotal) {
                highestCards.clear();
                highestCards.add(personnel);
            } else if (personnel.getTotalAttributes() >= highestTotal) {
                highestCards.add(personnel);
            }
        }
        return highestCards;
    }

    public static Collection<PhysicalCard> filterActive(DefaultGame game, Filterable... filters) {
        CardFilter filter = Filters.and(filters);
        GetCardsMatchingFilterVisitor getCardsMatchingFilter = new GetCardsMatchingFilterVisitor(game, filter);
        game.getGameState().iterateActiveCards(getCardsMatchingFilter);
        return getCardsMatchingFilter.getPhysicalCards();
    }

    public static Collection<PhysicalCard> filterCardsInPlay(DefaultGame game, Filterable... filters) {
        CardFilter filter = Filters.and(filters);
        List<PhysicalCard> cards = game.getGameState().getAllCardsInPlay();
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : cards) {
            if (filter.accepts(game, card))
                result.add(card);
        }
        return result;
    }

    public static Collection<PhysicalCard> filter(DefaultGame cardGame, Iterable<? extends Filterable> filters) {
        CardFilter filter = Filters.and(filters);
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : cardGame.getGameState().getAllCardsInGame()) {
            if (filter.accepts(cardGame, card))
                result.add(card);
        }
        return result;
    }

    public static Collection<PhysicalCard> filter(DefaultGame game, Filterable... filters) {
        CardFilter filter = Filters.and(filters);
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : game.getGameState().getAllCardsInGame()) {
            if (filter.accepts(game, card))
                result.add(card);
        }
        return result;
    }


    public static Collection<PhysicalCard> filter(Iterable<? extends PhysicalCard> cards, DefaultGame game, Filterable... filters) {
        CardFilter filter = Filters.and(filters);
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : cards) {
            if (filter.accepts(game, card))
                result.add(card);
        }
        return result;
    }

    public static Collection<PhysicalCard> filter(Iterable<? extends PhysicalCard> cards, Filterable... filters) {
        CardFilter filter = Filters.and(filters);
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : cards) {
            if (filter.accepts(card.getGame(), card))
                result.add(card);
        }
        return result;
    }


    // Filters available
    public static CardFilter strengthEqual(final Evaluator evaluator) {
        return (game, physicalCard) -> physicalCard.getStrength(game) == (int) evaluator.evaluateExpression(game);
    }

    public static CardFilter moreStrengthThan(final float strength) {
        return (game, physicalCard) -> physicalCard.getStrength(game) > strength;
    }

    public static CardFilter lessStrengthThan(final float strength) {
        return (game, physicalCard) -> physicalCard.getStrength(game) < strength;
    }

    public static CardFilter lessStrengthThan(final PhysicalCard card) {
        return (game, physicalCard) -> physicalCard.getStrength(game) < card.getStrength(game);
    }

    public static CardFilter topOfPlayPile(Player player) {
        return (game, physicalCard) -> {
            if (game instanceof TribblesGame tribblesGame) {
                return tribblesGame.getGameState().getPlayPile(player.getPlayerId()).getLast() == physicalCard;
            } else {
                return false;
            }
        };
    }


    private static CardFilter species(final Species species) {
        return (game, physicalCard) -> physicalCard.getBlueprint().isSpecies(species);
    }

    public static CardFilter inCards(Collection<PhysicalCard> cards) {
        return (game, physicalCard) -> cards.contains(physicalCard);
    }

    public static final CardFilter personnel = Filters.or(CardType.PERSONNEL);
    public static final CardFilter ship = Filters.or(CardType.SHIP);
    public static final CardFilter facility = Filters.or(CardType.FACILITY);
    public static final CardFilter universal = Filters.or(Uniqueness.UNIVERSAL);
    public static final CardFilter android = Filters.or(Species.ANDROID);

    public static final CardFilter hologram = Filters.or(Species.HOLOGRAM);

    public static final CardFilter exposedShip = (game, physicalCard) -> {
        if (physicalCard instanceof PhysicalShipCard shipCard) {
            return shipCard.isExposed();
        } else {
            return false;
        }
    };

    public static final CardFilter controllerControlsMatchingPersonnelAboard = (game, physicalCard) -> {
        if (physicalCard instanceof CardWithHullIntegrity hullCard) {
            Collection<PersonnelCard> cardsAboard = hullCard.getPersonnelAboard();
            for (PersonnelCard personnel : cardsAboard) {
                if (personnel.matchesAffiliationOf(hullCard) && personnel.hasSameControllerAsCard(game, hullCard))
                    return true;
            }
        }
        return false;
    };


    public static CardFilter matchingAffiliation(final PhysicalCard cardToMatch) {
        return (game, physicalCard) -> {
            if (physicalCard instanceof AffiliatedCard affilCard1 && cardToMatch instanceof AffiliatedCard affilCard2) {
                return affilCard1.matchesAffiliationOf(affilCard2);
            } else {
                return false;
            }
        };
    }

    public static CardFilter matchingAffiliation(Collection<PhysicalCard> cardsToMatch) {
        return (game, physicalCard) -> {
            boolean matching = true;
            for (PhysicalCard cardToMatch : cardsToMatch) {
                if (physicalCard instanceof AffiliatedCard affilCard1 && cardToMatch instanceof AffiliatedCard affilCard2) {
                    if (!affilCard1.matchesAffiliationOf(affilCard2))
                        matching = false;
                }
            }
            return matching;
        };
    }

    public static CardFilter dockedAt(FacilityCard facility) {
        return (game, physicalCard) -> physicalCard instanceof PhysicalShipCard shipCard && shipCard.isDockedAt(facility);
    }


    public static CardFilter presentWith(final PhysicalCard card) {
        return (game, physicalCard) -> physicalCard.isPresentWith(card);
    }

    public static CardFilter yourMatchingOutposts(Player player, PhysicalCard card) {
        return Filters.and(your(player.getPlayerId()), FacilityType.OUTPOST, matchingAffiliation(card));
    }

        // TODO - This isn't great logic for "Nor"
    public static final CardFilter Nor =
            (game, physicalCard) -> physicalCard.getTitle().endsWith(" Nor") || physicalCard.getTitle().equals("Nor") ||
                    physicalCard.getTitle().equals("Deep Space 9");
    public static final CardFilter equipment = Filters.or(CardType.EQUIPMENT);
    public static final CardFilter planetLocation = Filters.and(CardType.MISSION, MissionType.PLANET);
    public static CardFilter atLocation(final GameLocation location) {
        return (game, physicalCard) -> physicalCard.getGameLocation() == location;
    }

    public static final CardFilter inPlay = (game, physicalCard) -> physicalCard.isInPlay();
    public static final CardFilter active = (game, physicalCard) -> game.getGameState().isCardInPlayActive(physicalCard);
    public static final CardFilter multiAffiliation = (game, physicalCard) ->
            physicalCard instanceof AffiliatedCard affilCard && affilCard.isMultiAffiliation();

    public static final CardFilter canBeRemovedFromTheGame = (game, physicalCard) -> true;

    public static CardFilter canBeDiscarded(final String performingPlayer, final PhysicalCard source) {
        return (game, physicalCard) -> game.getGameState().getModifiersQuerying().canBeDiscardedFromPlay(performingPlayer, physicalCard, source);
    }

    public static CardFilter yourHand(Player player) {
        return (game, physicalCard) -> player.getCardsInHand().contains(physicalCard);
    }

    public static CardFilter bottomCardsOfDiscard(Player player, int cardCount, Filterable... filterables) {
        return (game, physicalCard) -> {
            List<PhysicalCard> discardCards = player.getCardGroupCards(Zone.DISCARD);
            int cardsIdentified = 0;
            for (int i = discardCards.size() - 1; i >= 0; i--) {
                if (and(filterables).accepts(game, discardCards.get(i)) && cardsIdentified < cardCount) {
                    if (physicalCard == discardCards.get(i)) {
                        return true;
                    }
                    cardsIdentified++;
                }
            }
            return false;
        };
    }



    public static final CardFilter playable = (game, physicalCard) -> physicalCard.canBePlayed(game);

    public static final CardFilter any = (game, physicalCard) -> true;

    public static final CardFilter none = (game, physicalCard) -> false;

    public static final CardFilter unique = (game, physicalCard) ->
            physicalCard.getBlueprint().getUniqueness() == Uniqueness.UNIQUE;

    public static CardFilter any(Characteristic characteristic) {
        return new CharacteristicFilter(characteristic);
    }

    public static CardFilter yourOtherCards(PhysicalCard contextCard, Filterable... filterables) {
        return and(your(contextCard.getControllerName()), and(filterables));
    }


    private static CardFilter affiliation(final Affiliation affiliation) {
        return (game, physicalCard) -> {
            if (physicalCard instanceof PhysicalNounCard1E noun)
                return noun.isAffiliation(affiliation);
            else return false;
        };
    }

    private static CardFilter uniqueness(final Uniqueness uniqueness) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getUniqueness() == uniqueness;
    }


    public static CardFilter owner(final String playerId) {
        return (game, physicalCard) -> physicalCard.getOwnerName() != null && physicalCard.getOwnerName().equals(playerId);
    }

    public static CardFilter controlledBy(final String playerId) {
        return (game, physicalCard) -> physicalCard.isControlledBy(playerId);
    }

    public static CardFilter your(final String playerId) {
                // TODO - Does this track with general usage of "your"
        return or(
                and(inPlay, controlledBy(playerId)),
                and(not(inPlay), owner(playerId))
        );
    }

    public static CardFilter your(final Player player) {
        return your(player.getPlayerId());
    }

    public static CardFilter yoursEvenIfNotInPlay(final String playerId) {
        return or(
                and(inPlay, controlledBy(playerId)),
                and(not(inPlay), owner(playerId))
        );
    }

    public static CardFilter copyOfCard(PhysicalCard copiedCard) {
        return (game, physicalCard) -> physicalCard.isCopyOf(copiedCard);
    }

    public static CardFilter youHaveNoCopiesInPlay(String playerName) {
        return (game, physicalCard) -> filterYourActive(game, playerName, copyOfCard(physicalCard)).isEmpty();
    }


    public static CardFilter hasAttributeMatchingPersonnel(final PersonnelCard cardToMatch) {
        return (game, matchingCard) -> matchingCard instanceof PersonnelCard matchingPersonnel && (
                Objects.equals(matchingPersonnel.getAttribute(CardAttribute.INTEGRITY), cardToMatch.getAttribute(CardAttribute.INTEGRITY)) ||
                Objects.equals(matchingPersonnel.getAttribute(CardAttribute.CUNNING), cardToMatch.getAttribute(CardAttribute.CUNNING)) ||
                Objects.equals(matchingPersonnel.getAttribute(CardAttribute.STRENGTH), cardToMatch.getAttribute(CardAttribute.STRENGTH)));
    }

    public static CardFilter hasStacked(final Filterable... filter) {
        return hasStacked(1, filter);
    }

    public static CardFilter hasStacked(final int count, final Filterable... filter) {
        return (game, physicalCard) -> {
            List<PhysicalCard> physicalCardList = physicalCard.getStackedCards(game);
            if (filter.length == 1 && filter[0] == Filters.any)
                return physicalCardList.size() >= count;
            return (Filters.filter(physicalCardList, game, Filters.and(filter, ownedByCurrentPlayer)).size() >= count);
        };
    }

    public static CardFilter notAll(final Filterable... filters) {
        return (game, physicalCard) -> !Filters.and(filters).accepts(game, physicalCard);
    }

    public static CardFilter notAny(final Filterable... filters) {
        return (game, physicalCard) -> !Filters.or(filters).accepts(game, physicalCard);
    }

    public static CardFilter not(final Filterable filter) {
        return (game, physicalCard) -> !and(filter).accepts(game, physicalCard);
    }

    public static CardFilter other(final PhysicalCard card) {
        return Filters.not(sameCard(card));
    }

    public static CardFilter otherCardPresentWith(final PhysicalCard card) {
        return and(other(card), presentWith(card));
    }

    public static CardFilter sameCard(final PhysicalCard card) {
        final int cardId = card.getCardId();
        return (game, physicalCard) -> (physicalCard.getCardId() == cardId);
    }

    public static CardFilter in(final Collection<? extends PhysicalCard> cards) {
        final Set<Integer> cardIds = new HashSet<>();
        for (PhysicalCard card : cards)
            cardIds.add(card.getCardId());
        return (game, physicalCard) -> cardIds.contains(physicalCard.getCardId());
    }

    public static CardFilter zone(final Zone zone) {
        return (game, physicalCard) -> physicalCard.getZone() == zone;
    }

    public static CardFilter name(final String name) {
            // TODO - Does not consider the colon rule
        return (game, physicalCard) -> physicalCard.getBlueprint().getTitle() != null &&
                physicalCard.getBlueprint().getTitle().equals(name);
    }

    private static CardFilter cardType(final CardType cardType) {
        return (game, physicalCard) -> (physicalCard.getCardType() == cardType);
    }

    private static CardFilter skillName(final SkillName skillName) {
        return (game, physicalCard) -> physicalCard.hasSkill(skillName);
    }

    private static CardFilter propertyLogo(final PropertyLogo propertyLogo) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getPropertyLogo() == propertyLogo);
    }

    private static CardFilter facilityType(final FacilityType facilityType) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getFacilityType() == facilityType);
    }

    public static CardFilter attachedTo(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getAttachedTo() != null && Filters.and(filters).accepts(game, physicalCard.getAttachedTo());
    }

    public static CardFilter stackedOn(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getStackedOn() != null && Filters.and(filters).accepts(game, physicalCard.getStackedOn());
    }

    public static CardFilter skillDotsLessThanOrEqualTo(Integer count) {
        return new SkillDotsLessThanCardFilter(count);
    }

    public static CardFilter specialDownloadIconCount(Integer count) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getSpecialDownloadIconCount() == count;
    }

    private static CardFilter characteristic(final Characteristic characteristic) {
        return (game, physicalCard) -> physicalCard.hasCharacteristic(characteristic);
    }


    public static CardFilter and(final Filterable... filters) {
        CardFilter[] filtersInt = convertToFilters(filters);
        return new AndFilter(filtersInt);
    }

    public static CardFilter and(Iterable<? extends Filterable> filters) {
        List<CardFilter> result = new LinkedList<>();
        for (Filterable filter : filters) {
            result.add(changeToFilter(filter));
        }
        return new AndFilter(result);
    }


    public static CardFilter or(final Filterable... filters) {
        CardFilter[] filtersInt = convertToFilters(filters);
        if (filtersInt.length == 1)
            return filtersInt[0];
        return new OrCardFilter(filtersInt);
    }

    private static CardFilter[] convertToFilters(Filterable... filters) {
        CardFilter[] filtersInt = new CardFilter[filters.length];
        for (int i = 0; i < filtersInt.length; i++)
            filtersInt[i] = changeToFilter(filters[i]);
        return filtersInt;
    }

    private static CardFilter changeToFilter(Filterable filter) {
        return switch (filter) {
            case CardFilter filter1 -> filter1;
            case PhysicalCard card -> Filters.sameCard(card);
            case CardType cardType -> _cardTypeFilterMap.get(cardType);
            case Characteristic characteristic -> _characteristicFilterMap.get(characteristic);
            case SkillName enumFilter -> _skillNameFilterMap.get(enumFilter);
            case CardIcon icon -> (game, physicalCard) -> physicalCard.hasIcon(game, icon);
            case MissionType missionType ->
                // TODO - Does not properly account for dual missions
                    (game, physicalCard) -> physicalCard.getBlueprint().getMissionType() == missionType;
            case FacilityType facilityType -> _facilityTypeFilterMap.get(facilityType);
            case PropertyLogo propertyLogo -> _propertyLogoFilterMap.get(propertyLogo);
            case Species enumFilter -> _speciesFilterMap.get(enumFilter);
            case Uniqueness enumFilter -> _uniquenessFilterMap.get(enumFilter);
            case Affiliation enumFilter -> _affiliationFilterMap.get(enumFilter);
            case Zone zone -> _zoneFilterMap.get(zone);
            case null, default -> throw new IllegalArgumentException("Unknown type of filterable: " + filter);
        };
    }

    public static final CardFilter ownedByCurrentPlayer = (game, physicalCard) -> physicalCard.getOwnerName().equals(game.getGameState().getCurrentPlayerId());

    private static CardFilter andInternal(final CardFilter... filters) {
        return (game, physicalCard) -> {
            for (CardFilter filter : filters) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            return true;
        };
    }

    public static CardFilter and(final Filterable[] filters1, final Filterable... filters2) {
        final CardFilter[] newFilters1 = convertToFilters(filters1);
        final CardFilter[] newFilters2 = convertToFilters(filters2);
        if (newFilters1.length == 1 && newFilters2.length == 0)
            return newFilters1[0];
        if (newFilters1.length == 0 && newFilters2.length == 1)
            return newFilters2[0];
        return (game, physicalCard) -> {
            for (CardFilter filter : newFilters1) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            for (CardFilter filter : newFilters2) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            return true;
        };
    }

    private static CardFilter orInternal(final CardFilter... filters) {
        return (game, physicalCard) -> {
            for (CardFilter filter : filters) {
                if (filter.accepts(game, physicalCard))
                    return true;
            }
            return false;
        };
    }

    public static CardFilter or(final List<CardFilter> filters) {
        return orInternal(filters.toArray(new CardFilter[0]));
    }

    public static CardFilter and(final List<CardFilter> filters) {
        return andInternal(filters.toArray(new CardFilter[0]));
    }


    public static final CardFilter undocked = (game, physicalCard) -> {
        if (physicalCard instanceof PhysicalShipCard shipCard)
            return !shipCard.isDocked();
        else return false;
    };

    public static final CardFilter Klingon = Filters.or(Affiliation.KLINGON, Species.KLINGON);
    public static final CardFilter Romulan = Filters.or(Affiliation.ROMULAN, Species.ROMULAN);
    public static final CardFilter Ferengi = Filters.or(Affiliation.FERENGI, Species.FERENGI);

    public static CardFilter classification(SkillName skillName) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getClassification() == skillName;
    }

    public static Filterable yourCardsPresentWith(Player player, PhysicalCard card) {
        return and(your(player), presentWith(card));
    }

    public static Filterable yourCardsPresentWithThisCard(PhysicalCard thisCard) {
        return and(your(thisCard.getControllerName()), presentWith(thisCard));
    }

    public static CardFilter costAtLeast(int num) {
        return (game, physicalCard) -> physicalCard.getCost() >= num;
    }

    public static CardFilter missionAffiliationIcon(Affiliation affiliation) {
        return (game, physicalCard) -> physicalCard instanceof MissionCard missionCard &&
                missionCard.hasAffiliationIconForOwner(affiliation);
    }

    public static CardFilter missionPointValueAtLeast(int pointValue) {
        return (game, physicalCard) -> physicalCard instanceof MissionCard missionCard &&
                missionCard.getPoints() >= pointValue;
    }

    public static CardFilter yourDiscard(Player player) {
        return (game, physicalCard) -> player.getCardGroupCards(Zone.DISCARD).contains(physicalCard);
    }

    public static CardFilter personnelInAttemptingUnit(AttemptingUnit attemptingUnit) {
        return (game, physicalCard) -> {
            Collection<PersonnelCard> personnel = attemptingUnit.getAttemptingPersonnel();
            return physicalCard instanceof PersonnelCard personnelCard && personnel.contains(personnelCard);
        };
    }

    public static CardFilter encounteringCard(PhysicalCard encounteredCard) {
        return (game, physicalCard) -> {
            Stack<Action> actionStack = game.getActionsEnvironment().getActionStack();
            for (Action action : actionStack) {
                if (action instanceof EncounterSeedCardAction encounterAction &&
                        encounterAction.getEncounteredCard() == encounteredCard) {
                    return encounterAction.getAttemptingUnit().getAttemptingPersonnel().contains(physicalCard);
                }
            }
            return false;
        };
    }

    public static CardFilter inYourDrawDeck(Player performingPlayer) {
        return new CardFilter() {
            @Override
            public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
                return performingPlayer.getCardsInDrawDeck().contains(physicalCard);
            }
        };
    }

    public static CardFilter inYourDrawDeck(String playerName) {
        return new CardFilter() {
            @Override
            public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
                try {
                    Player performingPlayer = game.getPlayer(playerName);
                    return performingPlayer.getCardsInDrawDeck().contains(physicalCard);
                } catch(PlayerNotFoundException exp) {
                    game.sendErrorMessage(exp);
                    return false;
                }
            }
        };
    }

    public static CardFilter cardsYouCanDownload(String performingPlayerName) {
        return inYourDrawDeck(performingPlayerName);
    }

    public static CardFilter cardsYouCanDownload(Player performingPlayer) {
        return inYourDrawDeck(performingPlayer);
    }

    public static CardFilter inYourHand(Player player) {
        return (game, physicalCard) -> player.getCardsInHand().contains(physicalCard);
    }

    public static CardFilter youControlAMatchingOutpost(Player player) {
        return (game, cardToCheck) -> {
            for (PhysicalCard outpostCard : Filters.filterCardsInPlay(game, FacilityType.OUTPOST)) {
                if (outpostCard instanceof FacilityCard outpost && outpost.isControlledBy(player) &&
                        cardToCheck instanceof AffiliatedCard affiliatedCard &&
                        affiliatedCard.matchesAffiliationOf(outpost)) {
                    return true;
                }
            }
            return false;
        };
    }


    private static class GetCardsMatchingFilterVisitor extends CompletePhysicalCardVisitor {
        private final DefaultGame game;
        private final CardFilter _filter;

        private final Set<PhysicalCard> _physicalCards = new HashSet<>();

        private GetCardsMatchingFilterVisitor(DefaultGame game, CardFilter filter) {
            this.game = game;
            _filter = filter;
        }

        @Override
        protected void doVisitPhysicalCard(PhysicalCard physicalCard) {
            if (_filter.accepts(game, physicalCard))
                _physicalCards.add(physicalCard);
        }

        public Set<PhysicalCard> getPhysicalCards() {
            return _physicalCards;
        }
    }
}