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

public class SelectMenuHandler extends ListenerAdapter{
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event){
        List<Movie> movies = MongoUtils.getMovieList();
        movies.forEach(movie -> {
            if (event.getComponentId().equals(movie.getName())){
                if (event.getValues().isEmpty()){
                    for (String type : Screen.SCREEN_TYPE_LIST){
                        String roleName = movie.getName()+"_"+type;
                        if (RoleManager.hasRole(JDAUtils.getGuild(), roleName, event.getUser())){
                            RoleManager.removeRoleFromMember(JDAUtils.getGuild(), roleName, event.getUser());
                        }
                    }
                } else{
                    List<String> roleNameList = new LinkedList<>();
                    
                    event.getValues().forEach(value -> {
                        roleNameList.add(movie.getName()+"_"+value);
                    });

                    for (String type : Screen.SCREEN_TYPE_LIST){
                        String roleName = movie.getName()+"_"+type;
                        if (roleNameList.contains(roleName)){
                            if (!RoleManager.hasRole(JDAUtils.getGuild(), roleName, event.getUser())){
                                RoleManager.addRoleToMember(JDAUtils.getGuild(), roleName, event.getUser());
                            }
                        } else {
                            if (RoleManager.hasRole(JDAUtils.getGuild(), roleName, event.getUser())){
                                RoleManager.removeRoleFromMember(JDAUtils.getGuild(), roleName, event.getUser());
                            }
                        }

                    }
                }
            }
        });

        event.deferReply(true)
        .queue();
    }
}
