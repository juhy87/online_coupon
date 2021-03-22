package com.kakaopay.coupon.biz.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class APIExceptionHandler {

    @ExceptionHandler(ApiRuntimeException.class)
    @ResponseBody
    protected ResponseEntity<APIErrorResponse> handleApiException(ApiRuntimeException e) {
        return ResponseEntity.badRequest().body(
                        new APIErrorResponse(
                                e.getError(),
                                e.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(ApiException.class)
    @ResponseBody
    protected ResponseEntity<APIErrorResponse> handleApiException(ApiException e) {
        return ResponseEntity.badRequest().body(
                new APIErrorResponse(
                        e.getError(),
                        e.getMessage(),
                        null
                )
        );
    }


}
