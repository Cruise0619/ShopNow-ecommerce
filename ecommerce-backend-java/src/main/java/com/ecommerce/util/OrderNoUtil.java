package com.ecommerce.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class OrderNoUtil {

    private static final Random RANDOM = new Random();

    public static String generateOrderNo() {
        String dateStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return "EC" + dateStr + sb.toString();
    }
}
