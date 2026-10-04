package vn.intentional.facebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(37, 99, 235);
    private static final int INK = Color.rgb(25, 35, 52);
    private static final int MUTED = Color.rgb(91, 103, 121);
    private static final int WHITE = Color.WHITE;
    private static final int BORDER = Color.rgb(222, 227, 234);
    private IntentBankStore store;
    private FacebookOpener opener;
    private int category = -1;
    private LinearLayout root, bankList, categoryList;
    private EditText detail;
    private TextView prompt, recent;
    private Button openButton, nextButton;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store = new IntentBankStore(this);
        opener = new FacebookOpener(this);
        buildScreen();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(32));
        root.setBackgroundColor(Color.rgb(247, 248, 250));
        scroll.addView(root);
        setContentView(scroll);

        TextView title = text("Bạn muốn mở Facebook làm gì?", 27, INK, true);
        root.addView(title);
        TextView sub = text("Chọn một mục đích trước khi vào.", 15, MUTED, false);
        topMargin(sub, 9);
        root.addView(sub);

        categoryList = column();
        topMargin(categoryList, 24);
        root.addView(categoryList);
        drawCategories();

        prompt = text("Chọn một lựa chọn ở trên để tiếp tục.", 16, MUTED, false);
        topMargin(prompt, 23);
        root.addView(prompt);

        bankList = column();
        topMargin(bankList, 12);
        root.addView(bankList);

        detail = new EditText(this);
        detail.setTextSize(16);
        detail.setSingleLine(false);
        detail.setMinLines(2);
        detail.setMaxLines(4);
        detail.setPadding(dp(14), dp(10), dp(14), dp(10));
        detail.setBackground(round(WHITE, BORDER, 12));
        detail.setVisibility(View.GONE);
        root.addView(detail);
        topMargin(detail, 14);

        Button manage = button("Sửa kho gợi ý", WHITE, BLUE);
        topMargin(manage, 10);
        manage.setVisibility(View.GONE);
        root.addView(manage);
        manage.setOnClickListener(v -> manageBank());

        openButton = button("Mở Facebook →", BLUE, WHITE);
        topMargin(openButton, 26);
        openButton.setVisibility(View.GONE);
        root.addView(openButton);
        openButton.setOnClickListener(v -> launch());

        nextButton = button("Facebook mở sai chỗ? Thử cách khác", WHITE, BLUE);
        topMargin(nextButton, 10);
        nextButton.setVisibility(View.GONE);
        root.addView(nextButton);
        nextButton.setOnClickListener(v -> {
            opener.tryNext();
            if (!opener.hasNext()) nextButton.setVisibility(View.GONE);
        });

        TextView note = text("Liên kết bên trong Facebook có thể thay đổi. Nếu mở sai nơi, quay lại đây và thử cách khác.", 13, MUTED, false);
        topMargin(note, 18);
        root.addView(note);

        recent = text("", 13, MUTED, false);
        topMargin(recent, 18);
        root.addView(recent);
        refreshRecent();

        this.manageButton = manage;
    }

    private Button manageButton;

    private void drawCategories() {
        categoryList.removeAllViews();
        for (int i = 0; i < IntentBankStore.TITLES.length; i++) {
            final int index = i;
            Button item = button(IntentBankStore.TITLES[i], i == category ? BLUE : WHITE,
                i == category ? WHITE : INK);
            item.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            item.setPadding(dp(18), 0, dp(12), 0);
            item.setBackground(round(i == category ? BLUE : WHITE, i == category ? BLUE : BORDER, 13));
            item.setMinHeight(dp(56));
            categoryList.addView(item, new LinearLayout.LayoutParams(-1, dp(56)));
            if (i != 0) topMarginFixed(item, 9, dp(56));
            item.setOnClickListener(v -> choose(index));
        }
    }

    private void choose(int index) {
        category = index;
        detail.setText("");
        detail.setHint(IntentBankStore.HINTS[index]);
        detail.setVisibility(View.VISIBLE);
        manageButton.setVisibility(View.VISIBLE);
        openButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        prompt.setText("Chọn từ kho gợi ý hoặc viết ý định của bạn:");
        drawCategories();
        drawBank();
    }

    private void drawBank() {
        bankList.removeAllViews();
        if (category < 0) return;
        List<String> values = store.get(category);
        for (String value : values) {
            Button chip = button(value, WHITE, BLUE);
            chip.setAllCaps(false);
            chip.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            chip.setPadding(dp(13), dp(5), dp(13), dp(5));
            chip.setBackground(round(WHITE, BORDER, 10));
            bankList.addView(chip, new LinearLayout.LayoutParams(-1, -2));
            if (bankList.getChildCount() > 1) topMargin(chip, 7);
            chip.setOnClickListener(v -> detail.setText(value));
        }
        if (values.isEmpty()) bankList.addView(text("Kho đang trống. Bạn có thể thêm gợi ý.", 14, MUTED, false));
    }

    private void manageBank() {
        if (category < 0) return;
        List<String> values = store.get(category);
        String[] rows = new String[values.size() + 1];
        rows[0] = "+ Thêm gợi ý";
        for (int i = 0; i < values.size(); i++) rows[i + 1] = values.get(i);
        new AlertDialog.Builder(this)
            .setTitle("Kho: " + IntentBankStore.TITLES[category])
            .setItems(rows, (dialog, which) -> {
                if (which == 0) editBankItem(-1);
                else editBankItem(which - 1);
            })
            .setNegativeButton("Đóng", null).show();
    }

    private void editBankItem(int position) {
        List<String> values = new ArrayList<>(store.get(category));
        EditText input = new EditText(this);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setPadding(dp(20), dp(10), dp(20), dp(10));
        input.setHint("Gợi ý hoặc ý định");
        if (position >= 0) input.setText(values.get(position));
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle(position >= 0 ? "Sửa gợi ý" : "Thêm gợi ý")
            .setView(input)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu", null);
        if (position >= 0) builder.setNeutralButton("Xóa", (d, w) -> {
            values.remove(position);
            store.put(category, values);
            drawBank();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) { input.setError("Nhập nội dung trước khi lưu"); return; }
            if (position >= 0) values.set(position, value); else values.add(value);
            store.put(category, values);
            drawBank();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void launch() {
        String value = detail.getText().toString().trim();
        if (TextUtils.isEmpty(value)) {
            detail.setError("Chọn gợi ý hoặc nhập ý định");
            detail.requestFocus();
            return;
        }
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(detail.getWindowToken(), 0);
        store.setLastIntent(category, value);
        refreshRecent();
        opener.open(category, value);
        nextButton.setVisibility(opener.hasNext() ? View.VISIBLE : View.GONE);
    }

    private void refreshRecent() {
        String value = store.getLastIntent();
        recent.setText(value.isEmpty() ? "" : "Lần gần nhất: " + value);
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private Button button(String value, int background, int foreground) {
        Button view = new Button(this);
        view.setText(value);
        view.setTextSize(15);
        view.setAllCaps(false);
        view.setTextColor(foreground);
        view.setBackground(round(background, background == WHITE ? BORDER : background, 12));
        view.setMinHeight(dp(50));
        return view;
    }

    private GradientDrawable round(int fill, int stroke, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private void topMargin(View view, int margin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(margin);
        view.setLayoutParams(params);
    }

    private void topMarginFixed(View view, int margin, int height) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, height);
        params.topMargin = dp(margin);
        view.setLayoutParams(params);
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
}
