package com.nore.chatroom.dao;
import com.nore.chatroom.userDTO.userDTO;

import java.util.List;
public interface userDAO {
    List<userDTO> getUsers(); // 获取所有用户
    userDTO loginUser(userDTO userDTO); // 用户登录
    List<userDTO> searchUsers(userDTO userDTO); // 搜索用户

    Integer deleteUser(userDTO userDTO); // 删除用户

    Integer insertUser(userDTO userDTO); // 插入用户

    Integer editUser(userDTO userDTO); // 编辑用户

    userDTO getUserById(Integer ID); // 根据ID获取用户

}
