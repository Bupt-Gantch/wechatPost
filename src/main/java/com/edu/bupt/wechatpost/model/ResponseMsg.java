package com.edu.bupt.wechatpost.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMsg<T> {

    private int code;

    private String msg;

    private T data;
}
