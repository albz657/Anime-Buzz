package me.jakemoritz.animebuzz.model;

import com.google.gson.annotations.SerializedName;

public class SenpaiAnime {

    private String name;

    @SerializedName("MALID")
    private String malId;

    @SerializedName("simulcast")
    private String simulcastProvider;

    private boolean missingAirtime;

    @SerializedName("airdate_u")
    private long airTime;

    @SerializedName("simulcast_airdate_u")
    private long simulcastTime;

    public SenpaiAnime(String name, String malId, String simulcastProvider, boolean missingAirtime, long airTime, long simulcastTime) {
        this.name = name;
        this.malId = malId;
        this.simulcastProvider = simulcastProvider;
        this.missingAirtime = missingAirtime;
        this.airTime = airTime;
        this.simulcastTime = simulcastTime;
    }

    public String getName() {
        return name;
    }

    public String getMalId() {
        return malId;
    }

    public String getSimulcastProvider() {
        return simulcastProvider;
    }

    public boolean isMissingAirtime() {
        return missingAirtime;
    }

    public long getAirTime() {
        return airTime;
    }

    public long getSimulcastTime() {
        return simulcastTime;
    }
}
