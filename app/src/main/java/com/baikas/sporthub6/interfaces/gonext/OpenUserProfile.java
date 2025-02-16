package com.baikas.sporthub6.interfaces.gonext;

import androidx.annotation.Nullable;

import java.util.concurrent.CompletableFuture;

public interface OpenUserProfile {
    CompletableFuture<Void> openUserProfile(String userId, @Nullable String sport);
}
