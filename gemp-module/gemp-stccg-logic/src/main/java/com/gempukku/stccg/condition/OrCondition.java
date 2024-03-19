package com.gempukku.stccg.condition;

public class OrCondition implements Condition {
    private final Condition[] _conditions;

    public OrCondition(Condition... conditions) {
        _conditions = conditions;
    }

    @Override
    public boolean isFulfilled() {
        for (Condition condition : _conditions) {
            if (condition != null && condition.isFulfilled())
                return true;
        }

        return false;
    }
}
