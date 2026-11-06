package uz.checkout.sdk;

import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client for the account-level (profile) v2 endpoints. Authenticated with
 * a <strong>profile API key</strong> (from account settings), sent as
 * {@code Authorization: Bearer <profileApiKey>}. This is a different
 * credential from a shop's API key used by {@link CheckoutClient} - it is
 * not tied to one shop and can read data across every shop the account owns.
 *
 * <pre>{@code
 * ProfileClient profile = new ProfileClient("profile_api_key");
 * profile.getShops();
 * }</pre>
 */
public class ProfileClient extends BaseClient {

    public ProfileClient(String profileApiKey) {
        super(V2Client.BASE_URL, profileApiKey);
    }

    ProfileClient(String profileApiKey, HttpTransport transport) {
        super(V2Client.BASE_URL, profileApiKey, transport);
    }

    /** Return the authenticated account's profile information. */
    public JSONObject getProfile() {
        return post("get_profile", Collections.emptyMap());
    }

    /** List every (non-deleted) shop owned by the authenticated account. */
    public JSONObject getShops() {
        return post("get_shops", Collections.emptyMap());
    }

    /** Return aggregate stats across all of the account's shops, broken down by shop and by provider. */
    public JSONObject getProfileStats() {
        return getProfileStats(Collections.emptyMap());
    }

    /** Accepted {@code options} keys: {@code dateFrom} (String), {@code dateTo} (String). */
    public JSONObject getProfileStats(Map<String, Object> options) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("date_from", options.get("dateFrom"));
        params.put("date_to", options.get("dateTo"));
        return post("get_profile_stats", params);
    }
}
