package me.jakemoritz.animebuzz.api.hummingbird;

public class KitsuAnimeHolder {

    private String englishTitle;
    private String imageURL;
    private String finishedAiringDate;
    private String startedAiringDate;
    private String showType;
    private int episodeCount;
    private String kitsuId;

    KitsuAnimeHolder(String englishTitle, String imageURL, String finishedAiringDate, String startedAiringDate, String showType, int episodeCount, String kitsuId) {
        this.englishTitle = englishTitle;
        this.imageURL = imageURL;
        this.finishedAiringDate = finishedAiringDate;
        this.startedAiringDate = startedAiringDate;
        this.showType = showType;
        this.episodeCount = episodeCount;
        this.kitsuId = kitsuId;
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

    public String getKitsuId() {
        return kitsuId;
    }
}
