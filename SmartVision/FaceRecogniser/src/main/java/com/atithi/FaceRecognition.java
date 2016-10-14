package com.atithi;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.Size;

//import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.*;

import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.presets.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_contrib.*;



public class FaceRecognition {
	
	private static final String MY_TRANSFORM = "src/main/resources/lbpcascade_frontalface.xml";
	private static final String IMAGE_PATH = "C:/Users/Gaurav/Pictures/Amdocs_LastDays/image1.JPG";
	
	private static int FIXED_IMAGE_WIDTH=277;
	private static int FIXED_IMAGE_HEIGHT=277;
	
	private void faceDetector(final String imageName,int cnt) {
		
		final Mat image = Highgui.imread(imageName);
		if(!image.empty()) {
			System.out.println("not empty");
			final CascadeClassifier cascadeClassifier = new CascadeClassifier();
			if(cascadeClassifier.load(MY_TRANSFORM)) {
				System.out.println("Loaded");
				final MatOfRect matOfRect = new MatOfRect();
				cascadeClassifier.detectMultiScale(image, matOfRect);
				int faces = matOfRect.toArray().length;
				if(faces> 0) {
				System.out.println("Faces detected"+faces);
				
				Rect rect_crop = null;
				
				for (Rect rect : matOfRect.toArray()) {
				      Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
				              new Scalar(0, 255, 0));
				      rect_crop = new Rect(rect.x, rect.y, rect.width, rect.height);
				      
				  }
				System.out.println("   Height is  "+rect_crop.height+"   Width is  "+rect_crop.width); 
				
				//Imgproc.resize(image, matOfRect, dsize);
				final Mat cropped_image = new Mat(image, rect_crop);
				final Mat resized = new Mat();
				org.opencv.core.Size size = new org.opencv.core.Size(FIXED_IMAGE_HEIGHT, FIXED_IMAGE_WIDTH);
				Imgproc.resize(cropped_image, resized, size);			
				//C:/Users/Gaurav/Pictures/Amdocs_LastDays/Cropped_Chao
				Highgui.imwrite("C:/Users/Gaurav/Pictures/Amdocs_LastDays/Murtaza/Cropped/O"+cnt+".png", resized);
				//String filename = "C:/Users/Gaurav/Pictures/Amdocs_LastDays/ouput_1.png";
				//System.out.println(String.format("Writing %s", filename));
				//Highgui.imwrite(filename, image);
				
			}
		}
		}				
	}
	
	
	
	public  void removeBackground(final String imagePath) {
		
		final Mat img0 = Highgui.imread(IMAGE_PATH,1);
		//"C:/Users/Gaurav/Pictures/Amdocs_LastDays/image2.JPG"
		final Mat img1 = new Mat();
		
		Imgproc.Canny(img0, img1, 100,200);
		
		final List<MatOfPoint> countours = new ArrayList<MatOfPoint>();
		
		//findContours(img1, contours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_NONE);
       
		//Imgproc.findContours(img1, countours, hierarchy, mode, method);
		
	}
	
	public static void main(final String args[]) {
		
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String fileName ="C:/Users/Gaurav/Pictures/Amdocs_LastDays/image1.JPG";         //IMAGE_PATH+"/"+args[0];
		final String source_folder = "C:/Users/Gaurav/Pictures/Amdocs_LastDays/Murtaza";
		final String target = "C:/Users/Gaurav/Pictures/Amdocs_LastDays/Murtaza/Cropped";
		
		File dir = new File(source_folder);
		File[] files = dir.listFiles();
		System.out.println("----->" +files.length);
		int i =1;
		
		for (final File file : files) {
			if(!file.isDirectory()) {
			final FaceRecognition faceRecognizer = new FaceRecognition();
			faceRecognizer.faceDetector(file.getAbsolutePath(),i);
			i++;
			}
			//break;
		} 
		
		//final FaceRecognition faceRecognizer = new FaceRecognition();
		//faceRecognizer.faceDetector(IMAGE_PATH);
		//faceRecognizer.removeBackground("");
         //FaceRecognizer recog =
		 
	}
	

}
