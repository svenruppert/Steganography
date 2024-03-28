package com.svenruppert.steganography.binary;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.IOException;

  public class HideMessageInPDF {
    public static void main(String[] args) {
      try {
        // Load an existing PDF document
        File file = new File("_data/ReadME.pdf");
        PDDocument document = PDDocument.load(file);
        // Retrieve the document's metadata
        PDDocumentInformation info = document.getDocumentInformation();
        // Add a hidden message to the metadata
        // You could use a less obvious key than "HiddenMessage" to make it less detectable
        info.setCustomMetadataValue("HiddenMessage", "This is a secret message");
        // Save the modified document
        document.save("_data/ReadME_with-data.pdf");
        document.close();
        System.out.println("Hidden message added to the PDF metadata.");
      } catch (IOException e) {
        e.printStackTrace();
      }

      try {
        // Load the PDF document
        File file = new File("_data/ReadME_with-data.pdf");
        PDDocument document = PDDocument.load(file);
        // Access the document's metadata
        PDDocumentInformation info = document.getDocumentInformation();
        // Retrieve the hidden message from the metadata
        String hiddenMessage = info.getCustomMetadataValue("HiddenMessage");
        System.out.println("Hidden message: " + hiddenMessage);
        document.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
