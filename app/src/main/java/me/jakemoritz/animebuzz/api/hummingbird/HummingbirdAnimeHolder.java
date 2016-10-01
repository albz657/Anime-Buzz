package me.jakemoritz.animebuzz.api.hummingbird;

public class HummingbirdAnimeHolder {

    private String englishTitle;
    private String imageURL;
    private String finishedAiringDate;

    public HummingbirdAnimeHolder(String englishTitle, String imageURL, String finishedAiringDate) {
        this.englishTitle = englishTitle;
        this.imageURL = imageURL;
        this.finishedAiringDate = finishedAiringDate;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getFinishedAiringDate() {
        return finishedAiringDate;
    }
}
