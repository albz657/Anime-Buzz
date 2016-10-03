package me.jakemoritz.animebuzz.api.hummingbird;

public class HummingbirdAnimeHolder {

    private String englishTitle;
    private String imageURL;
    private String finishedAiringDate;
    private String startedAiringDate;
    private String showType;
    private int episodeCount;

    public HummingbirdAnimeHolder(String englishTitle, String imageURL, String finishedAiringDate, String startedAiringDate, String showType, int episodeCount) {
        this.englishTitle = englishTitle;
        this.imageURL = imageURL;
        this.finishedAiringDate = finishedAiringDate;
        this.startedAiringDate = startedAiringDate;
        this.showType = showType;
        this.episodeCount = episodeCount;
    }

    public int getEpisodeCount() {
        return episodeCount;
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

    public String getShowType() {
        return showType;
    }
}
