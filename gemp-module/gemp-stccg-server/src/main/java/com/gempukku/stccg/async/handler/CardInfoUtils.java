package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardInfoUtils {

    public static String getCardInfoHTML(DefaultGame game, PhysicalCard card) {
        String info = getBasicCardInfoHTML(card);
        return switch (card) {
            case PersonnelCard personnel -> info + getPersonnelInfo(game, personnel);
            case PhysicalShipCard ship -> info + getShipCardInfo(game, ship);
            case FacilityCard facility -> info + getFacilityCardInfo(facility);
            case MissionCard mission -> info + getMissionCardInfo(mission);
            default -> info;
        };
    }


    public static String getBasicCardInfoHTML(PhysicalCard card) {
        if (card.getZone().isInPlay() || card.getZone() == Zone.HAND) {
            StringBuilder sb = new StringBuilder();

            Collection<Modifier> modifiers = card.getGame().getModifiersQuerying().getModifiersAffecting(card);
            if (!modifiers.isEmpty()) {
                sb.append(HTMLUtils.makeBold("Active modifiers:")).append(HTMLUtils.NEWLINE);
                for (Modifier modifier : modifiers) {
                    sb.append(modifier.getCardInfoText(card));
                }
            }

            List<PhysicalCard> stackedCards = card.getStackedCards(card.getGame());
            if (!stackedCards.isEmpty()) {
                sb.append("<br><b>Stacked cards:</b>");
                sb.append("<br>").append(TextUtils.getConcatenatedCardLinks(stackedCards));
            }

            return sb.toString();
        } else {
            return "";
        }

    }

    static String getPersonnelInfo(DefaultGame game, PersonnelCard personnel) {

        StringBuilder sb = new StringBuilder();

        sb.append(HTMLUtils.NEWLINE).append(HTMLUtils.makeBold("Affiliation: "));
        for (Affiliation affiliation : Affiliation.values())
            if (personnel.isAffiliation(affiliation))
                sb.append(affiliation.toHTML());

        sb.append(HTMLUtils.NEWLINE).append(getCardIcons(game, personnel));

        return sb.toString();
    }


    static String getShipCardInfo(DefaultGame game, PhysicalShipCard ship) {
        StringBuilder sb = new StringBuilder();
        Map<String, Collection<PhysicalCard>> attachedCards = new HashMap<>();
        attachedCards.put("Crew",ship.getCrew());
        for (Map.Entry<String, Collection<PhysicalCard>> entry : attachedCards.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                sb.append(HTMLUtils.NEWLINE);
                sb.append("<b>").append(entry.getKey()).append(" (").append(entry.getValue().size())
                        .append("):</b> ");
                sb.append(TextUtils.getConcatenatedCardLinks(entry.getValue()));
                sb.append(HTMLUtils.NEWLINE);
            }
        }

        sb.append(HTMLUtils.NEWLINE).append(HTMLUtils.makeBold("Staffing requirements: "));
        if (ship.getBlueprint().getStaffing() == null || ship.getBlueprint().getStaffing().isEmpty())
            sb.append("<i>none</i>");
        else {
            sb.append(HTMLUtils.NEWLINE);
            for (CardIcon icon : ship.getBlueprint().getStaffing())
                sb.append("<img src='").append(icon.getIconURL()).append("'>");
        }

        String isStaffed = (ship.isStaffed()) ? "staffed" : "not staffed";
        if (ship.isStaffed()) {
            sb.append(HTMLUtils.NEWLINE).append("<i>(Ship is ").append(isStaffed).append("</i>");
            sb.append(HTMLUtils.NEWLINE);
        }

        sb.append(HTMLUtils.NEWLINE).append(HTMLUtils.makeBold("Printed RANGE: "))
                .append(ship.getBlueprint().getRange());
        sb.append(HTMLUtils.NEWLINE).append(HTMLUtils.makeBold("RANGE available: "))
                .append(ship.getRangeAvailable());
        sb.append(HTMLUtils.NEWLINE).append(getCardIcons(game, ship));

        return sb.toString();
    }

    private static String getCardIcons(DefaultGame game, PhysicalCard card) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTMLUtils.makeBold("Icons: "));

        for (CardIcon icon : CardIcon.values())
            if (card.hasIcon(game, icon))
                sb.append(icon.toHTML());

        return sb.toString();
    }


    static String getFacilityCardInfo(FacilityCard facility) {
        StringBuilder sb = new StringBuilder();
        Map<String, Collection<PhysicalCard>> attachedCards = new HashMap<>();
        attachedCards.put("Docked ships", facility.getDockedShips());
        attachedCards.put("Crew", facility.getCrew());
        for (Map.Entry<String, Collection<PhysicalCard>> entry : attachedCards.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                sb.append(HTMLUtils.NEWLINE);
                sb.append("<b>").append(entry.getKey()).append(" (").append(entry.getValue().size())
                        .append("):</b> ").append(TextUtils.getConcatenatedCardLinks(entry.getValue()));
                sb.append(HTMLUtils.NEWLINE);
            }
        }
        return sb.toString();
    }

    static String getMissionCardInfo(MissionCard mission) {
        StringBuilder sb = new StringBuilder();
        ST1EGame cardGame = mission.getGame();
        try {
            if (mission.getBlueprint().getMissionType() == MissionType.PLANET && mission.getZone().isInPlay()) {
                MissionLocation location = mission.getLocation();
                long awayTeamCount = location.getAwayTeamsOnSurface(cardGame).count();
                sb.append(HTMLUtils.NEWLINE);
                sb.append(HTMLUtils.makeBold("Away Teams on Planet: "));
                sb.append(awayTeamCount);
                if (awayTeamCount > 0) {
                    location.getAwayTeamsOnSurface(cardGame).forEach(awayTeam -> {
                                sb.append(HTMLUtils.NEWLINE);
                                sb.append(HTMLUtils.makeBold("Away Team: "));
                                sb.append("(").append(awayTeam.getPlayerId()).append(") ");
                                sb.append(TextUtils.getConcatenatedCardLinks(awayTeam.getCards()));
                            }
                    );
                }
            }
        } catch(InvalidGameLogicException exp) {
            mission.getGame().sendErrorMessage(exp);
        }
        sb.append(HTMLUtils.NEWLINE).append(HTMLUtils.NEWLINE);
        sb.append(HTMLUtils.makeBold("Mission Requirements: "));
        sb.append(mission.getMissionRequirements().replace(" OR ", " <a style='color:red'>OR</a> "));
        return sb.toString();
    }

}