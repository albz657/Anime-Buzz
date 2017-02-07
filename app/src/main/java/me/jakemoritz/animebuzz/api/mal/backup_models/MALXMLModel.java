package me.jakemoritz.animebuzz.api.mal.backup_models;

import java.util.ArrayList;

public class MALXMLModel {
    private UserInfoXMLModel userInfoXMLModel;

    private ArrayList<MALAnimeXMLModel> MALAnimeXMLModelList;

    public MALXMLModel() {
        this.MALAnimeXMLModelList = new ArrayList<>();
    }

    public UserInfoXMLModel getUserInfoXMLModel() {
        return userInfoXMLModel;
    }

    public void setUserInfoXMLModel(UserInfoXMLModel userInfoXMLModel) {
        this.userInfoXMLModel = userInfoXMLModel;
    }

    public ArrayList<MALAnimeXMLModel> getMALAnimeXMLModelList() {
        return MALAnimeXMLModelList;
    }

    public void setMALAnimeXMLModelList(ArrayList<MALAnimeXMLModel> MALAnimeXMLModelList) {
        this.MALAnimeXMLModelList = MALAnimeXMLModelList;
    }

    @Override
    public String toString() {
        return "ClassPojo [userInfoXMLModel = " + userInfoXMLModel + ", MALAnimeXMLModel = ";
    }
}

