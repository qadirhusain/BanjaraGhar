package com.banjaraghar.backend.email;

import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.entity.OrderItem;
import com.banjaraghar.backend.shipping.entity.ReturnRequest;
import com.banjaraghar.backend.shipping.entity.Shipment;
import com.banjaraghar.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Builds the customer emails about an order. Call these inside the transaction that changes the order:
 * the text is built right away and the mail is only sent after that transaction commits.
 */
@Service
@RequiredArgsConstructor
public class OrderEmailService {

    private static final String SIGN_OFF = "\n\nThank you for shopping with us,\nBanjara Ghar";

    private final ApplicationEventPublisher eventPublisher;

    public void orderConfirmed(Order order) {
        StringBuilder items = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            items.append("  - ").append(item.getProductName()).append(" x ").append(item.getQuantity())
                    .append("   ").append(rupees(item.getLineTotal())).append('\n');
        }

        StringBuilder totals = new StringBuilder("Subtotal: ").append(rupees(order.getSubtotal())).append('\n');
        if (order.getDiscountAmount() > 0) {
            String code = order.getCouponCode() == null ? "" : " (" + order.getCouponCode() + ")";
            totals.append("Discount").append(code).append(": -").append(rupees(order.getDiscountAmount())).append('\n');
        }
        totals.append("Shipping: ").append(order.getShippingFee() == 0 ? "Free" : rupees(order.getShippingFee()))
                .append('\n')
                .append("Total paid: ").append(rupees(order.getTotalAmount()));

        String body = greeting(order)
                + "Thank you for your order! We have received your payment and will pack it soon.\n\n"
                + "Order number: " + order.getOrderNumber() + "\n\n"
                + items + "\n"
                + totals + "\n\n"
                + "Delivering to:\n" + address(order) + "\n\n"
                + "We will email you again as soon as your order ships."
                + SIGN_OFF;

        publish(order, "Order " + order.getOrderNumber() + " confirmed - Banjara Ghar", body);
    }

    public void orderShipped(Order order, Shipment shipment, String trackingUrl) {
        StringBuilder body = new StringBuilder(greeting(order))
                .append("Good news! Your order ").append(order.getOrderNumber()).append(" is on its way.\n\n");
        if (shipment.getCourierName() != null) {
            body.append("Courier: ").append(shipment.getCourierName()).append('\n');
        }
        body.append("Tracking number: ").append(shipment.getAwbCode()).append('\n')
                .append("Track your parcel: ").append(trackingUrl).append("\n\n")
                .append("Delivering to:\n").append(address(order)).append(SIGN_OFF);

        publish(order, "Your order " + order.getOrderNumber() + " has shipped - Banjara Ghar", body.toString());
    }

    /** A paid order was canceled and its refund has been started. */
    public void orderCancelledWithRefund(Order order) {
        long amount = order.getRefundAmount() == null ? order.getTotalAmount() : order.getRefundAmount();
        String body = greeting(order)
                + "Your order " + order.getOrderNumber() + " has been cancelled.\n\n"
                + "We have started a refund of " + rupees(amount) + " to your original payment method. "
                + "It usually reaches your account within 5 to 7 working days."
                + SIGN_OFF;

        publish(order, "Order " + order.getOrderNumber() + " cancelled - Banjara Ghar", body);
    }

    /** The customer paid after the order had already expired or been canceled, so the money is being returned. */
    public void latePaymentRefunded(Order order) {
        long amount = order.getRefundAmount() == null ? order.getTotalAmount() : order.getRefundAmount();
        String body = greeting(order)
                + "We received your payment for order " + order.getOrderNumber()
                + " after the order had already been closed, so we could not process it.\n\n"
                + "We have started a full refund of " + rupees(amount) + " to your original payment method. "
                + "It usually reaches your account within 5 to 7 working days. "
                + "You are welcome to place a new order any time."
                + SIGN_OFF;

        publish(order, "Refund for order " + order.getOrderNumber() + " - Banjara Ghar", body);
    }

    public void returnApproved(Order order, ReturnRequest request) {
        StringBuilder body = new StringBuilder(greeting(order))
                .append("Your return request for order ").append(order.getOrderNumber()).append(" has been approved.\n\n")
                .append("Refund: ").append(rupees(request.getRefundAmount()))
                .append(" to your original payment method, usually within 5 to 7 working days.\n\n");
        if (request.getReturnAwb() != null) {
            body.append("A courier pickup has been arranged (tracking number ").append(request.getReturnAwb())
                    .append("). Please keep the item packed and ready.\n\n");
        } else {
            body.append("We will contact you shortly with the details for sending the item back.\n\n");
        }
        if (request.getAdminNote() != null) {
            body.append("Note from us: ").append(request.getAdminNote()).append("\n\n");
        }
        body.append("Reply to this email if you have any questions.").append(SIGN_OFF);

        publish(order, "Return approved for order " + order.getOrderNumber() + " - Banjara Ghar", body.toString());
    }

    public void returnRejected(Order order, ReturnRequest request) {
        StringBuilder body = new StringBuilder(greeting(order))
                .append("We have reviewed your return request for order ").append(order.getOrderNumber())
                .append(" and, unfortunately, we are not able to approve it.\n\n");
        if (request.getAdminNote() != null) {
            body.append("Reason: ").append(request.getAdminNote()).append("\n\n");
        }
        body.append("If you think this is a mistake, reply to this email and we will take another look.").append(SIGN_OFF);

        publish(order, "Update on your return for order " + order.getOrderNumber() + " - Banjara Ghar", body.toString());
    }

    // ----------------------------------------------------------------- helpers

    private void publish(Order order, String subject, String body) {
        User user = order.getUser();
        if (user.getDeletedAt() != null) {
            return;   // the account was deleted, so there is nobody to write to
        }
        eventPublisher.publishEvent(new EmailRequestedEvent(user.getEmail(), subject, body));
    }

    private String greeting(Order order) {
        String firstName = order.getUser().getFullName().trim().split("\\s+")[0];
        return "Hi " + firstName + ",\n\n";
    }

    private String address(Order order) {
        StringBuilder address = new StringBuilder(order.getShipName()).append('\n').append(order.getShipLine1()).append('\n');
        if (order.getShipLine2() != null && !order.getShipLine2().isBlank()) {
            address.append(order.getShipLine2()).append('\n');
        }
        return address.append(order.getShipCity()).append(", ").append(order.getShipState())
                .append(" - ").append(order.getShipPincode()).append('\n')
                .append("Phone: ").append(order.getShipPhone()).toString();
    }

    private static String rupees(long paise) {
        return String.format(Locale.ENGLISH, "Rs %,.2f", BigDecimal.valueOf(paise, 2));
    }
}
