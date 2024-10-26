package com.movinfo.messenger.command;

import java.util.List;

import java.awt.Color;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class RoleManager {

    public static final int MAX_NUM_ROLES = 250;

    private RoleManager(){}

    public static int getNumRoles(Guild guild){
        return guild.getRoles().size();
    }

    public static boolean isRoleExist(Guild guild, String roleName){
        return !guild.getRolesByName(roleName, true).isEmpty();
    }

    public static Role getRoleByName(Guild guild, String roleName){
        return guild.getRolesByName(roleName, true).get(0);
    }

    public static void createRole(Guild guild, String roleName, Color color) {
        guild.createRole()
             .setName(roleName)
             .setColor(color)
             .setMentionable(true)
             .queue(
                 role -> System.out.println("Role Created '" + role.getName()),
                 error -> System.err.println("Role Creation Failed : " + error.getMessage())
             );
    }    

    public static void addRoleToMember(Guild guild, String roleName, User user){
        Role role = guild.getRolesByName(roleName, true).get(0);
        guild.addRoleToMember(user, role).queue();
    }

    public static boolean hasRole(Guild guild, String roleName, User user){
        Member member = guild.getMember(user);

        if (member != null){
            Role role = guild.getRolesByName(roleName, true).get(0);
            return member.getRoles().contains(role);
        }

        return false;
    }

    public static void removeRoleFromMember(Guild guild, String roleName, User user){
        Role role = guild.getRolesByName(roleName, true).get(0);
        guild.removeRoleFromMember(user, role);
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
