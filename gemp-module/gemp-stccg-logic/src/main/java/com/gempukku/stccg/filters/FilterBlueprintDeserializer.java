package com.gempukku.stccg.filters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.SingleMemoryEvaluator;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.game.DefaultGame;

import java.io.IOException;
import java.util.*;

public class FilterBlueprintDeserializer extends StdDeserializer<FilterBlueprint> {

    private final static String OR_WITH_NO_PARENTHESES = "\\s+OR\\s+(?![^\\(]*\\))";
    private final static String AND_WITH_NO_PARENTHESES = "\\s+\\+\\s+(?![^\\(]*\\))";

    private final Map<String, FilterBlueprint> simpleFilters = new HashMap<>();
    private final Map<String, FilterableSourceProducer> parameterFilters = new HashMap<>();
    private final ObjectMapper _mapper = new ObjectMapper();


    public FilterBlueprintDeserializer() {
        this(null);
    }

    public FilterBlueprintDeserializer(Class<?> vc) {
        super(vc);
        loadSimpleFilters();
        loadParameterFilters();
    }

    private void loadSimpleFilters() {
        for (CardIcon value : CardIcon.values())
            appendFilter(value);
        for (CardType value : CardType.values())
            appendFilter(value);
        for (Affiliation value : Affiliation.values())
            appendFilter(value);
        for (Uniqueness value : Uniqueness.values())
            appendFilter(value);
        for (FacilityType value : FacilityType.values())
            appendFilter(value);
        for (PropertyLogo value : PropertyLogo.values())
            appendFilter(value);

        simpleFilters.put("another", (actionContext) -> Filters.not(actionContext.getSource()));
        simpleFilters.put("any", (actionContext) -> Filters.any);
        simpleFilters.put("cardyoucandownload", (actionContext) ->
                Filters.cardsYouCanDownload(actionContext.getPerformingPlayer()));
        simpleFilters.put("encounteringthiscard", (actionContext) ->
                Filters.encounteringCard(actionContext.getSource()));
        simpleFilters.put("inplay", (actionContext) -> Filters.inPlay);
        simpleFilters.put("inyourdrawdeck", (actionContext) ->
                Filters.inYourDrawDeck(actionContext.getPerformingPlayer()));
        simpleFilters.put("nor", (actionContext) -> Filters.Nor);
        simpleFilters.put("self", ActionContext::getSource);
        simpleFilters.put("unique", (actionContext) -> Filters.unique);
        simpleFilters.put("your", (actionContext) -> Filters.your(actionContext.getPerformingPlayerId()));
        simpleFilters.put("yours", (actionContext) -> Filters.your(actionContext.getPerformingPlayerId()));
        simpleFilters.put("yoursevenifnotinplay", (actionContext) ->
                Filters.yoursEvenIfNotInPlay(actionContext.getPerformingPlayerId()));
        simpleFilters.put("you have no copies in play", (actionContext) ->
                Filters.youHaveNoCopiesInPlay(actionContext.getPerformingPlayer()));
        simpleFilters.put("yourcardspresentwiththiscard", (actionContext) -> Filters.yourCardsPresentWithThisCard(actionContext.getSource()));
    }

    private void loadParameterFilters() {
        parameterFilters.put("and",
                (parameter) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterBlueprint[] filterables = new FilterBlueprint[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = generateFilter(filters[i]);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.and(filters1);
                    };
                });
        parameterFilters.put("attachedto",
                (parameter) -> {
                    final FilterBlueprint filterBlueprint = generateFilter(parameter);
                    return (actionContext) -> Filters.attachedTo(filterBlueprint.getFilterable(actionContext));
                });
        parameterFilters.put("affiliation", (parameter) -> {
            final Affiliation affiliation = Affiliation.findAffiliation(parameter);
            if (affiliation == null)
                throw new InvalidCardDefinitionException("Unable to find affiliation for: " + parameter);
            return (actionContext) -> affiliation;
        });
        parameterFilters.put("hasstacked",
                (parameter) -> {
                    final FilterBlueprint filterBlueprint = generateFilter(parameter);
                    return (actionContext) -> Filters.hasStacked(filterBlueprint.getFilterable(actionContext));
                });
        parameterFilters.put("hasstackedcount",
                (parameter) -> {
                    String[] parameterSplit = parameter.split(",", 2);
                    int count = Integer.parseInt(parameterSplit[0]);
                    final FilterBlueprint filterBlueprint = generateFilter(parameterSplit[1]);
                    return (actionContext) -> Filters.hasStacked(count, filterBlueprint.getFilterable(actionContext));
                });
        parameterFilters.put("loweststrength",
                (parameter) -> {
                    final FilterBlueprint filterBlueprint = generateFilter(parameter);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterBlueprint.getFilterable(actionContext);
                        return Filters.and(
                                sourceFilterable, Filters.strengthEqual(
                                        new SingleMemoryEvaluator(actionContext,
                                                new Evaluator() {
                                                    @Override
                                                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                                        int minStrength = Integer.MAX_VALUE;
                                                        for (PhysicalCard card : Filters.filterActive(cardAffected.getGame(), sourceFilterable))
                                                            minStrength = Math.min(minStrength, cardAffected.getGame().getGameState().getModifiersQuerying().getStrength(card));
                                                        return minStrength;
                                                    }
                                                }
                                        )
                                )
                        );
                    };
                });
        parameterFilters.put("memory",
                (parameter) -> (actionContext) -> Filters.in(actionContext.getCardsFromMemory(parameter)));

        parameterFilters.put("name",
                (parameter) -> {
                    String name = Sanitize(parameter);
                    return (actionContext) -> (CardFilter)
                            (game, physicalCard) -> physicalCard.getBlueprint().getTitle() != null && name.equals(Sanitize(physicalCard.getBlueprint().getTitle()));
                });
        parameterFilters.put("namefrommemory",
                (parameter) -> actionContext -> {
                    Set<String> titles = new HashSet<>();
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(parameter))
                        titles.add(physicalCard.getBlueprint().getTitle());
                    return (CardFilter) (game, physicalCard) -> titles.contains(physicalCard.getBlueprint().getTitle());
                });
        parameterFilters.put("nameinstackedon",
                (parameter) -> {
                    final FilterBlueprint filterBlueprint = generateFilter(parameter);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterBlueprint.getFilterable(actionContext);
                        return (CardFilter) (game, physicalCard) -> {
                            for (PhysicalCard cardWithStack : Filters.filterActive(game, sourceFilterable)) {
                                for (PhysicalCard stackedCard : cardWithStack.getStackedCards(game)) {
                                    if (stackedCard.getBlueprint().getTitle().equals(physicalCard.getBlueprint().getTitle()))
                                        return true;
                                }
                            }
                            return false;
                        };
                    };
                });
        parameterFilters.put("not",
                (parameter) -> {
                    final FilterBlueprint filterBlueprint = generateFilter(parameter);
                    return (actionContext) -> Filters.not(filterBlueprint.getFilterable(actionContext));
                });
        parameterFilters.put("or",
                (parameter) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterBlueprint[] filterables = new FilterBlueprint[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = generateFilter(filters[i]);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.or(filters1);
                    };
                });
        parameterFilters.put("strengthlessthan",
                (parameter) -> {
                    final ValueSource valueSource = _mapper.readValue(parameter, ValueSource.class);

                    return (actionContext) -> {
                        int amount = valueSource.evaluateExpression(actionContext, null);
                        return Filters.lessStrengthThan(amount);
                    };
                });
        parameterFilters.put("strengthmorethan",
                new FilterableSourceProducer() {
                    @Override
                    public FilterBlueprint createFilterableSource(String parameter) throws InvalidCardDefinitionException, JsonProcessingException {
                        final ValueSource valueSource = _mapper.readValue(parameter, ValueSource.class);

                        return (actionContext) -> {
                            int amount = valueSource.evaluateExpression(actionContext, null);
                            return Filters.moreStrengthThan(amount);
                        };
                    }
                });
        parameterFilters.put("title",parameterFilters.get("name"));
        parameterFilters.put("zone",
                (parameter) -> {
                    final Zone zone = _mapper.readValue(parameter, Zone.class);
                    return actionContext -> zone;
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
        if (value.split(OR_WITH_NO_PARENTHESES).length > 1)
            return createOrFilter(value);
        if (value.split(AND_WITH_NO_PARENTHESES).length > 1)
            return createAndFilter(value);
        if (value.startsWith("(") && value.endsWith(")")) {
            return parseSTCCGFilter(value.substring(1, value.length() - 1));
        }
        if (value.startsWith("not(") && value.endsWith(")")) {
            FilterBlueprint filterBlueprint = parseSTCCGFilter(value.substring(4, value.length() - 1));
            return (actionContext) -> Filters.not(filterBlueprint.getFilterable(actionContext));
        }
        if (value.startsWith("name(") && value.endsWith(")")) {
            return (actionContext) -> Filters.name(value.substring(5, value.length() - 1));
        }
        if (value.startsWith("affiliation=")) {
            String affiliationName = value.substring(12);
            Affiliation affiliation = Affiliation.findAffiliation(affiliationName);
            return (actionContext) -> Filters.and(affiliation);
        }
        if (value.startsWith("classification=")) {
            String skillName = value.substring("classification=".length());
            SkillName skill = SkillName.valueOf(skillName.toUpperCase(Locale.ROOT));
            return (actionContext) -> Filters.classification(skill);
        }
        if (value.startsWith("skill-dots<=")) {
            String[] stringSplit = value.split("<=");
            return (actionContext) -> Filters.skillDotsLessThanOrEqualTo(Integer.parseInt(stringSplit[1]));
        }
        if (value.startsWith("sd-icons=")) {
            String[] stringSplit = value.split("=");
            return (actionContext) -> Filters.specialDownloadIconCount(Integer.parseInt(stringSplit[1]));
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
        return (actionContext) -> {
            List<Filterable> filterables = new LinkedList<>();
            for (FilterBlueprint filterBlueprint : filterBlueprints)
                filterables.add(filterBlueprint.getFilterable(actionContext));
            return Filters.or(filterables.toArray(new Filterable[0]));
        };
    }

    private FilterBlueprint createAndFilter(String value) throws InvalidCardDefinitionException {
        String[] stringSplit = value.split(AND_WITH_NO_PARENTHESES);
        List<FilterBlueprint> filterBlueprints = new LinkedList<>();
        for (String string : stringSplit)
            filterBlueprints.add(parseSTCCGFilter(string));
        return (actionContext) -> {
            List<Filterable> filterables = new LinkedList<>();
            for (FilterBlueprint filterBlueprint : filterBlueprints)
                filterables.add(filterBlueprint.getFilterable(actionContext));
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
        simpleFilters.put(filterName, (actionContext) -> value);
        if (!optionalFilterName.equals(filterName))
            simpleFilters.put(optionalFilterName, (actionContext -> value));
    }

    private FilterBlueprint generateFilter(String value) throws
            InvalidCardDefinitionException {
        try {
            if (value == null)
                throw new InvalidCardDefinitionException("Filter not specified");
            String[] filterStrings = splitIntoFilters(value);
            if (filterStrings.length == 0)
                return (actionContext) -> Filters.any;
            if (filterStrings.length == 1)
                return createFilter(filterStrings[0]);

            FilterBlueprint[] filters = new FilterBlueprint[filterStrings.length];
            for (int i = 0; i < filters.length; i++)
                filters[i] = createFilter(filterStrings[i]);
            return (actionContext) -> {
                Filterable[] filter = new Filterable[filters.length];
                for (int i = 0; i < filter.length; i++) {
                    filter[i] = filters[i].getFilterable(actionContext);
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