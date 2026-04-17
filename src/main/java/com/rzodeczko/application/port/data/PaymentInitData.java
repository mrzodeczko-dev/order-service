package com.rzodeczko.application.port.data;

import java.util.UUID;

public record PaymentInitData(UUID paymentId, String redirectUrl) {
}
