package com.gempukku.stccg.cards.blueprints;

public class PointBox {
    private int _pointsShowing;

    public PointBox(int points) {
        _pointsShowing = points;
    }

    public int getPointsShown() {
        return _pointsShowing;
    }
}