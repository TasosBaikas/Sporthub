package com.baikas.sporthub6.listeners;

import com.baikas.sporthub6.models.Match;

import java.util.concurrent.CompletableFuture;

public interface OnClickShowMatchDetails {
    CompletableFuture<Void> onClickShowMatchDetails(Match match);
}
