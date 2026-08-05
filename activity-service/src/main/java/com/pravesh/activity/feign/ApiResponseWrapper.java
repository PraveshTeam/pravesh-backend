package com.pravesh.activity.feign;

public record ApiResponseWrapper<T>(boolean success, String message, T data) {}
