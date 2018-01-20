package br.serpro.supss.processamentoimagem.test;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class Sandbox {

	public static void main(String[] args) throws IOException{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//Video.videotest();
		//Base64.extractbase64();
		ColorCorrection.correctImage();
		Mat mat = new Mat();
		int codigo = mat.get(0, 0, new byte[0]);
		mat.put(0, 0, new byte[0]);
		//Core.divide(scale, src2, dst);
	}
	
	
}