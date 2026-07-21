package com.abrar.BOOKSTORE.Login;

public record ApiResponse(boolean success, String message) {

    public static ApiResponse success(String message) {
        return new ApiResponse(true, message);
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public String message() {
        return message;
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message);
    }
}
