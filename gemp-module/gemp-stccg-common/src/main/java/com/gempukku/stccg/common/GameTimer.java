package com.gempukku.stccg.common;

import java.util.Locale;

public record GameTimer(boolean longGame, String name, int maxSecondsPerPlayer, int maxSecondsPerDecision) {

    public static final GameTimer DEBUG_TIMER =
            new GameTimer(false, "Debug", 60 * 120, 60 * 120);
    private static final GameTimer DEFAULT_TIMER =
            new GameTimer(false, "Default", 60 * 80, 60 * 5);
    private static final GameTimer BLITZ_TIMER =
            new GameTimer(false, "Blitz!", 60 * 30, 60 * 5);
    private static final GameTimer SLOW_TIMER =
            new GameTimer(false, "Slow", 60 * 120, 60 * 10);
    public static final GameTimer GLACIAL_TIMER =
            new GameTimer(true, "Glacial",
                    60 * 60 * 24 * 3, 60 * 60 * 24);
    // 5 minutes timeout, 40 minutes per game per player
    public static final GameTimer COMPETITIVE_TIMER =
            new GameTimer(false, "Competitive", 60 * 40, 60 * 5);
    public static final GameTimer TOURNAMENT_TIMER =
            new GameTimer(false, "Tournament", 60 * 40, 60 * 5);

    public static GameTimer ResolveTimer(String timer) {
        if (timer == null) return DEFAULT_TIMER;
        return switch(timer.toLowerCase(Locale.ROOT)) {
            case "debug" -> DEBUG_TIMER;
            case "blitz" -> BLITZ_TIMER;
            case "slow" -> SLOW_TIMER;
            case "glacial" -> GLACIAL_TIMER;
            default -> DEFAULT_TIMER;
        };
    }
}