package junit.com.svenruppert.steganography.video;

import com.svenruppert.steganography.video.VideoSteganography;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VideoSteganographyTest {

  @Test
  void textToBinaryTest() {
    String message = "Hello World";
    String helloWorld = VideoSteganography.toBinaryString(message);
    String toText = VideoSteganography.binaryStringToText(helloWorld);
    Assertions.assertEquals(message, toText);
  }

  @Test
  void encodeDecodeTest() {
    BufferedImage image = createWhiteBufferedImage(512, 512);
    String message = "Hello World";
    BufferedImage encodeMessageOnBlue = VideoSteganography.encodeMessageOnBlue(image, message);
    String messageFromBlue = VideoSteganography.extractMessageFromBlue(encodeMessageOnBlue);
    Assertions.assertTrue(messageFromBlue.startsWith(message));
    System.out.println("messageFromBlue = " + messageFromBlue);
  }

  private static BufferedImage createWhiteBufferedImage(int width, int height) {
    // Create a BufferedImage with the specified dimensions and type
    // TYPE_INT_ARGB represents an image with an Alpha channel (transparency) and Red, Green, Blue color components
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    // Get the graphics context of the image, which allows us to draw on the image
    Graphics2D graphics = image.createGraphics();

    // Set the paint color to white
    graphics.setPaint(Color.WHITE);

    // Fill the entire image area with the white color
    graphics.fillRect(0, 0, width, height);

    // Dispose of the graphics context to release resources
    graphics.dispose();

    // Return the white BufferedImage
    return image;
  }


  @Test
  void createDemoVideoTest() {

      final int speed = 4;
      final int ballSize = 40;

    AWTSequenceEncoder enc = null;
    try {
      enc = AWTSequenceEncoder.create25Fps(new File(VideoSteganography.INPUT_FILE));
      int framesToEncode = 200;

      long totalNano = 0;
      BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
      for (int i = 0, x = 0, y = 0, incX = speed, incY = speed; i < framesToEncode; i++, x += incX, y += incY) {
        Graphics g = image.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(Color.YELLOW);
        if (x >= image.getWidth() - ballSize)
          incX = -speed;
        if (y >= image.getHeight() - ballSize)
          incY = -speed;
        if (x <= 0)
          incX = speed;
        if (y <= 0)
          incY = speed;
        g.fillOval(x, y, ballSize, ballSize);
        long start = System.nanoTime();
        enc.encodeImage(image);
        totalNano += System.nanoTime() - start;
      }
      enc.finish();

      System.out.println("FPS: " + ((1000000000L * framesToEncode) / totalNano));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
}
