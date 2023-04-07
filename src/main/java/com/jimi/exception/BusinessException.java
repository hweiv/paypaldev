package com.jimi.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessException extends Exception {
    private static final long serialVersionUID = 1L;
    private String msg;

}
