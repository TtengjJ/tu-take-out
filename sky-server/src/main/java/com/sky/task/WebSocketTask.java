package com.sky.task;

import com.sky.WebSocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class WebSocketTask {
    @Autowired
    private WebSocketServer webSocketServer;

    @Scheduled(cron = "0/30 * * * * ?") // 每30秒执行一次
    public void sendMessageToClient(){
        webSocketServer.sendToAllClient("服务器定时消息");
    }
}
