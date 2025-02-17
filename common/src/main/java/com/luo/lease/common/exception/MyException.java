package com.luo.lease.common.exception;

import com.luo.lease.common.result.ResultCodeEnum;
import lombok.Data;

@Data
public class MyException extends RuntimeException{
    private Integer code;

    public MyException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public MyException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
