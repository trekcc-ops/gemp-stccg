package com.gempukku.stccg.chat;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class ChatStrings {

    public final static String SYSTEM_USER_ID = "System";
    final static String ALL_USERS_PREFIX = "@everyone ";

    private ChatStrings() {
    }

    public static String user(String userId) {
        return "User " + userId;
    }

    static String userJoinedRoom(String userId) { return userId + " joined the room"; }
    static String userLeftRoom(String userId) { return userId + " left the room"; }

    @SuppressWarnings({"All"})
    /* Despite a thorough culling of most Lord of the Rings content in this code base for the Star Trek implementation,
        this is delightful and I never want to lose it. */
    static List<String> getIgnoredUserMessages() {
        List<String> result = new LinkedList<>();
        result.add("You don't have any friends. Nobody likes you.");
        result.add("Not listening. Not listening!");
        result.add("You're a liar and a thief.");
        result.add("Nope.");
        result.add("Murderer!");
        result.add("Go away. Go away!");
        result.add("Hahahaha!");
        result.add("I hate you, I hate you.");
        result.add("Where would you be without me? Gollum, Gollum. I saved us. It was me. We survived because of me!");
        result.add("Not anymore.");
        result.add("What did you say?");
        result.add("Master looks after us now. We don't need you.");
        result.add("What?");
        result.add("Leave now and never come back.");
        result.add("No!");
        result.add("Leave now and never come back!");
        result.add("Argh!");
        result.add("Leave NOW and NEVER COME BACK!");
        result.add("...");
        result.add("We... We told him to go away! And away he goes, preciouss! Gone, gone, gone! Smeagol is free!");
        return result;
    }
}