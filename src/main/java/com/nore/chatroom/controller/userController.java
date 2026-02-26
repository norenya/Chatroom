package com.nore.chatroom.controller;

import com.nore.chatroom.userDTO.ResultVO;
import com.nore.chatroom.userDTO.userDTO;
import com.nore.chatroom.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/auth/login")
    public ResultVO<userDTO> login(@RequestBody userDTO userDTO)
    {
        userDTO userDTO1 = userService.loginUser(userDTO);
        if(userDTO1 != null){
            if(userDTO.getPassword().equals(userDTO1.getPassword())){
                userDTO1.setPassword("");
                return ResultVO.success(userDTO1);
            }else{
                return ResultVO.error("密码错误");
            }
        }
        else{
            return ResultVO.error("用户不存在");
        }
    }

    @PostMapping("/auth/register")
    public ResultVO<userDTO> register(@RequestBody userDTO userDTO)
    {
        // 检查用户是否已存在
        userDTO existingUser = userService.loginUser(userDTO);
        if(existingUser != null){
            return ResultVO.error("该电话号码已注册");
        }

        // 设置默认用户名（使用电话号码）
        if(userDTO.getUsername() == null || userDTO.getUsername().isEmpty()){
            userDTO.setUsername(userDTO.getPhone());
        }

        // 插入用户
        ResultVO<Integer> insertResult = userService.insertUser(userDTO);
        if(insertResult.getCode().equals(200)){
            // 插入成功后，查询用户信息返回
            userDTO newUser = userService.loginUser(userDTO);
            if(newUser != null){
                newUser.setPassword("");
                return ResultVO.success(newUser);
            }
        }
        return ResultVO.error("注册失败");
    }

    @PostMapping("/user/login")
    public ResultVO<userDTO> oldLogin(@RequestBody userDTO userDTO)
    {
        return login(userDTO);
    }

    @PostMapping("/user/deleteuser")
    public ResultVO<Integer> deleteUser(@RequestBody userDTO userDTO) {
        return userService.deleteUser(userDTO);
    }

    @PostMapping("/user/getusers")
    public ResultVO<List<userDTO>> getUsers() {
        List<userDTO> userDTO = userService.getUsers();
        return ResultVO.success(userDTO);
    }

    @PostMapping("/user/insertuser")
    public ResultVO<Integer> insertUser(@RequestBody userDTO userDTO) {
        return userService.insertUser(userDTO);
    }

    @PostMapping("/user/editUser")
    public ResultVO<Integer> editUser(@RequestBody userDTO userDTO) {
        System.out.println(userDTO.toString());
        return ResultVO.success(userService.editUser(userDTO));
    }

    @PostMapping("/user/searchUsers")
    public ResultVO<List<userDTO>> searchUsers(@RequestBody userDTO userDTO) {
        return ResultVO.success(userService.searchUsers(userDTO));
    }
}
