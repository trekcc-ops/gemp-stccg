package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.*;
import java.util.stream.Stream;

public class MissionCard2E extends MissionCard {
    private final MissionType _missionType;

    public MissionCard2E(ST1EGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(game, cardId, owner, blueprint);
        _missionType = blueprint.getMissionType();
    }

    public Set<Affiliation> getAffiliationIcons(String playerId) {
        if (Objects.equals(playerId, _owner.getPlayerId())) {
            return _blueprint.getOwnerAffiliationIcons();
        } else if (_blueprint.getOpponentAffiliationIcons() == null) {
            return _blueprint.getOwnerAffiliationIcons();
        } else {
            // TODO: Assumes all missions are symmetric
            return _blueprint.getOwnerAffiliationIcons();
        }
    }

    public Set<Affiliation> getAffiliationIconsForPlayer(Player player) {
        return getAffiliationIcons(player.getPlayerId());
    }

    public boolean isHeadquartersFor(PhysicalNounCard1E card) {
        if (_blueprint.getMissionType() != MissionType.HEADQUARTERS)
            return false;
        if (card.getCardType() == CardType.EQUIPMENT)
            return true;
        // TODO - Manually set up for now
        return switch (_blueprint.getBlueprintId()) {
            case "1_161" -> card.isAffiliation(Affiliation.NON_ALIGNED) || card.hasIcon(CardIcon.TNG_ICON) ||
                    card.hasIcon(CardIcon.EARTH);
            case "1_162" -> card.isAffiliation(Affiliation.NON_ALIGNED) || card.hasIcon(CardIcon.EARTH) ||
                    (card.isAffiliation(Affiliation.FEDERATION) && (card.hasIcon(CardIcon.DS9_ICON)));
            case "1_191" -> card.isAffiliation(Affiliation.KLINGON) || card.isAffiliation(Affiliation.NON_ALIGNED);
            default -> false;
        };
    }

    public boolean mayBeAttemptedByPlayer(Player player) {
            // Rule 7.2.1, Paragraph 1
            // TODO - Does not address shared missions, multiple copies of universal missions, or dual missions
        return _owner == player;
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(Player player) {
        return getAwayTeamsOnSurface().filter(awayTeam -> awayTeam.getPlayer() == player);
    }

    public Stream<AwayTeam> getAwayTeamsOnSurface() {
        return getGame().getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
    }

    private Collection<PhysicalCard> getCardsOnSurface() {
        if (_missionType == MissionType.SPACE)
            return new LinkedList<>();
        else
            return getAttachedCards();
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(Player player) {
        List<Action> actions = new LinkedList<>();
        if (_game.getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            Action action = new AttemptMissionAction(player, this);
            if (action.canBeInitiated())
                actions.add(action);
        }
        return actions;
    }
}