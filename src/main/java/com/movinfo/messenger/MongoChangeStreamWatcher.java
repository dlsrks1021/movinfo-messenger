package com.movinfo.messenger;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.BsonDocument;
import org.bson.Document;

public class MongoChangeStreamWatcher {

    private MongoCollection<Document> collection;
    private MongoCollection<Document> tokenCollection;

    public MongoChangeStreamWatcher(MongoCollection<Document> collection, MongoCollection<Document> tokenCollection) {
        this.collection = collection;
        this.tokenCollection = tokenCollection;
    }

    public void watchForUpdates() {
        Document tokenDoc = tokenCollection.find(new Document("stream", "movieStream")).first();
        BsonDocument lastResumeToken = tokenDoc != null ? tokenDoc.get("resumeToken", BsonDocument.class) : null;

        MongoCursor<ChangeStreamDocument<Document>> cursor;
        if (lastResumeToken != null) {
            cursor = collection.watch().resumeAfter(lastResumeToken).iterator();
        } else {
            cursor = collection.watch().iterator();
        }

        while (cursor.hasNext()) {
            ChangeStreamDocument<Document> change = cursor.next();
            Document updatedDocument = change.getFullDocument();
            String operationType = change.getOperationType().getValue();

            if (updatedDocument != null && operationType.equals("insert")) {
                String name = updatedDocument.getString("name");
                String date = updatedDocument.getString("date");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("["+date+"]\n");
                stringBuilder.append(name);
                DiscordWebhookSender.sendMessage(stringBuilder.toString());
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
