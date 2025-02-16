package com.baikas.sporthub6.models;

import com.baikas.sporthub6.interfaces.ObjectWithId;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class TerrainAddress implements ObjectWithId, Serializable  {

    private String id;
    private String addressTitle;
    private String address;
    private String sport;
    private long priority;
    private String creatorId;
    private double latitude;
    private double longitude;
    private long createdAtUTC;

    public TerrainAddress(String id) {
        this.id = id;
    }

    public TerrainAddress(String id, String addressTitle, String address, String sport,long priority, String creatorId, double latitude, double longitude, long createdAtUTC) {
        this.id = id;
        this.addressTitle = addressTitle;
        this.address = address;
        this.sport = sport;
        this.priority = priority;
        this.creatorId = creatorId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAtUTC = createdAtUTC;
    }

    public TerrainAddress(Map<String,Object> map) {
        this.id = (String)map.get("id");
        this.addressTitle = (String)map.get("addressTitle");
        this.address = (String)map.get("address");
        this.sport = (String)map.get("sport");

        if (map.get("priority") instanceof Integer){
            this.priority = ((Integer) map.get("priority")).longValue();
        }else
            this.priority = (Long) map.get("priority");


        this.creatorId = (String)map.get("creatorId");
        this.latitude = (Double)map.get("latitude");
        this.longitude = (Double)map.get("longitude");

        if (map.get("createdAtUTC") instanceof Integer){
            this.createdAtUTC = ((Integer) map.get("createdAtUTC")).longValue();
        }else
            this.createdAtUTC = (Long) map.get("createdAtUTC");

    }

    public TerrainAddress(TerrainAddress terrainAddress) {
        this.id = terrainAddress.getId();
        this.addressTitle = terrainAddress.getAddressTitle();
        this.address = terrainAddress.getAddress();
        this.sport = terrainAddress.getSport();
        this.priority = terrainAddress.getPriority();
        this.creatorId = terrainAddress.getCreatorId();
        this.latitude = terrainAddress.getLatitude();
        this.longitude = terrainAddress.getLongitude();

        this.createdAtUTC = terrainAddress.getCreatedAtUTC();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TerrainAddress that = (TerrainAddress) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (createdAtUTC != that.createdAtUTC) return false;
        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(addressTitle, that.addressTitle))
            return false;
        if (!Objects.equals(address, that.address)) return false;
        if (!Objects.equals(sport, that.sport)) return false;
        if (!Objects.equals(priority, that.priority)) return false;
        return Objects.equals(creatorId, that.creatorId);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        result = 31 * result + (addressTitle != null ? addressTitle.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (sport != null ? sport.hashCode() : 0);
        result = 31 * result + (creatorId != null ? creatorId.hashCode() : 0);
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (createdAtUTC ^ (createdAtUTC >>> 32));
        result = 31 * result + (int) (priority ^ (priority >>> 32));
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddressTitle() {
        return addressTitle;
    }

    public void setAddressTitle(String addressTitle) {
        this.addressTitle = addressTitle;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getCreatedAtUTC() {
        return createdAtUTC;
    }

    public void setCreatedAtUTC(long createdAtUTC) {
        this.createdAtUTC = createdAtUTC;
    }
}
