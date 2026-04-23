package com.example.swaggerservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 응답")
public class OrderResponse {

    @Schema(description = "주문 ID", example = "ORD-20260422-0001")
    private String orderId;

    @Schema(description = "주문 상태", example = "CREATED")
    private String status;

    @Schema(description = "응답 메시지", example = "주문이 생성되었습니다.")
    private String message;

    public OrderResponse() {
    }

    public OrderResponse(String orderId, String status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
