package com.svenruppert.steganography.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class AudioSteganography {

  public static void main(String[] args) {
    File inputFile = new File("_data/Security - 2024.06 - What is Steganography-16bit-single-track_A01_L.wav");
    File outputFile = new File("_data/output.wav");
    String message = "Secret Message";
    hideMessage(inputFile, outputFile, message);

    File inputFileEncoded = new File("_data/output.wav"); // This is the file with the hidden message
    String extractedMessage = extractMessage(inputFileEncoded, 14); // Assuming we know the message length
    System.out.println("Extracted Message: " + extractedMessage);
  }

  public static void hideMessage(File inputFile, File outputFile, String message) {
    try {
      // Convert the message to a binary string
      byte[] messageBytes = message.getBytes();
      StringBuilder binaryMessage = new StringBuilder();
      for (byte b : messageBytes) {
        binaryMessage.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
      }
      String messageBinary = binaryMessage.toString();
      // Load the audio file
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputFile);
      AudioFormat format = audioInputStream.getFormat();
      byte[] audioBytes = audioInputStream.readAllBytes();
      // Hide the message in the LSB of the audio bytes
      int messageIndex = 0;
      for (int i = 0; i < audioBytes.length && messageIndex < messageBinary.length(); i += 2) { // Skip every other byte for 16-bit samples
        if (messageBinary.charAt(messageIndex) == '1') {
          audioBytes[i] = (byte) (audioBytes[i] | 1); // Set LSB to 1
        } else {
          audioBytes[i] = (byte) (audioBytes[i] & ~1); // Set LSB to 0
        }
        messageIndex++;
      }
      // Write the modified samples to a new file
      ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
      AudioInputStream outputAudioInputStream = new AudioInputStream(bais, format, audioBytes.length / format.getFrameSize());
      AudioSystem.write(outputAudioInputStream, AudioFileFormat.Type.WAVE, outputFile);
      System.out.println("The message has been hidden in " + outputFile.getName());
    } catch (UnsupportedAudioFileException | IOException e) {
      e.printStackTrace();
    }
  }

  public static String extractMessage(File inputFile, int messageLength) {
    try {
      // Load the audio file
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputFile);
      byte[] audioBytes = audioInputStream.readAllBytes();
      // Extract bits to reconstruct the message binary string
      StringBuilder messageBinary = new StringBuilder();
      for (int i = 0; i < messageLength * 8 * 2; i += 2) { // Assuming 16-bit samples, adjust for actual sample size
        byte b = audioBytes[i];
        int lsb = b & 1; // Extract the LSB
        messageBinary.append(lsb);
      }
      // Convert the binary string to text
      StringBuilder message = new StringBuilder();
      for (int i = 0; i < messageBinary.length(); i += 8) {
        String byteString = messageBinary.substring(i, i + 8);
        int charCode = Integer.parseInt(byteString, 2);
        message.append((char) charCode);
      }
      return message.toString();
    } catch (UnsupportedAudioFileException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}

