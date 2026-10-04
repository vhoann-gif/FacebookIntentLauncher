package vn.intentional.facebook;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/** A launch request can succeed while Facebook still lands on a different screen. */
final class FacebookOpener {
    private static final String FACEBOOK = "com.facebook.katana";
    private final Activity activity;
    private final List<Intent> routes = new ArrayList<>();
    private int lastRoute = -1;

    FacebookOpener(Activity activity) { this.activity = activity; }

    boolean hasNext() { return lastRoute + 1 < routes.size(); }

    void open(int category, String detail) {
        routes.clear();
        lastRoute = -1;
        String url = urlFor(category, detail);

        if (category == IntentBankStore.NOTIFICATIONS) {
            routes.add(view("fb://notifications", true)); // Experimental, may be ignored.
        }
        if (category == IntentBankStore.PERSON) {
            routes.add(view(url, true)); // Prefer an ordinary profile URL when supplied.
        }
        if (category == IntentBankStore.SEARCH || category == IntentBankStore.PERSON
                || category == IntentBankStore.NOTIFICATIONS) {
            String modal = new Uri.Builder().scheme("fb").authority("facewebmodal")
                .appendPath("f").appendQueryParameter("href", url).build().toString();
            routes.add(view(modal, true)); // Undocumented Facebook route.
        }
        if (category != IntentBankStore.PERSON) {
            routes.add(view(url, true)); // Ask Facebook to handle its web URL.
        }
        routes.add(view(url, false)); // Let Android choose an app or browser.
        routes.add(null);             // Open Facebook's main activity as the final route.
        tryNext();
    }

    void tryNext() {
        while (hasNext()) {
            lastRoute++;
            try {
                Intent route = routes.get(lastRoute);
                if (route == null) {
                    route = activity.getPackageManager().getLaunchIntentForPackage(FACEBOOK);
                    if (route == null) continue;
                }
                activity.startActivity(route);
                return;
            } catch (ActivityNotFoundException | SecurityException ignored) {
                // Try the next route only when Android cannot start this one.
            }
        }
        Toast.makeText(activity, "Không mở được Facebook hoặc trình duyệt.", Toast.LENGTH_LONG).show();
    }

    private Intent view(String uri, boolean facebookOnly) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (facebookOnly) intent.setPackage(FACEBOOK);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        return intent;
    }

    private String urlFor(int category, String detail) {
        switch (category) {
            case IntentBankStore.SEARCH:
                return new Uri.Builder().scheme("https").authority("www.facebook.com")
                    .appendPath("search").appendPath("top")
                    .appendQueryParameter("q", detail).build().toString();
            case IntentBankStore.PERSON:
                String link = facebookLink(detail);
                if (link != null) return link;
                return new Uri.Builder().scheme("https").authority("www.facebook.com")
                    .appendPath("search").appendPath("people")
                    .appendQueryParameter("q", detail).build().toString();
            case IntentBankStore.NOTIFICATIONS:
                return "https://www.facebook.com/notifications/";
            default:
                return "https://www.facebook.com/";
        }
    }

    /** Accept only Facebook HTTPS profile links. A person's name becomes a search instead. */
    private String facebookLink(String value) {
        Uri uri;
        try { uri = Uri.parse(value); } catch (RuntimeException ignored) { return null; }
        String host = uri.getHost();
        if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null) return null;
        host = host.toLowerCase(java.util.Locale.ROOT);
        if (!host.equals("facebook.com") && !host.endsWith(".facebook.com")) return null;
        return uri.toString();
    }
}
