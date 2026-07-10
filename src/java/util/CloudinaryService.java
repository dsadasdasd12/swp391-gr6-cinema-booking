package util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** Uploads movie posters to Cloudinary using server environment variables. */
public final class CloudinaryService {

    private static final String CLOUD_NAME = "CLOUDINARY_CLOUD_NAME";
    private static final String API_KEY = "CLOUDINARY_API_KEY";
    private static final String API_SECRET = "CLOUDINARY_API_SECRET";

    private CloudinaryService() {
    }

    /** Uploads an image and returns its HTTPS delivery URL. */
    public static String uploadMoviePoster(Part part) throws IOException {
        if (part == null || part.getSize() == 0) {
            throw new IOException("Khong tim thay file poster.");
        }

        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", requiredEnvironment(CLOUD_NAME),
                "api_key", requiredEnvironment(API_KEY),
                "api_secret", requiredEnvironment(API_SECRET),
                "secure", true
        ));

        try (InputStream input = part.getInputStream()) {
            Map<?, ?> result = cloudinary.uploader().upload(input, ObjectUtils.asMap(
                    "folder", "rapviet/movies",
                    "resource_type", "image",
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false
            ));
            Object secureUrl = result.get("secure_url");
            if (!(secureUrl instanceof String url) || url.isBlank()) {
                throw new IOException("Cloudinary khong tra ve secure_url.");
            }
            return url;
        }
    }

    private static String requiredEnvironment(String name) throws IOException {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IOException("Chua cau hinh bien moi truong " + name + ".");
        }
        return value;
    }
}
