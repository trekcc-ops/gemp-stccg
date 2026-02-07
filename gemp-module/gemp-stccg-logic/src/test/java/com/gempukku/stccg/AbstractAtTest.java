package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("MethodWithMultipleReturnPoints")
public abstract class AbstractAtTest implements UserInputSimulator {

    protected String P1 = "player1";
    protected String P2 = "player2";
    protected List<String> _players = List.of("player1", "player2");

    protected ST1EGame _game;
    protected static final CardBlueprintLibrary _cardLibrary = new CardBlueprintLibrary();
    protected FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);

    public DefaultGame getGame() {
        return _game;
    }


    protected boolean personnelAttributesAre(PersonnelCard personnel, List<Integer> attributeValues) {
        if (!Objects.equals(personnel.getIntegrity(_game), attributeValues.get(0))) {
            return false;
        }
        if (!Objects.equals(personnel.getCunning(_game), attributeValues.get(1))) {
            return false;
        }
        if (!Objects.equals(personnel.getStrength(_game), attributeValues.get(2))) {
            return false;
        }
        return true;
    }


}