package com.baikas.sporthub6.listeners;


import java.util.concurrent.CompletableFuture;

public interface OnClickJoinMatch {
    CompletableFuture<Void> onClickJoinMatch(String requesterId, String matchId, String sport);
}
