package com.pravesh.payment.feign;

public record ApiResponseWrapper<T>(boolean success, String message, T data) {}
