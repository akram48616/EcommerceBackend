package org.example.ecommercebackend;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;

public class GenerateTestSignature {
    public static void main(String[] args) throws Exception {
        String orderId = "order_";  // ← your real razorpayOrderId from last test
        String paymentId = "pay_te456";  // ← any fake string, format doesn't matter for this test
        String secret = "bVqE6Ov7brWer27MWv6xD4xA";

        String payload = orderId + "|" + paymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes());

        String signature = HexFormat.of().formatHex(hash);
        System.out.println("razorpay_payment_id: " + paymentId);
        System.out.println("razorpay_signature: " + signature);
    }
}
