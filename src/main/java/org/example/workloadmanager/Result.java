package org.example.workloadmanager;

import lombok.Getter;

@Getter
public class Result<T> {

    private T value;
    private boolean success;
    private String errorMessage;

    private Result(T value, boolean success, String errorMessage){
        this.value = value;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static <T> Result<T> success(T value){
        return new Result<>(value, true, null);
    }

    public static <T> Result<T> success(){
        return new Result<>(null, true, null);
    }

    public static <T> Result<T> failure(String errorMessage){
        return new Result<>(null, false, errorMessage);
    }

}
