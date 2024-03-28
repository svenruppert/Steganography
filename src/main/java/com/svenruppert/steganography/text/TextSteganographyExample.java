package com.svenruppert.steganography.text;
import java.util.Scanner;

public class TextSteganographyExample {

  public static void main(String[] args) {
    String binaryData = "101"; // Binary data to hide
    String sourceText = "Hello\nWorld\nThis is a test\n"; // Source text to hide data in
    String hiddenText = hideData(sourceText, binaryData);

    System.out.println("Text without hidden data:");
    System.out.println(sourceText);
    System.out.println("Text with hidden data:");
    System.out.println(hiddenText);

    //extract the hidden message
    String extractedData = extractData(hiddenText);
    System.out.println("extractedData = " + extractedData);
  }

  public static String hideData(String sourceText, String binaryData) {
    Scanner scanner = new Scanner(sourceText);
    StringBuilder hiddenTextBuilder = new StringBuilder();
    int index = 0;
    while (scanner.hasNextLine() && index < binaryData.length()) {
      String line = scanner.nextLine();
      // Append a space for '0', or a tab for '1'
      char appendChar = binaryData.charAt(index) == '0' ? ' ' : '\t';
      hiddenTextBuilder.append(line).append(appendChar).append("\n");
      index++;
    }
    // If there's more of the source text, add it as is
    while (scanner.hasNextLine()) {
      hiddenTextBuilder.append(scanner.nextLine()).append("\n");
    }
    scanner.close();
    return hiddenTextBuilder.toString();
  }

  public static String extractData(String hiddenText) {
    Scanner scanner = new Scanner(hiddenText);
    StringBuilder binaryDataBuilder = new StringBuilder();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      if (line.endsWith("\t")) {
        // If line ends with a tab, append '1'
        binaryDataBuilder.append('1');
      } else if (line.endsWith(" ")) {
        // If line ends with a space, append '0'
        binaryDataBuilder.append('0');
      }
    }
    scanner.close();
    return binaryDataBuilder.toString();
  }
}
