package com.nore.chatroom;

import com.nore.chatroom.controller.UserController;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nore.chatroom.dao")
public class ChatroomApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatroomApplication.class, args);
    }
    UserController userController = new UserController();
}
