package com.example.swaggerservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 생성 요청")
public class CreateOrderRequest {

    @NotBlank
    @Schema(
            description = "주문자 ID",
            example = "user-1001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userId;

    @NotBlank
    @Schema(
            description = "상품 코드",
            example = "ITEM-001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String productCode;

    @NotNull
    @Min(1)
    @Schema(
            description = "수량",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer quantity;

    @NotBlank
    @Schema(
            description = "결제 수단",
            example = "CARD",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
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