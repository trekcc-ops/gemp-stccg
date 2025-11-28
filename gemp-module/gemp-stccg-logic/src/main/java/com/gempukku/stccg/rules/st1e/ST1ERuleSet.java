package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.WeaponsDisabledModifier;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.rules.UndefinedRuleException;
import com.gempukku.stccg.rules.generic.RuleSet;
import org.apache.logging.log4j.core.net.Facility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ST1ERuleSet extends RuleSet<ST1EGame> {

    // Based on Modern rules
    private final DockingRules _dockingRules = new DockingRules();

    @Override
    protected void applySpecificRules(ST1EGame cardGame) {
        applyActionProxiesAsRules(cardGame,
                new ST1EPlayCardInPhaseRule(cardGame),
                new ST1EChangeAffiliationRule(cardGame),
                new ST1EPhaseActionsRule(cardGame)
        );

        List<Modifier> modifiers = getGlobalRulesBasedModifiersForCardsInPlay();
        for (Modifier modifier : modifiers) {
            cardGame.getModifiersEnvironment().addAlwaysOnModifier(modifier);
        }

        new ST1EAffiliationAttackRestrictionsRule(cardGame).applyRule();
    }

    public boolean isLocationValidPlayCardDestinationPerRules(ST1EGame game, FacilityCard facility,
                                                              GameLocation location,
                                                              Class<? extends PlayCardAction> actionClass,
                                                              String performingPlayerName,
                                                              Collection<Affiliation> affiliationOptions) {
        try {
            Player performingPlayer = game.getPlayer(performingPlayerName);
            return PlayCardDestinationRules.isLocationValidPlayCardDestinationForFacilityPerRules(
                    game, location, facility, actionClass, performingPlayer, affiliationOptions);
        } catch(UndefinedRuleException | PlayerNotFoundException exp) {
            game.sendErrorMessage(exp);
            game.cancelGame();
            return false;
        }
    }




    public boolean areCardsCompatiblePerRules(PhysicalNounCard1E card1, PhysicalNounCard1E card2) {
        return CompatibilityRule.areCardsCompatible(card1, card2);
    }

    @Override
    public List<Modifier> getModifiersWhileCardIsInPlay(PhysicalCard card) {
        List<Modifier> result = new ArrayList<>();
        if (card instanceof FacilityCard facility) {
            result.add(_dockingRules.getExtendedShieldsModifier(facility));
        }
        return result;
    }

    public List<Modifier> getGlobalRulesBasedModifiersForCardsInPlay() {
        // Rule about using WEAPONS
        List<Modifier> result = new ArrayList<>();
        CardFilter facilityOrExposedShip = Filters.or(Filters.exposedShip, CardType.FACILITY);
        CardFilter controllerControlsMatchingPersonnelAboard = Filters.controllerControlsMatchingPersonnelAboard;
        CardFilter affectedFilter = Filters.and(
                Filters.or(Filters.ship, Filters.facility),
                Filters.notAll(Filters.active, facilityOrExposedShip, controllerControlsMatchingPersonnelAboard)
        );
        Modifier weaponsModifier = new WeaponsDisabledModifier(affectedFilter);
        result.add(weaponsModifier);
        return result;
    }

}