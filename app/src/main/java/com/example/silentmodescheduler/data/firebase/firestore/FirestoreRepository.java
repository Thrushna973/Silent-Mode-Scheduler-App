package com.example.silentmodescheduler.data.firebase.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;

public interface FirestoreRepository {
    Task<Void> setDocument(String collectionPath, String documentId, Map<String, Object> data);
    Task<Void> updateDocument(String collectionPath, String documentId, Map<String, Object> data);
    Task<DocumentSnapshot> getDocument(String collectionPath, String documentId);
    Task<QuerySnapshot> getCollection(String collectionPath);
    Task<QuerySnapshot> getCollectionWhereEqualTo(String collectionPath, String field, Object value);
    Task<Void> deleteDocument(String collectionPath, String documentId);
}
