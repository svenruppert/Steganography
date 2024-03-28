package com.svenruppert.steganography.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageSteganography {

  // Method to encode a message into an image
  public static BufferedImage encodeMessage(BufferedImage image, String message) {
    int messageLength = message.length();
    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();
    int[] imagePixels = new int[imageWidth * imageHeight];
    image.getRGB(0, 0, imageWidth, imageHeight, imagePixels, 0, imageWidth);

    // Convert the message into a binary string
    StringBuilder binaryMessage = new StringBuilder();
    for (char character : message.toCharArray()) {
      binaryMessage.append(String.format("%8s", Integer.toBinaryString(character)).replaceAll(" ", "0"));
    }

    // Encode the message length at the beginning
    String messageLengthBinary = String.format("%32s", Integer.toBinaryString(messageLength)).replace(' ', '0');
    binaryMessage.insert(0, messageLengthBinary);

    // Encode the binary message into the image
    for (int i = 0; i < binaryMessage.length(); i++) {
      int pixel = imagePixels[i];
      int blue = pixel & 0xFF;
      int green = (pixel >> 8) & 0xFF;
      int red = (pixel >> 16) & 0xFF;
      int alpha = (pixel >> 24) & 0xFF;

      // Modify the LSB of the blue part of the pixel to match the current bit of the message
      blue = (blue & 0xFE) | (binaryMessage.charAt(i) - '0');
      int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;

      imagePixels[i] = newPixel;
    }

    BufferedImage newImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
    newImage.setRGB(0, 0, imageWidth, imageHeight, imagePixels, 0, imageWidth);
    return newImage;
  }
  // Method to decode the hidden message from an image
  public static String decodeMessage(BufferedImage image) {
    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();
    int[] imagePixels = new int[imageWidth * imageHeight];
    image.getRGB(0, 0, imageWidth, imageHeight, imagePixels, 0, imageWidth);

    // Extract the length of the message
    StringBuilder messageLengthBinary = new StringBuilder();
    for (int i = 0; i < 32; i++) {
      int pixel = imagePixels[i];
      int blue = pixel & 0xFF;
      messageLengthBinary.append(blue & 1);
    }
    int messageLength = Integer.parseInt(messageLengthBinary.toString(), 2);

    // Extract the binary message from the image
    StringBuilder binaryMessage = new StringBuilder();
    for (int i = 32; i < 32 + messageLength * 8; i++) {
      int pixel = imagePixels[i];
      int blue = pixel & 0xFF;
      binaryMessage.append(blue & 1);
    }

    // Convert the binary message to string
    StringBuilder message = new StringBuilder();
    for (int i = 0; i < binaryMessage.length(); i += 8) {
      String byteString = binaryMessage.substring(i, i + 8);
      int charCode = Integer.parseInt(byteString, 2);
      message.append((char) charCode);
    }

    return message.toString();
  }
  public static void main(String[] args) throws IOException {
    File originalImageFile = new File("_data/_DSC1259.png"); // Specify the path to the input image
    BufferedImage originalImage = ImageIO.read(originalImageFile);
    String secretMessage = "Secret message goes here";

    // Encode the message into the image
    BufferedImage encodedImage = encodeMessage(originalImage, secretMessage);

    // Save the encoded image
    File outputImageFile = new File("_data/_DSC1259_with-data.png"); // Specify the path to the output image
    ImageIO.write(encodedImage, "png", outputImageFile);
    System.out.println("The message has been encoded into the image.");


    File imageFile = new File("_data/_DSC1259_with-data.png"); // Specify the path to the encoded image
    BufferedImage image = ImageIO.read(imageFile);

    // Decode the message from the image
    String decodedMessage = decodeMessage(image);
    System.out.println("The hidden message is: " + decodedMessage);
    calculatingMaxInfoAmount("_data/_DSC1259_with-data.png");
  }
  private static void calculatingMaxInfoAmount(String pathname){
    try {
      File imageFile = new File(pathname); // Specify the path to your image
      BufferedImage image = ImageIO.read(imageFile);
      int imageWidth = image.getWidth();
      int imageHeight = image.getHeight();
      long maxBits = (long) imageWidth * imageHeight; // Maximum bits that can be stored
      long maxBytes = maxBits / 8; // Convert bits to bytes

      System.out.println("Maximum information that can be stored in bytes: " + maxBytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
