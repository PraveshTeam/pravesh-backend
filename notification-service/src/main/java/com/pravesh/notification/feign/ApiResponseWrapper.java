package com.pravesh.notification.feign;

public record ApiResponseWrapper<T>(boolean success, String message, T data) {}