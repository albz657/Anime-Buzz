package me.jakemoritz.animebuzz.api.hummingbird;

public class HummingbirdAnimeHolder {

    private String englishTitle;
    private String imageURL;
    private String finishedAiringDate;
    private String startedAiringDate;

    public HummingbirdAnimeHolder(String englishTitle, String imageURL, String finishedAiringDate, String startedAiringDate) {
        this.englishTitle = englishTitle;
        this.imageURL = imageURL;
        this.finishedAiringDate = finishedAiringDate;
        this.startedAiringDate = startedAiringDate;
    }

    public String getStartedAiringDate() {
        return startedAiringDate;
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
