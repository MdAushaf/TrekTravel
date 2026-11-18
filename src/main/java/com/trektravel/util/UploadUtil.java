package com.trektravel.util;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Handles secure file uploads with magic-byte validation and safe disk storage.
 */
public class UploadUtil {

    public static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB

    public static class UploadResult {
        private final boolean success;
        private final String fileName;
        private final String errorMessage;

        public UploadResult(boolean success, String fileName, String errorMessage) {
            this.success = success;
            this.fileName = fileName;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public String getFileName() { return fileName; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Inspects magic bytes and saves valid PDF/JPEG/PNG files to upload directory.
     */
    public static UploadResult saveIdProof(Part part) {
        if (part == null || part.getSize() <= 0) {
            return new UploadResult(false, null, "Please select an ID proof file to upload.");
        }

        if (part.getSize() > MAX_FILE_SIZE) {
            return new UploadResult(false, null, "File exceeds maximum allowed size of 2 MB.");
        }

        try (InputStream in = part.getInputStream()) {
            byte[] header = new byte[8];
            int read = in.read(header);
            if (read < 4) {
                return new UploadResult(false, null, "Uploaded file is corrupted or empty.");
            }

            String extension = detectExtension(header);
            if (extension == null) {
                return new UploadResult(false, null, "Invalid file format. Only PDF, JPG, and PNG documents are allowed.");
            }

            String safeFileName = UUID.randomUUID().toString() + extension;
            String uploadDir = DBUtil.getUploadDir();
            File targetDir = new File(uploadDir);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            Path targetPath = Paths.get(uploadDir, safeFileName);

            // Re-open input stream to write the whole file including the header bytes
            try (InputStream fullIn = part.getInputStream()) {
                Files.copy(fullIn, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return new UploadResult(true, safeFileName, null);
        } catch (Exception e) {
            return new UploadResult(false, null, "Failed to store uploaded file: " + e.getMessage());
        }
    }

    /**
     * Determines extension by checking signature magic bytes.
     */
    private static String detectExtension(byte[] h) {
        // PDF magic bytes: %PDF (0x25 0x50 0x44 0x46)
        if (h[0] == 0x25 && h[1] == 0x50 && h[2] == 0x44 && h[3] == 0x46) {
            return ".pdf";
        }
        // JPEG magic bytes: FF D8 FF
        if ((h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return ".jpg";
        }
        // PNG magic bytes: 89 50 4E 47 0D 0A 1A 0A
        if ((h[0] & 0xFF) == 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47) {
            return ".png";
        }
        return null;
    }

    /**
     * Returns standard MIME content type for file serving.
     */
    public static String getMimeType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}
