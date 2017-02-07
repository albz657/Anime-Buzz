package me.jakemoritz.animebuzz.api.mal.backup_models;

public class UserXMLModel {

    private MALXMLModel MALXMLModel;

    public MALXMLModel getMALXMLModel() {
        return MALXMLModel;
    }

    public void setMALXMLModel(MALXMLModel MALXMLModel) {
        this.MALXMLModel = MALXMLModel;
    }

    @Override
    public String toString() {
        return "ClassPojo [MALXMLModel = " + MALXMLModel + "]";
    }
}

