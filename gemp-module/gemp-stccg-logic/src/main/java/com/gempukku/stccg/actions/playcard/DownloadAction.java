package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public interface DownloadAction extends TopLevelSelectableAction {

    Collection<? extends PhysicalCard> getDownloadableTargets(DefaultGame cardGame);

    void setCardToDownload(PhysicalCard cardToDownload);

}