package com.movinfo.messenger.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import java.awt.Color;

import com.movinfo.messenger.command.MessageSender;
import com.movinfo.messenger.command.RoleManager;
import com.movinfo.messenger.handler.ButtonClickHandler;
import com.movinfo.messenger.handler.InitialHandler;
import com.movinfo.messenger.handler.MessageReceiveHandler;
import com.movinfo.messenger.handler.SelectMenuHandler;
import com.movinfo.messenger.model.Movie;
import com.movinfo.messenger.model.Screen;

public class JDAUtils {
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");
    private static final String GUILD_ID = System.getenv("GUILD_ID");
    private static final String NOTI_SET_CHANNEL_ID = System.getenv("NOTI_SET_CHANNEL_ID");
    private static final String MOVIE_CHANNEL_ID = System.getenv("MOVIE_CHANNEL_ID");
    private static final String SCREEN_CHANNEL_ID = System.getenv("SCREEN_CHANNEL_ID");
    private static JDA jda;

    private JDAUtils(){}

    public static String getNotiSetChannelId(){
        return NOTI_SET_CHANNEL_ID;
    }
    public static String getMovieChannelId() {
        return MOVIE_CHANNEL_ID;
    }
    public static String getScreenChannelId() {
        return SCREEN_CHANNEL_ID;
    }

    public static Guild getGuild(){
        return jda.getGuildById(GUILD_ID);
    }

    public static void init(){
        EnumSet<GatewayIntent> intents = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.MESSAGE_CONTENT
        );
    
        try{
            JDABuilder builder = JDABuilder.createLight(BOT_TOKEN, intents);

            builder.addEventListeners(
                new ButtonClickHandler(),
                new MessageReceiveHandler(),
                new SelectMenuHandler(),
                new InitialHandler()
            );

            jda = builder.build();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private static String dateToString(Date date, String pattern){
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    public static void sendScreenInfoToScreenChannel(String movieName, Date dateOpen, List<String> screenTypes){
        String dateOpenString = dateToString(dateOpen, "yyyy.MM.dd");

        List<String> roleNames = new LinkedList<>();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[" + movieName + "]\n");
        stringBuilder.append("상영일 : " + dateOpenString + "\n");
        stringBuilder.append("상영관 : [");
        screenTypes.forEach(screenType -> {
            stringBuilder.append(" " + screenType + " ");
            String roleName = movieName+"_"+screenType;
            roleNames.add(roleName);
        });
        stringBuilder.append("]");

        MessageChannel channel = jda.getChannelById(MessageChannel.class, SCREEN_CHANNEL_ID);
        MessageSender.sendMessageWithRoleMentions(channel, dateOpenString, roleNames);
    }

    private static void createRoleForMovie(Movie movie){
        for (String type : Screen.SCREEN_TYPE_LIST){
            String roleName = movie.getName()+"_"+type;
            Color color = Color.GRAY;
            if (roleName.contains("SCREENX")){
                color = Color.RED;
            } else if (roleName.contains("TEMPUR CINEMA")) {
                color = Color.DARK_GRAY;
            } else if (roleName.contains("GOLD CLASS")) {
                color = Color.YELLOW;
            } else if (roleName.contains("IMAX")) {
                color = Color.BLUE;
            } else if (roleName.contains("4DX")) {
                color = Color.ORANGE;
            } else {
                color = Color.GRAY;
            }
            RoleManager.createRole(getGuild(), roleName, color);
        }
    }

    private static Movie deleteOldestMovieFromList(){
        Movie oldMovie = MongoUtils.getMovieList().get(0);
        for (Movie movie : MongoUtils.getMovieList()){
            if (movie.getDateOpen().before(oldMovie.getDateOpen())){
                oldMovie = movie;
            }
        }
        MongoUtils.getMovieList().remove(oldMovie);
        return oldMovie;
    }

    public static void sendMovieInfoToMovieChannel(Movie movie){
        String dateOpen = dateToString(movie.getDateOpen(), "yyyy.MM.dd");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[" + movie.getName() + "]\n");
        stringBuilder.append("개봉 일자 : " + dateOpen);
        
        if (RoleManager.getNumRoles(getGuild()) + 6 > RoleManager.MAX_NUM_ROLES){
            Movie oldMovie = deleteOldestMovieFromList();
            for (String type : Screen.SCREEN_TYPE_LIST){
                String roleName = oldMovie.getName()+"_"+type;
                RoleManager.deleteRole(getGuild(), roleName);
            }
        }
        createRoleForMovie(movie);

        MessageChannel channel = jda.getChannelById(MessageChannel.class, MOVIE_CHANNEL_ID);
        MessageSender.sendMessageWithImage(channel, stringBuilder.toString(), movie.getPoster(), movie.getName());
    }
}
