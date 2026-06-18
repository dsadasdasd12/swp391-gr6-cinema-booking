package util;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tải poster phim từ internet và lưu vào thư mục web/assets/uploads/movies/{id}/.
 */
public final class MoviePosterService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** Poster TMDB / CDN công khai theo id phim mẫu */
    private static final Map<Integer, String> POSTER_SOURCES = new LinkedHashMap<>();

    static {
        POSTER_SOURCES.put(1, "https://image.tmdb.org/t/p/w500/8s9rDqgqgQk7XgFKW8PCeLHxKzL.jpg"); // Lật Mặt 7
        POSTER_SOURCES.put(2, "https://image.tmdb.org/t/p/w500/w39yx5N7y5P99hBDqmJ9Nqi2nzP.jpg"); // Mai
        POSTER_SOURCES.put(3, "https://image.tmdb.org/t/p/w500/1pdfLVWWdIOh0J1ioh0jD6W2Y9Z.jpg"); // Dune 2
        POSTER_SOURCES.put(4, "https://image.tmdb.org/t/p/w500/xvqDOS8FFetD5lSsV9cYQ955i6.jpg"); // Inside Out 2
    }

    private MoviePosterService() {}

    public static void ensurePosters(Connection conn, String webRootPath) {
        if (conn == null || webRootPath == null) {
            return;
        }
        System.out.println("==> Đang tải poster phim...");
        for (Map.Entry<Integer, String> e : POSTER_SOURCES.entrySet()) {
            int movieId = e.getKey();
            try {
                String relative = downloadPoster(movieId, e.getValue(), webRootPath);
                if (relative != null) {
                    updatePosterUrl(conn, movieId, relative);
                }
            } catch (Exception ex) {
                System.err.println("==> Không tải được poster phim " + movieId + ": " + ex.getMessage());
            }
        }
    }

    private static String downloadPoster(int movieId, String sourceUrl, String webRoot) throws Exception {
        Path dir = Path.of(webRoot, "assets", "uploads", "movies", String.valueOf(movieId));
        Files.createDirectories(dir);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sourceUrl))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "RapViet-Cinema/1.0")
                .GET()
                .build();

        HttpResponse<InputStream> response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }

        String ext = sourceUrl.contains(".png") ? "png" : "jpg";
        Path file = dir.resolve("poster." + ext);
        try (InputStream in = response.body()) {
            Files.copy(in, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        return "assets/uploads/movies/" + movieId + "/poster." + ext;
    }

    private static void updatePosterUrl(Connection conn, int movieId, String relativeUrl) throws SQLException {
        String sql = "UPDATE dbo.MOVIES SET poster_url = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, relativeUrl);
            ps.setInt(2, movieId);
            ps.executeUpdate();
        }
    }

}
