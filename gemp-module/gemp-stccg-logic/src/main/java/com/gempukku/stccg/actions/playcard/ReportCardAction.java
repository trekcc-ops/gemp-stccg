package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ReportCardResolver;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.Collections;

public class ReportCardAction extends STCCGPlayCardAction {
    private final ReportCardResolver _targetResolver;

    private ReportCardAction(DefaultGame cardGame, ReportableCard cardToPlay, boolean forFree,
                             ReportCardResolver targetResolver) {
        // TODO - Zone is null because these will be attached and the implementation is weird
        super(cardGame, cardToPlay, null, cardToPlay.getOwnerName(), forFree);
        _targetResolver = targetResolver;
        _cardTargets.add(targetResolver);
    }

    public ReportCardAction(DefaultGame cardGame, ReportableCard cardToPlay, boolean forFree) {
        this(cardGame, cardToPlay, forFree, new ReportCardResolver(cardToPlay));
    }

    public ReportCardAction(DefaultGame cardGame, ReportableCard cardToPlay, boolean forFree,
                            Collection<FacilityCard> destinationOptions) {
        this(cardGame, cardToPlay, forFree, new ReportCardResolver(cardToPlay, destinationOptions));
    }

    public ReportCardAction(DefaultGame cardGame, ReportableCard cardToPlay, boolean forFree,
                            FacilityCard facilityCard) {
        this(cardGame, cardToPlay, forFree, new ReportCardResolver(cardToPlay, facilityCard));
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return _cardEnteringPlay.canBePlayed(cardGame) && !_targetResolver.cannotBeResolved(cardGame);
    }

    public void processEffect(DefaultGame cardGame) {
        try {
            if (_cardEnteringPlay instanceof ReportableCard reportable) {
                Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
                if (reportable instanceof AffiliatedCard affiliatedCard) {
                    Affiliation chosenAffiliation = _targetResolver.getAffiliationToReportAs();
                    affiliatedCard.setCurrentAffiliation(chosenAffiliation);
                    performingPlayer.addPlayedAffiliation(chosenAffiliation);
                }
                setAsSuccessful();

                FacilityCard facility = _targetResolver.getDestinationFacility();
                GameState gameState = cardGame.getGameState();

                cardGame.removeCardsFromZone(Collections.singleton(reportable));
                reportable.setLocationId(cardGame, facility.getLocationId());
                reportable.attachTo(facility);
                gameState.addCardToZone(cardGame, reportable, Zone.ATTACHED, _actionContext);

                if (reportable instanceof ShipCard ship) {
                    ship.dockAtFacility(facility);
                }

                saveResult(new PlayCardResult(this, _cardEnteringPlay), cardGame);
            } else {
                throw new InvalidGameLogicException("Tried to report a non-reportable card");
            }
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    public void setDestination(FacilityCard card) {
        _targetResolver.setDestination(card);
    }

    public void setAffiliation(Affiliation affiliation) {
        _targetResolver.setAffiliation(affiliation);
    }


}