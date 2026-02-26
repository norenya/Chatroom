package com.nore.chatroom.userDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ResultVO<T> implements Serializable {

    // 响应状态码,默认200
    private Integer code = 200;

    // 响应消息,默认success
    private String message = "success";

    // 响应数据
    private T data;

    // 响应时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime timestamp = LocalDateTime.now();

    // 成功响应(无数据)
    public static <T> ResultVO<T> success() {
        return new ResultVO<>();
    }

    // 成功响应(有数据)
    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> resultVO = new ResultVO<>();
        resultVO.setData(data);
        return resultVO;
    }

    // 成功响应(自定义消息)
    public static <T> ResultVO<T> success(T data, String message) {
        ResultVO<T> resultVO = new ResultVO<>();
        resultVO.setData(data);
        resultVO.setMessage(message);
        return resultVO;
    }

    // 失败响应(无数据)
    public static <T> ResultVO<T> error() {
        ResultVO<T> resultVO = new ResultVO<>();
        resultVO.setCode(9527);
        return resultVO;
    }

    // 失败响应(有数据)
    public static <T> ResultVO<T> error(String message) {
        ResultVO<T> resultVO = new ResultVO<>();
        resultVO.setCode(9527);
        resultVO.setMessage(message);
        return resultVO;
    }

    // 失败响应(自定义消息)
    public static <T> ResultVO<T> error(String message, Integer code) {
        ResultVO<T> resultVO = new ResultVO<>();
        resultVO.setCode(code);
        resultVO.setMessage(message);
        return resultVO;
    }


    // 链式调用方法
    public ResultVO<T> code(Integer code) {
        this.code = code;
        return this;
    }

    public ResultVO<T> message(String message) {
        this.message = message;
        return this;
    }

    public ResultVO<T> data(T data) {
        this.data = data;
        return this;
    }
}
