package com.gempukku.lotro.cards;

import com.gempukku.lotro.cards.actions.PlayPermanentAction;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

import java.util.LinkedList;
import java.util.List;

public class AbstractMinion extends AbstractLotroCardBlueprint {
    private int _twilightCost;
    private int _strength;
    private int _vitality;
    private int _site;
    private Keyword _race;

    public AbstractMinion(int twilightCost, int strength, int vitality, int site, Keyword race, Culture culture, String name) {
        this(twilightCost, strength, vitality, site, race, culture, name, false);
    }

    public AbstractMinion(int twilightCost, int strength, int vitality, int site, Keyword race, Culture culture, String name, boolean unique) {
        super(Side.SHADOW, CardType.MINION, culture, name, unique);
        _twilightCost = twilightCost;
        _strength = strength;
        _vitality = vitality;
        _site = site;
        _race = race;
        addKeyword(_race);
    }

    private void appendPlayMinionActions(List<Action> actions, String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canPlayCardDuringPhase(game, Phase.SHADOW, self))
            actions.add(getPlayCardAction(playerId, game, self, 0));
    }

    @Override
    public final List<? extends Action> getPhaseActions(String playerId, LotroGame game, PhysicalCard self) {
        List<Action> actions = new LinkedList<Action>();

        if (checkPlayRequirements(playerId, game, self, 0))
            appendPlayMinionActions(actions, playerId, game, self);

        List<? extends Action> extraActions = getExtraPhaseActions(playerId, game, self);
        if (extraActions != null)
            actions.addAll(extraActions);

        return actions;
    }

    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self, int twilightModifier) {
        return PlayConditions.checkUniqueness(game.getGameState(), game.getModifiersQuerying(), self)
                && PlayConditions.canPayForShadowCard(game, self, twilightModifier);
    }

    public Action getPlayCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier) {
        return new PlayPermanentAction(self, Zone.SHADOW_CHARACTERS);
    }

    protected List<? extends Action> getExtraPhaseActions(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public int getTwilightCost() {
        return _twilightCost;
    }

    @Override
    public int getStrength() {
        return _strength;
    }

    @Override
    public int getVitality() {
        return _vitality;
    }

    @Override
    public int getSiteNumber() {
        return _site;
    }
}
