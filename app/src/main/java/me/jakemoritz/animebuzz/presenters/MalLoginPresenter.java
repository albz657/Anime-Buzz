package me.jakemoritz.animebuzz.presenters;

public class MalLoginPresenter implements MalLoginListener {

    private MalLoginListener malLoginListener;

    public MalLoginPresenter(MalLoginListener malLoginListener) {
        this.malLoginListener = malLoginListener;
    }

    @Override
    public void logInToMal(String username, String password) {
        malLoginListener.logInToMal(username, password);
    }
}
