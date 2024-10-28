package com.movinfo.messenger.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.movinfo.messenger.model.Movie;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoChangeStreamWatcher {

    private MongoCollection<Document> collection;
    private MongoCollection<Document> tokenCollection;

    public MongoChangeStreamWatcher(MongoCollection<Document> collection, MongoCollection<Document> tokenCollection) {
        this.collection = collection;
        this.tokenCollection = tokenCollection;
    }

    public void watchForUpdates() {
        Document tokenDoc = tokenCollection.find(new Document("stream", "movieStream")).first();
        BsonDocument lastResumeToken = tokenDoc != null ? tokenDoc.get("resumeToken", Document.class).toBsonDocument() : null;

        MongoCursor<ChangeStreamDocument<Document>> cursor;
        if (lastResumeToken != null) {
            cursor = collection.watch().resumeAfter(lastResumeToken).iterator();
        } else {
            cursor = collection.watch().iterator();
        }

        while (cursor.hasNext()) {
            ChangeStreamDocument<Document> change = cursor.next();
            Document updatedDocument = change.getFullDocument();
            OperationType operationType = change.getOperationType();

            if (updatedDocument != null) {
                if (operationType.equals(OperationType.INSERT)){
                    Movie movie =  MongoUtils.parseMovieFromDocument(updatedDocument);
                    MongoUtils.addMovieToList(movie);
                    JDAUtils.sendMovieInfoToMovieChannel(movie);
                }
                else if (operationType.equals(OperationType.UPDATE)){
                    Movie movie =  MongoUtils.parseMovieFromDocument(updatedDocument);
                    MongoUtils.checkAndSendMessageForUpdatedScreenFromMovie(movie);
                }
                else if (operationType.equals(OperationType.DELETE)){
                    ObjectId deletedId = change.getDocumentKey().getObjectId("_id").getValue();
                    MongoUtils.removeMovieFromList(deletedId);
                }
            }

            BsonDocument resumeToken = change.getResumeToken();
            tokenCollection.updateOne(
                new Document("stream", "movieStream"),
                new Document("$set", new Document("resumeToken", resumeToken)),
                new UpdateOptions().upsert(true)
            );
        }
    }
}
