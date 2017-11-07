package me.jakemoritz.animebuzz.dagger.modules;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="user", strict = false)
public class MalVerifyCredentialsWrapper {

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
