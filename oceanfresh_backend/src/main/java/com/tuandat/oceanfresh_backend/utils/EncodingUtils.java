package com.tuandat.oceanfresh_backend.utils;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodingUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(EncodingUtils.class);
    
    /**
     * Fix encoding từ ISO-8859-1 sang UTF-8
     * Dùng cho dữ liệu từ multipart/form-data
     */
    public static String fixEncoding(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        try {
            // Kiểm tra xem có phải là encoding bị lỗi không
            if (containsInvalidChars(input)) {
                byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
                return new String(bytes, StandardCharsets.UTF_8);
            }
            return input;
        } catch (Exception e) {
            logger.warn("Không thể fix encoding cho: {}", input, e);
            return input;
        }
    }
    
    /**
     * Kiểm tra xem string có chứa ký tự bị lỗi encoding không
     */
    private static boolean containsInvalidChars(String input) {
        // Kiểm tra một số ký tự tiếng Việt bị lỗi encoding phổ biến
        return input.contains("?") || 
               input.contains("â€") || 
               input.contains("Ã¡") || 
               input.contains("Ã ") ||
               input.contains("Ã©") ||
               input.contains("Ã¢") ||
               input.contains("Ã´") ||
               input.contains("Ã¹") ||
               input.contains("Ã­") ||
               input.contains("Ã³") ||
               input.contains("Ã»") ||
               input.contains("Ã½") ||
               input.contains("Ä") ||
               input.contains("Ã¯") ||
               input.contains("Ã·") ||
               input.contains("Ã¸") ||
               input.contains("Ã¶") ||
               input.contains("Ã±") ||
               input.contains("Ã¤") ||
               input.contains("Ã¥") ||
               input.contains("Ã¦") ||
               input.contains("Ã§") ||
               input.contains("Ã¨") ||
               input.contains("Ãª") ||
               input.contains("Ã«") ||
               input.contains("Ã¬") ||
               input.contains("Ã®") ||
               input.contains("Ã¯") ||
               input.contains("Ã°") ||
               input.contains("Ã²") ||
               input.contains("Ã³") ||
               input.contains("Ã µ") ||
               input.contains("Ã¶") ||
               input.contains("Ã·") ||
               input.contains("Ã¸") ||
               input.contains("Ã¹") ||
               input.contains("Ãº") ||
               input.contains("Ã¼") ||
               input.contains("Ã½") ||
               input.contains("Ã¾") ||
               input.contains("Ã¿");
    }
    
    /**
     * Encode string sang UTF-8
     */
    public static String toUTF8(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            return new String(input.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("Không thể encode sang UTF-8: {}", input, e);
            return input;
        }
    }
    
    /**
     * Kiểm tra xem string có phải UTF-8 hợp lệ không
     */
    public static boolean isValidUTF8(String input) {
        if (input == null) {
            return true;
        }
        
        try {
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            return input.equals(decoded);
        } catch (Exception e) {
            return false;
        }
    }
}
