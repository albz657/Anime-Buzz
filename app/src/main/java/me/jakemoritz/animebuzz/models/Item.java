
package me.jakemoritz.animebuzz.models;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Item {

    private String name;
    private String mALID;
    private String aNNID;
    private String notes;
    private String airdate;
    private boolean missingAirdate;
    private boolean missingAirtime;
    private boolean isShort;
    private boolean quip;
    private boolean simulcast;
    private int simulcastDelayOrig;
    private boolean simulcastDelay;
    private boolean simulcastLink;
    private boolean simulcastInvalid;
    private String simulcastClass;
    private String fansubClass;
    private String airdateOrig;
    private int airdateU;
    private boolean isAired;
    private int timeToAirU;
    private String timeToAirRd;
    private int simulcastAirdateU;
    private SimulcastAirdates simulcastAirdates;
    private boolean isSimulcastAired;
    private int simulcastTimeToAirU;
    private String simulcastTimeToAirRd;
    private boolean simulcastDelayRd;
    private boolean simulcastDelayType;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The mALID
     */
    public String getMALID() {
        return mALID;
    }

    /**
     * 
     * @param mALID
     *     The MALID
     */
    public void setMALID(String mALID) {
        this.mALID = mALID;
    }

    /**
     * 
     * @return
     *     The aNNID
     */
    public String getANNID() {
        return aNNID;
    }

    /**
     * 
     * @param aNNID
     *     The ANNID
     */
    public void setANNID(String aNNID) {
        this.aNNID = aNNID;
    }

    /**
     * 
     * @return
     *     The notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * 
     * @param notes
     *     The notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * 
     * @return
     *     The airdate
     */
    public String getAirdate() {
        return airdate;
    }

    /**
     * 
     * @param airdate
     *     The airdate
     */
    public void setAirdate(String airdate) {
        this.airdate = airdate;
    }

    /**
     * 
     * @return
     *     The missingAirdate
     */
    public boolean isMissingAirdate() {
        return missingAirdate;
    }

    /**
     * 
     * @param missingAirdate
     *     The missingAirdate
     */
    public void setMissingAirdate(boolean missingAirdate) {
        this.missingAirdate = missingAirdate;
    }

    /**
     * 
     * @return
     *     The missingAirtime
     */
    public boolean isMissingAirtime() {
        return missingAirtime;
    }

    /**
     * 
     * @param missingAirtime
     *     The missingAirtime
     */
    public void setMissingAirtime(boolean missingAirtime) {
        this.missingAirtime = missingAirtime;
    }

    /**
     * 
     * @return
     *     The isShort
     */
    public boolean isIsShort() {
        return isShort;
    }

    /**
     * 
     * @param isShort
     *     The isShort
     */
    public void setIsShort(boolean isShort) {
        this.isShort = isShort;
    }

    /**
     * 
     * @return
     *     The quip
     */
    public boolean isQuip() {
        return quip;
    }

    /**
     * 
     * @param quip
     *     The quip
     */
    public void setQuip(boolean quip) {
        this.quip = quip;
    }

    /**
     * 
     * @return
     *     The simulcast
     */
    public boolean isSimulcast() {
        return simulcast;
    }

    /**
     * 
     * @param simulcast
     *     The simulcast
     */
    public void setSimulcast(boolean simulcast) {
        this.simulcast = simulcast;
    }

    /**
     * 
     * @return
     *     The simulcastDelayOrig
     */
    public int getSimulcastDelayOrig() {
        return simulcastDelayOrig;
    }

    /**
     * 
     * @param simulcastDelayOrig
     *     The simulcast_delay_orig
     */
    public void setSimulcastDelayOrig(int simulcastDelayOrig) {
        this.simulcastDelayOrig = simulcastDelayOrig;
    }

    /**
     * 
     * @return
     *     The simulcastDelay
     */
    public boolean isSimulcastDelay() {
        return simulcastDelay;
    }

    /**
     * 
     * @param simulcastDelay
     *     The simulcast_delay
     */
    public void setSimulcastDelay(boolean simulcastDelay) {
        this.simulcastDelay = simulcastDelay;
    }

    /**
     * 
     * @return
     *     The simulcastLink
     */
    public boolean isSimulcastLink() {
        return simulcastLink;
    }

    /**
     * 
     * @param simulcastLink
     *     The simulcast_link
     */
    public void setSimulcastLink(boolean simulcastLink) {
        this.simulcastLink = simulcastLink;
    }

    /**
     * 
     * @return
     *     The simulcastInvalid
     */
    public boolean isSimulcastInvalid() {
        return simulcastInvalid;
    }

    /**
     * 
     * @param simulcastInvalid
     *     The simulcast_invalid
     */
    public void setSimulcastInvalid(boolean simulcastInvalid) {
        this.simulcastInvalid = simulcastInvalid;
    }

    /**
     * 
     * @return
     *     The simulcastClass
     */
    public String getSimulcastClass() {
        return simulcastClass;
    }

    /**
     * 
     * @param simulcastClass
     *     The simulcastClass
     */
    public void setSimulcastClass(String simulcastClass) {
        this.simulcastClass = simulcastClass;
    }

    /**
     * 
     * @return
     *     The fansubClass
     */
    public String getFansubClass() {
        return fansubClass;
    }

    /**
     * 
     * @param fansubClass
     *     The fansubClass
     */
    public void setFansubClass(String fansubClass) {
        this.fansubClass = fansubClass;
    }

    /**
     * 
     * @return
     *     The airdateOrig
     */
    public String getAirdateOrig() {
        return airdateOrig;
    }

    /**
     * 
     * @param airdateOrig
     *     The airdate_orig
     */
    public void setAirdateOrig(String airdateOrig) {
        this.airdateOrig = airdateOrig;
    }

    /**
     * 
     * @return
     *     The airdateU
     */
    public int getAirdateU() {
        return airdateU;
    }

    /**
     * 
     * @param airdateU
     *     The airdate_u
     */
    public void setAirdateU(int airdateU) {
        this.airdateU = airdateU;
    }

    /**
     * 
     * @return
     *     The isAired
     */
    public boolean isIsAired() {
        return isAired;
    }

    /**
     * 
     * @param isAired
     *     The isAired
     */
    public void setIsAired(boolean isAired) {
        this.isAired = isAired;
    }

    /**
     * 
     * @return
     *     The timeToAirU
     */
    public int getTimeToAirU() {
        return timeToAirU;
    }

    /**
     * 
     * @param timeToAirU
     *     The timeToAir_u
     */
    public void setTimeToAirU(int timeToAirU) {
        this.timeToAirU = timeToAirU;
    }

    /**
     * 
     * @return
     *     The timeToAirRd
     */
    public String getTimeToAirRd() {
        return timeToAirRd;
    }

    /**
     * 
     * @param timeToAirRd
     *     The timeToAir_rd
     */
    public void setTimeToAirRd(String timeToAirRd) {
        this.timeToAirRd = timeToAirRd;
    }

    /**
     * 
     * @return
     *     The simulcastAirdateU
     */
    public int getSimulcastAirdateU() {
        return simulcastAirdateU;
    }

    /**
     * 
     * @param simulcastAirdateU
     *     The simulcast_airdate_u
     */
    public void setSimulcastAirdateU(int simulcastAirdateU) {
        this.simulcastAirdateU = simulcastAirdateU;
    }

    /**
     * 
     * @return
     *     The simulcastAirdates
     */
    public SimulcastAirdates getSimulcastAirdates() {
        return simulcastAirdates;
    }

    /**
     * 
     * @param simulcastAirdates
     *     The simulcast_airdates
     */
    public void setSimulcastAirdates(SimulcastAirdates simulcastAirdates) {
        this.simulcastAirdates = simulcastAirdates;
    }

    /**
     * 
     * @return
     *     The isSimulcastAired
     */
    public boolean isIsSimulcastAired() {
        return isSimulcastAired;
    }

    /**
     * 
     * @param isSimulcastAired
     *     The isSimulcastAired
     */
    public void setIsSimulcastAired(boolean isSimulcastAired) {
        this.isSimulcastAired = isSimulcastAired;
    }

    /**
     * 
     * @return
     *     The simulcastTimeToAirU
     */
    public int getSimulcastTimeToAirU() {
        return simulcastTimeToAirU;
    }

    /**
     * 
     * @param simulcastTimeToAirU
     *     The simulcast_timeToAir_u
     */
    public void setSimulcastTimeToAirU(int simulcastTimeToAirU) {
        this.simulcastTimeToAirU = simulcastTimeToAirU;
    }

    /**
     * 
     * @return
     *     The simulcastTimeToAirRd
     */
    public String getSimulcastTimeToAirRd() {
        return simulcastTimeToAirRd;
    }

    /**
     * 
     * @param simulcastTimeToAirRd
     *     The simulcast_timeToAir_rd
     */
    public void setSimulcastTimeToAirRd(String simulcastTimeToAirRd) {
        this.simulcastTimeToAirRd = simulcastTimeToAirRd;
    }

    /**
     * 
     * @return
     *     The simulcastDelayRd
     */
    public boolean isSimulcastDelayRd() {
        return simulcastDelayRd;
    }

    /**
     * 
     * @param simulcastDelayRd
     *     The simulcast_delay_rd
     */
    public void setSimulcastDelayRd(boolean simulcastDelayRd) {
        this.simulcastDelayRd = simulcastDelayRd;
    }

    /**
     * 
     * @return
     *     The simulcastDelayType
     */
    public boolean isSimulcastDelayType() {
        return simulcastDelayType;
    }

    /**
     * 
     * @param simulcastDelayType
     *     The simulcast_delay_type
     */
    public void setSimulcastDelayType(boolean simulcastDelayType) {
        this.simulcastDelayType = simulcastDelayType;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
