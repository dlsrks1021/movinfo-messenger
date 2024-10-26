package com.movinfo.messenger.command;

import java.util.List;

import com.movinfo.messenger.util.JDAUtils;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

public class MessageSender {

    private MessageSender(){}

    public static void sendPrivateMessage(User user, String content){
        user.openPrivateChannel().queue(channel -> 
            channel.sendMessage(content).queue()
        );
    }

    public static void sendMessage(MessageChannel channel, String text){
        MessageCreateAction action = channel.sendMessage(text);
        action.queue();
    }

    public static void sendMessageWithRoleMentions(MessageChannel channel, String text, List<String> roleNames){
        StringBuilder stringBuilder = new StringBuilder(text);
        stringBuilder.append("\n");
        roleNames.forEach(roleName -> {
            Role role = RoleManager.getRoleByName(JDAUtils.getGuild(), roleName);
            stringBuilder.append(role.getAsMention());
        });
        
        channel.sendMessage(stringBuilder.toString()).queue();
    }

    public static void sendMessageWithImage(MessageChannel channel, String text, byte[] image, String imageName){
        channel.sendMessage(text)
               .addFiles(FileUpload.fromData(image, imageName))
               .queue();
    }

    public static void sendButtonMessage(MessageChannel channel, Button button, String text){
        MessageCreateAction action = channel.sendMessage(text).setActionRow(button);
        action.queue();
    }
}
