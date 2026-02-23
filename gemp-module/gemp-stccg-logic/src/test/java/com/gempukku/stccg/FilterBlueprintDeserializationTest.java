package com.gempukku.stccg;

import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.filters.*;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import java.util.*;

public class FilterBlueprintDeserializationTest {
//    private final static String OR_WITH_NO_PARENTHESES = "\\s+OR\\s+(?![^\\(]*\\))";
//    private final static String AND_WITH_NO_PARENTHESES = "\\s+\\+\\s+(?![^\\(]*\\))";
    private final static String OR_WITH_NO_PARENTHESES = "\\sOR\\s";
    private final static String AND_WITH_NO_PARENTHESES = "\\s\\+\\s";
    private final FilterBlueprintDeserializer _currentDeserializer = new FilterBlueprintDeserializer();

    private final Map<String, FilterBlueprint> simpleFilters;
    private final Map<String, FilterableSourceProducer> parameterFilters;

    public FilterBlueprintDeserializationTest() throws InvalidCardDefinitionException {
        simpleFilters = _currentDeserializer.getSimpleFilters();
        parameterFilters = _currentDeserializer.loadParameterFilters();
    }

    @Test
    public void deserializeTest() throws Exception {
//        FilterBlueprint blueprint = parseSTCCGFilter("personnel + (Empathy OR Diplomacy OR name=Morn OR characteristic=Scotty) + presentWithThisCard");
        String sampleString = "personnel + (Empathy OR Diplomacy OR name=Morn OR characteristic(Scotty)) + name(Jean-Luc Picard) + presentWithThisCard";
//        String sampleString = "personnel + (Empathy OR (Diplomacy + SCIENCE)) + other stuff";
        FilterBlueprint blueprint = createFilterBlueprint(sampleString);
        int x = 5;
    }

    private FilterBlueprint createFilterBlueprint(String initialText) throws InvalidCardDefinitionException {
        System.out.println("Calling createFilterBlueprint for '" + initialText + "'");
        Map<String, List<String>> result = breakOutParentheticals(initialText);
        String newString = Iterables.getOnlyElement(result.keySet());
        List<String> subStrings = Iterables.getOnlyElement(result.values());
        if (newString.split(OR_WITH_NO_PARENTHESES).length > 1) {
            Collection<FilterBlueprint> filters = new ArrayList<>();
            for (String filter : newString.split(OR_WITH_NO_PARENTHESES)) {
                String textToUse = filter;
                if (filter.startsWith("{") && filter.endsWith("}")) {
                    int indexNum = Integer.parseInt(filter.replace("{", "").replace("}",""));
                    textToUse = subStrings.get(indexNum);
                    System.out.println("Replacing " + filter + " with '" + textToUse + "'");
                }
                filters.add(createFilterBlueprint(textToUse));
            }
            return new OrFilterBlueprint(filters);
        } else if (newString.split(AND_WITH_NO_PARENTHESES).length > 1) {
            Collection<FilterBlueprint> filters = new ArrayList<>();
            for (String filter : newString.split(AND_WITH_NO_PARENTHESES)) {
                String textToUse = filter;
                if (filter.startsWith("{") && filter.endsWith("}")) {
                    int indexNum = Integer.parseInt(filter.replace("{", "").replace("}",""));
                    textToUse = subStrings.get(indexNum);
                }
                filters.add(createFilterBlueprint(textToUse));
            }
            return new AndFilterBlueprint(filters);
        } else if (isParameterizedFilter(newString)) {
            System.out.println("Sending '" + newString + "' to deserializer");
            return createParameterizedFilter(newString);
        } else {
            System.out.println("Sending '" + newString + "' to deserializer");
            return _currentDeserializer.parseSTCCGFilter(newString);
        }
    }

    private boolean isParameterizedFilter(String text) {
        return (text.indexOf("(") > text.indexOf(" ") && text.endsWith(")")) ||
                (!text.contains(" ") && text.contains("="));
    }

    private FilterBlueprint createParameterizedFilter(String text) throws InvalidCardDefinitionException {
        String type = "";
        String parameter = "";
        if (!text.contains(" ") && text.contains("=")) {
            String[] stringSplit = text.split("=");
            type = stringSplit[0];
            parameter = stringSplit[1];
        } else if (text.indexOf("(") > text.indexOf(" ") && text.endsWith(")")) {
            type = text.substring(0, text.indexOf("("));
            parameter = text.substring(text.indexOf("(") + 1, text.length() - 1);
        }
        return new FilterBlueprintDeserializer().parseSTCCGFilter(text);
    }

    private Map<String, List<String>> breakOutParentheticals(String fullString) {
        System.out.println("Calling breakOutParentheticals for '" + fullString + "'");
        Integer openParenIndex = null;
        Integer closingParenIndex = null;
        int openParensFound = 0;
        int closingParensFound = 0;
        List<String> parentheticals = new ArrayList<>();

        String newString = fullString;
        String initialString;
        int loopsCompleted = 0;

        do {
            initialString = newString;

            for (int i = 0; i < initialString.length(); i++) {
                if (initialString.charAt(i) == '(') {
                    openParensFound++;
                    if (openParenIndex == null &&
                            (i == 0 || initialString.charAt(i - 1) == ' ')
                    ) {
                        openParenIndex = i;
                    } else if (openParenIndex == null) {
//                        String truncString = initialString.substring(0, i);
//                        openParenIndex = Math.max(truncString.lastIndexOf(' ')+1, 0);
                    }
                } else if (initialString.charAt(i) == ')') {
                    closingParensFound++;
                    if (openParensFound == closingParensFound) {
                        closingParenIndex = i;
                        break;
                    }
                }
            }
            if (openParenIndex != null && closingParenIndex != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(initialString.substring(0, openParenIndex));
                sb.append("{").append(parentheticals.size()).append("}");
                String parentheticalToAdd = initialString.substring(openParenIndex, closingParenIndex+1);
                if (parentheticalToAdd.startsWith("(") && parentheticalToAdd.endsWith(")")) {
                    parentheticalToAdd = parentheticalToAdd.substring(1, parentheticalToAdd.length() - 1);
                }
                parentheticals.add(parentheticalToAdd);
                sb.append(initialString.substring(closingParenIndex + 1));
                newString = sb.toString();
            }
            openParenIndex = null;
            closingParenIndex = null;
            openParensFound = 0;
            closingParensFound = 0;
            loopsCompleted++;
        } while (!newString.equals(initialString));

        Map<String, List<String>> result = new HashMap<>();
        result.put(newString, parentheticals);
        System.out.println("Ending breakOutParentheticals with return value '" + result + "'");
        return result;
    }


}