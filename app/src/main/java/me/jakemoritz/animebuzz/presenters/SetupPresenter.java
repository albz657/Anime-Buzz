package me.jakemoritz.animebuzz.presenters;

public class SetupPresenter implements SetupListener {

    private SetupListener setupListener;

    public SetupPresenter(SetupListener setupListener) {
        this.setupListener = setupListener;
    }

    @Override
    public void finishSetup() {
        setupListener.finishSetup();
    }
}
