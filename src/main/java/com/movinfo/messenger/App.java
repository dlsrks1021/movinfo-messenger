package com.movinfo.messenger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        MongoDBController mongoDBController = new MongoDBController();
        mongoDBController.watchMovieInfo();
    }
}
