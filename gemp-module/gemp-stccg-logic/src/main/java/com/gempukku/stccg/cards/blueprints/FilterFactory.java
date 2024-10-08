package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.SingleMemoryEvaluator;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public class FilterFactory {
    private static final Map<String, FilterableSourceProducer> parameterFilters = new HashMap<>();

    public FilterFactory() {
        parameterFilters.put("and",
                (parameter) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterableSource[] filterables = new FilterableSource[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = FilterFactory.generateFilter(filters[i]);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.and(filters1);
                    };
                });
        parameterFilters.put("attachedto",
                (parameter) -> {
                    final FilterableSource filterableSource = FilterFactory.generateFilter(parameter);
                    return (actionContext) -> Filters.attachedTo(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("affiliation", (parameter) -> {
            final Affiliation affiliation = Affiliation.findAffiliation(parameter);
            if (affiliation == null)
                throw new InvalidCardDefinitionException("Unable to find affiliation for: " + parameter);
            return (actionContext) -> affiliation;
        });
        parameterFilters.put("hasstacked",
                (parameter) -> {
                    final FilterableSource filterableSource = FilterFactory.generateFilter(parameter);
                    return (actionContext) -> Filters.hasStacked(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasstackedcount",
                (parameter) -> {
                    String[] parameterSplit = parameter.split(",", 2);
                    int count = Integer.parseInt(parameterSplit[0]);
                    final FilterableSource filterableSource = FilterFactory.generateFilter(parameterSplit[1]);
                    return (actionContext) -> Filters.hasStacked(count, filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("loweststrength",
                (parameter) -> {
                    final FilterableSource filterableSource = FilterFactory.generateFilter(parameter);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return Filters.and(
                                sourceFilterable, Filters.strengthEqual(
                                        new SingleMemoryEvaluator(actionContext,
                                                new Evaluator(actionContext) {
                                                    @Override
                                                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                                        int minStrength = Integer.MAX_VALUE;
                                                        for (PhysicalCard card : Filters.filterActive(cardAffected.getGame(), sourceFilterable))
                                                            minStrength = Math.min(minStrength, cardAffected.getGame().getModifiersQuerying().getStrength(card));
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
                    return (actionContext) -> (Filter)
                            (game, physicalCard) -> physicalCard.getBlueprint().getTitle() != null && name.equals(Sanitize(physicalCard.getBlueprint().getTitle()));
                });
        parameterFilters.put("namefrommemory",
                (parameter) -> actionContext -> {
                    Set<String> titles = new HashSet<>();
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(parameter))
                        titles.add(physicalCard.getBlueprint().getTitle());
                    return (Filter) (game, physicalCard) -> titles.contains(physicalCard.getBlueprint().getTitle());
                });
        parameterFilters.put("nameinstackedon",
                (parameter) -> {
                    final FilterableSource filterableSource = FilterFactory.generateFilter(parameter);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return (Filter) (game, physicalCard) -> {
                            for (PhysicalCard cardWithStack : Filters.filterActive(game, sourceFilterable)) {
                                for (PhysicalCard stackedCard : cardWithStack.getStackedCards()) {
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
                    final FilterableSource filterableSource = FilterFactory.generateFilter(parameter);
                    return (actionContext) -> Filters.not(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("or",
                (parameter) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterableSource[] filterables = new FilterableSource[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = FilterFactory.generateFilter(filters[i]);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.or(filters1);
                    };
                });
        parameterFilters.put("strengthlessthan",
                (parameter) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter);

                    return (actionContext) -> {
                        int amount = valueSource.evaluateExpression(actionContext, null);
                        return Filters.lessStrengthThan(amount);
                    };
                });
        parameterFilters.put("strengthmorethan",
                (parameter) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter);

                    return (actionContext) -> {
                        int amount = valueSource.evaluateExpression(actionContext, null);
                        return Filters.moreStrengthThan(amount);
                    };
                });
        parameterFilters.put("title",parameterFilters.get("name"));
        parameterFilters.put("zone",
                (parameter) -> {
                    final Zone zone = BlueprintUtils.getEnum(Zone.class, parameter, "parameter");
                    return actionContext -> zone;
                });
    }

    public static FilterableSource generateFilter(String value) throws
            InvalidCardDefinitionException {
        if (value == null)
            throw new InvalidCardDefinitionException("Filter not specified");
        String[] filterStrings = splitIntoFilters(value);
        if (filterStrings.length == 0)
            return (actionContext) -> Filters.any;
        if (filterStrings.length == 1)
            return createFilter(filterStrings[0]);

        FilterableSource[] filters = new FilterableSource[filterStrings.length];
        for (int i = 0; i < filters.length; i++)
            filters[i] = createFilter(filterStrings[i]);
        return (actionContext) -> {
            Filterable[] filter = new Filterable[filters.length];
            for (int i = 0; i < filter.length; i++) {
                filter[i] = filters[i].getFilterable(actionContext);
            }

            return Filters.and(filter);
        };
    }

    public static FilterableSource parseSTCCGFilter(String value) throws InvalidCardDefinitionException {
        String orNoParens = "\\s+OR\\s+(?![^\\(]*\\))";
        String andNoParens = "\\s+\\+\\s+(?![^\\(]*\\))";
        if (value == null)
            return null;
        if (value.split(orNoParens).length > 1) {
            String[] stringSplit = value.split(orNoParens);
            List<FilterableSource> filterableSources = new LinkedList<>();
            for (String string : stringSplit)
                filterableSources.add(parseSTCCGFilter(string));
            return (actionContext) -> {
                List<Filterable> filterables = new LinkedList<>();
                for (FilterableSource filterableSource : filterableSources)
                    filterables.add(filterableSource.getFilterable(actionContext));
                return Filters.or(filterables.toArray(new Filterable[0]));
            };
        }
        if (value.split(andNoParens).length > 1) {
            String[] stringSplit = value.split(andNoParens);
            List<FilterableSource> filterableSources = new LinkedList<>();
            for (String string : stringSplit)
                filterableSources.add(parseSTCCGFilter(string));
            return (actionContext) -> {
                List<Filterable> filterables = new LinkedList<>();
                for (FilterableSource filterableSource : filterableSources)
                    filterables.add(filterableSource.getFilterable(actionContext));
                return Filters.and(filterables.toArray(new Filterable[0]));
            };
        }
        if (value.startsWith("(") && value.endsWith(")")) {
            return parseSTCCGFilter(value.substring(1, value.length() - 1));
        }
        if (value.startsWith("not(") && value.endsWith(")")) {
            FilterableSource filterableSource = parseSTCCGFilter(value.substring(4, value.length() - 1));
            return (actionContext) -> Filters.not(filterableSource.getFilterable(actionContext));
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
        if (value.equals("you have no copies in play")) {
            return (actionContext) -> Filters.youHaveNoCopiesInPlay(actionContext.getPerformingPlayer());
        }
        FilterableSource result = getSimpleFilter(Sanitize(value));
        if (result == null)
            throw new InvalidCardDefinitionException("Unknown filter: " + value);
        else return result;
    }

    private static String[] splitIntoFilters(String value) throws InvalidCardDefinitionException {
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

    private static FilterableSource createFilter(String filterString) throws InvalidCardDefinitionException {
        if (filterString.contains("(") && filterString.endsWith(")")) {
            String filterName = filterString.substring(0, filterString.indexOf("("));
            String filterParameter =
                    filterString.substring(filterString.indexOf("(") + 1, filterString.lastIndexOf(")"));
            return lookupFilter(Sanitize(filterName), Sanitize(filterParameter));
        }
        return lookupFilter(Sanitize(filterString), null);
    }

    private static FilterableSource getSimpleFilter(String value) {
        return switch(value) {
            case "another" -> (actionContext) -> Filters.not(actionContext.getSource());
            case "any" -> (actionContext) -> Filters.any;
            case "ds9-icon" -> (actionContext) -> Filters.and(CardIcon.DS9_ICON);
            case "ds9-logo" -> (actionContext) -> Filters.and(PropertyLogo.DS9_LOGO);
            case "generations-logo" -> (actionContext) -> Filters.and(PropertyLogo.GENERATIONS_LOGO);
            case "idinstored" -> (actionContext -> (Filter) (game, physicalCard) -> {
                final String whileInZoneData = (String) actionContext.getSource().getWhileInZoneData();
                if (whileInZoneData == null)
                    return false;
                for (String cardId : whileInZoneData.split(",")) {
                    if (cardId.equals(String.valueOf(physicalCard.getCardId())))
                        return true;
                }
                return false;
            });
            case "inplay" -> (actionContext) -> Filters.inPlay;
            case "nor" -> (actionContext) -> Filters.Nor;
            case "personnel" -> (actionContext) -> Filters.personnel;
            case "reactor-core" -> (actionContext) -> Filters.and(CardIcon.REACTOR_CORE);
            case "self" -> ActionContext::getSource;
            case "ship" -> (actionContext) -> Filters.ship;
            case "tng-icon" -> (actionContext) -> Filters.and(CardIcon.TNG_ICON);
            case "tng-logo" -> (actionContext) -> Filters.and(PropertyLogo.TNG_LOGO);
            case "unique" -> (actionContext) -> Filters.unique;
            case "universal" -> (actionContext) -> Filters.universal;
            case "warp-core" -> (actionContext) -> Filters.and(CardIcon.WARP_CORE);
            case "yours" -> (actionContext) -> Filters.your(actionContext.getPerformingPlayerId());
            case "yoursevenifnotinplay" ->
                    (actionContext) -> Filters.yoursEvenIfNotInPlay(actionContext.getPerformingPlayerId());
            default -> null;
        };
    }

    private static FilterableSource lookupFilter(String name, String parameter) throws InvalidCardDefinitionException {
        FilterableSource filterableSource;
        if (parameter == null) {
            filterableSource = getSimpleFilter(Sanitize(name));
            if (filterableSource != null)
                return filterableSource;
        } else {
            final FilterableSourceProducer filterableSourceProducer = parameterFilters.get(Sanitize(name));
            if (filterableSourceProducer != null)
                return filterableSourceProducer.createFilterableSource(parameter);
        }
        throw new InvalidCardDefinitionException("Unable to find filter: " + name);
    }

    public static String Sanitize(String input)
    {
        return input
                .toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }
}