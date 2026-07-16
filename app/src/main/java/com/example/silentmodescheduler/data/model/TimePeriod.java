package com.example.silentmodescheduler.data.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TimePeriod implements Serializable {
    private String id;
    private String name;
    private String startTime;
    private String endTime;
    private boolean enabled;

    // Required empty constructor for Firestore
    public TimePeriod() {
        this.id = "";
        this.name = "";
        this.startTime = "";
        this.endTime = "";
        this.enabled = true;
    }

    public TimePeriod(String id, String name, String startTime, String endTime, boolean enabled) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("enabled", enabled);
        return map;
    }

    public static TimePeriod fromMap(Map<String, Object> map) {
        if (map == null) return new TimePeriod();
        return new TimePeriod(
            (String) map.getOrDefault("id", ""),
            (String) map.getOrDefault("name", ""),
            (String) map.getOrDefault("startTime", ""),
            (String) map.getOrDefault("endTime", ""),
            (Boolean) map.getOrDefault("enabled", true)
        );
    }
}
