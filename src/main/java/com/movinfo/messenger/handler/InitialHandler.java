package com.movinfo.messenger.handler;

import com.movinfo.messenger.command.MessageSender;
import com.movinfo.messenger.util.JDAUtils;
import com.movinfo.messenger.util.MongoUtils;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class InitialHandler extends ListenerAdapter{
    @Override
    public void onReady(ReadyEvent event){
        sendNotificationButtonIfNotSent(event);
    }

    private void sendNotificationButtonIfNotSent(ReadyEvent event){
        MongoUtils.watchMovieInfo();
        
        MessageChannel channel = event.getJDA().getTextChannelById(JDAUtils.getNotiSetChannelId());
        
        if (channel != null) {
            channel.getHistoryFromBeginning(1).queue(message -> {
                if (message.getRetrievedHistory().isEmpty()){
                    Button button = Button.primary("noti-btn", "알림 설정");
                    String text = "아래의 버튼을 눌러 원하는 영화의 알림을 설정해주세요.";
                    MessageSender.sendButtonMessage(channel, button, text);
                }
            });
        }
    }
}
