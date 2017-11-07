package me.jakemoritz.animebuzz.presenters;

public interface SetupListener {
    void finishSetup();
    void logInToMal(String username, String password);
}
