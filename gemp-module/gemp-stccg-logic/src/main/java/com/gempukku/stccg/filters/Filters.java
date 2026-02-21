package com.gempukku.stccg.filters;

import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.player.Player;

import java.util.*;

public class Filters {

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
            _zoneFilterMap.put(zone, new InZoneFilter(zone));
        for (CardType cardType : CardType.values())
            _cardTypeFilterMap.put(cardType, new CardTypeFilter(cardType));
        for (SkillName skillName : SkillName.values())
            _skillNameFilterMap.put(skillName, new HasSkillFilter(skillName));
        for (PropertyLogo propertyLogo : PropertyLogo.values())
            _propertyLogoFilterMap.put(propertyLogo, new PropertyLogoFilter(propertyLogo));
        for (FacilityType facilityType : FacilityType.values())
            _facilityTypeFilterMap.put(facilityType, new FacilityTypeFilter(facilityType));
        for (Affiliation affiliation : Affiliation.values())
            _affiliationFilterMap.put(affiliation, new AffiliationFilter(affiliation));
        for (Uniqueness uniqueness : Uniqueness.values())
            _uniquenessFilterMap.put(uniqueness, new UniquenessFilter(uniqueness));
        for (Species species : Species.values())
            _speciesFilterMap.put(species, new SpeciesFilter(species));
        for (Characteristic characteristic : Characteristic.values())
            _characteristicFilterMap.put(characteristic, new CharacteristicFilter(characteristic));
    }




    public static final CardFilter active = new ActiveCardFilter();
    public static final CardFilter any = new AnyCardFilter();
    public static final CardFilter Bajoran = Filters.or(Affiliation.BAJORAN, Species.BAJORAN);
    public static final CardFilter Borg = Filters.or(Affiliation.BORG, Species.BORG);
    public static final CardFilter Cardassian = Filters.or(Affiliation.CARDASSIAN, Species.CARDASSIAN);
    public static final CardFilter controllerControlsMatchingPersonnelAboard =
            new ControllerControlsMatchingPersonnelAboardFilter();
    public static final CardFilter equipment = Filters.or(CardType.EQUIPMENT);
    public static final CardFilter exposedShip = new ExposedShipFilter();
    public static final CardFilter facility = Filters.or(CardType.FACILITY);
    public static final CardFilter Ferengi = Filters.or(Affiliation.FERENGI, Species.FERENGI);
    public static final CardFilter hologram = Filters.changeToFilter(Species.HOLOGRAM);
    public static final CardFilter inPlay = new InPlayFilter();
    public static final CardFilter Klingon = Filters.or(Affiliation.KLINGON, Species.KLINGON);
    public static final CardFilter personnel = Filters.or(CardType.PERSONNEL);
    public static final CardFilter planetLocation =
            Filters.and(Filters.or(CardType.MISSION, CardType.TIME_LOCATION), MissionType.PLANET);
    public static final CardFilter Romulan = Filters.or(Affiliation.ROMULAN, Species.ROMULAN);
    public static final CardFilter ship = Filters.or(CardType.SHIP);
    public static final CardFilter undocked = new UndockedFilter();
    public static final CardFilter unique = Filters.or(Uniqueness.UNIQUE);
    public static final CardFilter universal = Filters.or(Uniqueness.UNIVERSAL);

    public static Collection<PhysicalCard> filterYourCardsInPlay(DefaultGame cardGame, String playerName,
                                                                 Filterable... filters) {
        return filterCardsInPlay(cardGame, your(playerName), and(filters));
    }


    public static Collection<PhysicalCard> filterYourCardsInPlay(DefaultGame cardGame, Player player,
                                                                 Filterable... filters) {
        return filterCardsInPlay(cardGame, your(player.getPlayerId()), and(filters));
    }

    public static Collection<PhysicalCard> filteredCardsYouOwnInPlay(DefaultGame cardGame, Player player,
                                                            Filterable... filters) {
        return filterCardsInPlay(cardGame, your(player.getPlayerId()), and(filters));
    }

    public static Collection<PhysicalCard> filterYourCardsPresentWith(DefaultGame cardGame,
                                                                      String playerName, PhysicalCard card,
                                                                      Filterable... filters) {
        return filterYourCardsInPlay(cardGame, playerName, presentWith(card), and(filters));
    }


    public static List<FacilityCard> yourFacilitiesInPlay(DefaultGame cardGame, String playerName) {
        List<FacilityCard> result = new LinkedList<>();
        Collection<PhysicalCard> facilities = filterYourCardsInPlay(cardGame, playerName, CardType.FACILITY);
        for (PhysicalCard facility : facilities) {
            if (facility instanceof FacilityCard && facility.isInPlay()) {
                result.add((FacilityCard) facility);
            }
        }
        return result;
    }

    public static List<FacilityCard> yourFacilitiesInPlay(DefaultGame cardGame, Player player) {
        List<FacilityCard> result = new LinkedList<>();
        Collection<PhysicalCard> facilities = filterYourCardsInPlay(cardGame, player.getPlayerId(), CardType.FACILITY);
        for (PhysicalCard facility : facilities) {
            if (facility instanceof FacilityCard && facility.isInPlay()) {
                result.add((FacilityCard) facility);
            }
        }
        return result;
    }

    public static Collection<PersonnelCard> highestTotalAttributes(Collection<PersonnelCard> personnelCards,
                                                                   DefaultGame cardGame) {
        List<PersonnelCard> highestCards = new LinkedList<>();
        int highestTotal = 0;
        for (PersonnelCard personnel : personnelCards) {
            int totalAttributes = personnel.getTotalAttributes(cardGame);
            if (totalAttributes > highestTotal) {
                highestCards.clear();
                highestCards.add(personnel);
                highestTotal = totalAttributes;
            } else if (totalAttributes == highestTotal) {
                highestCards.add(personnel);
            }
        }
        return highestCards;
    }

    public static Collection<PhysicalCard> filterCardsInPlay(DefaultGame game, Filterable... filters) {
        CardFilter filter = Filters.and(filters);
        Collection<PhysicalCard> cards = game.getAllCardsInPlay();
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : cards) {
            if (filter.accepts(game, card))
                result.add(card);
        }
        return result;
    }

    public static Collection<PhysicalCard> filterCardsInSeedDeck(String seedDeckOwnerName, DefaultGame game,
                                                                 Filterable... filters) {
        CardFilter filter = Filters.and(filters);
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : game.getGameState().getCardGroup(seedDeckOwnerName, Zone.SEED_DECK).getCards()) {
            if (filter.accepts(game, card))
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


    public static CardFilter presentWith(final PhysicalCard card) {
        return new PresentWithCardFilter(card);
    }

    public static CardFilter atLocation(int locationId) {
        return new AtLocationFilter(locationId);
    }

    public static CardFilter atLocation(final GameLocation location) {
        return new AtLocationFilter(location.getLocationId());
    }

    public static CardFilter yourHand(String playerName) {
        return new InYourHandFilter(playerName);
    }

    public static CardFilter yourHand(Player player) {
        return new InYourHandFilter(player.getPlayerId());
    }


    public static CardFilter yourOtherCards(PhysicalCard contextCard, Filterable... filterables) {
        return and(your(contextCard.getControllerName()), and(filterables));
    }


    public static CardFilter owner(final String playerId) {
        return new OwnedByPlayerFilter(playerId);
    }

    public static CardFilter your(final String playerId) {
        return and(inPlay, new ControlledByPlayerFilter(playerId));
    }

    public static CardFilter your(final Player player) {
        return your(player.getPlayerId());
    }

    public static CardFilter yoursEvenIfNotInPlay(final String playerId) {
        return or(
                and(inPlay, new ControlledByPlayerFilter(playerId)),
                and(not(inPlay), owner(playerId))
        );
    }

    public static CardFilter copyOfCard(PhysicalCard copiedCard) {
        return new CopyOfCardFilter(copiedCard.getBlueprintId());
    }

    public static CardFilter inCards(Collection<? extends PhysicalCard> cards) {
        return new InCardListFilter(cards);
    }

    public static CardFilter youHaveNoCopiesInPlay(String playerName) {
        return new YouOwnNoCopiesInPlayFilter(playerName);
    }


    public static CardFilter hasAttributeMatchingPersonnel(final PersonnelCard cardToMatch) {
        return new MatchingAttributeFilter(cardToMatch);
    }

    public static CardFilter notAll(final CardFilter... filters) {
        return new NotAllFilter(filters);
    }

    public static CardFilter notAny(final Filterable... filters) {
        return new NotAnyFilter(convertToFilters(filters));
    }

    public static CardFilter not(final Filterable filter) {
        return new NotAllFilter(changeToFilter(filter));
    }

    public static CardFilter other(final PhysicalCard card) {
        return Filters.not(new SameCardFilter(card));
    }

    public static CardFilter otherCardPresentWith(final PhysicalCard card) {
        return and(other(card), presentWith(card));
    }

    public static CardFilter name(final String name) {
        return new TitleFilter(name);
    }


    public static CardFilter and(final Filterable... filters) {
        CardFilter[] filtersInt = convertToFilters(filters);
        return new AndFilter(filtersInt);
    }

    public static CardFilter and(Iterable<Filterable> filterables) {
        List<CardFilter> result = new LinkedList<>();
        for (Filterable filter : filterables) {
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

    public static CardFilter changeToFilter(Filterable filter) {
        return switch (filter) {
            case CardFilter filter1 -> filter1;
            case PhysicalCard card -> new SameCardFilter(card);
            case CardType cardType -> _cardTypeFilterMap.get(cardType);
            case Characteristic characteristic -> _characteristicFilterMap.get(characteristic);
            case SkillName enumFilter -> _skillNameFilterMap.get(enumFilter);
            case CardIcon icon -> new HasIconFilter(icon);
            case MissionType missionType -> new MissionTypeFilter(missionType);
            case FacilityType facilityType -> _facilityTypeFilterMap.get(facilityType);
            case PropertyLogo propertyLogo -> _propertyLogoFilterMap.get(propertyLogo);
            case Species enumFilter -> _speciesFilterMap.get(enumFilter);
            case Uniqueness enumFilter -> _uniquenessFilterMap.get(enumFilter);
            case Affiliation enumFilter -> _affiliationFilterMap.get(enumFilter);
            case Zone zone -> _zoneFilterMap.get(zone);
            case null, default -> throw new IllegalArgumentException("Unknown type of filterable: " + filter);
        };
    }



    public static Filterable yourCardsPresentWithThisCard(PhysicalCard thisCard) {
        return and(your(thisCard.getControllerName()), presentWith(thisCard));
    }

    public static CardFilter yourCardsPresentWithThisCard(String playerName, int thisCardId) {
        return and(your(playerName), new PresentWithCardFilter(thisCardId));
    }


    public static CardFilter cardsYouCanDownload(String performingPlayerName) {
        return and(
                or(
                    new InYourDrawDeckFilter(performingPlayerName),
                    new InYourHandFilter(performingPlayerName)
                ),
                new CanEnterPlayFilter(EnterPlayActionType.PLAY)
        );
    }

    public static CardFilter youControlAMatchingOutpost(String yourPlayerName) {
        return new YouControlAMatchingOutpostFilter(yourPlayerName);
    }


    public static CardFilter card(PhysicalCard thisCard) {
        return new SameCardFilter(thisCard);
    }

    public static CardFilter cardId(int cardId) {
        return new SameCardFilter(cardId);
    }

    public static CardFilter presentWithThisCard(int thisCardId) {
        return new PresentWithCardFilter(thisCardId);
    }

    public static CardFilter integrityGreaterThan(int integrityAmount) {
        return new AttributeFilter(CardAttribute.INTEGRITY, ComparatorType.GREATER_THAN, integrityAmount);
    }
}