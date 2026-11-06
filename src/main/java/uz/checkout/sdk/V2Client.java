package uz.checkout.sdk;

import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client for the CHECKOUT.UZ merchant API v2 ({@code /api/v2}).
 * Authenticated with the same shop API key as v1. v2 adds idempotent
 * payment creation, pagination, cancellation/refunds and fiscal receipts
 * on top of everything v1 offers.
 *
 * <p>Note: {@code get_profile}/{@code get_shops}/{@code get_profile_stats}
 * are account-level (not shop-level) and use a <em>different</em>
 * credential - see {@link ProfileClient}.
 */
public class V2Client extends BaseClient {

    public static final String BASE_URL = "https://checkout.uz/api/v2";

    public V2Client(String apiKey) {
        super(BASE_URL, apiKey);
    }

    V2Client(String apiKey, HttpTransport transport) {
        super(BASE_URL, apiKey, transport);
    }

    /** Create a new payment invoice. {@code amount} is in UZS (1000-10,000,000). */
    public JSONObject createPayment(double amount) {
        return createPayment(amount, Collections.emptyMap());
    }

    /**
     * Create a new payment invoice. Accepted {@code options} keys:
     * {@code description} (String), {@code expireTime} (Integer, seconds, 60-432000, default 3600),
     * {@code webhookUrl} (String), {@code returnUrl} (String), {@code idempotencyKey} (String).
     *
     * <p>If {@code idempotencyKey} is given and the same key was used for this shop within the
     * last 24 hours, the original invoice is returned instead of creating a new one
     * ({@code idempotent_replay} in the response tells you which happened).
     */
    public JSONObject createPayment(double amount, Map<String, Object> options) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("amount", amount);
        params.put("description", options.get("description"));
        params.put("expire_time", options.get("expireTime"));
        params.put("webhook_url", options.get("webhookUrl"));
        params.put("return_url", options.get("returnUrl"));

        Map<String, String> headers = Collections.emptyMap();
        Object idempotencyKey = options.get("idempotencyKey");
        if (idempotencyKey != null) {
            headers = Collections.singletonMap("Idempotency-Key", idempotencyKey.toString());
        }

        return post("create_payment", params, headers);
    }

    /** Look up an invoice by its numeric id. */
    public JSONObject statusPaymentById(long id) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("id", id);
        return post("status_payment", params);
    }

    /** Look up an invoice by its uuid (pay_token). */
    public JSONObject statusPaymentByUuid(String uuid) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("uuid", uuid);
        return post("status_payment", params);
    }

    /** Return the shop's current balance per currency (uppercase currency codes). */
    public JSONObject getBalance() {
        return post("get_balance", Collections.emptyMap());
    }

    /** Return the shop's 10 most recent invoices (newest first). */
    public JSONObject getHistory() {
        return getHistory(Collections.emptyMap());
    }

    /**
     * Return the shop's invoices, newest first. Accepted {@code options} keys:
     * {@code limit} (Integer, 1-200, default 10), {@code offset} (Integer, default 0),
     * {@code status} (String: pending/paid/canceled/reversed), {@code dateFrom} (String),
     * {@code dateTo} (String). The response's {@code has_more} tells you whether another page exists.
     */
    public JSONObject getHistory(Map<String, Object> options) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("limit", options.getOrDefault("limit", 10));
        params.put("offset", options.getOrDefault("offset", 0));
        params.put("status", options.get("status"));
        params.put("date_from", options.get("dateFrom"));
        params.put("date_to", options.get("dateTo"));
        return post("get_history", params);
    }

    /** Return aggregate order count / income for the shop, broken down by payment provider. */
    public JSONObject getStats() {
        return getStats(Collections.emptyMap());
    }

    /** Accepted {@code options} keys: {@code dateFrom} (String), {@code dateTo} (String). */
    public JSONObject getStats(Map<String, Object> options) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("date_from", options.get("dateFrom"));
        params.put("date_to", options.get("dateTo"));
        return post("get_stats", params);
    }

    /** List the payment methods enabled for this shop, including min/max amount and fiscal support. */
    public JSONObject getPaymentMethods() {
        return post("get_payment_methods", Collections.emptyMap());
    }

    /** Start a direct card payment (Uzcard/Humo) for a pending invoice. Sends an SMS code to the cardholder. */
    public JSONObject payViaCard(long orderId, String cardNumber, String cardExpiry) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("order_id", orderId);
        params.put("card_number", cardNumber);
        params.put("card_expiry", cardExpiry);
        return post("pay_via_card", params);
    }

    /** Confirm a card payment started with {@link #payViaCard} using the SMS code the cardholder received. */
    public JSONObject confirmCardPayment(String paymentToken, String smsCode) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("payment_token", paymentToken);
        params.put("sms_code", smsCode);
        return post("confirm_card_payment", params);
    }

    /** Cancel a pending invoice. Fails if the invoice is not in {@code pending} status. */
    public JSONObject cancelPayment(long orderId) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("order_id", orderId);
        return post("cancel_payment", params);
    }

    /**
     * Reverse a successful card/Payme/Click payment and deduct it back from the shop balance.
     * Only available within a limited window after the original payment (currently 22 hours);
     * the API returns an error past that point.
     */
    public JSONObject refundPayment(long orderId) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("order_id", orderId);
        return post("refund_payment", params);
    }

    /** Fetch the fiscal (tax) receipt for a paid order. Supported for Payme, Click and Plum payments only. */
    public JSONObject getFiscal(long orderId) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("order_id", orderId);
        return post("get_fiscal", params);
    }
}
