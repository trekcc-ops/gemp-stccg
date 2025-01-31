package com.gempukku.stccg.filters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;

import java.io.IOException;
import java.util.*;

public class FilterBlueprintDeserializer extends StdDeserializer<FilterBlueprint> {

    private final static String OR_WITH_NO_PARENTHESES = "\\s+OR\\s+(?![^\\(]*\\))";
    private final static String AND_WITH_NO_PARENTHESES = "\\s+\\+\\s+(?![^\\(]*\\))";

    private final Map<String, FilterBlueprint> simpleFilters = new HashMap<>();
    private final ObjectMapper _mapper = new ObjectMapper();


    public FilterBlueprintDeserializer() {
        this(null);
    }

    public FilterBlueprintDeserializer(Class<?> vc) {
        super(vc);
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
        simpleFilters.put("encounteringthiscard", (actionContext) ->
                Filters.encounteringCard(actionContext.getSource()));
        simpleFilters.put("inplay", (actionContext) -> Filters.inPlay);
        simpleFilters.put("nor", (actionContext) -> Filters.Nor);
        simpleFilters.put("self", ActionContext::getSource);
        simpleFilters.put("unique", (actionContext) -> Filters.unique);
        simpleFilters.put("your", (actionContext) -> Filters.your(actionContext.getPerformingPlayerId()));
        simpleFilters.put("yours", (actionContext) -> Filters.your(actionContext.getPerformingPlayerId()));
        simpleFilters.put("yoursevenifnotinplay", (actionContext) -> Filters.yoursEvenIfNotInPlay(actionContext.getPerformingPlayerId()));
        simpleFilters.put("you have no copies in play", (actionContext) ->
                Filters.youHaveNoCopiesInPlay(actionContext.getPerformingPlayer()));
    }

    @Override
    public FilterBlueprint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        if (object != null && object.isTextual() && object.textValue() != null)
            return parseSTCCGFilter(object.textValue());
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

}