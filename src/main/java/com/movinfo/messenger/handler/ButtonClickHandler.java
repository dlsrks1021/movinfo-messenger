package com.movinfo.messenger.handler;

import java.util.List;

import com.movinfo.messenger.model.Movie;
import com.movinfo.messenger.util.MongoUtils;

import java.util.LinkedList;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class ButtonClickHandler extends ListenerAdapter{
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event){
        if(event.getComponentId().equals("noti-btn")){
            sendEphemeralMenu(event);
        }
    }

    private void sendEphemeralMenu(ButtonInteractionEvent event){
        List<Movie> movies = MongoUtils.getMovieList();
        List<StringSelectMenu> stringSelectMenus = new LinkedList<>();
        movies.forEach(movie -> {
            StringSelectMenu menu = StringSelectMenu.create(movie.getName())
                .setPlaceholder(movie.getName())
                .setMaxValues(6)
                .addOption("SCREENX", "SCREENX")
                .addOption("TEMPUR CINEMA", "TEMPUR CINEMA")
                .addOption("GOLD CLASS", "GOLD CLASS")
                .addOption("IMAX", "IMAX")
                .addOption("4DX", "4DX")
                .addOption("2D", "2D")
                .build(); 
            stringSelectMenus.add(menu);
        });

        ReplyCallbackAction reply =  event.reply("알림 설정할 영화와 극장 종류를 선택해주세요.").setEphemeral(true);
        stringSelectMenus.forEach(menu -> 
            reply.addActionRow(menu)
        );

        reply.queue();
    }
}
