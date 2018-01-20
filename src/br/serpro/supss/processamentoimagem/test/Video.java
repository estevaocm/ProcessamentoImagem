package br.serpro.supss.processamentoimagem.test;
import java.io.File;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {
	public static void videotest(){
		//Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		//System.out.println("mat = " + mat.dump());
		String video = "~/Downloads/Bom Dia Brasil - QR Code.mp4";
		VideoCapture cap = captureVideo(video);
		Mat frame = initFrame(cap);
		outputFrame(cap, frame, "frame.png");
	}

	public static VideoCapture captureVideo(String file){
		VideoCapture cap = new VideoCapture(file, Videoio.CAP_FFMPEG);
		int w = (int) cap.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int h = (int) cap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		double fps = cap.get(Videoio.CAP_PROP_FPS);
		fps = Math.round(fps *100)/100.0;
		int t = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
		System.out.println(w + "x" + h + "@" + fps + "fps" + ", " + t + " frames");
		//System.out.println(cap.get(Videoio.CAP_PROP_FORMAT));
		//System.out.println(cap.get(Videoio.CAP_PROP_FOURCC));
		//VideoWriter
		return cap;
	}
	
	public static Mat initFrame(VideoCapture cap){
		int w = (int) cap.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int h = (int) cap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		Mat frame = new Mat(h, w, CvType.CV_8UC3);
		return frame;
	}
	
	public static void outputFrame(VideoCapture cap, Mat frame, String file){
		cap.read(frame);
		//Mat [ 3*3*CV_8UC1, isCont=true, isSubmat=false, nativeObj=0x7efd4c11b920, dataAddr=0x7efd4c11bc80 ]
		//System.out.println(frame.type());
		//System.out.println(CvType.typeToString(frame.type()));
		Imgcodecs.imwrite(file, frame);
		File img = new File(file);
		System.out.println(img.exists() ? img.getAbsolutePath() : "output error");
	}


}
