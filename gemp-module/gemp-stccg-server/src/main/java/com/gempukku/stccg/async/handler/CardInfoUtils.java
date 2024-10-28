package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CardInfoUtils {

    public static String getCardInfoHTML(PhysicalCard card) {
        String info = getBasicCardInfoHTML(card);
        return switch (card) {
            case PersonnelCard personnel -> info + getPersonnelInfo(personnel);
            case PhysicalShipCard ship -> info + getShipCardInfo(ship);
            case FacilityCard facility -> info + getFacilityCardInfo(facility);
            case MissionCard mission -> info + getMissionCardInfo(mission);
            default -> info;
        };
    }

    public static String getBasicCardInfoHTML(PhysicalCard card) {
        if (card.getZone().isInPlay() || card.getZone() == Zone.HAND) {
            StringBuilder sb = new StringBuilder();

/*            if (getZone() == Zone.HAND)
                sb.append("<b>Card is in hand - stats are only provisional</b><br><br>");
            else if (Filters.filterActive(getGame(), this).isEmpty())
                sb.append("<b>Card is inactive - current stats may be inaccurate</b><br><br>");*/

            Collection<Modifier> modifiers = card.getGame().getModifiersQuerying().getModifiersAffecting(card);
            if (!modifiers.isEmpty()) {
                sb.append(HTMLUtils.makeBold("Active modifiers:")).append(HTMLUtils.NEWLINE);
                for (Modifier modifier : modifiers) {
                    sb.append(modifier.getCardInfoText(card));
                }
            }
/*
            List<PhysicalCard> stackedCards = getStackedCards();
            if (!stackedCards.isEmpty()) {
                sb.append("<br><b>Stacked cards:</b>");
                sb.append("<br>").append(TextUtils.getConcatenatedCardLinks(stackedCards));
            }
*/
            return sb.toString();
        } else {
            return "";
        }

    }

    static String getPersonnelInfo(PersonnelCard personnel) {

        StringBuilder sb = new StringBuilder();

        sb.append(HTMLUtils.NEWLINE).append(HTMLUtils.makeBold("Affiliation: "));
        for (Affiliation affiliation : Affiliation.values())
            if (personnel.isAffiliation(affiliation))
                sb.append(affiliation.toHTML());

        sb.append(HTMLUtils.NEWLINE).append(getCardIcons(personnel));

        return sb.toString();
    }

    static String getShipCardInfo(PhysicalShipCard ship) {
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
        sb.append(HTMLUtils.NEWLINE).append(getCardIcons(ship));

        return sb.toString();
    }

    private static String getCardIcons(PhysicalCard card) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTMLUtils.makeBold("Icons: "));

        for (CardIcon icon : CardIcon.values())
            if (card.hasIcon(icon))
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
        if (mission.getBlueprint().getMissionType() == MissionType.PLANET && mission.getZone().isInPlay()) {
            long awayTeamCount = mission.getAwayTeamsOnSurface().count();
            sb.append(HTMLUtils.NEWLINE);
            sb.append(HTMLUtils.makeBold("Away Teams on Planet: "));
            sb.append(awayTeamCount);
            if (awayTeamCount > 0) {
                mission.getAwayTeamsOnSurface().forEach(awayTeam -> {
                            sb.append(HTMLUtils.NEWLINE);
                            sb.append(HTMLUtils.makeBold("Away Team: "));
                            sb.append("(").append(awayTeam.getPlayerId()).append(") ");
                            sb.append(TextUtils.getConcatenatedCardLinks(awayTeam.getCards()));
                        }
                );
            }
        }
        sb.append(HTMLUtils.NEWLINE).append(HTMLUtils.NEWLINE);
        sb.append(HTMLUtils.makeBold("Mission Requirements: "));
        sb.append(mission.getMissionRequirements().replace(" OR ", " <a style='color:red'>OR</a> "));
        return sb.toString();
    }

}