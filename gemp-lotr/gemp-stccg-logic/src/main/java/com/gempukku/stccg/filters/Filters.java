package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CompletePhysicalCardVisitor;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalCardVisitor;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.ST1ELocation;

import java.util.*;

public class Filters {
    private static final Map<CardType, Filter> _cardTypeFilterMap = new HashMap<>();
    private static final Map<MissionType, Filter> _missionTypeFilterMap = new HashMap<>();
    private static final Map<PossessionClass, Filter> _possessionClassFilterMap = new HashMap<>();
    private static final Map<Race, Filter> _raceFilterMap = new HashMap<>();
    private static final Map<Zone, Filter> _zoneFilterMap = new HashMap<>();
    private static final Map<Side, Filter> _sideFilterMap = new HashMap<>();
    private static final Map<Culture, Filter> _cultureFilterMap = new HashMap<>();
    private static final Map<Keyword, Filter> _keywordFilterMap = new HashMap<>();

    static {
        for (Culture culture : Culture.values())
            _cultureFilterMap.put(culture, culture(culture));
        for (Side side : Side.values())
            _sideFilterMap.put(side, side(side));
        for (Zone zone : Zone.values())
            _zoneFilterMap.put(zone, zone(zone));
        for (CardType cardType : CardType.values())
            _cardTypeFilterMap.put(cardType, cardType(cardType));
        for (MissionType missionType : MissionType.values())
            _missionTypeFilterMap.put(missionType, missionType(missionType));
        for (Race race : Race.values())
            _raceFilterMap.put(race, race(race));
        for (PossessionClass possessionClass : PossessionClass.values())
            _possessionClassFilterMap.put(possessionClass, possessionClass(possessionClass));
        for (Keyword keyword : Keyword.values())
            _keywordFilterMap.put(keyword, keyword(keyword));

        // Some simple shortcuts for filters
        // Only companions can be rangers
        _keywordFilterMap.put(Keyword.RANGER, Filters.and(CardType.COMPANION, keyword(Keyword.RANGER)));
        // Only allies can be villagers
        _keywordFilterMap.put(Keyword.VILLAGER, Filters.and(CardType.ALLY, keyword(Keyword.VILLAGER)));

        // Minion groups
        _keywordFilterMap.put(Keyword.SOUTHRON, Filters.and(CardType.MINION, keyword(Keyword.SOUTHRON)));
        _keywordFilterMap.put(Keyword.EASTERLING, Filters.and(CardType.MINION, keyword(Keyword.EASTERLING)));
        _keywordFilterMap.put(Keyword.CORSAIR, Filters.and(CardType.MINION, keyword(Keyword.CORSAIR)));
        _keywordFilterMap.put(Keyword.TRACKER, Filters.and(CardType.MINION, keyword(Keyword.TRACKER)));
        _keywordFilterMap.put(Keyword.WARG_RIDER, Filters.and(CardType.MINION, keyword(Keyword.WARG_RIDER)));
        _keywordFilterMap.put(Keyword.BESIEGER, Filters.and(CardType.MINION, keyword(Keyword.BESIEGER)));
    }

    public static boolean canSpot(DefaultGame game, Filterable... filters) {
        return canSpot(game, 1, filters);
    }

    public static boolean canSpot(DefaultGame game, int count, Filterable... filters) {
        return countSpottable(game, filters)>=count;
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

    public static PhysicalCard findFirstActive(DefaultGame game, Filterable... filters) {
        FindFirstActiveCardInPlayVisitor visitor = new FindFirstActiveCardInPlayVisitor(game, Filters.and(filters));
        game.getGameState().iterateActiveCards(visitor);
        return visitor.getCard();
    }

    public static int countSpottable(DefaultGame game, Filterable... filters) {
        GetCardsMatchingFilterVisitor matchingFilterVisitor = new GetCardsMatchingFilterVisitor(game, Filters.and(filters, Filters.spottable));
        game.getGameState().iterateActiveCards(matchingFilterVisitor);
        int result = matchingFilterVisitor.getCounter();
        if (filters.length==1)
            result+=game.getModifiersQuerying().getSpotBonus(game, filters[0]);
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
            if (condition.isFulfilled(game))
                return filter2.accepts(game, physicalCard);
            else
                return filter1.accepts(game, physicalCard);
        };
    }

    public static Filter maxResistance(final int resistance) {
        return (game, physicalCard) -> game.getModifiersQuerying().getResistance(game, physicalCard) <= resistance;
    }

    public static Filter minResistance(final int resistance) {
        return (game, physicalCard) -> game.getModifiersQuerying().getResistance(game, physicalCard) >= resistance;
    }

    public static Filter strengthEqual(final Evaluator evaluator) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) == evaluator.evaluateExpression(game, null);
    }

    public static Filter moreStrengthThan(final int strength) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) > strength;
    }

    public static Filter lessStrengthThan(final int strength) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) < strength;
    }

    public static Filter lessStrengthThan(final PhysicalCard card) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) < game.getModifiersQuerying().getStrength(game, card);
    }


    private static Filter possessionClass(final PossessionClass possessionClass) {
        return (game, physicalCard) -> {
            final Set<PossessionClass> possessionClasses = physicalCard.getBlueprint().getPossessionClasses();
            return possessionClasses != null && possessionClasses.contains(possessionClass);
        };
    }

    public static Filter maxPrintedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() <= printedTwilightCost;
    }

    public static Filter minPrintedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() >= printedTwilightCost;
    }

    public static Filter notPreventedByEffectToAssign(final Side assignedBySide, final PhysicalCard againstCard) {
        return (game, physicalCard) -> {
            if (againstCard.getBlueprint().getSide() == Side.FREE_PEOPLE) {
                Map<PhysicalCard, Set<PhysicalCard>> assignment = new HashMap<>();
                assignment.put(againstCard, Collections.singleton(physicalCard));
                return game.getModifiersQuerying().isValidAssignments(game, assignedBySide, assignment);
            } else {
                Map<PhysicalCard, Set<PhysicalCard>> assignment = new HashMap<>();
                assignment.put(physicalCard, Collections.singleton(againstCard));
                return game.getModifiersQuerying().isValidAssignments(game, assignedBySide, assignment);
            }
        };
    }

    public static final Filter aragorn = Filters.name("Aragorn");
    public static final Filter gandalf = Filters.name("Gandalf");
    public static final Filter weapon = Filters.or(PossessionClass.HAND_WEAPON, PossessionClass.RANGED_WEAPON);
    public static final Filter item = Filters.or(CardType.ARTIFACT, CardType.POSSESSION);
    public static final Filter character = Filters.or(CardType.ALLY, CardType.COMPANION, CardType.MINION);
    public static final Filter personnel = Filters.or(CardType.PERSONNEL);
    public static final Filter ship = Filters.or(CardType.SHIP);
    public static final Filter facility = Filters.or(CardType.FACILITY);
    public static final Filter equipment = Filters.or(CardType.EQUIPMENT);
    public static final Filter planetLocation = Filters.and(CardType.MISSION, MissionType.PLANET);
    public static final Filter atLocation(final ST1ELocation location) {
        return (game, physicalCard) -> physicalCard.getCurrentLocation() == location;
    }

    public static final Filter inPlay = (game, physicalCard) -> physicalCard.getZone().isInPlay();

    public static final Filter active = (game, physicalCard) -> game.getGameState().isCardInPlayActive(physicalCard);

    public static Filter canBeDiscarded(final PhysicalCard source) {
        return (game, physicalCard) -> game.getModifiersQuerying().canBeDiscardedFromPlay(game, source.getOwner(), physicalCard, source);
    }

    public static Filter canBeDiscarded(final String performingPlayer, final PhysicalCard source) {
        return (game, physicalCard) -> game.getModifiersQuerying().canBeDiscardedFromPlay(game, performingPlayer, physicalCard, source);
    }

    public static Filter playable(final DefaultGame game) {
        return playable(game, 0);
    }

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
        return (game1, physicalCard) -> {
            Side expectedSide = (physicalCard.getOwner().equals(game1.getGameState().getCurrentPlayerId()) ? Side.FREE_PEOPLE : Side.SHADOW);
            final CardBlueprint blueprint = physicalCard.getBlueprint();
            if (blueprint.getSide() != expectedSide)
                return false;
            return game1.checkPlayRequirements(physicalCard);
        };
    }

    public static final Filter any = (game, physicalCard) -> true;

    public static final Filter none = (game, physicalCard) -> false;

    public static final Filter unique = (game, physicalCard) ->
            physicalCard.getBlueprint().getUniqueness() == Uniqueness.UNIQUE;

    private static Filter race(final Race race) {
        return Filters.and(
                Filters.or(CardType.COMPANION, CardType.ALLY, CardType.MINION, CardType.FOLLOWER),
                (Filter) (game, physicalCard) -> {
                    CardBlueprint blueprint = physicalCard.getBlueprint();
                    return blueprint.getRace() == race;
                });
    }


    private static Filter side(final Side side) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getSide() == side;
    }

    public static Filter owner(final String playerId) {
        return (game, physicalCard) -> physicalCard.getOwner() != null && physicalCard.getOwner().equals(playerId);
    }

    public static Filter your(final String playerId) {
        return (game, physicalCard) -> physicalCard.isControlledBy(playerId);
    }

    public static Filter hasAttached(final Filterable... filters) {
        return hasAttached(1, filters);
    }

    public static Filter hasAttached(int count, final Filterable... filters) {
        return (game, physicalCard) -> {
            List<PhysicalCard> physicalCardList = game.getGameState().getAttachedCards(physicalCard);
            return (Filters.filter(physicalCardList, game, filters).size() >= count);
        };
    }

    public static Filter hasStacked(final Filterable... filter) {
        return hasStacked(1, filter);
    }

    public static Filter hasStacked(final int count, final Filterable... filter) {
        return (game, physicalCard) -> {
            List<PhysicalCard> physicalCardList = game.getGameState().getStackedCards(physicalCard);
            if (filter.length == 1 && filter[0] == Filters.any)
                return physicalCardList.size() >= count;
            return (Filters.filter(physicalCardList, game, Filters.and(filter, activeSide)).size() >= count);
        };
    }

    public static Filter not(final Filterable... filters) {
        return (game, physicalCard) -> !Filters.and(filters).accepts(game, physicalCard);
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
        return (game, physicalCard) -> name != null && physicalCard.getBlueprint().getTitle() != null && physicalCard.getBlueprint().getTitle().equals(name);
    }

    private static Filter cardType(final CardType cardType) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getCardType() == cardType)
                || game.getModifiersQuerying().isAdditionalCardType(game, physicalCard, cardType);
    }

    private static Filter missionType(final MissionType missionType) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getMissionType() == missionType);
    }

    public static Filter attachedTo(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getAttachedTo() != null && Filters.and(filters).accepts(game, physicalCard.getAttachedTo());
    }

    public static Filter stackedOn(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getStackedOn() != null && Filters.and(filters).accepts(game, physicalCard.getStackedOn());
    }


    private static Filter culture(final Culture culture) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getCulture() == culture);
    }

    private static Filter keyword(final Keyword keyword) {
        return (game, physicalCard) -> game.getModifiersQuerying().hasKeyword(game, physicalCard, keyword);
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
        else if (filter instanceof Culture)
            return _cultureFilterMap.get((Culture) filter);
        else if (filter instanceof Keyword)
            return _keywordFilterMap.get((Keyword) filter);
        else if (filter instanceof MissionType)
            return _missionTypeFilterMap.get((MissionType) filter);
        else if (filter instanceof PossessionClass)
            return _possessionClassFilterMap.get((PossessionClass) filter);
        else if (filter instanceof Race)
            return _raceFilterMap.get((Race) filter);
        else if (filter instanceof Side)
            return _sideFilterMap.get((Side) filter);
        else if (filter instanceof Zone)
            return _zoneFilterMap.get((Zone) filter);
        else
            throw new IllegalArgumentException("Unknown type of filterable: " + filter);
    }

    public static final Filter activeSide = (game, physicalCard) -> {
        boolean shadow = physicalCard.getBlueprint().getSide() == Side.SHADOW;
        if (shadow)
            return !physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId());
        else
            return physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId());
    };

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

    public static final Filter ringBoundCompanion = Filters.and(CardType.COMPANION, Keyword.RING_BOUND);
    public static final Filter unboundCompanion = Filters.and(CardType.COMPANION, Filters.not(Keyword.RING_BOUND));
    public static final Filter roamingMinion = Filters.and(CardType.MINION, Keyword.ROAMING);
    public static final Filter mounted = Filters.or(Filters.hasAttached(PossessionClass.MOUNT), Keyword.MOUNTED);

    public static final Filter spottable = (game, physicalCard) -> true;

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
