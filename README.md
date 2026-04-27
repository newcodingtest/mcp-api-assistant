### 자연어 기반으로 사내 Swagger/OpenAPI 명세를 탐색할 수 있는 AI 개발자 도우미입니다.
<BR>
Spring AI와 MCP(Model Context Protocol)를 기반으로,
<BR>
LLM이 API 탐색 과정을 직접 수행하도록 설계하였습니다.

사용자는 단순한 질문만으로 아래와 같은 내용을 한번에 확인 가능 합니다.
```
- 서비스 검색
- endpoint 탐색
- API 상세 명세 조회
- request/response 구조 분석
```


<BR>
이를 통해 기존의 정적 API 문서 탐색을
동적이고 맥락 기반의 API 조회 방식으로 전환했습니다.

---
## 사용 방법

### 1. API 요청

아래와 같이 AI API 엔드포인트에 질문을 전달합니다.

```http
POST http://localhost:8089/api/ai/swagger
Content-Type: application/json
{
  "message": "test1 서비스의 OpenAPI 명세서를 조회해서 핵심 엔드포인트를 설명해줘"
}
```

## 2.응답
```text
test1 서비스의 OpenAPI 명세서를 조회한 결과, 핵심 엔드포인트는 다음과 같습니다:

### **주문 API (Order)**

1. **주문 생성**
   - **METHOD**: `POST`
   - **PATH**: `/api/v1/orders`
   - **설명**: 신규 주문을 생성합니다.
   - **요청 본문**: `application/json` 형식의 `CreateOrderRequest` (userId, productCode, quantity, paymentMethod 포함)
   - **응답**: `200 OK` - `OrderResponse` (orderId, status, message 포함)

2. **주문 조회**
   - **METHOD**: `GET`
   - **PATH**: `/api/v1/orders/{orderId}`
   - **설명**: 주문 ID로 주문 정보를 조회합니다.
   - **요청 파라미터**: `orderId` (path parameter, string 형식)
   - **응답**: `200 OK` - `OrderResponse` (orderId, status, message 포함)

3. **주문 취소**
   - **METHOD**: `DELETE`
   - **PATH**: `/api/v1/orders/{orderId}`
   - **설명**: 주문 ID로 주문을 취소합니다.
   - **요청 파라미터**: `orderId` (path parameter, string 형식)
   - **응답**: `200 OK` - `OrderResponse` (orderId, status, message 포함)

---

### **요청/응답 스키마**

- **CreateOrderRequest**:
  - `userId`: 주문자 ID (string, minLength: 1)
  - `productCode`: 상품 코드 (string, minLength: 1)
  - `quantity`: 수량 (integer, minimum: 1)
  - `paymentMethod`: 결제 수단 (string, minLength: 1)

- **OrderResponse**:
  - `orderId`: 주문 ID (string)
  - `status`: 주문 상태 (string)
  - `message`: 응답 메시지 (string)

---

### **서버 정보**
- **URL**: `http://localhost:8082`
- **버전**: `v1.0.0`
- **설명**: 테스트 서비스1용 OpenAPI 문서

이 서비스는 주문 관리 기능을 제공하는 API로, 주문 생성, 조회, 취소 기능을 제공합니다.

```


---
## 기술스택
- Spring Boot
- Spring AI
- MCP (Model Context Protocol)
- Ollama Qwen3.5:9b (Local LLM)
- OpenAPI / Swagger
