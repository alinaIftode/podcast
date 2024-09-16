package models;

import java.util.List;
import java.util.Map;

public class Opportunity {
    private long originalEventTime;
    private int maxDuration;
    private Map<String, Zone> zones;
    private Map<String, List<String>> positionUrlSegments;
    private int insertionRate;

    public long getOriginalEventTime() {
        return originalEventTime;
    }

    public void setOriginalEventTime(long originalEventTime) {
        this.originalEventTime = originalEventTime;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public Map<String, Zone> getZones() {
        return zones;
    }

    public void setZones(Map<String, Zone> zones) {
        this.zones = zones;
    }

    public Map<String, List<String>> getPositionUrlSegments() {
        return positionUrlSegments;
    }

    public void setPositionUrlSegments(Map<String, List<String>> positionUrlSegments) {
        this.positionUrlSegments = positionUrlSegments;
    }

    public int getInsertionRate() {
        return insertionRate;
    }

    public void setInsertionRate(int insertionRate) {
        this.insertionRate = insertionRate;
    }

    public static class Zone {
        private int id;
        private int maxAds;
        private int maxDuration;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getMaxAds() {
            return maxAds;
        }

        public void setMaxAds(int maxAds) {
            this.maxAds = maxAds;
        }

        public int getMaxDuration() {
            return maxDuration;
        }

        public void setMaxDuration(int maxDuration) {
            this.maxDuration = maxDuration;
        }
    }
}
