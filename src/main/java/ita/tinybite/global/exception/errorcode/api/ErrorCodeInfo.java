package ita.tinybite.global.exception.errorcode.api;

import org.springframework.http.HttpStatus;

public record ErrorCodeInfo(HttpStatus httpStatus, String code, String message) {}
