package com.gempukku.stccg;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class ActionInheritanceTest extends AbstractAtTest {

    @Test
    public void inheritanceTest() {
        FirstClass firstObj = new FirstClass();
        Interface<FirstClass> firstInter = new FirstInter();
        Interface<ParentClass> parentInter = new ParentInter();

        SecondClass secondObj = new SecondClass();
        Interface<SecondClass> secondInter = new SecondInter();

        List<Interface<? super FirstClass>> interList = new LinkedList<>();
        interList.add(firstInter);
        interList.add(parentInter);

        for (Interface<? super FirstClass> intr : interList) {
            takeIntrfc(intr, firstObj);
        }

    }

    private <T extends ParentClass> void takeIntrfc(Interface<T> blork, T classObj) {
        blork.doEffect(classObj);
    }

    private interface Interface<T extends ParentClass> {
        void doEffect(T t);
    }

    private class ParentClass {

    }

    private class FirstClass extends ParentClass {

    }

    private class SecondClass extends ParentClass {

    }

    private class ParentInter implements Interface<ParentClass> {
        @Override
        public void doEffect(ParentClass parent) {
            System.out.println("This is a Parent object");
        }
    }


    private class FirstInter implements Interface<FirstClass> {
        @Override
        public void doEffect(FirstClass first) {
            System.out.println("This is a First object");
        }
    }

    private class SecondInter implements Interface<SecondClass> {
        @Override
        public void doEffect(SecondClass second) {
            System.out.println("This is a Second object");
        }
    }

}