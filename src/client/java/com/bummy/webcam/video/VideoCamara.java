package com.bummy.webcam.video;

import com.bummy.webcam.WebcamMod;
import com.bummy.webcam.Video.PlayerVideo;
import com.github.sarxos.webcam.*;
import org.jetbrains.annotations.Nullable;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public class VideoCamara {
    private static Webcam webcam;

    public static void init() {
        List<Webcam> webcams = Webcam.getWebcams();
        if (webcams.isEmpty()) {
            throw new WebcamException("No webcams found on this system!");
        }
        
        WebcamMod.LOGGER.info("Found {} webcam(s)", webcams.size());
        
        for(Webcam wc : webcams) {
            try {
                WebcamMod.LOGGER.info("Attempting to open webcam: {}", wc.getName());
                wc.open();
                webcam = wc;
                WebcamMod.LOGGER.info("Successfully opened webcam: {}", wc.getName());
                return;
            } catch (WebcamException e) {
                WebcamMod.LOGGER.warn("Webcam {} not usable: {}", wc.getName(), e.getMessage());
            } catch (Exception e) {
                WebcamMod.LOGGER.warn("Unexpected error opening webcam {}: {}", wc.getName(), e.getMessage());
            }
        }

        throw new WebcamLockException("All webcams are in use or unavailable!");
    }

    public static void release() {
        if (webcam != null) {
            try {
                webcam.close();
                WebcamMod.LOGGER.info("Webcam closed successfully");
            } catch (Exception e) {
                WebcamMod.LOGGER.warn("Error closing webcam", e);
            } finally {
                webcam = null;
            }
        }
    }

    public static List<String> getWebcamList() {
        return Webcam.getWebcams().stream().map((wc) -> wc.getName()).toList();
    }

    public static void setWebcamByName(String name) {
        Webcam wc = Webcam.getWebcamByName(name);
        if (wc == null) {
            throw new WebcamException("Webcam not found: " + name);
        }
        
        // Try to open the new webcam first before closing the old one
        try {
            wc.open();
        } catch (WebcamException e) {
            WebcamMod.LOGGER.error("Failed to open webcam: " + name, e);
            throw new WebcamException("Failed to open webcam: " + name + " - " + e.getMessage());
        }
        
        // Only close the old webcam if the new one opened successfully
        if (webcam != null) {
            try {
                webcam.close();
            } catch (Exception e) {
                WebcamMod.LOGGER.warn("Failed to close previous webcam", e);
            }
        }

        webcam = wc;
        WebcamMod.LOGGER.info("Successfully switched to webcam: {}", name);
    }

    public static String getCurrentWebcam() {
        if (webcam == null) {
            return null;
        }

        return webcam.getName();
    }

    public static void get(PlayerVideo playerVideo) throws IOException {
        if (webcam == null) {
            throw new IOException("Webcam is not initialized");
        }
        
        BufferedImage image = webcam.getImage();
        if (image == null) {
            throw new IOException("Failed to capture image from webcam");
        }
        
        // Force image to square aspect ratio for consistent bubble display
        BufferedImage squareImage = forceSquareAspectRatio(image);
        
        // Compress to target size for network transmission
        BufferedImage compressedImage = aggressiveCompress(squareImage);
        
        // Update the player video with the compressed image dimensions
        playerVideo.width = compressedImage.getWidth();
        playerVideo.height = compressedImage.getHeight();

        // Final JPEG compression with balanced quality
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = writers.next();
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.4f); // Improved quality for better appearance
        writer.setOutput(ios);
        IIOImage outputImage = new IIOImage(compressedImage, null, null);

        writer.write(null, outputImage, jpegParams);
        writer.dispose();
        ios.close();
        
        byte[] frameData = baos.toByteArray();
        
        // Final size check - if still too large, compress more
        if (frameData.length > 30000) { // Slightly increased limit
            frameData = emergencyCompress(compressedImage);
        }
        
        playerVideo.setFrame(frameData);
    }

    private static BufferedImage aggressiveCompress(BufferedImage original) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // Increase resolution slightly for better quality
        int targetWidth = Math.min(originalWidth, 240);  // Increased from 160
        int targetHeight = Math.min(originalHeight, 180); // Increased from 120
        
        // Maintain aspect ratio
        double ratio = Math.min((double) targetWidth / originalWidth, (double) targetHeight / originalHeight);
        targetWidth = (int) (originalWidth * ratio);
        targetHeight = (int) (originalHeight * ratio);
        
        // Ensure minimum size
        if (targetWidth < 48) targetWidth = 48;   // Increased from 32
        if (targetHeight < 48) targetHeight = 48; // Increased from 32
        
        return resize(original, targetWidth, targetHeight);
    }

    private static byte[] emergencyCompress(BufferedImage image) throws IOException {
        // Emergency compression with moderate quality reduction
        BufferedImage emergencyImage = resize(image, 
            Math.max(32, (int)(image.getWidth() * 0.75)), 
            Math.max(32, (int)(image.getHeight() * 0.75)));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = writers.next();
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.15f); // Improved emergency quality from 0.05f to 0.15f
        writer.setOutput(ios);
        IIOImage outputImage = new IIOImage(emergencyImage, null, null);

        writer.write(null, outputImage, jpegParams);
        writer.dispose();
        ios.close();
        
        return baos.toByteArray();
    }

    private static BufferedImage forceSquareAspectRatio(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Determine the size of the square (use the smaller dimension to avoid cropping)
        int size = Math.min(width, height);
        
        // Create a square image
        BufferedImage square = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = square.createGraphics();
        
        // Enable high-quality rendering
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate position to center the image
        int x = (size - width) / 2;
        int y = (size - height) / 2;
        
        // Fill background with black
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, size, size);
        
        // Draw the original image, stretching it to fill the square
        g.drawImage(original, 0, 0, size, size, null);
        g.dispose();
        
        return square;
    }

    public static BufferedImage resize(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        
        // Enable high-quality rendering
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        return resized;
    }
}
