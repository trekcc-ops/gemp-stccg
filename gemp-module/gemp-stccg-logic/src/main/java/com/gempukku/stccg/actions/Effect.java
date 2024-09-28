package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.game.DefaultGame;

public interface Effect {

    /**
     * Returns the text that represents this effect. This text might be displayed
     * to the user.
     *
     * @return
     */
    String getText();

    /**
     * Returns the type of the effect. This should list the type of effect it represents
     * if the effect is a recognizable by the game.
     *
     * @return
     */
    EffectType getType();

    /**
     * Checks whether this effect can be played in full. This is required to check
     * for example for cards that give a choice of effects to carry out and one
     * that can be played in full has to be chosen.
     *
     * @return
     */
    boolean isPlayableInFull();

    /**
     * Plays the effect and emits the results.
     *
     * @return
     */
    void playEffect();


    /**
     * Returns if the effect was carried out (not prevented) in full. This is required
     * for checking if effect that player can prevent by paying some cost should be
     * played anyway. If it was prevented, the original event has to be played.
     *
     * @return
     */
    boolean wasCarriedOut();

    default PhysicalCard getSource() { return null; }

    String getPerformingPlayerId();

    DefaultGame getGame();

}
