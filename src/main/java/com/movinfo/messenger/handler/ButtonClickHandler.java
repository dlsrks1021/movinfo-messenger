package com.movinfo.messenger.handler;

import java.util.List;

import com.movinfo.messenger.model.Movie;
import com.movinfo.messenger.util.MongoUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class ButtonClickHandler extends ListenerAdapter{

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event){
        if(event.getComponentId().equals("noti-btn")){
            sendMovieSelectMenu(event);
        }
    }

    private void sendMovieSelectMenu(ButtonInteractionEvent event){
        List<Movie> movies = MongoUtils.getMovieList();
        Collections.sort(movies, Comparator.comparing(Movie::getDateOpen));

        List<StringSelectMenu> stringSelectMenus = new LinkedList<>();
        List<SelectOption> options = new LinkedList<>();
        for (int i = 0; i < movies.size(); ++i){
            SelectOption option = SelectOption.of(movies.get(i).getName(), movies.get(i).getName());
            options.add(option);

            if (options.size() == SelectMenuHandler.MAX_NUM_OPTIONS || i + 1 == movies.size()){
                int menuIndex = stringSelectMenus.size() + 1;
                StringSelectMenu menu = StringSelectMenu.create("movie-select-menu-" + menuIndex)
                    .setPlaceholder("영화 목록 [" + menuIndex + "]")
                    .addOptions(options)
                    .build();
            
                stringSelectMenus.add(menu);
                options = new LinkedList<>();
            }

            if (stringSelectMenus.size() == SelectMenuHandler.MAX_NUM_MENUS){
                break;
            }
        }

        ReplyCallbackAction reply =  event.reply("알림 설정할 영화를 선택해주세요.").setEphemeral(true);
        stringSelectMenus.forEach(menu -> 
            reply.addActionRow(menu)
        );

        reply.queue();
    }

    private void sendEphemeralMenu(ButtonInteractionEvent event){
        List<Movie> movies = MongoUtils.getMovieList();
        List<StringSelectMenu> stringSelectMenus = new LinkedList<>();
        movies.forEach(movie -> {
            StringSelectMenu menu = StringSelectMenu.create(movie.getName())
                .setPlaceholder(movie.getName())
                .setMaxValues(6)
                .addOptions(
                    SelectOption.of("SCREENX", "SCREENX").withDefault(false),
                    SelectOption.of("TEMPUR CINEMA", "TEMPUR CINEMA").withDefault(false),
                    SelectOption.of("GOLD CLASS", "GOLD CLASS").withDefault(false),
                    SelectOption.of("IMAX", "IMAX").withDefault(false),
                    SelectOption.of("4DX", "4DX").withDefault(false),
                    SelectOption.of("2D", "2D").withDefault(false)
                )
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
