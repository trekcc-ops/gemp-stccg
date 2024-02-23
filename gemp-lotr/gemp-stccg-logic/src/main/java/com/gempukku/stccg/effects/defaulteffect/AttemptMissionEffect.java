package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.common.filterable.RegularSkill;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.AwayTeam;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.*;

public class AttemptMissionEffect extends DefaultEffect {
    private final DefaultGame _game;

    public AttemptMissionEffect(Player player, AwayTeam awayTeam, PhysicalMissionCard missionCard) {
        _game = player.getGame();
    }

    @Override
    public String getText() {
        return "Attempt mission";
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        // TODO - Pretty much skipping all the actual pieces of attempting a mission except attempting to solve it
        // TODO - NEver mind, didn't actual add that either
        _game.getGameState().sendMessage("DEBUG - Attempted mission. No code has been written yet for this.");

        // Crew must be able to attempt mission to solve it
/*        if (_awayTeam.canAttemptMission(_missionCard))
            if (checkRequirements(_missionCard.getMissionRequirements(), _awayTeam)) */
                // Mission solved
        return new FullEffectResult(true);
    }

    private boolean checkRequirements(String string, AwayTeam awayTeam) {
        String orNoParens = "\\s+OR\\s+(?![^\\(]*\\))";
        String andNoParens = "\\s+\\+\\s+(?![^\\(]*\\))";

        if (string.split(orNoParens).length > 1) {
            String[] stringSplit = string.split(orNoParens);
            return Arrays.stream(stringSplit).anyMatch(requirement -> checkRequirements(requirement, awayTeam));
/*            for (int i = 0; i < stringSplit.length; i++) {
                stringSplit[i] = checkRequirements(stringSplit[i]);
            }
            return "OR(" + String.join(", ", stringSplit) + ")"; */
        } else if (string.split(andNoParens).length > 1) {
            String[] stringSplit = string.split(andNoParens);
            return Arrays.stream(stringSplit).allMatch(requirement -> checkRequirements(requirement, awayTeam));
/*            for (int i = 0; i < stringSplit.length; i++) {
                stringSplit[i] = checkRequirements(stringSplit[i]);
            }
            return "AND(" + String.join(", ", stringSplit) + ")"; */
        } else if (string.startsWith("(") && string.endsWith(")")) {
            return checkRequirements(string.substring(1, string.length() - 1), awayTeam);
        }

        List<RegularSkill> skillList = new ArrayList<>(Arrays.asList(RegularSkill.values()));
        Map<String, RegularSkill> skillMap = new HashMap<>();
        skillList.forEach(regularSkill -> skillMap.put(regularSkill.get_humanReadable(), regularSkill));
        RegularSkill regularSkill = skillMap.get(string);
        if (regularSkill != null) {
            return awayTeam.hasSkill(regularSkill); // TODO - Doesn't count multipliers
        } else {
            return false;
        }
    }
    /* NOTES FOR mission req AND/OR

    orCondition(andCondition(Diplomacy,orCondition(Sarek,Spock),Mindmeld),Guinan)

    REQ 1 - andCondition(Diplomacy,orCondition(Sarek,Spock),Mindmeld)
    REQ 2 - Guinan

    REQ 1 - [Diplomacy, orCondition(Sarek,Spock), Mindmeld]
    REQ 2 - Guinan

    for (Condition : REQ 1)


     */

}