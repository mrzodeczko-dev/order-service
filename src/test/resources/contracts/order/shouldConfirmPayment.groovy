package contracts.order

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should confirm payment for an order and return 200 OK"

    request {
        method POST()
        url "/orders/${value(consumer(anyUuid()), producer('a1b2c3d4-5678-9abc-def0-123456789abc'))}/payment"
        headers {
            contentType applicationJson()
        }
        body(
                paymentId: $(anyUuid())
        )
    }

    response {
        status OK()
    }
}
