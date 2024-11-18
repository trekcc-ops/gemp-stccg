package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.game.Player;

public class CanSeedAUIconCardsModifier extends AbstractModifier {

    private Player _affectedPlayer;

    public CanSeedAUIconCardsModifier(Player affectedPlayer) {
        super(affectedPlayer.getGame(), ModifierEffect.AU_CARDS_ENTER_PLAY);
        _affectedPlayer = affectedPlayer;
    }

    public Player getAffectedPlayer() {
        return _affectedPlayer;
    }
}