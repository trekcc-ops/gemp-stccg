package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.CardWithCompatibility;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Map;

public interface FilterBlueprintMethods {

    Map<String, FilterBlueprint> getSimpleFilters();

    default void appendSimpleFilter(String label, FilterBlueprint blueprint) throws InvalidCardDefinitionException {
        String labelToUse = label.toLowerCase();
        if (getSimpleFilters().get(label) != null) {
            throw new InvalidCardDefinitionException("Duplicate filter blueprint label: " + labelToUse);
        } else {
            getSimpleFilters().put(labelToUse, blueprint);
        }
    }

    default void appendSimpleFilter(Filterable value) throws InvalidCardDefinitionException {
        FilterBlueprint blueprint = (cardGame, actionContext) -> Filters.changeToFilter(value);
        String firstString = Sanitize(value.toString());
        appendSimpleFilter(firstString, blueprint);
        String secondString = value.toString().toLowerCase().replace("_", "-");
        if (!firstString.equals(secondString)) {
            appendSimpleFilter(secondString, blueprint);
        }
    }


    default void loadSimpleFilters() throws InvalidCardDefinitionException {
        FilterBlueprint thisCard = (cardGame, actionContext) -> Filters.card(actionContext.card());
        FilterBlueprint notThisCard = (cardGame, actionContext) -> Filters.not(Filters.card(actionContext.card()));
        FilterBlueprint yours = (cardGame, actionContext) -> Filters.your(actionContext.yourName());

        for (CardIcon value : CardIcon.values())
            appendSimpleFilter(value);
        for (CardType value : CardType.values())
            appendSimpleFilter(value);
        for (Characteristic value : Characteristic.values())
            appendSimpleFilter(value);
        for (Uniqueness value : Uniqueness.values())
            appendSimpleFilter(value);
        for (FacilityType value : FacilityType.values())
            appendSimpleFilter(value);
        for (PropertyLogo value : PropertyLogo.values())
            appendSimpleFilter(value);
        for (SkillName value : SkillName.values())
            appendSimpleFilter(value);

        // Characteristics
        appendSimpleFilter("android", (cardGame, actionContext) -> Filters.changeToFilter(Species.ANDROID));
        appendSimpleFilter("bajoran", (cardGame, actionContext) -> Filters.Bajoran);
        appendSimpleFilter("borg", (cardGame, actionContext) -> Filters.Borg);
        appendSimpleFilter("cardassian", (cardGame, actionContext) -> Filters.Cardassian);
        appendSimpleFilter("dominion", (cardGame, actionContext) -> Filters.changeToFilter(Affiliation.DOMINION));
        appendSimpleFilter("federation", (cardGame, actionContext) -> Filters.changeToFilter(Affiliation.FEDERATION));
        appendSimpleFilter("female", (cardGame, actionContext) -> Filters.female);
        appendSimpleFilter("hologram", (cardGame, actionContext) -> Filters.hologram);
        appendSimpleFilter("klingon", (cardGame, actionContext) -> Filters.Klingon);
        appendSimpleFilter("romulan", (cardGame, actionContext) -> Filters.Romulan);

        // More complicated game state checks
        appendSimpleFilter("atNonHomeworldMission", (cardGame, actionContext) -> (game, physicalCard) ->
                game instanceof ST1EGame stGame &&
                        physicalCard.getGameLocation(stGame) instanceof MissionLocation missionLocation &&
                        !missionLocation.isHomeworld());
        appendSimpleFilter("atThisLocation", (cardGame, actionContext) -> (game, physicalCard) -> {
            PhysicalCard thisCard1 = actionContext.card();
            return physicalCard.isAtSameLocationAsCard(thisCard1);
        });
        appendSimpleFilter("cardyoucandownload", (cardGame, actionContext) ->
                Filters.cardsYouCanDownload(actionContext.yourName()));
        appendSimpleFilter("compatibleWithThisCard", (cardGame, actionContext) -> (game, physicalCard) -> {
            PhysicalCard thisCard12 = actionContext.card();
            return physicalCard instanceof CardWithCompatibility compatibleCard &&
                    game instanceof ST1EGame stGame &&
                    thisCard12 instanceof CardWithCompatibility compatibleThisCard &&
                    compatibleCard.isCompatibleWith(stGame, compatibleThisCard);
        });
        appendSimpleFilter("encounteringthiscard", (cardGame, actionContext) ->
                new EncounteringCardFilter(actionContext.card()));
        appendSimpleFilter("inCrewOrAwayTeamEncountering", (cardGame, actionContext) ->
                new EncounteringCardFilter(actionContext.card()));
        appendSimpleFilter("inplay", (cardGame, actionContext) -> Filters.inPlay);
        appendSimpleFilter("inYourDiscard", (cardGame, actionContext) ->
                new InYourDiscardFilter(actionContext.yourName()));
        appendSimpleFilter("inYourHand", (cardGame, actionContext) ->
                new InYourHandFilter(actionContext.yourName()));
        appendSimpleFilter("inYourDrawDeck", (cardGame, actionContext) ->
                new InYourDrawDeckFilter(actionContext.yourName()));
        appendSimpleFilter("onPlanetMissionSeededByYourOpponent", (cardGame, actionContext) -> {
            String opponentName = cardGame.getOpponent(actionContext.yourName());
            return new OnPlanetMissionFilter(opponentName);
        });
        appendSimpleFilter("presentWithThisCard", (cardGame, actionContext) ->
                Filters.presentWithThisCard(actionContext.card()));
        appendSimpleFilter("thisCardIsAboard", (cardGame, actionContext) ->
                new ThisCardIsAboardFilter(actionContext.card()));
        appendSimpleFilter("thisMission", (cardGame, actionContext) -> (game, physicalCard) ->
                physicalCard.getCardType() == CardType.MISSION &&
                        physicalCard.isAtSameLocationAsCard(actionContext.card()));
        appendSimpleFilter("thisPersonnel", (cardGame, actionContext) -> (game, physicalCard) -> {
            if (physicalCard.getCardType() != CardType.PERSONNEL || actionContext.card() == null) {
                return false;
            } else {
                return physicalCard == actionContext.card() || actionContext.card().isAtop(physicalCard);
            }
        });
        appendSimpleFilter("thisShip", (cardGame, actionContext) -> new ThisShipFilter(actionContext.card()));
        appendSimpleFilter("youControlAMatchingOutpost", (cardGame, actionContext) ->
                new YouControlAMatchingOutpostFilter(actionContext.yourName()));
        appendSimpleFilter("youOwnNoCopiesInPlay", (cardGame, actionContext) ->
                Filters.youHaveNoCopiesInPlay(actionContext.yourName()));
        appendSimpleFilter("yoursEvenIfNotInPlay", (cardGame, actionContext) ->
                Filters.yoursEvenIfNotInPlay(actionContext.yourName()));

        // Broad card filters
        appendSimpleFilter("another", notThisCard);
        appendSimpleFilter("any", (cardGame, actionContext) -> Filters.any);
        appendSimpleFilter("here", (cardGame, actionContext) -> Filters.here(actionContext.card()));
        appendSimpleFilter("missionSpecialist", (cardGame, actionContext) -> new MissionSpecialistFilter());
        appendSimpleFilter("notThisCard", notThisCard);
        appendSimpleFilter("self", thisCard);
        appendSimpleFilter("stopped", (cardGame, actionContext) -> Filters.stopped);
        appendSimpleFilter("thisCard", thisCard);
        appendSimpleFilter("ownedByYou", (cardGame, actionContext) ->
                new OwnedByPlayerFilter(actionContext.yourName()));
        appendSimpleFilter("your", yours);
        appendSimpleFilter("yours", yours);
    }

    default String Sanitize(String input)
    {
        return input
                .toLowerCase()
                .replace(" ", "")
                .replace("_", "")
                .replace(".", "");
    }


}