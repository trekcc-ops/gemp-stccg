package com.gempukku.lotro.decisions;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAwaitingDecision implements AwaitingDecision {
    private final int _id;
    private final String _text;
    private final AwaitingDecisionType _decisionType;
    private final Map<String, String[]> _params = new HashMap<>();

    public AbstractAwaitingDecision(int id, String text, AwaitingDecisionType decisionType) {
        _id = id;
        _text = text;
        _decisionType = decisionType;
    }

    protected void setParam(String name, String value) {
        setParam(name, new String[] {value});
    }

    protected void setParam(String name, String[] value) {
        _params.put(name, value);
    }

    @Override
    public int getAwaitingDecisionId() {
        return _id;
    }

    @Override
    public String getText() {
        return _text;
    }

    @Override
    public AwaitingDecisionType getDecisionType() {
        return _decisionType;
    }

    @Override
    public Map<String, String[]> getDecisionParameters() {
        return _params;
    }
}