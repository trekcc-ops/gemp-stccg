package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardItem;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.MultipleComparator;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;

import java.text.Normalizer;
import java.util.*;

public class SortAndFilterCards {

    public static <T extends CardItem> List<T> process(String filter, Iterable<? extends T> items,
                                                       CardBlueprintLibrary cardLibrary, FormatLibrary formatLibrary) {
        if (filter == null)
            filter = "";
        String[] filterParams = filter.split("\\|");
        String[] rarity = getRarityFilter(filterParams);
        String[] sets = getSetFilter(filterParams);
        List<String> words = getWords(filterParams);
        Set<CardType> cardTypes = getEnumFilter(CardType.values(), CardType.class, "cardType", filterParams);
        Set<TribblePower> tribblePowers = getEnumFilter(TribblePower.values(), TribblePower.class,
                "tribblePower", filterParams);
        Set<Affiliation> affiliations = getEnumFilter(Affiliation.values(), Affiliation.class, "affiliation",
                filterParams);

        List<T> result = new ArrayList<>();
        Map<String, CardBlueprint> cardBlueprintMap = new HashMap<>();
        for (T item : items) {
            String blueprintId = item.getBlueprintId();

            if (isPack(blueprintId)) {
                CardBlueprint blueprint = cardBlueprintMap.get(blueprintId);
                if (acceptsFilters(blueprint, cardLibrary, formatLibrary, blueprintId, sets, cardTypes,
                        rarity, affiliations, tribblePowers, words))
                    result.add(item);
            } else {
                try {
                    cardBlueprintMap.put(blueprintId, cardLibrary.getCardBlueprint(blueprintId));
                    CardBlueprint blueprint = cardBlueprintMap.get(blueprintId);
                    if (acceptsFilters(blueprint, cardLibrary, formatLibrary, blueprintId, sets, cardTypes,
                            rarity, affiliations, tribblePowers, words))
                        result.add(item);
                } catch (CardNotFoundException e) {
                    // Ignore the card
                }
            }
        }

        String sort = getSort(filterParams);
        if (sort == null || sort.isEmpty())
            sort = "name";

        MultipleComparator<CardItem> comparators = new MultipleComparator<>();
        for (String oneSort : sort.split(",")) {
            Comparator<CardItem> comparator = getCardItemComparator(oneSort, cardBlueprintMap);
            if (comparator != null)
                comparators.addComparator(new PacksFirstComparator(comparator));
        }

        result.sort(comparators);

        return result;
    }

    private static Comparator<CardItem> getCardItemComparator(String oneSort,
                                                              Map<String, CardBlueprint> blueprintMap) {
        Comparator<CardItem> comparator;
        switch (oneSort) {
            case "strength" -> comparator = new StrengthComparator(blueprintMap);
            case "cardType" -> comparator = new CardTypeComparator(blueprintMap);
            case "name" -> comparator = new NameComparator(blueprintMap);
            case "tribbleValue" -> comparator = new TribblesValueComparator(blueprintMap);
            default -> comparator = null;
        }
        return comparator;
    }

    private static boolean acceptsFilters(CardBlueprint blueprint, CardBlueprintLibrary library,
                                          FormatLibrary formatLibrary, String blueprintId, String[] sets,
                                          Collection<CardType> cardTypes, String[] rarity,
                                          Collection<Affiliation> affiliations, Collection<TribblePower> tribblePowers,
                                          Iterable<String> words) {
        if (sets != null && !isInSets(blueprintId, sets, library, formatLibrary))
            return false;
        if (rarity != null && !Arrays.stream(rarity).toList().contains(blueprint.getRarity()))
            return false;
        if (cardTypes != null && !cardTypes.contains(blueprint.getCardType()))
            return false;
        if (tribblePowers != null && !tribblePowers.contains(blueprint.getTribblePower()))
            return false;
        if (affiliations != null && affiliations.stream().noneMatch(
                affiliation -> blueprint.getAffiliations().contains(affiliation) ||
                        blueprint.getOwnerAffiliationIcons().contains(affiliation)))
            return false;
        return containsAllWords(blueprint, words);
    }

    private static String[] getRarityFilter(String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("rarity:"))
                return filterParam.substring("rarity:".length()).split(",");
        }
        return null;
    }

    private static String[] getSetFilter(String[] filterParams) {
        String setStr = getSetNumber(filterParams);
        String[] sets = null;
        if (setStr != null)
            sets = setStr.split(",");
        return sets;
    }

    private static boolean isInSets(String blueprintId, String[] sets, CardBlueprintLibrary library,
                                    FormatLibrary formatLibrary) {
        for (String setId : sets) {
            GameFormat format = formatLibrary.get(setId);

            if (format != null) {
                String valid = format.validateCard(library, blueprintId);
                return valid == null || valid.isEmpty();
            } else {
                if (blueprintId.startsWith(setId + "_") || library.hasAlternateInSet(blueprintId, setId))
                    return true;
            }
        }

        return false;
    }

    private static String getSetNumber(String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("set:"))
                return filterParam.substring("set:".length());
        }
        return null;
    }

    private static List<String> getWords(String[] filterParams) {
        List<String> result = new LinkedList<>();
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("name:"))
                result.add(replaceSpecialCharacters(filterParam.substring("name:".length()).toLowerCase()));
        }
        return result;
    }

    private static String getSort(String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("sort:"))
                return filterParam.substring("sort:".length());
        }
        return null;
    }

    private static boolean containsAllWords(CardBlueprint blueprint, Iterable<String> words) {
        for (String word : words) {
            if (blueprint == null || !replaceSpecialCharacters(blueprint.getFullName().toLowerCase()).contains(word))
                return false;
        }
        return true;
    }

    public static String replaceSpecialCharacters(CharSequence text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("’", "'")
                .replaceAll("‘", "'")
                .replaceAll("”", "\"")
                .replaceAll("“", "\"")
                .replaceAll("\\p{M}", "");
    }

    private static <T extends Enum<T>> Set<T> getEnumFilter(T[] enumValues, Class<T> enumType, String prefix,
                                                            String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith(prefix + ":")) {
                String values = filterParam.substring((prefix + ":").length());
                if (values.startsWith("-")) {
                    values = values.substring(1);
                    Set<T> cardTypes = new HashSet<>(Arrays.asList(enumValues));
                    for (String v : values.split(",")) {
                        T t = Enum.valueOf(enumType, v);
                        cardTypes.remove(t);
                    }
                    return cardTypes;
                } else {
                    Set<T> cardTypes = new HashSet<>();
                    for (String v : values.split(","))
                        cardTypes.add(Enum.valueOf(enumType, v));
                    return cardTypes;
                }
            }
        }
        return null;
    }

    private static boolean isPack(String blueprintId) {
        return !blueprintId.contains("_");
    }

    private static class PacksFirstComparator implements Comparator<CardItem> {
        private final Comparator<? super CardItem> _cardComparator;

        private PacksFirstComparator(Comparator<? super CardItem> cardComparator) {
            _cardComparator = cardComparator;
        }

        @Override
        public final int compare(CardItem o1, CardItem o2) {
            final boolean pack1 = isPack(o1.getBlueprintId());
            final boolean pack2 = isPack(o2.getBlueprintId());
            if (pack1 && pack2)
                return o1.getBlueprintId().compareTo(o2.getBlueprintId());
            else if (pack1)
                return -1;
            else if (pack2)
                return 1;
            else
                return _cardComparator.compare(o1, o2);
        }
    }

    private static class NameComparator implements Comparator<CardItem> {
        private final Map<String, ? extends CardBlueprint> _cardBlueprintMap;

        private NameComparator(Map<String, ? extends CardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public final int compare(CardItem o1, CardItem o2) {
            String blueprintId1 = o1.getBlueprintId();
            String blueprintId2 = o2.getBlueprintId();
            String cardName1 = _cardBlueprintMap.get(blueprintId1).getFullName();
            String cardName2 = _cardBlueprintMap.get(blueprintId2).getFullName();
            return cardName1.compareTo(cardName2);
        }
    }

    private static class TribblesValueComparator implements Comparator<CardItem> {
        private final Map<String, ? extends CardBlueprint> _cardBlueprintMap;

        private TribblesValueComparator(Map<String, ? extends CardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public final int compare(CardItem o1, CardItem o2) {
            return _cardBlueprintMap.get(o1.getBlueprintId()).getTribbleValue() -
                    _cardBlueprintMap.get(o2.getBlueprintId()).getTribbleValue();
        }
    }

    private static class StrengthComparator implements Comparator<CardItem> {
        private final Map<String, ? extends CardBlueprint> _cardBlueprintMap;

        private StrengthComparator(Map<String, ? extends CardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public final int compare(CardItem o1, CardItem o2) {
            return getStrengthSafely(_cardBlueprintMap.get(o1.getBlueprintId())) -
                    getStrengthSafely(_cardBlueprintMap.get(o2.getBlueprintId()));
        }

        private static int getStrengthSafely(CardBlueprint blueprint) {
            try {
                return blueprint.getStrength();
            } catch (UnsupportedOperationException exp) {
                return Integer.MAX_VALUE;
            }
        }
    }

    private static class CardTypeComparator implements Comparator<CardItem> {
        private final Map<String, ? extends CardBlueprint> _cardBlueprintMap;

        private CardTypeComparator(Map<String, ? extends CardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public final int compare(CardItem o1, CardItem o2) {
            CardType cardType1 = _cardBlueprintMap.get(o1.getBlueprintId()).getCardType();
            CardType cardType2 = _cardBlueprintMap.get(o2.getBlueprintId()).getCardType();
            return cardType1.ordinal() - cardType2.ordinal();
        }
    }

}