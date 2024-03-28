package com.svenruppert.steganography.video;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VideoSteganography {
  //Should be extracted from the orig video
  public static final int FPS = 25;
//  public static final String INPUT_FILE = "_data/_DSC_0469-H264-HD-25fps.mp4";
  public static final String INPUT_FILE = "_data/generated.mp4";
  public static final String OUTPUT_FILE = "_data/output.mp4";
  public static final String MESSAGE = "Hello Message";

  public static void main(String[] args) throws Exception {
    encodingProcess();
    decodeProcess();
  }

  private static void decodeProcess() throws IOException, JCodecException {
    FileChannelWrapper fileChannelWrapperIN = NIOUtils.readableChannel(new File(OUTPUT_FILE));
    FrameGrab grab = FrameGrab.createFrameGrab(fileChannelWrapperIN);
    Picture picture;
    while (null != (picture = grab.getNativeFrame())) {
      // Here, convert the Picture to BufferedImage (RGB)
      BufferedImage frame = AWTUtil.toBufferedImage(picture);
      // Extract the hidden message from the image
      String hiddenMessage = extractMessageFromBlue(frame);
      System.out.println("Hidden message: " + hiddenMessage);
    }
    // Finalize video encoding and clean up
    NIOUtils.closeQuietly(fileChannelWrapperIN);
  }

  private static void encodingProcess() throws IOException, JCodecException {
    FileChannelWrapper fileChannelWrapperIN = NIOUtils.readableChannel(new File(INPUT_FILE));
    File outputFile = new File(OUTPUT_FILE);
    SequenceEncoder encoder = SequenceEncoder.createSequenceEncoder(outputFile, FPS);
    FrameGrab grab = FrameGrab.createFrameGrab(fileChannelWrapperIN);
    Picture picture;
    // to improve to process
    // 0. detect the FPS from the input video
    // 1. calculate the max amount of possible bytes that can be encoded
    // 2. calculate the max amount of bytes that can be stored in each frame
    // 3. split the message into chunks to fit into a frame
    // 4. define a sequence to mark the end of the message

    while (null != (picture = grab.getNativeFrame())) {
      // Here, convert the Picture to BufferedImage
      BufferedImage frame = AWTUtil.toBufferedImage(picture);
      // Modify the frame to encode part of the message
      BufferedImage encodedMessageOnBlue = encodeMessageOnBlue(frame, MESSAGE);
      // Convert BufferedImage to Picture (required by JCodec)
      Picture pic = AWTUtil.fromBufferedImage(encodedMessageOnBlue, ColorSpace.RGB);
      // Encode the modified frame back into the video
      encoder.encodeNativeFrame(pic);
    }
    // Finalize video encoding and clean up
    NIOUtils.closeQuietly(fileChannelWrapperIN);
    // Finalize and close the encoder (important!)
    encoder.finish();
  }

  public static String toBinaryString(String message) {
    StringBuilder binary = new StringBuilder();
    for (char character : message.toCharArray()) {
      binary.append(String
          .format("%8s", Integer.toBinaryString(character))
          .replace(' ', '0'));
    }
    return binary.toString();
  }
  public static String binaryStringToText(String binary) {
    StringBuilder text = new StringBuilder();
    for (int i = 0; i < binary.length(); i += 8) {
      String byteString = binary.substring(i, i + 8);
      char character = (char) Integer.parseInt(byteString, 2);
      text.append(character);
    }
    return text.toString();
  }

  public static BufferedImage encodeMessageOnBlue(BufferedImage image, String message) {
    int messageIndex = 0;
    String binaryMessage = toBinaryString(message);
    int messageLength = binaryMessage.length();


    for (int y = 0; (y < image.getHeight()) && (messageIndex < messageLength); y++) {
      for (int x = 0; (x < image.getWidth()) && (messageIndex < messageLength); x++) {
        int pixel = image.getRGB(x, y);
        int alpha = (pixel >> 24) & 0xFF;
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;

        // Modify the LSB of the blue component
        blue = (blue & 0xFE) | (binaryMessage.charAt(messageIndex) - '0');
        messageIndex++;

        // Reconstruct the pixel and set it back
        int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
        image.setRGB(x, y, newPixel);
      }
    }
    return image;
  }

  public static String extractMessageFromBlue(BufferedImage image) {
    StringBuilder binaryMessage = new StringBuilder();
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        int pixel = image.getRGB(x, y);
        int alpha = (pixel >> 24) & 0xFF;
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        // Extract the LSB of the blue component
        int lsb = blue & 1;
        binaryMessage.append(lsb);
      }
    }
    // Convert the binary string to text
    String binaryMessageString = binaryMessage.toString();
    return binaryStringToText(binaryMessageString);
  }
}

