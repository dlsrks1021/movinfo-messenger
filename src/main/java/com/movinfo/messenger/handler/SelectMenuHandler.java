package com.movinfo.messenger.handler;

import java.util.LinkedList;
import java.util.List;

import com.movinfo.messenger.command.RoleManager;
import com.movinfo.messenger.model.Movie;
import com.movinfo.messenger.model.Screen;
import com.movinfo.messenger.util.JDAUtils;
import com.movinfo.messenger.util.MongoUtils;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class SelectMenuHandler extends ListenerAdapter{

    public static int MAX_NUM_MENUS = 5;
    public static int MAX_NUM_OPTIONS = 25;

    private void handleMovieSelectMenu(StringSelectInteractionEvent event){
        String selectedMovieName = event.getValues().get(0);

        List<SelectOption> selectOptions = new LinkedList<>();
        for (String type : Screen.SCREEN_TYPE_LIST){
            String roleName = selectedMovieName+"_"+type;
            SelectOption option;
            if (RoleManager.hasRole(JDAUtils.getGuild(), selectedMovieName+"_"+type, event.getUser())){
                option = SelectOption.of(type, roleName).withDefault(true);
            } else {
                option = SelectOption.of(type, roleName).withDefault(false);
            }
            selectOptions.add(option);
        }

        StringSelectMenu menu = StringSelectMenu.create("screen-select-menu-"+selectedMovieName)
            .setPlaceholder("상영관 종류")
            .setMaxValues(6)
            .setMinValues(0)
            .addOptions(selectOptions)
            .build();
            
        event.reply("[" + selectedMovieName + "] 영화에 대해 알림을 설정할 극장 종류를 선택해주세요.").setEphemeral(true)
            .addActionRow(menu)
            .queue();
    }

    private void handleScreenSelectMenu(StringSelectInteractionEvent event){
        String selectedMovieName = event.getComponentId().substring("screen-select-menu-".length());
        List<String> selectedRoleNames = event.getValues();

        for (String type : Screen.SCREEN_TYPE_LIST){
            String roleName = selectedMovieName+"_"+type;
            if (selectedRoleNames.contains(roleName)){
                if (!RoleManager.hasRole(JDAUtils.getGuild(), roleName, event.getUser())){
                    if (!RoleManager.isRoleExist(JDAUtils.getGuild(), roleName)){
                        RoleManager.createRoleAndAddRoleToMember(JDAUtils.getGuild(), roleName, event.getUser());
                    } else {
                        RoleManager.addRoleToMember(JDAUtils.getGuild(), roleName, event.getUser());
                    }
                }
            } else{
                if (RoleManager.hasRole(JDAUtils.getGuild(), roleName, event.getUser())){
                    RoleManager.removeRoleFromMember(JDAUtils.getGuild(), roleName, event.getUser());
                }
            }
        }
        
        event.deferEdit().queue();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event){
        if (event.getComponentId().contains("movie-select-menu")){
            handleMovieSelectMenu(event);
        } else if (event.getComponentId().contains("screen-select-menu")){
            handleScreenSelectMenu(event);
        }
    }
}
