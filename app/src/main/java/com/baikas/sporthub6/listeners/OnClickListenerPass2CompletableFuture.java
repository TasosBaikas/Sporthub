package com.baikas.sporthub6.listeners;

import java.util.concurrent.CompletableFuture;

public interface OnClickListenerPass2CompletableFuture<T1,T2> {
    CompletableFuture<Void> onClick(T1 item1, T2 item2);
}
