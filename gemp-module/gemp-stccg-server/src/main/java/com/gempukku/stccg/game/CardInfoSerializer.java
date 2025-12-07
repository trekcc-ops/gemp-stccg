package com.gempukku.stccg.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.CardWithCrew;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.*;

public class CardInfoSerializer {

    public static String serialize(DefaultGame cardGame, PhysicalCard card) throws JsonProcessingException {
        return getCardInfoJson(cardGame, card);
    }

    private static String getCardInfoJson(DefaultGame cardGame, PhysicalCard card) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, Object> itemsToSerialize = new HashMap<>();
        if (card == null || (!card.isInPlay() && !card.isInHand(cardGame)))
            return mapper.writeValueAsString(itemsToSerialize);

        Collection<String> modifiersToAdd = new ArrayList<>();
        for (Modifier modifier : cardGame.getGameState().getModifiersQuerying().getModifiersAffecting(card)) {
            if (modifier != null && !Objects.equals(modifier.getCardInfoText(cardGame, card), "null")) {
                modifiersToAdd.add(modifier.getCardInfoText(cardGame, card));
            }
        }
        itemsToSerialize.put("modifiers", modifiersToAdd);

        if (card instanceof ST1EPhysicalCard stCard) {
            itemsToSerialize.put("isStopped", stCard.isStopped());
        }

        List<String> affiliationTexts = new ArrayList<>();
        if (card instanceof AffiliatedCard affiliatedCard) {
            for (Affiliation affiliation : Affiliation.values()) {
                if (affiliatedCard.isAffiliation(affiliation)) {
                    affiliationTexts.add(affiliation.toHTML());
                }
            }
        }
        itemsToSerialize.put("affiliations", affiliationTexts);

        List<String> cardIconTexts = new ArrayList<>();
        for (CardIcon icon : CardIcon.values()) {
            if (card.hasIcon(cardGame, icon)) {
                cardIconTexts.add(icon.toHTML());
            }
        }
        itemsToSerialize.put("icons", cardIconTexts);


        List<Map<Object, Object>> crew = new ArrayList<>();
        if (card instanceof CardWithCrew cardWithCrew) {
            for (PhysicalCard crewCard : cardWithCrew.getCrew(cardGame)) {
                crew.add(getCardProperties(crewCard));
            }
        }
        itemsToSerialize.put("crew", crew);

        List<Map<Object, Object>> dockedCards = new ArrayList<>();
        if (card instanceof FacilityCard facility) {
            for (PhysicalCard ship : facility.getDockedShips(cardGame)) {
                dockedCards.add(getCardProperties(ship));
            }
        }
        itemsToSerialize.put("dockedCards", dockedCards);

        if (card instanceof ShipCard ship) {
            List<String> staffingRequirements = new ArrayList<>();
            if (!ship.getStaffingRequirements().isEmpty()) {
                for (CardIcon icon : ship.getStaffingRequirements()) {
                    staffingRequirements.add(icon.toHTML());
                }
            }
            itemsToSerialize.put("staffingRequirements", staffingRequirements);

            itemsToSerialize.put("isStaffed", ship.isStaffed(cardGame));
            itemsToSerialize.put("printedRange", ship.getBlueprint().getRange());
            itemsToSerialize.put("rangeAvailable", ship.getRangeAvailable(cardGame));
        }

        if (card instanceof MissionCard mission) {
            itemsToSerialize.put("missionRequirements", mission.getMissionRequirements());

            List<Map<Object, Object>> serializableAwayTeams = new ArrayList<>();
            if (cardGame instanceof ST1EGame stGame &&
                    mission.getGameLocation(stGame) instanceof MissionLocation missionLocation &&
                    missionLocation.isPlanet()) {
                List<AwayTeam> awayTeamsOnPlanet = missionLocation.getAwayTeamsOnSurface(stGame).toList();
                for (AwayTeam team : awayTeamsOnPlanet) {
                    Map<Object, Object> awayTeamInfo = new HashMap<>();
                    awayTeamInfo.put("playerId", team.getControllerName());
                    List<Map<Object, Object>> awayTeamMembers = new ArrayList<>();
                    for (PhysicalCard member : team.getCards()) {
                        awayTeamMembers.add(getCardProperties(member));
                    }
                    awayTeamInfo.put("cardsInAwayTeam", awayTeamMembers);
                    serializableAwayTeams.add(awayTeamInfo);
                }
            }
            itemsToSerialize.put("awayTeams", serializableAwayTeams);
        }

        return mapper.writeValueAsString(itemsToSerialize);
    }

    private static Map<Object, Object> getCardProperties(PhysicalCard card) {
        List<CardType> cardTypesShowingUniversal = new ArrayList<>();
        cardTypesShowingUniversal.add(CardType.PERSONNEL);
        cardTypesShowingUniversal.add(CardType.SHIP);
        cardTypesShowingUniversal.add(CardType.FACILITY);
        cardTypesShowingUniversal.add(CardType.SITE);


        Map<Object, Object> cardMap = new HashMap<>();
        cardMap.put("title", card.getTitle());
        cardMap.put("cardId", card.getCardId());
        cardMap.put("blueprintId", card.getBlueprintId());
        cardMap.put("uniqueness", card.getUniqueness().name());
        cardMap.put("cardType", card.getCardType().name());
        cardMap.put("imageUrl", card.getImageUrl());
        boolean hasUniversalIcon = card.isUniversal() &&
                cardTypesShowingUniversal.contains(card.getCardType());
        cardMap.put("hasUniversalIcon", hasUniversalIcon);

        return cardMap;
    }

}