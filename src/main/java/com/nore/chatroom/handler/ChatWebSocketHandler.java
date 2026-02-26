package com.nore.chatroom.handler;

import com.nore.chatroom.userDTO.userDTO;
import com.nore.chatroom.userDTO.MessageDTO;
import com.nore.chatroom.service.UserServiceImpl;
import com.nore.chatroom.service.MessageService;
import com.nore.chatroom.service.impl.MessageServiceImpl;
import com.google.gson.Gson;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final UserServiceImpl userService;
    private final MessageService messageService;
    private final Gson gson = new Gson();
    private final Map<WebSocketSession, String> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(UserServiceImpl userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("新连接: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);

        Map messageData = gson.fromJson(payload, Map.class);
        String type = (String) messageData.get("type");

        if ("login".equals(type)) {
            handleLogin(session, messageData);
        } else if ("chat".equals(type)) {
            handleChat(messageData);
        }
    }

    private void handleLogin(WebSocketSession session, Map messageData) throws Exception {
        String token = (String) messageData.get("token");
        userDTO user = userService.getUserById(Integer.parseInt(token));

        if (user != null) {
            sessionUserMap.put(session, user.getID().toString());
            userSessionMap.put(user.getID().toString(), session);

            Map response = new HashMap();
            response.put("type", "loginSuccess");
            response.put("user", user);
            session.sendMessage(new TextMessage(gson.toJson(response)));

            sendUserListToAll();
        }
    }

    private void sendUserListToAll() throws Exception {
        Map userListMessage = new HashMap();
        userListMessage.put("type", "userList");
        
        List<userDTO> onlineUsers = new ArrayList<>();
        for (String userId : userSessionMap.keySet()) {
            userDTO user = userService.getUserById(Integer.parseInt(userId));
            if (user != null) {
                onlineUsers.add(user);
            }
        }
        userListMessage.put("users", onlineUsers);

        for (WebSocketSession webSocketSession : sessionUserMap.keySet()) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(gson.toJson(userListMessage)));
            }
        }
    }

    private void handleChat(Map messageData) throws Exception {
        // Save message to database
        saveMessageToDatabase(messageData);
        
        // Broadcast message to all connected users with channel filtering
        for (WebSocketSession webSocketSession : sessionUserMap.keySet()) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(gson.toJson(messageData)));
            }
        }
    }
    
    private void saveMessageToDatabase(Map messageData) {
        try {
            MessageDTO message = new MessageDTO();
            
            // Handle sender ID conversion properly
            Object senderIdObj = messageData.get("senderId");
            Integer senderId = parseInteger(senderIdObj);
            if (senderId == null) {
                System.err.println("Invalid sender ID: " + senderIdObj);
                return;
            }
            message.setSenderId(senderId);
            
            message.setSenderName((String) messageData.get("senderName"));
            
            // Handle different message types
            String type = (String) messageData.get("type");
            message.setMessageType(type);
            
            // Set content based on message type
            if ("file".equals(type) || "privateFile".equals(type) || "groupFile".equals(type)) {
                message.setContent((String) messageData.get("fileName")); // Store filename as content for file messages
                message.setFileName((String) messageData.get("fileName"));
                message.setFileUrl((String) messageData.get("fileUrl"));
            } else {
                message.setContent((String) messageData.get("text"));
            }
            
            // Set target user ID for private messages
            Object targetUserIdObj = messageData.get("targetUserId");
            if (targetUserIdObj != null) {
                Integer targetUserId = parseInteger(targetUserIdObj);
                if (targetUserId != null) {
                    message.setTargetUserId(targetUserId);
                }
            }
            
            // Set channel ID from message data (important for channel isolation)
            Object channelIdObj = messageData.get("channelId");
            if (channelIdObj != null) {
                Integer channelId = parseInteger(channelIdObj);
                if (channelId != null) {
                    message.setChannelId(channelId);
                    System.out.println("Setting channel ID: " + channelId + " for message");
                } else {
                    System.err.println("Failed to parse channel ID: " + channelIdObj);
                }
            } else {
                System.err.println("Channel ID is null in message data");
            }
            
            // Save to database
            System.out.println("Saving message to database: " + message);
            messageService.saveMessage(message);
            System.out.println("Message saved successfully");
            
        } catch (Exception e) {
            System.err.println("Error saving message to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper method to safely parse integers from objects that might be floats
    private Integer parseInteger(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            if (obj instanceof Integer) {
                return (Integer) obj;
            } else if (obj instanceof Double) {
                return ((Double) obj).intValue();
            } else if (obj instanceof Float) {
                return ((Float) obj).intValue();
            } else if (obj instanceof Long) {
                return ((Long) obj).intValue();
            } else {
                // Try to parse as string
                String str = obj.toString();
                // Handle decimal values like "2.0"
                if (str.contains(".")) {
                    return (int) Double.parseDouble(str);
                } else {
                    return Integer.parseInt(str);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Could not parse integer from: " + obj + ", error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = sessionUserMap.remove(session);
        if (userId != null) {
            userSessionMap.remove(userId);
            System.out.println("用户断开连接: " + userId);
            sendUserListToAll();
        }
        System.out.println("连接关闭: " + session.getId());
    }
}
