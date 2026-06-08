package io.github.anitect.rerollager.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Checks Modrinth on startup for a newer release and logs a notice to the console. Best effort:
 * any network or parsing problem (including the project not being published yet) is ignored
 * quietly, so it never affects startup.
 */
@NullMarked
public final class UpdateChecker {

    private static final String MODRINTH_SLUG = "rerollager";
    private static final String VERSIONS_URL =
            "https://api.modrinth.com/v2/project/" + MODRINTH_SLUG + "/version";

    private final Plugin plugin;
    private final String currentVersion;

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getPluginMeta().getVersion();
    }

    /** Run the check off the main thread. */
    public void checkAsync() {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> check());
    }

    private void check() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(VERSIONS_URL))
                    .header("User-Agent", "Anitect/Rerollager/" + currentVersion + " (anirudh@anitect.com)")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return; // not published yet, or a transient error
            }

            String latest = latestReleaseVersion(JsonParser.parseString(response.body()).getAsJsonArray());
            if (latest != null && isNewer(latest, currentVersion)) {
                plugin.getSLF4JLogger().info(
                        "A new version of Rerollager is available: {} (you have {}). Get it at https://modrinth.com/plugin/{}",
                        latest, currentVersion, MODRINTH_SLUG);
            }
        } catch (Exception e) {
            plugin.getSLF4JLogger().debug("Update check skipped: {}", e.toString());
        }
    }

    private static @Nullable String latestReleaseVersion(JsonArray versions) {
        String best = null;
        for (var element : versions) {
            JsonObject obj = element.getAsJsonObject();
            String type = obj.has("version_type") ? obj.get("version_type").getAsString() : "release";
            if (!"release".equals(type)) {
                continue; // ignore alpha/beta pre-releases
            }
            String number = obj.get("version_number").getAsString();
            if (best == null || isNewer(number, best)) {
                best = number;
            }
        }
        return best;
    }

    /** True if {@code a} is a strictly greater dotted-number version than {@code b}. */
    private static boolean isNewer(String a, String b) {
        String[] pa = stripSuffix(a).split("\\.");
        String[] pb = stripSuffix(b).split("\\.");
        int length = Math.max(pa.length, pb.length);
        for (int i = 0; i < length; i++) {
            int ai = i < pa.length ? toInt(pa[i]) : 0;
            int bi = i < pb.length ? toInt(pb[i]) : 0;
            if (ai != bi) {
                return ai > bi;
            }
        }
        return false;
    }

    private static String stripSuffix(String version) {
        int dash = version.indexOf('-');
        return dash >= 0 ? version.substring(0, dash) : version;
    }

    private static int toInt(String s) {
        String digits = s.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }
}
