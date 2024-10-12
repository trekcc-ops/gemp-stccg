package com.gempukku.stccg.db;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@SuppressWarnings("unused") // This class is used but IDEA doesn't detect it
public class DBData {

    public static class GameHistory {

        public int id;
        public String gameId;

        public String winner;
        public int winnerId;
        public String loser;
        public int loserId;

        public String win_reason;
        public String lose_reason;

        public String win_recording_id;
        public String lose_recording_id;

        public ZonedDateTime start_date;
        public ZonedDateTime end_date;

        public String format_name;

        public String winner_deck_name;
        public String loser_deck_name;

        public String tournament;

        public int winner_site;
        public int loser_site;

        public String game_length_type;
        public int max_game_time;
        public int game_timeout;
        public int winner_clock_remaining;
        public int loser_clock_remaining;

        public int replay_version = -1;
    }

    public static class Collection {
        public int id;
        public int player_id;
        public String type;
        public String extra_info;
    }

    public static class CollectionEntry {
        public int collection_id;
        public int quantity;
        public String product_type;
        public String product_variant;
        public String product;
        public String source;
        public LocalDateTime created_date;
        public LocalDateTime modified_date;
        public String notes;
    }

    public static class DBPlayer {
        public int id;
        public String name;
        public String password;
        public String type;
        public Integer last_login_reward;
        public Integer banned_until;
        public String create_ip;
        public String last_ip;

        public Date GetBannedUntilDate()
        {
            if(banned_until == null)
                return null;
            return new Date(banned_until);
        }
    }

}