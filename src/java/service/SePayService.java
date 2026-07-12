package service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SePayService {

    // BIDV
    private static final String BANK_CODE = "BIDV";

    // Số tài khoản BIDV
    private static final String ACCOUNT_NO = "96247GGDYW";

    // Chủ tài khoản
    private static final String ACCOUNT_NAME = "TRAN THE TRUONG";

    public String buildTransferContent(int bookingId) {
        return "RVS" + bookingId;
    }

    public String buildQrUrl(int bookingId, double amount) {

        String content = buildTransferContent(bookingId);

        return "https://img.vietqr.io/image/"
                + BANK_CODE
                + "-"
                + ACCOUNT_NO
                + "-compact2.png"
                + "?amount=" + (long) amount
                + "&addInfo=" + encode(content)
                + "&accountName=" + encode(ACCOUNT_NAME);
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}