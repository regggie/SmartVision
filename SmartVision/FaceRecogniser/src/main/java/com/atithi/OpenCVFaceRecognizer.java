package com.atithi;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.IntBuffer;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_contrib.*;



public class OpenCVFaceRecognizer {
	
  public static void main(String[] args) throws IOException {
	 
	//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	String trainingDir = "E:/MS/TRANING_SET";
	System.out.println(System.getProperty("java.library.path"));
	File image = new File("E:/MS/3-Omkar.png");
	Mat testImage = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
	 
    File root = new File(trainingDir);
    FilenameFilter pngFilter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".png");
      }
    };

    File[] imageFiles = root.listFiles(pngFilter);

    MatVector images = new MatVector(imageFiles.length);
    int cnt=0;
    
    Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
    IntBuffer labelsBuf = labels.getIntBuffer();
    
    for (File imageFile : imageFiles) {
      // Get image and label: 
      Mat test =  imread(imageFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
      int label = Integer.parseInt(imageFile.getName().split("\\-")[0]);
     
      images.put(cnt, test);
      System.out.println("Lebel is"+label);
      
      labelsBuf.put(cnt, label);
      cnt++;
    }

    
    org.bytedeco.javacpp.opencv_contrib.FaceRecognizer faceRecognizer
    =createFisherFaceRecognizer();
       
    faceRecognizer.train(images, labels);
    int predictedLabel = faceRecognizer.predict(testImage);
    System.out.println("Predicted label: " + predictedLabel);
  }
}