package util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Sửa chuỗi tiếng Việt bị lỗi encoding (UTF-8 đọc nhầm Latin-1 / Windows-1252).
 */
public final class EncodingUtil {

    private static final Charset WINDOWS_1252 = Charset.forName("Windows-1252");

    private EncodingUtil() {}

    public static String fix(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (!looksCorrupted(value)) {
            return value;
        }

        String best = value;
        int bestScore = vietnameseScore(value);

        String iso = reencode(value, StandardCharsets.ISO_8859_1);
        if (iso != null) {
            int s = vietnameseScore(iso);
            if (s > bestScore) {
                best = iso;
                bestScore = s;
            }
        }

        String win = reencode(value, WINDOWS_1252);
        if (win != null) {
            int s = vietnameseScore(win);
            if (s > bestScore) {
                best = win;
            }
        }

        return best;
    }

    public static String getString(ResultSet rs, String column) throws SQLException {
        return fix(rs.getString(column));
    }

    public static boolean looksCorrupted(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return s.contains("Ã")
                || s.contains("áº")
                || s.contains("Æ°")
                || s.contains("Ä‘")
                || s.contains("á»")
                || s.contains("TrÃ")
                || s.contains("â€");
    }

    private static String reencode(String value, Charset from) {
        try {
            String out = new String(value.getBytes(from), StandardCharsets.UTF_8);
            if (!out.isEmpty() && !out.equals(value)) {
                return out;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static int vietnameseScore(String s) {
        int score = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ("àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđÀÁẢÃẠĂẰẮẲẴẶÂẦẤẨẪẬÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ".indexOf(c) >= 0) {
                score += 3;
            } else if (Character.isLetter(c)) {
                score += 1;
            }
            if (looksCorrupted(String.valueOf(c))) {
                score -= 4;
            }
        }
        return score;
    }
}
