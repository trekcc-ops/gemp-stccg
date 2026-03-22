package com.gempukku.stccg.common;

public enum ComparatorType {
    EQUAL_TO,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL_TO,
    LESS_THAN,
    LESS_THAN_OR_EQUAL_TO;

    public boolean isTrue(float num1, float num2) {
        return switch(this) {
            case EQUAL_TO -> num1 == num2;
            case GREATER_THAN -> num1 > num2;
            case GREATER_THAN_OR_EQUAL_TO -> num1 >= num2;
            case LESS_THAN -> num1 < num2;
            case LESS_THAN_OR_EQUAL_TO -> num1 <= num2;
        };
    }
}