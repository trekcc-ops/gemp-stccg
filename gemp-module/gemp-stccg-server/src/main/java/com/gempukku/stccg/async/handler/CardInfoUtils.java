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
                sb.append("<b>Active modifiers:</b><br>");
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
        sb.append("<br><b>Affiliation:</b> ");
        for (Affiliation affiliation : Affiliation.values())
            if (personnel.isAffiliation(affiliation))
                sb.append(affiliation.toHTML());
        sb.append("<br><b>Icons:</b> ");
        for (CardIcon icon : CardIcon.values())
            if (personnel.hasIcon(icon))
                sb.append(icon.toHTML());
        return sb.toString();
    }

    static String getShipCardInfo(PhysicalShipCard ship) {
        StringBuilder sb = new StringBuilder();
        Map<String, Collection<PhysicalCard>> attachedCards = new HashMap<>();
        attachedCards.put("Crew",ship.getCrew());
        for (Map.Entry<String, Collection<PhysicalCard>> entry : attachedCards.entrySet()) {
            if (!entry.getValue().isEmpty())
                sb.append("<br><b>").append(entry.getKey()).append(" (").append(entry.getValue().size())
                        .append("):</b> ").append(TextUtils.getConcatenatedCardLinks(entry.getValue())).append("<br>");
        }

        sb.append("<br><b>").append("Staffing requirements:</b> ");
        if (ship.getBlueprint().getStaffing() == null || ship.getBlueprint().getStaffing().isEmpty())
            sb.append("<i>none</i>");
        else {
            sb.append("<br>");
            for (CardIcon icon : ship.getBlueprint().getStaffing())
                sb.append("<img src='").append(icon.getIconURL()).append("'>");
        }
        if (ship.isStaffed())
            sb.append("<br><i>(Ship is staffed)</i><br>");
        else sb.append("<br><i>(Ship is not staffed)</i><br>");

        sb.append("<br><b>Printed RANGE:</b> ").append( ship.getBlueprint().getRange());
        sb.append("<br><b>RANGE available:</b> ").append(ship.getRangeAvailable());

        sb.append("<br><b>Icons:</b> ");

        for (CardIcon icon : CardIcon.values())
            if (ship.hasIcon(icon))
                sb.append(icon.toHTML());

        return sb.toString();
    }

    static String getFacilityCardInfo(FacilityCard facility) {
        StringBuilder sb = new StringBuilder();
        Map<String, Collection<PhysicalCard>> attachedCards = new HashMap<>();
        attachedCards.put("Docked ships", facility.getDockedShips());
        attachedCards.put("Crew", facility.getCrew());
        for (Map.Entry<String, Collection<PhysicalCard>> entry : attachedCards.entrySet()) {
            if (!entry.getValue().isEmpty())
                sb.append("<br><b>").append(entry.getKey()).append(" (").append(entry.getValue().size())
                        .append("):</b> ").append(TextUtils.getConcatenatedCardLinks(entry.getValue())).append("<br>");
        }
        return sb.toString();
    }

    static String getMissionCardInfo(MissionCard mission) {
        StringBuilder sb = new StringBuilder();
        if (mission.getBlueprint().getMissionType() == MissionType.PLANET && mission.getZone().isInPlay()) {
            long awayTeamCount = mission.getAwayTeamsOnSurface().count();
            sb.append("<br><b>Away Teams on Planet</b>: ").append(awayTeamCount);
            if (awayTeamCount > 0) {
                mission.getAwayTeamsOnSurface().forEach(awayTeam -> {
                            sb.append("<br><b>Away Team:</b> (").append(awayTeam.getPlayerId()).append(") ");
                            sb.append(TextUtils.getConcatenatedCardLinks(awayTeam.getCards()));
                        }
                );
            }
        }
        sb.append("<br><br><b>Mission Requirements</b>: ").append(
                mission.getMissionRequirements().replace(" OR ", " <a style='color:red'>OR</a> "));
        return sb.toString();
    }

}