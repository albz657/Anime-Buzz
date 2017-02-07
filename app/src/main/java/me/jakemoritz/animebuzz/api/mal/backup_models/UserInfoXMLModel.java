package me.jakemoritz.animebuzz.api.mal.backup_models;

/**
 * Created by jakem on 2/7/2017.
 */

public class UserInfoXMLModel
{
    private String user_name;

    private String user_onhold;

    private String user_watching;

    private String user_plantowatch;

    private String user_completed;

    private String user_dropped;

    private String user_id;

    private String user_days_spent_watching;

    public String getUser_name ()
    {
        return user_name;
    }

    public void setUser_name (String user_name)
    {
        this.user_name = user_name;
    }

    public String getUser_onhold ()
    {
        return user_onhold;
    }

    public void setUser_onhold (String user_onhold)
    {
        this.user_onhold = user_onhold;
    }

    public String getUser_watching ()
    {
        return user_watching;
    }

    public void setUser_watching (String user_watching)
    {
        this.user_watching = user_watching;
    }

    public String getUser_plantowatch ()
    {
        return user_plantowatch;
    }

    public void setUser_plantowatch (String user_plantowatch)
    {
        this.user_plantowatch = user_plantowatch;
    }

    public String getUser_completed ()
    {
        return user_completed;
    }

    public void setUser_completed (String user_completed)
    {
        this.user_completed = user_completed;
    }

    public String getUser_dropped ()
    {
        return user_dropped;
    }

    public void setUser_dropped (String user_dropped)
    {
        this.user_dropped = user_dropped;
    }

    public String getUser_id ()
    {
        return user_id;
    }

    public void setUser_id (String user_id)
    {
        this.user_id = user_id;
    }

    public String getUser_days_spent_watching ()
    {
        return user_days_spent_watching;
    }

    public void setUser_days_spent_watching (String user_days_spent_watching)
    {
        this.user_days_spent_watching = user_days_spent_watching;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [user_name = "+user_name+", user_onhold = "+user_onhold+", user_watching = "+user_watching+", user_plantowatch = "+user_plantowatch+", user_completed = "+user_completed+", user_dropped = "+user_dropped+", user_id = "+user_id+", user_days_spent_watching = "+user_days_spent_watching+"]";
    }
}
