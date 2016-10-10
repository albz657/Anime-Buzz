package me.jakemoritz.animebuzz.api.hummingbird;

public class HummingbirdAnimeHolder {

    private String englishTitle;
    private String imageURL;
    private String finishedAiringDate;
    private String startedAiringDate;
    private String showType;
    private int episodeCount;

    HummingbirdAnimeHolder(String englishTitle, String imageURL, String finishedAiringDate, String startedAiringDate, String showType, int episodeCount) {
        this.englishTitle = englishTitle;
        this.imageURL = imageURL;
        this.finishedAiringDate = finishedAiringDate;
        this.startedAiringDate = startedAiringDate;
        this.showType = showType;
        this.episodeCount = episodeCount;
    }

    int getEpisodeCount() {
        return episodeCount;
    }

    String getStartedAiringDate() {
        return startedAiringDate;
    }

    String getEnglishTitle() {
        return englishTitle;
    }

    String getImageURL() {
        return imageURL;
    }

    String getFinishedAiringDate() {
        return finishedAiringDate;
    }

    String getShowType() {
        return showType;
    }
}
