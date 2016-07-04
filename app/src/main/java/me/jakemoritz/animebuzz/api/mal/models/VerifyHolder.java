package me.jakemoritz.animebuzz.api.mal.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="user", strict = false)
public class VerifyHolder {

    @Element(name="username")
    private String username;

    @Element(name="id")
    private String userID;

    public String getUsername() {
        return username;
    }

    public String getUserID() {
        return userID;
    }
}
