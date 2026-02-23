package com.example.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderSystemTest {

    @Test
    void processOrder_withStudentDiscountAndCard_marksPaidAndReturnsTotal() {
        Order order = new Order();
        order.addItem(new OrderItem("Book", 2, 25.0));
        order.addItem(new OrderItem("Bag", 1, 50.0));

        OrderService service = new OrderService();
        double total = service.processOrder(order, "student10", "card");

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(108.0, total, 1e-9);
    }

    @Test
    void processOrder_withCryptoPayment_cancelsOrderAndReturnsZero() {
        Order order = new Order();
        order.addItem(new OrderItem("Notebook", 1, 20.0));

        OrderService service = new OrderService();
        double total = service.processOrder(order, "BLACKFRIDAY", "crypto");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(0.0, total, 1e-9);
    }

    @Test
    void paymentValidator_withNullMethod_returnsFalse() {
        PaymentValidator validator = new PaymentValidator();
        assertEquals(false, validator.isPaymentMethodValid(null));
    }

    @Test
    void paymentValidator_withPaypal_returnsTrue() {
        PaymentValidator validator = new PaymentValidator();
        assertEquals(true, validator.isPaymentMethodValid("paypal"));
    }

    @Test
    void paymentValidator_withUnknownMethod_throws() {
        PaymentValidator validator = new PaymentValidator();
        assertThrows(UnsupportedOperationException.class, () -> validator.isPaymentMethodValid("wire"));
    }
}
