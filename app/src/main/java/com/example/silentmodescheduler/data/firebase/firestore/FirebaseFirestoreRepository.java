package com.example.silentmodescheduler.data.firebase.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;

public class FirebaseFirestoreRepository implements FirestoreRepository {
    private final FirebaseFirestore firestore;

    public FirebaseFirestoreRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestoreRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Task<Void> setDocument(String collectionPath, String documentId, Map<String, Object> data) {
        return firestore.collection(collectionPath)
                .document(documentId)
                .set(data);
    }

    @Override
    public Task<Void> updateDocument(String collectionPath, String documentId, Map<String, Object> data) {
        return firestore.collection(collectionPath)
                .document(documentId)
                .update(data);
    }

    @Override
    public Task<DocumentSnapshot> getDocument(String collectionPath, String documentId) {
        return firestore.collection(collectionPath)
                .document(documentId)
                .get();
    }

    @Override
    public Task<QuerySnapshot> getCollection(String collectionPath) {
        return firestore.collection(collectionPath)
                .get();
    }

    @Override
    public Task<QuerySnapshot> getCollectionWhereEqualTo(String collectionPath, String field, Object value) {
        return firestore.collection(collectionPath)
                .whereEqualTo(field, value)
                .get();
    }

    @Override
    public Task<Void> deleteDocument(String collectionPath, String documentId) {
        return firestore.collection(collectionPath)
                .document(documentId)
                .delete();
    }
}
