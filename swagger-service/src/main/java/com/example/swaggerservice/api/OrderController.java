package com.example.swaggerservice.api;

import com.example.swaggerservice.api.dto.CreateOrderRequest;
import com.example.swaggerservice.api.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "주문 API")
public class OrderController {

    @PostMapping
    @Operation(summary = "주문 생성", description = "신규 주문을 생성한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            )
    })
    public OrderResponse createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "주문 생성 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateOrderRequest.class)
                    )
            )
            @RequestBody CreateOrderRequest request) {
        return new OrderResponse(
                "ORD-" + UUID.randomUUID(),
                "CREATED",
                "주문이 생성되었습니다."
        );
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 조회", description = "주문 ID로 주문 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            )
    })
    public OrderResponse getOrder(
    @Parameter(
            name = "orderId",
            description = "주문 ID",
            required = true,
            example = "ORD-20260422-0001"
    )@PathVariable String orderId) {
        return new OrderResponse(
                orderId,
                "CREATED",
                "주문 조회 성공"
        );
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소", description = "주문 ID로 주문을 취소한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            )
    })
    public OrderResponse cancelOrder(
            @Parameter(
                    name = "orderId",
                    description = "주문 ID",
                    required = true,
                    example = "ORD-20260422-0001"
            )
            @PathVariable String orderId
    ) {
        return null;
    }
}
