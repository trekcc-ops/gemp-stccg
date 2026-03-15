package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public interface DownloadAction extends CardPerformedAction {

    Collection<? extends PhysicalCard> getDownloadableTargets(DefaultGame cardGame);

    void selectCardToDownload(PhysicalCard cardToDownload) throws DecisionResultInvalidException;

}