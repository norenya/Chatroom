package com.nore.chatroom.service;

import com.nore.chatroom.dao.userDAO;
import com.nore.chatroom.userDTO.ResultVO;
import com.nore.chatroom.userDTO.userDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private userDAO userDAO;

    @Override
    public ResultVO<Integer> insertUser(userDTO userDTO) {
        int result = 0;
        try {
            result = userDAO.insertUser(userDTO);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return ResultVO.error("新增失败，重复的ID");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.error("新增失败，未知错误");
        }
        if (result == 0) {
            return ResultVO.error("新增失败");
        } else {
            return ResultVO.success(result);
        }
    }

    @Override
    public List<userDTO> getUsers() {
        return userDAO.getUsers();
    }

    @Override
    public userDTO loginUser(userDTO userDTO) {
        return userDAO.loginUser(userDTO);
    }

    @Override
    public ResultVO<Integer> deleteUser(userDTO userDTO) {
        int result = userDAO.deleteUser(userDTO);
        if (result == 0) {
            return ResultVO.error("删除失败");
        } else {
            return ResultVO.success(result);
        }
    }

    @Override
    public List<userDTO> searchUsers(userDTO userDTO) {
        return userDAO.searchUsers(userDTO);
    }

    @Override
    public Integer editUser(userDTO userDTO) {
        return userDAO.editUser(userDTO);
    }

    @Override
    public userDTO getUserById(Integer ID) {
        return userDAO.getUserById(ID);
    }
}
