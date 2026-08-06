package com.pravesh.forum.feign;

public record ApiResponseWrapper<T>(boolean success, String message, T data) {}
