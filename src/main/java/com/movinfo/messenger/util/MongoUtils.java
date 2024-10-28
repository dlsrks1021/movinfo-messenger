package com.movinfo.messenger.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.types.Binary;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.movinfo.messenger.command.RoleManager;
import com.movinfo.messenger.model.Movie;
import com.movinfo.messenger.model.Screen;

public class MongoUtils {
    private static final String MONGO_URL = System.getenv("MONGO_URL");
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    private static MongoChangeStreamWatcher mongoChangeStreamWatcher;
    private static List<Movie> movieList;

    private MongoUtils(){}

    public static void init(){
        mongoClient = MongoClients.create(MONGO_URL);
        mongoDatabase = mongoClient.getDatabase("movinfo");
        movieList = new LinkedList<>();

        saveMoviesToListFromDB();

        MongoCollection<Document> movieCollection = mongoDatabase.getCollection("movies");
        MongoCollection<Document> resumeTokenCollection = mongoDatabase.getCollection("resume_token_storage");
        mongoChangeStreamWatcher = new MongoChangeStreamWatcher(movieCollection, resumeTokenCollection);
    }

    private static Screen parseScreenFromDocument(Document screenDocument, String dateString){
        List<String> screenTypes = screenDocument.getList(dateString, String.class);
        try{
            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            formatter.setTimeZone(TimeZone.getTimeZone("KST"));
            Date date = formatter.parse(dateString);
    
            return new Screen(date, screenTypes);
        } catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Movie parseMovieFromDocument(Document movieDocument){
        ObjectId id = movieDocument.getObjectId("_id");
        String name = movieDocument.getString("name");
        Date dateOpen = movieDocument.getDate("dateOpen");
        byte[] poster = movieDocument.get("poster", Binary.class).getData();
        
        Document screenDocument = movieDocument.get("screen", Document.class);

        Movie movie = new Movie(id, name, dateOpen, poster);
        if (screenDocument != null){
            Set<String> screenDate = screenDocument.keySet();
            screenDate.forEach(dateString -> {
                Screen parsedScreen = parseScreenFromDocument(screenDocument, dateString);
                if (parsedScreen != null)
                    movie.addScreen(parsedScreen);
            });
        }

        return movie;
    }

    public static void addMovieToList(Movie movie){
        for (Movie movieInList : movieList){
            if (movieInList.getId().equals(movie.getId())){
                return;
            }
        }
        movieList.add(movie);
    }

    public static void removeMovieFromList(ObjectId id){
        for (Movie movie : movieList){
            if (movie.getId().equals(id)){
                for (String type : Screen.SCREEN_TYPE_LIST){
                    String roleName = movie.getName()+"_"+type;
                    RoleManager.deleteRole(JDAUtils.getGuild(), roleName);
                }
                
                movieList.remove(movie);

                return;
            }
        }
    }

    public static List<Movie> getMovieList(){
        return movieList;
    }

    public static void checkAndSendMessageForUpdatedScreenFromMovie(Movie updateMovie){
        Movie existMovie = null;
        for (Movie movie : movieList){
            if (movie.getDateOpen().equals(updateMovie.getDateOpen())){
                existMovie = movie;
                break;
            }
        }

        if (existMovie != null){
            for (Date date : updateMovie.getScreenMap().keySet()){
                if (existMovie.getScreenMap().containsKey(date)){
                    List<String> existScreenTypes = existMovie.getScreenMap().get(date).getScreentypes();
                    List<String> updateScreenTypes = updateMovie.getScreenMap().get(date).getScreentypes();
    
                    updateScreenTypes.removeAll(existScreenTypes);
                    existScreenTypes.addAll(updateScreenTypes);
                    
                    JDAUtils.sendScreenInfoToScreenChannel(updateMovie.getName(), date, updateScreenTypes);
                } else{
                    Screen screen = updateMovie.getScreenMap().get(date);
                    existMovie.addScreen(screen);
                    JDAUtils.sendScreenInfoToScreenChannel(updateMovie.getName(), date, screen.getScreentypes());
                }
            }
        }
    }

    private static void saveMoviesToListFromDB(){
        MongoCollection<Document> movieCollection =  mongoDatabase.getCollection("movies");
        FindIterable<Document> movies = movieCollection.find();

        movieList.clear();

        movies.forEach(movieDocument -> {
            Movie movie = parseMovieFromDocument(movieDocument);
            addMovieToList(movie);
        });
    }

    public static void watchMovieInfo(){
        Thread thread = new Thread(() ->
            mongoChangeStreamWatcher.watchForUpdates()
        );
        thread.start();
    }
}
