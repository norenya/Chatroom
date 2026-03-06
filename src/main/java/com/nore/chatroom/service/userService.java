package com.nore.chatroom.service;

import com.nore.chatroom.userDTO.userDTO;
import com.nore.chatroom.userDTO.ResultVO;
import com.nore.chatroom.userDTO.MessageDTO;
import java.util.List;
import java.util.Map;

public interface UserService {
    ResultVO<Integer> insertUser(userDTO userDTO);

    //获取全部用户
    List<userDTO> getUsers();

    //登录
    userDTO loginUser(userDTO userDTO);

    ResultVO<Integer> deleteUser(userDTO userDTO);

    List<userDTO> searchUsers(userDTO userDTO);

    Integer editUser(userDTO userDTO);

    //根据ID获取用户
    userDTO getUserById(Integer ID);
    
    //获取用户最近的聊天记录
    List<MessageDTO> getRecentChatMessages(int userId, int limit);
}
