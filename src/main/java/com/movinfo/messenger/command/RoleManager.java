package com.movinfo.messenger.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.movinfo.messenger.model.Movie;
import com.movinfo.messenger.model.Screen;
import com.movinfo.messenger.util.JDAUtils;
import com.movinfo.messenger.util.MongoUtils;

import java.awt.Color;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class RoleManager {

    public static final int MAX_NUM_ROLES = 250;

    private RoleManager(){}

    private static Color getRoleColorByRoleName(String roleName){
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

        return color;
    }

    public static int getNumRoles(Guild guild){
        return guild.getRoles().size();
    }

    public static boolean isRoleExist(Guild guild, String roleName){
        return !guild.getRolesByName(roleName, true).isEmpty();
    }

    public static Role getRoleByName(Guild guild, String roleName){
        if (!guild.getRolesByName(roleName, true).isEmpty()){
            return guild.getRolesByName(roleName, true).get(0);
        } else {
            return null;
        }
    }

    public static void createRoleAndAddRoleToMember(Guild guild, String roleName, User user) {
        while (getNumRoles(guild) >= MAX_NUM_ROLES){
            Movie oldMovie = JDAUtils.deleteOldestMovieFromList();
            for (String type : Screen.SCREEN_TYPE_LIST){
                String oldRoleName = oldMovie.getName()+"_"+type;
                if (isRoleExist(guild, oldRoleName)){
                    deleteRole(guild, oldRoleName);
                }
            }
        }

        guild.createRole()
             .setName(roleName)
             .setColor(getRoleColorByRoleName(roleName))
             .setMentionable(true)
             .queue(
                 role -> {
                    System.out.println("Role Created '" + role.getName());
                    if (user != null){
                        addRoleToMember(guild, roleName, user);
                    }
                },
                 error -> System.err.println("Role Creation Failed : " + error.getMessage())
             );
    }    

    public static void addRoleToMember(Guild guild, String roleName, User user){
        Role role = guild.getRolesByName(roleName, true).get(0);
        guild.addRoleToMember(user, role).queue();
    }

    public static CompletableFuture<Boolean> hasRole(Guild guild, String roleName, User user){
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        guild.retrieveMember(user).queue(
            member -> {
                if (member != null){
                    if (!guild.getRolesByName(roleName, true).isEmpty()){
                        Role role = guild.getRolesByName(roleName, true).get(0);
                        future.complete(member.getRoles().contains(role));
                    } else {
                        future.complete(false);
                    }
                    
                } else {
                    future.complete(false);
                }
            },
            error -> {
                future.complete(false);
            }
        );

        return future;
    }

    public static void removeRoleFromMember(Guild guild, String roleName, User user){
        Role role = guild.getRolesByName(roleName, true).get(0);
        guild.removeRoleFromMember(user, role).queue(
            success -> {
                List<Member> membersWithRole = guild.getMembersWithRoles(role);
                if (membersWithRole.isEmpty()){
                    deleteRole(guild, roleName);
                }
            },

            error -> {
                System.err.println("Failed to remove role " + role + " from member " +user.getName());
            }
        );
    }

    public static void deleteRole(Guild guild, String roleName) {
        List<Role> roles = guild.getRolesByName(roleName, true);
    
        if (!roles.isEmpty()) {
            Role roleToDelete = roles.get(0);
            roleToDelete.delete().queue(
                success -> System.out.println("Role Deleted '" + roleName),
                error -> System.err.println("Role Deletion Failed : " + error.getMessage())
            );
        }
    }
    
}
