package me.jakemoritz.animebuzz.interfaces.mal;

public interface VerifyCredentialsResponse {
    void verifyCredentialsResponseReceived(boolean verified);
    void verifyCredentialsResponseReceived(boolean verified, String MALID);
}
