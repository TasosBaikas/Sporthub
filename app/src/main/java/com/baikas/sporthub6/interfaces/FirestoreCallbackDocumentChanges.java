package com.baikas.sporthub6.interfaces;

import java.util.List;

public interface FirestoreCallbackDocumentChanges<T> {

    void onSuccess(List<T> documents);
    void onError(Exception e);

}
