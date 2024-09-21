package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.CompletePhysicalCardVisitor;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.common.filterable.lotr.*;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1ELocation;

import java.util.*;

public class Filters {
        // TODO - There is almost certainly a cleaner way of implementing this
    private static final Map<CardType, Filter> _cardTypeFilterMap = new HashMap<>();
    private static final Map<SkillName, Filter> _skillNameFilterMap = new HashMap<>();
    private static final Map<FacilityType, Filter> _facilityTypeFilterMap = new HashMap<>();
    private static final Map<PropertyLogo, Filter> _propertyLogoFilterMap = new HashMap<>();
    private static final Map<Species, Filter> _speciesFilterMap = new HashMap<>();
    private static final Map<Affiliation, Filter> _affiliationFilterMap = new HashMap<>();
    private static final Map<Uniqueness, Filter> _uniquenessFilterMap = new HashMap<>();
    private static final Map<Zone, Filter> _zoneFilterMap = new HashMap<>();
    private static final Map<Keyword, Filter> _keywordFilterMap = new HashMap<>();

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
        for (Keyword keyword : Keyword.values())
            _keywordFilterMap.put(keyword, keyword(keyword));
    }

    public static Collection<PhysicalCard> filterYourActive(Player player, Filterable... filters) {
        return filterActive(player.getGame(), Filters.your(player), Filters.and(filters));
    }

    public static Collection<PhysicalCard> filterYourCardsPresentWith(Player player, PhysicalCard card,
                                                                      Filterable... filters) {
        return filterYourActive(player, Filters.presentWith(card), Filters.and(filters));
    }

    public static Collection<FacilityCard> yourActiveFacilities(Player player) {
        Collection<FacilityCard> result = new LinkedList<>();
        Collection<PhysicalCard> facilities = filterYourActive(player, CardType.FACILITY);
        for (PhysicalCard facility : facilities) {
            if (facility instanceof FacilityCard) {
                result.add((FacilityCard) facility);
            }
        }
        return result;
    }

    public static Collection<PhysicalCard> filterActive(DefaultGame game, Filterable... filters) {
        Filter filter = Filters.and(filters);
        GetCardsMatchingFilterVisitor getCardsMatchingFilter = new GetCardsMatchingFilterVisitor(game, filter);
        game.getGameState().iterateActiveCards(getCardsMatchingFilter);
        return getCardsMatchingFilter.getPhysicalCards();
    }

    public static Collection<PhysicalCard> filter(Iterable<? extends PhysicalCard> cards, DefaultGame game, Filterable... filters) {
        Filter filter = Filters.and(filters);
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : cards) {
            if (filter.accepts(game, card))
                result.add(card);
        }
        return result;
    }

    public static Collection<PhysicalCard> filter(Iterable<? extends PhysicalCard> cards, Filterable... filters) {
        Filter filter = Filters.and(filters);
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : cards) {
            if (filter.accepts(card.getGame(), card))
                result.add(card);
        }
        return result;
    }

    public static int countActive(DefaultGame game, Filterable... filters) {
        GetCardsMatchingFilterVisitor matchingFilterVisitor = new GetCardsMatchingFilterVisitor(game, Filters.and(filters));
        game.getGameState().iterateActiveCards(matchingFilterVisitor);
        return matchingFilterVisitor.getCounter();
    }

    // Filters available

    public static Filter conditionFilter(final Filterable defaultFilters, final Condition condition, final Filterable conditionMetFilter) {
        final Filter filter1 = changeToFilter(defaultFilters);
        final Filter filter2 = changeToFilter(conditionMetFilter);
        return (game, physicalCard) -> {
            if (condition.isFulfilled())
                return filter2.accepts(game, physicalCard);
            else
                return filter1.accepts(game, physicalCard);
        };
    }

    public static Filter strengthEqual(final Evaluator evaluator) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(physicalCard) == evaluator.evaluateExpression(game, null);
    }

    public static Filter moreStrengthThan(final int strength) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(physicalCard) > strength;
    }

    public static Filter lessStrengthThan(final int strength) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(physicalCard) < strength;
    }

    public static Filter lessStrengthThan(final PhysicalCard card) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(physicalCard) < game.getModifiersQuerying().getStrength(card);
    }


    private static Filter species(final Species species) {
        return (game, physicalCard) -> {
            return false;
            // TODO - Have not implemented this because there is no card data for species yet
        };
    }


    public static final Filter personnel = Filters.or(CardType.PERSONNEL);
    public static final Filter ship = Filters.or(CardType.SHIP);
    public static final Filter facility = Filters.or(CardType.FACILITY);
    public static final Filter universal = Filters.or(Uniqueness.UNIVERSAL);
    public static final Filter android = Filters.or(Species.ANDROID);
    public static final Filter hologram = Filters.or(Species.HOLOGRAM);
    public static Filter matchingAffiliation(final PhysicalCard cardToMatch) {
        return (game, physicalCard) -> {
            if (cardToMatch.getBlueprint().getAffiliations() == null)
                return false;
            if (physicalCard.getBlueprint().getAffiliations() == null)
                return false;
            List<Affiliation> affiliationsToMatch = new LinkedList<>();
            List<Affiliation> filteredAffiliations = new LinkedList<>();
            if (cardToMatch.getZone().isInPlay())
                affiliationsToMatch.addAll(cardToMatch.getBlueprint().getAffiliations());
            else affiliationsToMatch.add(((PhysicalNounCard1E) cardToMatch).getCurrentAffiliation());
            if (physicalCard.getZone().isInPlay())
                filteredAffiliations.addAll(physicalCard.getBlueprint().getAffiliations());
            else filteredAffiliations.add(((PhysicalNounCard1E) physicalCard).getCurrentAffiliation());
            for (Affiliation matchAffiliation : affiliationsToMatch)
                for (Affiliation filterAffiliation : filteredAffiliations)
                    if (matchAffiliation == filterAffiliation)
                        return true;
            return false;
        };
    }

    public static Filter presentWith(final PhysicalCard card) {
        return (game, physicalCard) -> physicalCard.isPresentWith(card);
    }

    public static Filter yourMatchingOutposts(Player player, PhysicalCard card) {
        return Filters.and(your(player.getPlayerId()), FacilityType.OUTPOST, matchingAffiliation(card));
    }

        // TODO - This isn't great logic for "Nor"
    public static final Filter Nor =
            (game, physicalCard) -> physicalCard.getTitle().endsWith(" Nor") || physicalCard.getTitle().equals("Nor") ||
                    physicalCard.getTitle().equals("Deep Space 9");
    public static final Filter equipment = Filters.or(CardType.EQUIPMENT);
    public static final Filter planetLocation = Filters.and(CardType.MISSION, MissionType.PLANET);
    public static Filter atLocation(final ST1ELocation location) {
        return (game, physicalCard) -> physicalCard.getLocation() == location;
    }

    public static final Filter inPlay = (game, physicalCard) -> physicalCard.getZone().isInPlay();
    public static final Filter active = (game, physicalCard) -> game.getGameState().isCardInPlayActive(physicalCard);

    public static Filter canBeDiscarded(final PhysicalCard source) {
        return (game, physicalCard) -> game.getModifiersQuerying().canBeDiscardedFromPlay(source.getOwnerName(), physicalCard, source);
    }

    public static Filter canBeDiscarded(final String performingPlayer, final PhysicalCard source) {
        return (game, physicalCard) -> game.getModifiersQuerying().canBeDiscardedFromPlay(performingPlayer, physicalCard, source);
    }

    public static final Filter playable = (game, physicalCard) -> physicalCard.canBePlayed();

    public static Filter playable(final DefaultGame game) {
        return playable(game, 0);
    }
    public static Filter playable() { return (game, physicalCard) -> physicalCard.canBePlayed(); }

    public static Filter playable(final DefaultGame game, final int twilightModifier) {
        return playable(game, twilightModifier, false);
    }

    public static Filter playable(final DefaultGame game, final int twilightModifier, final boolean ignoreRoamingPenalty) {
        return playable(twilightModifier, ignoreRoamingPenalty, false);
    }

    public static Filter playable(final int twilightModifier, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile) {
        return playable(0, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, false);
    }

    public static Filter playable(final int twilightModifier, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile, final boolean ignoreResponseEvents) {
        return playable(0, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, ignoreResponseEvents);
    }

    public static Filter playable(final int withTwilightRemoved, final int twilightModifier, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile, final boolean ignoreResponseEvents) {
        return (game1, physicalCard) -> physicalCard.canBePlayed();
    }

    public static final Filter any = (game, physicalCard) -> true;

    public static final Filter none = (game, physicalCard) -> false;

    public static final Filter unique = (game, physicalCard) ->
            physicalCard.getBlueprint().getUniqueness() == Uniqueness.UNIQUE;


    private static Filter affiliation(final Affiliation affiliation) {
        return (game, physicalCard) -> {
            if (physicalCard instanceof PhysicalNounCard1E noun)
                return noun.isAffiliation(affiliation);
            else return false;
        };
    }

    private static Filter uniqueness(final Uniqueness uniqueness) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getUniqueness() == uniqueness;
    }


    public static Filter owner(final String playerId) {
        return (game, physicalCard) -> physicalCard.getOwnerName() != null && physicalCard.getOwnerName().equals(playerId);
    }

    public static Filter controlledBy(final String playerId) {
        return (game, physicalCard) -> physicalCard.isControlledBy(playerId);
    }

    public static Filter your(final String playerId) {
                // TODO - Does this track with general usage of "your"
        return or(
                and(inPlay, controlledBy(playerId)),
                and(not(inPlay), owner(playerId))
        );
    }

    public static Filter your(final Player player) {
        return your(player.getPlayerId());
    }

    public static Filter yoursEvenIfNotInPlay(final String playerId) {
        return or(
                and(inPlay, controlledBy(playerId)),
                and(not(inPlay), owner(playerId))
        );
    }

    public static Filter copyOfCard(PhysicalCard copiedCard) {
        return (game, physicalCard) -> physicalCard.isCopyOf(copiedCard);
    }

    public static Filter youHaveNoCopiesInPlay(final Player player) {
        return (game, physicalCard) -> filterYourActive(player, copyOfCard(physicalCard)).isEmpty();
    }

    public static Filter hasStacked(final Filterable... filter) {
        return hasStacked(1, filter);
    }

    public static Filter hasStacked(final int count, final Filterable... filter) {
        return (game, physicalCard) -> {
            List<PhysicalCard> physicalCardList = physicalCard.getStackedCards();
            if (filter.length == 1 && filter[0] == Filters.any)
                return physicalCardList.size() >= count;
            return (Filters.filter(physicalCardList, game, Filters.and(filter, ownedByCurrentPlayer)).size() >= count);
        };
    }

    public static Filter not(final Filterable... filters) {
        return (game, physicalCard) -> !Filters.and(filters).accepts(game, physicalCard);
    }

    public static Filter other(final PhysicalCard card) {
        return Filters.not(sameCard(card));
    }

    public static Filter otherCardPresentWith(final PhysicalCard card) {
        return and(other(card), presentWith(card));
    }

    public static Filter sameCard(final PhysicalCard card) {
        final int cardId = card.getCardId();
        return (game, physicalCard) -> (physicalCard.getCardId() == cardId);
    }

    public static Filter in(final Collection<? extends PhysicalCard> cards) {
        final Set<Integer> cardIds = new HashSet<>();
        for (PhysicalCard card : cards)
            cardIds.add(card.getCardId());
        return (game, physicalCard) -> cardIds.contains(physicalCard.getCardId());
    }

    public static final Filter onTable = (game, physicalCard) -> physicalCard.getZone() != null && (physicalCard.getZone().isInPlay() || physicalCard.getZone()==Zone.TABLE);

    public static Filter zone(final Zone zone) {
        return (game, physicalCard) -> physicalCard.getZone() == zone;
    }

    public static Filter name(final String name) {
            // TODO - Does not consider the colon rule
        return (game, physicalCard) -> physicalCard.getBlueprint().getTitle() != null &&
                physicalCard.getBlueprint().getTitle().equals(name);
    }

    private static Filter cardType(final CardType cardType) {
        return (game, physicalCard) -> (physicalCard.getCardType() == cardType)
                || game.getModifiersQuerying().isAdditionalCardType(game, physicalCard, cardType);
    }

    private static Filter skillName(final SkillName skillName) {
        return (game, physicalCard) -> physicalCard.hasSkill(skillName);
    }

    private static Filter propertyLogo(final PropertyLogo propertyLogo) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getPropertyLogo() == propertyLogo);
    }

    private static Filter facilityType(final FacilityType facilityType) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getFacilityType() == facilityType);
    }

    public static Filter attachedTo(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getAttachedTo() != null && Filters.and(filters).accepts(game, physicalCard.getAttachedTo());
    }

    public static Filter stackedOn(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getStackedOn() != null && Filters.and(filters).accepts(game, physicalCard.getStackedOn());
    }

    public static Filter skillDotsLessThanOrEqualTo(Integer count) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getSkillDotCount() <= count;
    }

    public static Filter specialDownloadIconCount(Integer count) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getSpecialDownloadIconCount() == count;
    }

    private static Filter keyword(final Keyword keyword) {
        return (game, physicalCard) -> game.getModifiersQuerying().hasKeyword(physicalCard, keyword);
    }

    public static Filter and(final Filterable... filters) {
        Filter[] filtersInt = convertToFilters(filters);
        if (filtersInt.length == 1)
            return filtersInt[0];
        return andInternal(filtersInt);
    }

    public static Filter or(final Filterable... filters) {
        Filter[] filtersInt = convertToFilters(filters);
        if (filtersInt.length == 1)
            return filtersInt[0];
        return orInternal(filtersInt);
    }

    private static Filter[] convertToFilters(Filterable... filters) {
        Filter[] filtersInt = new Filter[filters.length];
        for (int i = 0; i < filtersInt.length; i++)
            filtersInt[i] = changeToFilter(filters[i]);
        return filtersInt;
    }

    private static Filter changeToFilter(Filterable filter) {
        if (filter instanceof Filter)
            return (Filter) filter;
        else if (filter instanceof PhysicalCard)
            return Filters.sameCard((PhysicalCard) filter);
        else if (filter instanceof CardType)
            return _cardTypeFilterMap.get((CardType) filter);
        else if (filter instanceof SkillName enumFilter)
            return _skillNameFilterMap.get(enumFilter);
        else if (filter instanceof CardIcon icon)
            return (game, physicalCard) -> physicalCard.hasIcon(icon);
        else if (filter instanceof Keyword)
            return _keywordFilterMap.get((Keyword) filter);
        else if (filter instanceof MissionType missionType)
                // TODO - Does not properly account for dual missions
            return (game, physicalCard) -> physicalCard.getBlueprint().getMissionType() == missionType;
        else if (filter instanceof FacilityType)
            return _facilityTypeFilterMap.get((FacilityType) filter);
        else if (filter instanceof PropertyLogo)
            return _propertyLogoFilterMap.get((PropertyLogo) filter);
        else if (filter instanceof Species enumFilter)
            return _speciesFilterMap.get(enumFilter);
        else if (filter instanceof Uniqueness enumFilter)
            return _uniquenessFilterMap.get(enumFilter);
        else if (filter instanceof Affiliation enumFilter)
            return _affiliationFilterMap.get(enumFilter);
        else if (filter instanceof Zone)
            return _zoneFilterMap.get((Zone) filter);
        else
            throw new IllegalArgumentException("Unknown type of filterable: " + filter);
    }

    public static final Filter ownedByCurrentPlayer = (game, physicalCard) -> physicalCard.getOwnerName().equals(game.getGameState().getCurrentPlayerId());

    private static Filter andInternal(final Filter... filters) {
        return (game, physicalCard) -> {
            for (Filter filter : filters) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            return true;
        };
    }

    public static Filter and(final Filterable[] filters1, final Filterable... filters2) {
        final Filter[] newFilters1 = convertToFilters(filters1);
        final Filter[] newFilters2 = convertToFilters(filters2);
        if (newFilters1.length == 1 && newFilters2.length == 0)
            return newFilters1[0];
        if (newFilters1.length == 0 && newFilters2.length == 1)
            return newFilters2[0];
        return (game, physicalCard) -> {
            for (Filter filter : newFilters1) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            for (Filter filter : newFilters2) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            return true;
        };
    }

    private static Filter orInternal(final Filter... filters) {
        return (game, physicalCard) -> {
            for (Filter filter : filters) {
                if (filter.accepts(game, physicalCard))
                    return true;
            }
            return false;
        };
    }

    public static Filter or(final List<Filter> filters) {
        return orInternal(filters.toArray(new Filter[0]));
    }

    public static Filter and(final List<Filter> filters) {
        return andInternal(filters.toArray(new Filter[0]));
    }


    public static Filter attachableTo(final DefaultGame game, final Filterable... filters) {
        return attachableTo(game, 0, filters);
    }

    public static Filter attachableTo(final DefaultGame game, final int twilightModifier, final Filterable... filters) {
        return Filters.and(Filters.playable(game, twilightModifier),
                (Filter) (game1, physicalCard) -> {
                    if (physicalCard.getBlueprint().getValidTargetFilter() == null)
                        return false;
                    return physicalCard.canBePlayed();
                });
    }


    public static final Filter undocked = (game, physicalCard) -> {
        if (physicalCard instanceof PhysicalShipCard shipCard)
            return !shipCard.isDocked();
        else return false;
    };

    public static final Filter Klingon = Filters.or(Affiliation.KLINGON, Species.KLINGON);
    public static final Filter Romulan = Filters.or(Affiliation.ROMULAN, Species.ROMULAN);

    public static Filter classification(SkillName skillName) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getClassification() == skillName;
    }

    public static Filterable yourCardsPresentWith(Player player, PhysicalCard card) {
        return and(your(player), presentWith(card));
    }

    private static class FindFirstActiveCardInPlayVisitor implements PhysicalCardVisitor {
        private final DefaultGame game;
        private final Filter _filter;
        private PhysicalCard _card;

        private FindFirstActiveCardInPlayVisitor(DefaultGame game, Filter filter) {
            this.game = game;
            _filter = filter;
        }

        @Override
        public boolean visitPhysicalCard(PhysicalCard physicalCard) {
            if (_filter.accepts(game, physicalCard)) {
                _card = physicalCard;
                return true;
            }
            return false;
        }

        public PhysicalCard getCard() {
            return _card;
        }
    }

    private static class GetCardsMatchingFilterVisitor extends CompletePhysicalCardVisitor {
        private final DefaultGame game;
        private final Filter _filter;

        private final Set<PhysicalCard> _physicalCards = new HashSet<>();

        private GetCardsMatchingFilterVisitor(DefaultGame game, Filter filter) {
            this.game = game;
            _filter = filter;
        }

        @Override
        protected void doVisitPhysicalCard(PhysicalCard physicalCard) {
            if (_filter.accepts(game, physicalCard))
                _physicalCards.add(physicalCard);
        }

        public int getCounter() {
            return _physicalCards.size();
        }

        public Set<PhysicalCard> getPhysicalCards() {
            return _physicalCards;
        }
    }
}
