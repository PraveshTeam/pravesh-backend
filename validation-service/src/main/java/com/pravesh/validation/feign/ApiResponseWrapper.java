package com.pravesh.validation.feign;

public record ApiResponseWrapper<T>(boolean success, String message, T data) {}