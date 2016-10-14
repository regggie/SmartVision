package com.atithi;

import org.opencv.core.Core;


import static org.bytedeco.javacpp.opencv_highgui.*;
//import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import org.bytedeco.javacpp.opencv_core.Mat;
//import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.*;

import static org.bytedeco.javacpp.opencv_contrib.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createEigenFaceRecognizer;
// import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;

import org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core.MatVector;

import java.io.File;
import java.nio.IntBuffer;


public class face_main {
	public static final int val = 0;


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Workaround for bug in 0.9
		System.out.println(System.getProperty("java.library.path"));
		File image = new File("E:/MS/3-Omkar.png");
		Mat testmag = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
		
		Mat label1 = new Mat(90,1,CV_32SC1);
		
		IntBuffer labelsBuf = label1.getIntBuffer();
		
		Mat testImage = imread("E:/Sunbeam/apache-tomcat-8.0.33/webapps/ImageManager/imageStore/MyGuest.png",val);
		
		MatVector final_images = new MatVector(90);
		
		int counter = 0;
		
		for(int i=1;i<=45;i++){
			//C:/Users/Gaurav/Pictures/Amdocs_LastDays/Cropped/Cropped
			Mat temp = imread("C:/Users/Gaurav/Pictures/Amdocs_LastDays/Cropped/Cropped/O"+i+".png",val);
			//System.out.println("Image "+i+" = " +temp.arrayWidth()+ " x " + temp.arrayHeight());
			if(temp.empty()) {
				System.out.println("Cannot read image O"+i);
				System.exit(0);
			}
			else{
				labelsBuf.put(counter, 0);
				final_images.put(counter, temp);
				counter++;
			}
		}
		for(int i=1;i<=45;i++){
			Mat temp = imread("C:/Users/Gaurav/Pictures/Amdocs_LastDays/Cropped/C"+i+".png",val);
			//Imgproc.Canny(image, temp, 10, 100, 3, true);
			//System.out.println("Image "+i+" = " +temp.arrayWidth()+ " x " + temp.arrayHeight());
			if(temp.empty()){
				System.out.println("Cannot read image C "+i);
				System.exit(0);
			}
			else{
				labelsBuf.put(counter, 1);
				final_images.put(counter, temp);
				counter++;
			}
		}
		
		FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
		FaceRecognizer eigen = createEigenFaceRecognizer();
		faceRecognizer.train(final_images, label1);
		eigen.train(final_images, label1);
		int[] plabel = new int[1];
		double[] pconfidence = new double[1];
		
		faceRecognizer.predict(testImage,plabel,pconfidence);
		
        System.out.println("Predicted label fisher: " +plabel[0] + " " +pconfidence[0]);

        eigen.predict(testImage,plabel,pconfidence);
		
        System.out.println("Predicted label eigen: " +plabel[0] + " " +pconfidence[0]);
        
}

}