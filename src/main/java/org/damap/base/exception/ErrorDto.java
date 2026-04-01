package org.damap.base.exception;

import org.damap.base.enums.EErrorCode;

public record ErrorDto(EErrorCode errorCode, String details) {}
