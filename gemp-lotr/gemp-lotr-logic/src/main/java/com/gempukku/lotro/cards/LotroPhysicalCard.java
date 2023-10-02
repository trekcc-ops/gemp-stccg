package com.gempukku.lotro.cards;

import com.gempukku.lotro.actions.OptionalTriggerAction;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.effects.EffectResult;
import com.gempukku.lotro.game.DefaultGame;

import java.util.List;

public interface LotroPhysicalCard extends Filterable {
    Zone getZone();
    String getBlueprintId();
    String getImageUrl();

    String getOwner();

    String getCardController();

    int getCardId();

    LotroCardBlueprint getBlueprint();

    LotroPhysicalCard getAttachedTo();

    LotroPhysicalCard getStackedOn();

    void setWhileInZoneData(Object object);

    Object getWhileInZoneData();

    void setSiteNumber(Integer number);

    Integer getSiteNumber();
    String getTitle();
    List<OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, DefaultGame game,
                                                               EffectResult effectResult,
                                                               LotroPhysicalCard self);

}
