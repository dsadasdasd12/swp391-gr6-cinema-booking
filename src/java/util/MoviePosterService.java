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
import util.MoviePosterFallbacks;

/**
 * Downloads poster images to local web assets and rewrites poster_url
 * so movie pages can render without depending on external hotlinks.
 */
public final class MoviePosterService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private MoviePosterService() {
    }

    public static void ensurePosters(Connection conn, String webRootPath) {
        if (conn == null || webRootPath == null || webRootPath.isBlank()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, title FROM dbo.MOVIES "
                        + "WHERE NULLIF(LTRIM(RTRIM(poster_url)), '') IS NULL "
                        + "OR poster_url LIKE 'http%' "
                        + "OR poster_url LIKE 'https%'"
        );
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int movieId = rs.getInt("id");
                String sourceUrl = MoviePosterFallbacks.resolve(rs.getString("title"));
                try {
                    String relative = downloadPoster(movieId, sourceUrl, webRootPath);
                    updatePosterUrl(conn, movieId, relative);
                } catch (Exception ex) {
                    System.err.println("==> Không tải được poster phim " + movieId + ": " + ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            System.err.println("==> Không đọc được danh sách phim để tải poster: " + ex.getMessage());
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

        String ext = sourceUrl.contains(".png") ? "png"
                : sourceUrl.contains(".webp") ? "webp"
                : "jpg";
        Path file = dir.resolve("poster." + ext);
        try (InputStream in = response.body()) {
            Files.copy(in, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        return "assets/uploads/movies/" + movieId + "/poster." + ext;
    }

    private static void updatePosterUrl(Connection conn, int movieId, String relativeUrl) throws SQLException {
        String sql = "UPDATE dbo.MOVIES SET poster_url = ?, last_update = GETDATE() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, relativeUrl);
            ps.setInt(2, movieId);
            ps.executeUpdate();
        }
    }
}
