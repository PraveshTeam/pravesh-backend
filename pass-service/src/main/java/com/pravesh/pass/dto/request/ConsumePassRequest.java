package com.pravesh.pass.dto.request;

public record ConsumePassRequest(
        boolean setConsumed // true for ONE_TIME or last MULTI_USE, false to just decrement
) {}