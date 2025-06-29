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
        for(Webcam wc : Webcam.getWebcams()) {
            try {
                wc.open();
                webcam = wc;
                WebcamMod.LOGGER.info("Using webcam: {}", wc.getName());
                return;
            } catch (WebcamException e) {
                WebcamMod.LOGGER.info("Webcam {} not usable, trying next one.", wc.getName());
            }
        }

        throw new WebcamLockException("All webcams in use!");
    }

    public static void release() {
        webcam.close();
    }

    public static List<String> getWebcamList() {
        return Webcam.getWebcams().stream().map((wc) -> wc.getName()).toList();
    }

    public static void setWebcamByName(String name) {
        Webcam wc = Webcam.getWebcamByName(name);
        if (wc == null) {
            throw new WebcamException("Webcam not found");
        }
        webcam.close();
        try {
            wc.open();
        } catch (WebcamException e) {
            throw e;
        }

        webcam = wc;
    }

    public static String getCurrentWebcam() {
        if (webcam == null) {
            return null;
        }

        return webcam.getName();
    }

    public static void get(PlayerVideo playerVideo) throws IOException {
        BufferedImage image = webcam.getImage();
        
        // Aggressive compression to stay within packet limits
        BufferedImage compressedImage = aggressiveCompress(image);
        
        // Update the player video with the compressed image dimensions
        playerVideo.width = compressedImage.getWidth();
        playerVideo.height = compressedImage.getHeight();

        // Final JPEG compression with very low quality
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = writers.next();
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.1f); // Very low quality for maximum compression
        writer.setOutput(ios);
        IIOImage outputImage = new IIOImage(compressedImage, null, null);

        writer.write(null, outputImage, jpegParams);
        writer.dispose();
        ios.close();
        
        byte[] frameData = baos.toByteArray();
        
        // Final size check - if still too large, compress more
        if (frameData.length > 25000) { // 25KB limit
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
        // Emergency compression with extremely low quality and smaller size
        BufferedImage emergencyImage = resize(image, 
            Math.max(16, image.getWidth() / 2), 
            Math.max(16, image.getHeight() / 2));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = writers.next();
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.05f); // Extremely low quality
        writer.setOutput(ios);
        IIOImage outputImage = new IIOImage(emergencyImage, null, null);

        writer.write(null, outputImage, jpegParams);
        writer.dispose();
        ios.close();
        
        return baos.toByteArray();
    }

    public static BufferedImage resize(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        return resized;
    }
}
