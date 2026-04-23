package com.example.swaggerservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 생성 요청")
public class CreateOrderRequest {

    @Schema(description = "주문자 ID", example = "user-1001")
    private String userId;

    @Schema(description = "상품 코드", example = "ITEM-001")
    private String productCode;

    @Schema(description = "수량", example = "2")
    private int quantity;

    @Schema(description = "결제 수단", example = "CARD")
    private String paymentMethod;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}