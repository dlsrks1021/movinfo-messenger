package com.movinfo.messenger;

import com.movinfo.messenger.util.JDAUtils;
import com.movinfo.messenger.util.MongoUtils;

public class App 
{
    public static void main( String[] args )
    {
        App app = new App();
        app.run();
    }

    private void run(){
        // init
        MongoUtils.init();
        JDAUtils.init();
    }
}