package me.jakemoritz.animebuzz.interfaces.mal;

public interface MalCredentialsVerifiedListener {
    void malCredentialsVerified(boolean verified);
    void malCredentialsVerified(boolean verified, String MALID);
}
