package vn.intentional.facebook;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Only the user's own prompts are stored. No Facebook data is read. */
final class IntentBankStore {
    static final int SEARCH = 0;
    static final int NOTIFICATIONS = 1;
    static final int PERSON = 2;
    static final int FEED = 3;
    static final String[] TITLES = {
        "Tìm kiếm", "Xem thông báo mới", "Xem một người", "Xem News Feed"
    };
    static final String[] HINTS = {
        "Từ khóa hoặc điều bạn muốn tìm",
        "Bạn muốn kiểm tra điều gì?",
        "Tên người hoặc liên kết Facebook của họ",
        "Bạn muốn xem Feed để làm gì?"
    };
    private static final String FILE = "intent_banks";
    private final SharedPreferences prefs;

    IntentBankStore(Context context) {
        prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    List<String> get(int category) {
        String raw = prefs.getString("bank_" + category, null);
        if (raw == null) return defaults(category);
        List<String> values = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, "").trim();
                if (!value.isEmpty()) values.add(value);
            }
        } catch (JSONException ignored) {
            return defaults(category);
        }
        return values;
    }

    void put(int category, List<String> values) {
        JSONArray array = new JSONArray();
        for (String value : values) array.put(value);
        prefs.edit().putString("bank_" + category, array.toString()).apply();
    }

    String getLastIntent() {
        return prefs.getString("last_intent", "");
    }

    void setLastIntent(int category, String value) {
        prefs.edit().putString("last_intent", TITLES[category] + ": " + value).apply();
    }

    private List<String> defaults(int category) {
        switch (category) {
            case SEARCH: return new ArrayList<>(Arrays.asList(
                "Montessori grammar", "Bài viết đã đọc gần đây", "Một chủ đề muốn tìm hiểu"));
            case NOTIFICATIONS: return new ArrayList<>(Arrays.asList(
                "Kiểm tra bình luận mới", "Xem ai đã nhắc đến tôi", "Trả lời tương tác cần thiết"));
            case PERSON: return new ArrayList<>(Arrays.asList(
                "Một người bạn", "Một người muốn liên hệ", "Một tác giả muốn xem bài"));
            default: return new ArrayList<>(Arrays.asList(
                "Xem cập nhật trong 5 phút", "Xem bài của bạn bè", "Nghỉ giải lao có chủ đích"));
        }
    }
}
