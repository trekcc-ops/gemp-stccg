package com.gempukku.stccg.cards;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.GameState;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ActionContext {
    Map<String, String> getValueMemory();
    Multimap<String, PhysicalCard> getCardMemory();
    DefaultGame getGame();
    GameState getGameState();
    void setValueToMemory(String memory, String value);
    String getValueFromMemory(String memory);
    void setCardMemory(String memory, PhysicalCard card);
    void setCardMemory(String memory, Collection<? extends PhysicalCard> cards);
    Collection<PhysicalCard> getCardsFromMemory(String memory);
    PhysicalCard getCardFromMemory(String memory);
    Player getPerformingPlayer();
    String getPerformingPlayerId();
    PhysicalCard getSource();
    ActionResult getEffectResult();

    boolean acceptsAllRequirements(Requirement[] requirementArray);
    boolean acceptsAllRequirements(List<Requirement> requirementList);
    boolean acceptsAnyRequirements(Requirement[] requirementArray);

    ActionContext createDelegateContext(ActionResult actionResult);
    ActionContext createDelegateContext(String playerId);
    String substituteText(String text);

    List<PhysicalCard> getZoneCards(Player player, Zone zone);


    ActionContext getParentContext();
}