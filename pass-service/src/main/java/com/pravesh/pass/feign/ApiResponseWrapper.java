package com.pravesh.pass.feign;

public record ApiResponseWrapper<T>(boolean success, String message, T data) {}