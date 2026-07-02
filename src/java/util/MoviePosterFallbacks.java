package util;

import java.text.Normalizer;

public final class MoviePosterFallbacks {

    private MoviePosterFallbacks() {
    }

    public static String resolve(String title) {
        String key = normalize(title);
        if (key.contains("lat mat")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/d/d0/John_Wick_-_Chapter_4_promotional_poster.jpg/250px-John_Wick_-_Chapter_4_promotional_poster.jpg";
        }
        if (key.contains("mai")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/3/36/Mai_2024_poster.jpg/250px-Mai_2024_poster.jpg";
        }
        if (key.contains("dune")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/5/52/Dune_Part_Two_poster.jpeg/250px-Dune_Part_Two_poster.jpeg";
        }
        if (key.contains("inside out")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/f/f7/Inside_Out_2_poster.jpg/250px-Inside_Out_2_poster.jpg";
        }
        if (key.contains("vu tru song song")) {
            return "https://upload.wikimedia.org/wikipedia/en/1/1e/Everything_Everywhere_All_at_Once.jpg";
        }
        if (key.contains("lan ranh sinh tu")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/0/02/Extraction_2_poster.jpg/250px-Extraction_2_poster.jpg";
        }
        if (key.contains("ngoi nha cam lang")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/e/e7/A_Quiet_Place_Day_One_%282024%29_poster.jpg/250px-A_Quiet_Place_Day_One_%282024%29_poster.jpg";
        }
        if (key.contains("tieng cuoi mua ha")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/c/c4/Anyone_but_You_%282023%29_official_poster.webp/250px-Anyone_but_You_%282023%29_official_poster.webp.png";
        }
        if (key.contains("hanh tinh bang gia")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/4/4a/Oppenheimer_%28film%29.jpg/250px-Oppenheimer_%28film%29.jpg";
        }
        if (key.contains("trai tim mua dong")) {
            return "https://upload.wikimedia.org/wikipedia/en/6/69/The_Big_Sick.jpg";
        }
        if (key.contains("the gioi ky dieu")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/4/4a/Oppenheimer_%28film%29.jpg/250px-Oppenheimer_%28film%29.jpg";
        }
        if (key.contains("bong dem tro lai")) {
            return "https://upload.wikimedia.org/wikipedia/en/thumb/d/d0/John_Wick_-_Chapter_4_promotional_poster.jpg/250px-John_Wick_-_Chapter_4_promotional_poster.jpg";
        }
        return "https://upload.wikimedia.org/wikipedia/en/thumb/5/52/Dune_Part_Two_poster.jpeg/250px-Dune_Part_Two_poster.jpeg";
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase();
    }
}
