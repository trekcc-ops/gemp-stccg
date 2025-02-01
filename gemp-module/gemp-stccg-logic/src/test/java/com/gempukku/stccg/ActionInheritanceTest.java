package com.gempukku.stccg;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class ActionInheritanceTest {

    @Test
    public void inheritanceTest() {
        // List that can only be used in ST1E
        List<GameAction<? super ST1EGamey>> st1eActions = new LinkedList<>();
        st1eActions.add(new DefaultGameAction());
        st1eActions.add(new ST1EGameAction());
//        st1eActions.add(new ST2EGameAction()); // This should fail

        for (GameAction<? super ST1EGamey> action : st1eActions) {
//            action.doEffect(new DefaultGame()); // This should fail
            action.doEffect(new ST1EGamey());
//            action.doEffect(new ST2EGame()); // This should fail
        }

        // List that can be used in any game
        List<GameAction<DefaultGamey>> defaultActions = new LinkedList<>();
        defaultActions.add(new DefaultGameAction());
//        defaultActions.add(new ST1EGameAction()); // This should fail
//        defaultActions.add(new ST2EGameAction()); // This should fail

        for (GameAction<DefaultGamey> action : defaultActions) {
//            action.doEffect(new DefaultGame());
            action.doEffect(new ST1EGamey());
            action.doEffect(new ST2EGame());
        }

        // List that can have any game added to it, but can't be used in any game
        List<GameAction<? extends DefaultGamey>> defaultActions2 = new LinkedList<>();
        defaultActions2.add(new DefaultGameAction());
        defaultActions2.add(new ST1EGameAction()); // This should fail
        defaultActions2.add(new ST2EGameAction()); // This should fail

        for (GameAction<? extends DefaultGamey> action : defaultActions2) {
//            action.doEffect(new DefaultGame());
//            action.doEffect(new ST1EGame());
//            action.doEffect(new ST2EGame());
        }



        takeList(st1eActions);
        takeList(defaultActions);

    }

    private <T extends DefaultGamey> GameAction<T> createAction(T cardGame) {
        if (cardGame instanceof ST1EGamey) {
            GameAction<T> result = (GameAction<T>) new ST1EGameAction();
            return result;
        }
        return null;
    }

    private void takeList(List<? extends GameAction<?>> list) {
        for (GameAction action : list) {
//            action.doEffect(new DefaultGame());
        }
    }

    private <T extends DefaultGamey> void takeIntrfc(GameAction<T> blork, T classObj) {
        blork.doEffect(classObj);
    }

    private interface GameAction<T extends DefaultGamey> {
        void doEffect(T t);
    }

    private abstract class DefaultGamey {

    }

    private class ST1EGamey extends DefaultGamey {

    }

    private class ST2EGame extends DefaultGamey {

    }

    private abstract class AbstractGameAction<T extends DefaultGamey> implements GameAction<T> {

    }
    
    private class DefaultGameAction extends AbstractGameAction<DefaultGamey> implements GameAction<DefaultGamey> {
        @Override
        public void doEffect(DefaultGamey parent) {
            System.out.println("This is a Parent object");
        }
    }


    private class ST1EGameAction extends AbstractGameAction<ST1EGamey> implements GameAction<ST1EGamey> {
        @Override
        public void doEffect(ST1EGamey first) {
            System.out.println("This is a First object");
        }
    }

    private class ST2EGameAction implements GameAction<ST2EGame> {
        @Override
        public void doEffect(ST2EGame second) {
            System.out.println("This is a Second object");
        }
    }

}