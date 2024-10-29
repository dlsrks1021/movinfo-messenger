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

import com.movinfo.messenger.command.MessageSender;
import com.movinfo.messenger.command.RoleManager;
import com.movinfo.messenger.handler.ButtonClickHandler;
import com.movinfo.messenger.handler.InitialHandler;
import com.movinfo.messenger.handler.MessageReceiveHandler;
import com.movinfo.messenger.handler.SelectMenuHandler;
import com.movinfo.messenger.model.Movie;

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

        boolean anyRoleExist = false;
        for (String roleName : roleNames){
            if (RoleManager.isRoleExist(getGuild(), roleName)){
                anyRoleExist = true;
                break;
            }
        }

        if (anyRoleExist){
            MessageChannel channel = jda.getChannelById(MessageChannel.class, SCREEN_CHANNEL_ID);
            MessageSender.sendMessageWithRoleMentions(channel, stringBuilder.toString(), roleNames);
        }
    }

    public static Movie deleteOldestMovieFromList(){
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

        MessageChannel channel = jda.getChannelById(MessageChannel.class, MOVIE_CHANNEL_ID);
        MessageSender.sendMessageWithImage(channel, stringBuilder.toString(), movie.getPoster(), movie.getName()+".png");
    }
}
