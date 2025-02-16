package com.baikas.sporthub6.models;

import java.util.concurrent.atomic.AtomicReference;

public class RequestMatchesFromServer {

    private String sport;
    private long dateBeginForPaginationUTC;
    private long dateLastForPaginationUTC;
    private MatchFilter matchFilter;
    private String lastVisibleDocumentMatchId;
    private int limit;


    public RequestMatchesFromServer(String sport, long dateBeginForPaginationUTC, long dateLastForPaginationUTC, String lastVisibleDocumentMatchId,MatchFilter matchFilter, int limit) {
        this.sport = sport;
        this.dateBeginForPaginationUTC = dateBeginForPaginationUTC;
        this.dateLastForPaginationUTC = dateLastForPaginationUTC;
        this.lastVisibleDocumentMatchId = lastVisibleDocumentMatchId;
        this.matchFilter = matchFilter;
        this.limit = limit;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public long getDateBeginForPaginationUTC() {
        return dateBeginForPaginationUTC;
    }

    public void setDateBeginForPaginationUTC(long dateBeginForPaginationUTC) {
        this.dateBeginForPaginationUTC = dateBeginForPaginationUTC;
    }

    public long getDateLastForPaginationUTC() {
        return dateLastForPaginationUTC;
    }

    public void setDateLastForPaginationUTC(long dateLastForPaginationUTC) {
        this.dateLastForPaginationUTC = dateLastForPaginationUTC;
    }

    public MatchFilter getMatchFilter() {
        return matchFilter;
    }

    public void setMatchFilter(MatchFilter matchFilter) {
        this.matchFilter = matchFilter;
    }

    public String getLastVisibleDocumentMatchId() {
        return lastVisibleDocumentMatchId;
    }

    public void setLastVisibleDocumentMatchId(String lastVisibleDocumentMatchId) {
        this.lastVisibleDocumentMatchId = lastVisibleDocumentMatchId;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
