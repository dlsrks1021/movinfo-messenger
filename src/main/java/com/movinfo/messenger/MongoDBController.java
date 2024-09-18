package com.movinfo.messenger;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

public class MongoDBController {

    private static final String MONGO_URL = "mongodb://localhost:27017";
    private MongoChangeStreamWatcher mongoChangeStreamWatcher;
    private MongoClient mongoClient;
    private MongoCollection<Document> movieCollection;
    private MongoCollection<Document> resumeTokenCollection;

    public MongoDBController(){
        mongoClient = MongoClients.create(MONGO_URL);
        movieCollection = mongoClient.getDatabase("movinfo").getCollection("movie");
        resumeTokenCollection = mongoClient.getDatabase("movinfo").getCollection("resume_token_storage");
        mongoChangeStreamWatcher = new MongoChangeStreamWatcher(movieCollection, resumeTokenCollection);
    }

    public void watchMovieInfo(){
        mongoChangeStreamWatcher.watchForUpdates();
    }
}
