package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.SelectCardsResolver;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SkillDotCountEvaluatorTest extends AbstractAtTest {

    @Test
    public void evaluatorTest() throws CardNotFoundException, PlayerNotFoundException, InvalidGameLogicException {
        setupSimple1EGame(50);
        assertNotNull(_game);
        PersonnelCard spock = (PersonnelCard) newCardForGame("106_018", P1);
        assertEquals(spock.getSkillDotCount(), 5);

        SelectVisibleCardAction selectCardAction = new SelectVisibleCardAction(_game, P1, "", List.of(spock));
        Evaluator dotEvaluator = new SkillDotCountEvaluator(selectCardAction);

        selectCardAction.nextAction(_game);
        assertTrue(selectCardAction.wasCarriedOut());

        assertEquals(dotEvaluator.evaluateExpression(_game), 5);
    }

    // WIP
    public void serializeTest() throws CardNotFoundException, JsonProcessingException {
        setupSimple1EGame(50);
        assertNotNull(_game);
        PersonnelCard spock = (PersonnelCard) newCardForGame("106_018", P1);
        assertEquals(spock.getSkillDotCount(), 5);

        SelectVisibleCardAction selectCardAction = new SelectVisibleCardAction(_game, P1, "", List.of(spock));
        ActionCardResolver resolver = new SelectCardsResolver(selectCardAction);
        JsonNode resolverJson = new ObjectMapper().valueToTree(resolver);
        System.out.println(resolverJson);
        ActionCardResolver resolverCopy = new ObjectMapper().treeToValue(resolverJson, ActionCardResolver.class);
/*        ValueSource dotEvaluator = new SkillDotCountEvaluator(selectCardAction);

        JsonNode evaluatorJson = new ObjectMapper().valueToTree(dotEvaluator);
        System.out.println(evaluatorJson);

        ValueSource dotEvaluatorCopy = new ObjectMapper().treeToValue(evaluatorJson, ValueSource.class);*/

    }

}