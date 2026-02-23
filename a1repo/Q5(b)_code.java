package com.example.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSystemTest {

    @Test
    void processOrder_withBlackFridayAndPaypal_marksPaid() {
        Order order = new Order();
        order.addItem(new OrderItem("Headphones", 1, 100.0));

        OrderService service = new OrderService();
        double total = service.processOrder(order, "BLACKFRIDAY", "paypal");

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(84.0, total, 1e-9);
    }

    @Test
    void processOrder_withBlankDiscountCode_chargesFullPricePlusTax() {
        Order order = new Order();
        order.addItem(new OrderItem("Mouse", 2, 25.0));

        OrderService service = new OrderService();
        double total = service.processOrder(order, "   ", "card");

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(60.0, total, 1e-9);
    }

    @Test
    void processOrder_withCrypto_cancelsOrder() {
        Order order = new Order();
        order.addItem(new OrderItem("Cable", 1, 10.0));

        OrderService service = new OrderService();
        double total = service.processOrder(order, "STUDENT10", "crypto");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(0.0, total, 1e-9);
    }

    @Test
    void processOrder_withUnknownPayment_throwsAndKeepsCreatedStatus() {
        Order order = new Order();
        order.addItem(new OrderItem("Keyboard", 1, 80.0));

        OrderService service = new OrderService();

        assertThrows(UnsupportedOperationException.class,
                () -> service.processOrder(order, "STUDENT10", "bank-transfer"));
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void discountService_coversAllBranches() {
        DiscountService discountService = new DiscountService();

        assertEquals(100.0, discountService.applyDiscount(100.0, null), 1e-9);
        assertEquals(100.0, discountService.applyDiscount(100.0, "   "), 1e-9);
        assertEquals(90.0, discountService.applyDiscount(100.0, "student10"), 1e-9);
        assertEquals(70.0, discountService.applyDiscount(100.0, "BLACKFRIDAY"), 1e-9);
        assertEquals(100.0, discountService.applyDiscount(100.0, "VIP"), 1e-9);
        assertThrows(IllegalArgumentException.class,
                () -> discountService.applyDiscount(100.0, "INVALID"));
    }

    @Test
    void paymentValidator_coversAllBranches() {
        PaymentValidator validator = new PaymentValidator();

        assertFalse(validator.isPaymentMethodValid(null));
        assertTrue(validator.isPaymentMethodValid("card"));
        assertTrue(validator.isPaymentMethodValid("paypal"));
        assertFalse(validator.isPaymentMethodValid("crypto"));
        assertThrows(UnsupportedOperationException.class,
                () -> validator.isPaymentMethodValid("wire"));
    }

    @Test
    void pricingService_coversSubtotalAndTaxBranches() {
        PricingService pricingService = new PricingService();
        Order order = new Order();
        order.addItem(new OrderItem("A", 1, 10.0));
        order.addItem(new OrderItem("B", 2, 15.0));

        assertEquals(40.0, pricingService.calculateSubtotal(order), 1e-9);
        assertEquals(0.0, pricingService.calculateTax(0.0), 1e-9);
        assertEquals(8.0, pricingService.calculateTax(40.0), 1e-9);
        assertThrows(IllegalArgumentException.class, () -> pricingService.calculateTax(-1.0));
    }

    @Test
    void orderItem_validAndInvalidInputs() {
        OrderItem item = new OrderItem("Phone", 2, 99.5);

        assertEquals(199.0, item.getTotalPrice(), 1e-9);
        assertEquals(2, item.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("BadQ", 0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("BadP", 1, -0.1));
    }

    @Test
    void order_disallowsAddAfterProcessed() {
        Order order = new Order();
        order.setStatus(OrderStatus.PAID);

        assertThrows(IllegalStateException.class, () -> order.addItem(new OrderItem("X", 1, 1.0)));
    }
}
