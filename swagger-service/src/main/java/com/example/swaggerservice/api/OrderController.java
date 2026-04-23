package com.example.swaggerservice.api;

import com.example.swaggerservice.api.dto.CreateOrderRequest;
import com.example.swaggerservice.api.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "주문 API")
public class OrderController {

    @PostMapping
    @Operation(summary = "주문 생성", description = "신규 주문을 생성한다.")
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        return new OrderResponse(
                "ORD-" + UUID.randomUUID(),
                "CREATED",
                "주문이 생성되었습니다."
        );
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 조회", description = "주문 ID로 주문 정보를 조회한다.")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return new OrderResponse(
                orderId,
                "CREATED",
                "주문 조회 성공"
        );
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소", description = "주문 ID로 주문을 취소한다.")
    public OrderResponse cancelOrder(@PathVariable String orderId) {
        return new OrderResponse(
                orderId,
                "CANCELLED",
                "주문이 취소되었습니다."
        );
    }
}
