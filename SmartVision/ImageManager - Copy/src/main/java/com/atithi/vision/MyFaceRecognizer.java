package com.atithi.vision;




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

import com.atithi.rest.services.impl.ImageServiceImpl;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;


public class MyFaceRecognizer {
	public static final int val = 0;
	
	public static final String TRAINING_DIR ="C:/Users/Gaurav/Pictures/Amdocs_LastDays/Cropped/";
	public static final String FAKE_IMAGE_PATH="E:/MS/3-Omkar.png";
	private FaceRecognizer faceRecognizer;
	
	public MyFaceRecognizer() {
		
		this.faceRecognizer = createFisherFaceRecognizer();
		
	}
	
	//  dir/
	private File[] getSets(Boolean isDir,String dir) {
		
		File [] directories = null;
		final File root = new File(dir);
		if(isDir){			
			directories = root.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					// TODO Auto-generated method stub
					return file.isDirectory();
				}
			});
		}
		else {
			directories = root.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					// TODO Auto-generated method stub
					return file.isFile();
				}
			});			
		}
		
		return directories;		
	}
	
	 
	
    
	public  void train() {
		//This is for bug in 2.4.9
		System.out.println(System.getProperty("java.library.path"));
		File image = new File(FAKE_IMAGE_PATH);
		final Mat testmag = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
		//code starts here
		File[] directories = this.getSets(true,ImageServiceImpl.ZIP_PATH_STORE+"Set");
		int totalFiles = 30*directories.length;
		Mat label1 = new Mat(totalFiles,1,CV_32SC1);
		IntBuffer labelsBuf = label1.getIntBuffer();
		MatVector final_images = new MatVector(totalFiles);
		//
		int counter = 0;
		int group =0;
		for (final File file : directories) {
			file.getAbsolutePath();
		final File[] files = this.getSets(false, file.getAbsolutePath());
			
			for(int i=1;i<=30;i++) {
				final Mat temp = imread(files[i].getAbsolutePath(),val);				
				if(temp.empty()) {
					System.out.println("Cannot read image "+files[i].getAbsolutePath());
					System.exit(0);
				}
				else{
					labelsBuf.put(counter, group);
					final_images.put(counter, temp);
					counter++;
				}
			}
			group++;
		}
		faceRecognizer.train(final_images, label1);
		System.out.println("Trainning is done!!");
	    
	}

	//C:/Users/Gaurav/Pictures/Amdocs_LastDays/Murtaza_Cropped/M1.png
	public Map<String, String> predict(final String testimagePath) {
		
		final Mat testImage = imread(testimagePath,val);
		int[] plabel = new int[1];
		double[] pconfidence = new double[1];
		this.faceRecognizer.predict(testImage,plabel,pconfidence);
		System.out.println("Predicted label fisher: " +plabel[0] + " " +pconfidence[0]);
		//eigen.predict(testImage,plabel,pconfidence);
		System.out.println("Predicted label eigen: " +plabel[0] + " " +pconfidence[0]);
		
		final Map<String, String> result = new HashMap<String, String>();
		final Integer res = new Integer(plabel[0]);
		final Double conf = new Double(pconfidence[0]);
		result.put("Label", res.toString());
		result.put("Confidence",conf.toString());		
		return result;
	}
	
	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		MyFaceRecognizer f = new MyFaceRecognizer();
		f.train();
		f.predict("C:\\Users\\Gaurav\\Pictures\\Amdocs_LastDays\\RASPBERRY\\IMAGES\\Set\\Set1\\O1.png");
        
	}*/

}