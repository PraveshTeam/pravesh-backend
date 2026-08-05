package com.pravesh.sos.feign;

public record ApiResponseWrapper<T>(boolean success, String message, T data) {}
