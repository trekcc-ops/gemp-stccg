package com.gempukku.stccg.hall;

public record GameTimer(boolean longGame, String name, int maxSecondsPerPlayer, int maxSecondsPerDecision) {

    private static final GameTimer DEBUG_TIMER =
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
        if (timer != null) {
            switch (timer.toLowerCase()) {
                case "debug" -> { return DEBUG_TIMER; }
                case "blitz" -> { return BLITZ_TIMER; }
                case "slow" -> { return SLOW_TIMER; }
                case "glacial" -> { return GLACIAL_TIMER; }
            }
        }
        return DEFAULT_TIMER;
    }
}