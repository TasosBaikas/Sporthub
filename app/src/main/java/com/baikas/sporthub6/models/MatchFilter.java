package com.baikas.sporthub6.models;

import java.io.Serializable;

public class MatchFilter implements Serializable {

    private int fromMembers;
    private int toMembers;
    private boolean noTerrain;
    private boolean yesTerrain;
    private boolean maybeTerrain;
    private boolean enabled;
    public static int fromMembersConstant = 1;
    public static int toMembersConstant = 15;

    public MatchFilter(int fromMembers, int toMembers, boolean enabled, boolean noTerrain, boolean yesTerrain, boolean maybeTerrain) {
        this.fromMembers = fromMembers;
        this.toMembers = toMembers;
        this.enabled = enabled;
        this.noTerrain = noTerrain;
        this.yesTerrain = yesTerrain;
        this.maybeTerrain = maybeTerrain;
    }

    public static MatchFilter resetFilterEnabled() {

        return new MatchFilter(fromMembersConstant,toMembersConstant,true,true,true,true);
    }

    public static MatchFilter resetFilterDisabled() {

        return new MatchFilter(fromMembersConstant,toMembersConstant,false,true,true,true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchFilter that = (MatchFilter) o;

        if (fromMembers != that.fromMembers) return false;
        if (toMembers != that.toMembers) return false;
        if (enabled != that.enabled) return false;
        if (noTerrain != that.noTerrain) return false;
        if (yesTerrain != that.yesTerrain) return false;
        return maybeTerrain == that.maybeTerrain;
    }

    @Override
    public int hashCode() {
        int result = fromMembers;
        result = 31 * result + toMembers;
        result = 31 * result + (noTerrain ? 1 : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (yesTerrain ? 1 : 0);
        result = 31 * result + (maybeTerrain ? 1 : 0);
        return result;
    }

    public boolean isNoTerrain() {
        return noTerrain;
    }

    public void setNoTerrain(boolean noTerrain) {
        this.noTerrain = noTerrain;
    }

    public boolean isYesTerrain() {
        return yesTerrain;
    }

    public void setYesTerrain(boolean yesTerrain) {
        this.yesTerrain = yesTerrain;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isMaybeTerrain() {
        return maybeTerrain;
    }

    public void setMaybeTerrain(boolean maybeTerrain) {
        this.maybeTerrain = maybeTerrain;
    }

    public int getFromMembers() {
        return fromMembers;
    }

    public void setFromMembers(int fromMembers) {
        this.fromMembers = fromMembers;
    }

    public int getToMembers() {
        return toMembers;
    }

    public void setToMembers(int toMembers) {
        this.toMembers = toMembers;
    }
}
