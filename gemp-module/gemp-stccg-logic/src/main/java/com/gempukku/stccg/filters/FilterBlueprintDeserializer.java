package com.gempukku.stccg.filters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;

import java.io.IOException;
import java.util.*;

public class FilterBlueprintDeserializer extends StdDeserializer<FilterBlueprint> {

    private final static String OR_WITH_NO_PARENTHESES = "\\s+OR\\s+(?![^\\(]*\\))";
    private final static String AND_WITH_NO_PARENTHESES = "\\s+\\+\\s+(?![^\\(]*\\))";

    private final Map<String, FilterBlueprint> simpleFilters = new HashMap<>();
    private final Map<String, FilterableSourceProducer> parameterFilters = new HashMap<>();
    private final ObjectMapper _mapper = new ObjectMapper();


    public FilterBlueprintDeserializer() throws InvalidCardDefinitionException {
        this(null);
    }

    public FilterBlueprintDeserializer(Class<?> vc) throws InvalidCardDefinitionException {
        super(vc);
        loadSimpleFilters();
        loadParameterFilters();
    }

    private void loadSimpleFilters() throws InvalidCardDefinitionException {
        for (CardIcon value : CardIcon.values())
            appendFilter(value);
        for (CardType value : CardType.values())
            appendFilter(value);
        for (Characteristic value : Characteristic.values())
            appendFilter(value);
        for (Uniqueness value : Uniqueness.values())
            appendFilter(value);
        for (FacilityType value : FacilityType.values())
            appendFilter(value);
        for (PropertyLogo value : PropertyLogo.values())
            appendFilter(value);
        for (SkillName value : SkillName.values())
            appendFilter(value);

        // Affiliations and species
        appendSimpleFilter("android", (cardGame, actionContext) -> Filters.changeToFilter(Species.ANDROID));
        appendSimpleFilter("bajoran", (cardGame, actionContext) -> Filters.Bajoran);
        appendSimpleFilter("borg", (cardGame, actionContext) -> Filters.Borg);
        appendSimpleFilter("cardassian", (cardGame, actionContext) -> Filters.Cardassian);
        appendSimpleFilter("dominion", (cardGame, actionContext) -> Filters.changeToFilter(Affiliation.DOMINION));
        appendSimpleFilter("federation", (cardGame, actionContext) -> Filters.changeToFilter(Affiliation.FEDERATION));
        appendSimpleFilter("hologram", (cardGame, actionContext) -> Filters.hologram);
        appendSimpleFilter("klingon", (cardGame, actionContext) -> Filters.Klingon);
        appendSimpleFilter("romulan", (cardGame, actionContext) -> Filters.Romulan);

        // Other
        appendSimpleFilter("another", (cardGame, actionContext) ->
                Filters.not(Filters.cardId(actionContext.getPerformingCardId())));
        appendSimpleFilter("any", (cardGame, actionContext) -> Filters.any);
        appendSimpleFilter("cardyoucandownload", (cardGame, actionContext) ->
                Filters.cardsYouCanDownload(actionContext.getPerformingPlayerId()));
        appendSimpleFilter("encounteringthiscard", (cardGame, actionContext) ->
                new EncounteringCardFilter(actionContext.getPerformingCardId()));
        appendSimpleFilter("inplay", (cardGame, actionContext) -> Filters.inPlay);
        appendSimpleFilter("inYourHand", (cardGame, actionContext) ->
                new InYourHandFilter(actionContext.getPerformingPlayerId()));
        appendSimpleFilter("inYourDrawDeck", (cardGame, actionContext) ->
                new InYourDrawDeckFilter(actionContext.getPerformingPlayerId()));
        appendSimpleFilter("missionSpecialist", (cardGame, actionContext) -> new MissionSpecialistFilter());
        appendSimpleFilter("notThisCard", (cardGame, actionContext) -> Filters.not(Filters.card(actionContext.card())));
        appendSimpleFilter("onPlanet(missionSeededByYourOpponent)", (cardGame, actionContext) -> {
                String opponentName = cardGame.getOpponent(actionContext.yourName());
                return new OnPlanetMissionFilter(opponentName);
        });
        appendSimpleFilter("self", (cardGame, actionContext) -> Filters.cardId(actionContext.getPerformingCardId()));
        appendSimpleFilter("thisCard", (cardGame, actionContext) -> Filters.cardId(actionContext.getPerformingCardId()));
        appendSimpleFilter("thisCardIsAboard", (cardGame, actionContext) -> new ThisCardIsAboardFilter(actionContext.card()));
        appendSimpleFilter("youControlAMatchingOutpost", (cardGame, actionContext) ->
                new YouControlAMatchingOutpostFilter(actionContext.getPerformingPlayerId()));
        appendSimpleFilter("youOwnNoCopiesInPlay", (cardGame, actionContext) ->
                Filters.youHaveNoCopiesInPlay(actionContext.getPerformingPlayerId()));
        appendSimpleFilter("your", (cardGame, actionContext) -> Filters.your(actionContext.getPerformingPlayerId()));
        appendSimpleFilter("yours", (cardGame, actionContext) -> Filters.your(actionContext.getPerformingPlayerId()));
        appendSimpleFilter("yoursevenifnotinplay", (cardGame, actionContext) ->
                Filters.yoursEvenIfNotInPlay(actionContext.getPerformingPlayerId()));
        appendSimpleFilter("presentWithThisCard", (cardGame, actionContext) ->
                Filters.presentWithThisCard(actionContext.getPerformingCardId()));
    }
    
    private void appendSimpleFilter(String label, FilterBlueprint blueprint) throws InvalidCardDefinitionException {
        String labelToUse = label.toLowerCase();
        if (simpleFilters.get(label) != null) {
            System.out.println(labelToUse);
            throw new InvalidCardDefinitionException("Duplicate filter blueprint label: " + labelToUse);
        }
        simpleFilters.put(labelToUse, blueprint);
    }

    private void loadParameterFilters() {
        parameterFilters.put("and",
                (parameter) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterBlueprint[] filterables = new FilterBlueprint[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = generateFilter(filters[i]);
                    return new AndFilterBlueprint(filterables);
                });
        parameterFilters.put("affiliation", (parameter) -> {
            final Affiliation affiliation = Affiliation.findAffiliation(parameter);
            if (affiliation == null)
                throw new InvalidCardDefinitionException("Unable to find affiliation for: " + parameter);
            return (cardGame, actionContext) -> new AffiliationFilter(affiliation);
        });
        parameterFilters.put("bottomcardsofyourdiscardpile", (parameter) -> {
            final String[] filters = splitIntoFilters(parameter);
            int cardCount = Integer.parseInt(filters[0]);
            FilterBlueprint additionalFilter = parseSTCCGFilter(filters[1]);
            return (cardGame, actionContext) -> new BottomCardsOfDiscardFilter(actionContext.getPerformingPlayerId(),
                    cardCount, additionalFilter.getFilterable(cardGame, actionContext));
        });
        parameterFilters.put("memory",
                (parameter) -> new FilterBlueprint() {
                    @Override
                    public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
                        Collection<Integer> cardIds = actionContext.getCardIdsFromMemory(parameter);
                        List<Integer> cardIdList = cardIds.stream().toList();
                        return new InCardListFilter(cardIdList);
                    }
                });
        parameterFilters.put("name",
                (parameter) -> {
                    String name = Sanitize(parameter);
                    return (cardGame, actionContext) -> new TitleFilter(name);
                });
        parameterFilters.put("not",
                (parameter) -> {
                    final FilterBlueprint filterBlueprint = generateFilter(parameter);
                    return (cardGame, actionContext) -> Filters.not(filterBlueprint.getFilterable(cardGame, actionContext));
                });
        parameterFilters.put("or",
                (parameter) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterBlueprint[] filterables = new FilterBlueprint[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = generateFilter(filters[i]);
                    return new FilterBlueprint() {
                        @Override
                        public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
                            Filterable[] filters1 = new Filterable[filterables.length];
                            for (int i = 0; i < filterables.length; i++)
                                filters1[i] = filterables[i].getFilterable(cardGame, actionContext);

                            return Filters.or(filters1);
                        }
                    };
                });
        parameterFilters.put("title",parameterFilters.get("name"));
        parameterFilters.put("zone",
                (parameter) -> {
                    final Zone zone = _mapper.readValue(parameter, Zone.class);
                    return (cardGame, actionContext) -> Filters.changeToFilter(zone);
                });

    }

    @Override
    public FilterBlueprint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        if (object != null && object.isTextual() && object.textValue() != null) {
            try {
                return parseSTCCGFilter(object.textValue());
            } catch(InvalidCardDefinitionException exp) {
                return generateFilter(object.textValue());
            }
        }
        else throw new InvalidCardDefinitionException("Unable to deserialize filter blueprint");
    }

    private FilterBlueprint parseSTCCGFilter(String value) throws InvalidCardDefinitionException {
//        System.out.println(value); // Very useful for debugging
        if (simpleFilters.get(Sanitize(value)) != null) {
            return simpleFilters.get(Sanitize(value));
        }
        if (value.startsWith("bottomCardsOfYourDiscardPile(") && value.endsWith(")")) {
            String remainingText = value.substring("bottomCardsOfYourDiscardPile(".length(), value.length() - 1);
            final String[] filters = splitIntoFilters(remainingText);
            int cardCount = Integer.parseInt(filters[0]);
            FilterBlueprint additionalFilter = parseSTCCGFilter(filters[1]);
            return (cardGame, actionContext) -> new BottomCardsOfDiscardFilter(actionContext.getPerformingPlayerId(),
                    cardCount, additionalFilter.getFilterable(cardGame, actionContext));
        }
        if (value.split(OR_WITH_NO_PARENTHESES).length > 1)
            return createOrFilter(value);
        if (value.split(AND_WITH_NO_PARENTHESES).length > 1)
            return createAndFilter(value);
        if (value.startsWith("(") && value.endsWith(")")) {
            return parseSTCCGFilter(value.substring(1, value.length() - 1));
        }
        if (value.startsWith("locationName(") && value.endsWith(")")) {
            String locationName = value.substring("locationName(".length(), value.length() - 1);
            return new LocationNameFilterBlueprint(locationName);
        }
        if (value.startsWith("not(") && value.endsWith(")")) {
            FilterBlueprint filterBlueprint = parseSTCCGFilter(value.substring(4, value.length() - 1));
            return (cardGame, actionContext) -> Filters.not(filterBlueprint.getFilterable(cardGame, actionContext));
        }
        if (value.startsWith("name(") && value.endsWith(")")) {
            String cardTitle = value.substring(5, value.length() - 1);
            return new CardTitleFilterBlueprint(cardTitle);
        }
        if (value.startsWith("name=") && !value.contains(" ")) {
            String cardTitle = value.substring("name=".length());
            return new CardTitleFilterBlueprint(cardTitle);
        }
        if (value.startsWith("memoryId=")) {
            String memoryId = value.substring("memoryId=".length());
            return (cardGame, actionContext) -> {
                Collection<Integer> memoryCardIds = actionContext.getCardIdsFromMemory(memoryId);
                return new InCardListFilter(memoryCardIds.stream().toList());
            };
        }
        if (value.startsWith("title(") && value.endsWith(")")) {
            String cardTitle = value.substring(6, value.length() - 1);
            return new CardTitleFilterBlueprint(cardTitle);
        }
        if (value.startsWith("facilityEngineerRequirement(") && value.endsWith(")")) {
            String engineerFilter = value.substring("facilityEngineerRequirement(".length(), value.length() - 1);
            FilterBlueprint engineerBlueprint = parseSTCCGFilter(engineerFilter);
            return new FacilityEngineerRequirementFilterBlueprint(engineerBlueprint);
        }
        if (value.startsWith("highestTotalAttributes(") && value.endsWith(")")) {
            String otherFilterText = value.substring("highestTotalAttributes(".length(), value.length() - 1);
            FilterBlueprint otherFilterBlueprint = parseSTCCGFilter(otherFilterText);
            return (cardGame, actionContext) -> {
                CardFilter otherFilter = otherFilterBlueprint.getFilterable(cardGame, actionContext);
                return new HighestTotalAttributeCardFilter(otherFilter);
            };
        }
        if (value.startsWith("integrity>")) {
            int integrityAmount = Integer.valueOf(value.substring("integrity>".length()));
            return (cardGame, actionContext) -> Filters.integrityGreaterThan(integrityAmount);
        }
        if (value.startsWith("affiliation=")) {
            String affiliationName = value.substring(12);
            Affiliation affiliation = Affiliation.findAffiliation(affiliationName);
            return (cardGame, actionContext) -> Filters.and(affiliation);
        }
        if (value.startsWith("characteristic=")) {
            String characteristicName = value.substring("characteristic=".length());
            Characteristic characteristic = Characteristic.findCharacteristic(characteristicName);
            return (cardGame, actionContext) -> Filters.and(characteristic);
        }
        if (value.startsWith("classification=")) {
            String skillName = value.substring("classification=".length());
            SkillName skill = SkillName.valueOf(skillName.toUpperCase(Locale.ROOT));
            return (cardGame, actionContext) -> new ClassificationFilter(skill);
        }
        if (value.startsWith("skill-dots<=")) {
            String[] stringSplit = value.split("<=");
            int skillDotCount = Integer.parseInt(stringSplit[1]);
            return (cardGame, actionContext) ->
                    new SkillDotFilter(skillDotCount, ComparatorType.LESS_THAN_OR_EQUAL_TO);
        }
        if (value.startsWith("sd-icons=")) {
            String[] stringSplit = value.split("=");
            int iconCount = Integer.parseInt(stringSplit[1]);
            return (cardGame, actionContext) -> new SpecialDownloadIconCountFilter(iconCount, ComparatorType.EQUAL_TO);
        }
        FilterBlueprint result = simpleFilters.get(Sanitize(value));
        if (result == null)
            throw new InvalidCardDefinitionException("Unknown filter: " + value);
        else return result;
    }

    private FilterBlueprint createOrFilter(String value) throws InvalidCardDefinitionException {
        String[] stringSplit = value.split(OR_WITH_NO_PARENTHESES);
        List<FilterBlueprint> filterBlueprints = new LinkedList<>();
        for (String string : stringSplit)
            filterBlueprints.add(parseSTCCGFilter(string));
        return new OrFilterBlueprint(filterBlueprints);
    }

    private FilterBlueprint createAndFilter(String value) throws InvalidCardDefinitionException {
        String[] stringSplit = value.split(AND_WITH_NO_PARENTHESES);
        List<FilterBlueprint> filterBlueprints = new LinkedList<>();
        for (String string : stringSplit)
            filterBlueprints.add(parseSTCCGFilter(string));
        return (cardGame, actionContext) -> {
            List<Filterable> filterables = new LinkedList<>();
            for (FilterBlueprint filterBlueprint : filterBlueprints)
                filterables.add(filterBlueprint.getFilterable(cardGame, actionContext));
            return Filters.and(filterables.toArray(new Filterable[0]));
        };
    }

    private static String Sanitize(String input)
    {
        return input
                .toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }

    private void appendFilter(Filterable value) {
        final String filterName = Sanitize(value.toString());
        final String optionalFilterName = value.toString().toLowerCase().replace("_", "-");
        if (simpleFilters.containsKey(filterName))
            throw new RuntimeException("Duplicate filter name: " + filterName);
        simpleFilters.put(filterName, (cardGame, actionContext) -> Filters.changeToFilter(value));
        if (!optionalFilterName.equals(filterName))
            simpleFilters.put(optionalFilterName, (cardGame, actionContext) -> Filters.changeToFilter(value));
    }

    private FilterBlueprint generateFilter(String value) throws
            InvalidCardDefinitionException {
        try {
            if (value == null)
                throw new InvalidCardDefinitionException("Filter not specified");
            String[] filterStrings = splitIntoFilters(value);
            if (filterStrings.length == 0)
                return (cardGame, actionContext) -> Filters.any;
            if (filterStrings.length == 1)
                return createFilter(filterStrings[0]);

            FilterBlueprint[] filters = new FilterBlueprint[filterStrings.length];
            for (int i = 0; i < filters.length; i++)
                filters[i] = createFilter(filterStrings[i]);
            return (cardGame, actionContext) -> {
                Filterable[] filter = new Filterable[filters.length];
                for (int i = 0; i < filter.length; i++) {
                    filter[i] = filters[i].getFilterable(cardGame, actionContext);
                }

                return Filters.and(filter);
            };
        } catch(JsonProcessingException exp) {
            throw new InvalidCardDefinitionException(exp.getMessage());
        }
    }

    private String[] splitIntoFilters(String value) throws InvalidCardDefinitionException {
        List<String> parts = new LinkedList<>();
        final char[] chars = value.toCharArray();

        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (char ch : chars) {
            if (depth > 0) {
                if (ch == ')')
                    depth--;
                if (ch == '(')
                    depth++;
                sb.append(ch);
            } else {
                if (ch == ',') {
                    parts.add(sb.toString());
                    sb = new StringBuilder();
                } else {
                    if (ch == ')')
                        throw new InvalidCardDefinitionException("Invalid filter definition: " + value);
                    if (ch == '(')
                        depth++;
                    sb.append(ch);
                }
            }
        }

        if (depth != 0)
            throw new InvalidCardDefinitionException("Not matching number of opening and closing brackets: " + value);

        parts.add(sb.toString());

        return parts.toArray(new String[0]);
    }

    private FilterBlueprint createFilter(String filterString) throws InvalidCardDefinitionException, JsonProcessingException {
        if (filterString.contains("(") && filterString.endsWith(")")) {
            String filterName = filterString.substring(0, filterString.indexOf("("));
            String filterParameter =
                    filterString.substring(filterString.indexOf("(") + 1, filterString.lastIndexOf(")"));
            return lookupFilter(Sanitize(filterName), Sanitize(filterParameter));
        }
        return lookupFilter(Sanitize(filterString), null);
    }



    private FilterBlueprint lookupFilter(String name, String parameter)
            throws InvalidCardDefinitionException, JsonProcessingException {
        if (parameter == null) {
            FilterBlueprint result = simpleFilters.get(Sanitize(name));
            if (result != null)
                return result;
        }

        final FilterableSourceProducer filterableSourceProducer = parameterFilters.get(Sanitize(name));
        if (filterableSourceProducer == null)
            throw new InvalidCardDefinitionException("Unable to find filter: " + name);

        return filterableSourceProducer.createFilterableSource(parameter);
    }


}