package br.serpro.supss.processamentoimagem.test;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import br.serpro.supss.processamentoimagem.ChanneledRaster;

public class ColorCorrection {

	//https://github.com/opencv/opencv_contrib/blob/master/modules/xphoto/src/simple_color_balance.cpp
	//https://web.stanford.edu/~sujason/ColorBalancing/simplestcb.html

	public static final double CAP_PERCENT = 0.0005;
	//public static final double CAP_PERCENT = 0.05;

	public static void correctImage(){
		//light:
		String src = "~/fotos/claras/23.jpg";
		String out = "~/fotos/proc/23.jpg";
		//dark:
		src = 		 "~/fotos/escuras/293.jpg";
		out = 		 "~/fotos/proc/293.jpg";
		//color:
		src = 		 "~/fotos/proc/171-src.jpg";
		out = 		 "~/fotos/proc/171.jpg";
		ChanneledRaster img = new ChanneledRaster(src);
		//saveHSV(equalizeHSV(getHSVPlanes(getMat(src))), out);
		//save(getMat(src), out);
		//save(equalize(getMat(src)), out);
		//save(gray(getMat(src)), out);
		//save(equalizeSingle(gray(getMat(src))), out);
		//save(getMat(src), out);
		//save(equalizeSingle(getHSVPlanes(getMat(src)).get(2)), out);
		//Mat dst = new Mat();
		//balanceWhite(getChannels(getMat(src)), dst);
		//Core.multiply(gray(getMat(src)), new Scalar(1.5), dst);
		//save(dst, out);
		//img.balanceWhite().save(out);
		corrigirDiretorio("~/consolidado");
		//corrigirDiretorio("~/ImgParaBiometriaFacial");
		//img.grayScale().balanceWhite().save(out);
		//img.balanceHSV().save(out);
		//img.balanceLuma().save(out);
		//img.balanceLuma().balanceSaturation().save(out);
		//img.balanceSaturation().balanceLuma().save(out);
		//img = img.balanceLightness();
		/*
		img = img.equalizeSaturation();
		List<ChanneledRaster> yuv = img.convertColor(Imgproc.COLOR_BGR2YUV).splitChannels();
		ChanneledRaster luma = yuv.get(0).balanceWhite();
		img = ChanneledRaster.newInstance(luma, yuv.get(1), yuv.get(2)).convertColor(Imgproc.COLOR_YUV2BGR);
		*/
		//img.save(out);
		/*
		int code;
		code = Imgproc.COLOR_BGR2HSV;
		code = Imgproc.COLOR_BGR2GRAY;
		code = Imgproc.COLOR_BGR2YUV;
		code = Imgproc.COLOR_BGR2YUV_I420;
		code = Imgproc.COLOR_BGR2YUV_IYUV;
		code = Imgproc.COLOR_BGR2YUV_YV12;
		code = Imgproc.COLOR_BGR2HLS;
		code = Imgproc.COLOR_BGR2Lab;
		code = Imgproc.COLOR_BGR2Luv;
		*/
	}
	
	private static void corrigirDiretorio(String endDir){
		File dir = checkDir(endDir);
		if(dir == null){
			throw new IllegalArgumentException("Diretório inválido");
		}
		String[] lista = dir.list();
		for(String end : lista){
			end = endDir + '/' + end;
			File dir2 = checkDir(end);
			if(dir2 == null){
				File arq = new File(end);
				if(!arq.exists() || (!end.toLowerCase().endsWith(".jpeg") 
						&& !end.toLowerCase().endsWith(".jpg") 
						&& !end.toLowerCase().endsWith(".png"))
						|| end.toLowerCase().indexOf("-processado") > -1){
					continue;
				}
				String out = end;
				int i = end.lastIndexOf('.');
				out = out.substring(0, i) + "-processado" + out.substring(i);
				ChanneledRaster img = new ChanneledRaster(end);
				//img = img.balanceWhite();
				//img = img.balanceLuma();
				img = img.balanceLightness();
				img.save(out);
			}
			else{
				corrigirDiretorio(end);
			}
		}
	}
	
	private static File checkDir(String endDir){
		File dir = new File(endDir);
		if(!dir.exists() || !dir.isDirectory()){
			return null;
		}
		return dir;
	}

	public static Mat equalize(Mat src){
		List<Mat> channels = getChannels(src);
		Mat b = equalizeSingle(channels.get(0));
		Mat g = equalizeSingle(channels.get(1));
		Mat r = equalizeSingle(channels.get(2));
		return merge(b, g, r);
	}

	public static Mat equalizeSingle(Mat src){
		Mat dst = new Mat();
		Imgproc.equalizeHist(src, dst);
		//Imgproc.equalizeHist(threshold(src), dst);//TODO
		minMax(src);
		minMax(dst);
		return dst;
	}

	public static List<Mat> getChannels(Mat src){
		List<Mat> planes = new ArrayList<Mat>();
		Core.split(src, planes);
		return planes;
	}

	public static Mat getMat(String file){
		Mat mat = Imgcodecs.imread(file);
		if(mat.empty()){
			System.out.println("imagem não encontrada");
			return null;
		}
		return mat;
	}

	public static Mat gray(Mat src){
		Mat dst = new Mat();
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
		return dst;
	}

	public static Mat histogram(Mat src){
		List<Mat> l = new ArrayList<Mat>();
		l.add(src);
		Mat mask = new Mat();
		Mat hist = new Mat();
		Imgproc.calcHist(l, new MatOfInt(0), mask, hist, new MatOfInt(256), new MatOfFloat(0, 256));
		return hist;
	}

	public static Mat merge(Mat b, Mat g, Mat r){
		List<Mat> channels = new ArrayList<Mat>();
		channels.clear();
		channels.add(b);
		channels.add(g);
		channels.add(r);
		Mat dst = new Mat();
		Core.merge(channels, dst);
		return dst;
	}

	private static void minMax(Mat mat){
		MinMaxLocResult mmlr = Core.minMaxLoc(mat);
		System.out.print((int)mmlr.minVal);
		System.out.print(' ');
		System.out.println((int)mmlr.maxVal);
	}

	public static void save(List<Mat> src, String file){
		Mat eq = new Mat();
		Core.merge(src, eq);
		save(eq, file);
	}

	public static void save(Mat src, String file){
		Imgcodecs.imwrite(file, src);
		File img = new File(file);
		System.out.println(img.exists() ? img.getAbsolutePath() : "output error");
	}

	public static void saveHSV(List<Mat> src, String file){
		Mat eq = new Mat();
		Core.merge(src, eq);
		saveHSV(eq, file);
	}

	public static void saveHSV(Mat src, String file){
		Mat dst = new Mat();
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_HSV2BGR);

		Imgcodecs.imwrite(file, dst);
		File img = new File(file);
		System.out.println(img.exists() ? img.getAbsolutePath() : "output error");
	}

	
	public static List<Mat> equalizeHSV(List<Mat> hsv){
		minMax(hsv.get(0));
		Mat saturation = equalizeSingle(hsv.get(1));
		Mat vt = threshold(hsv.get(2));
		Mat value = equalizeSingle(vt);
		//value = hsv.get(2);
		//value = vt;
		//Mat value = equalizeSingle(hsv.get(2));

		//Core.normalize(src, eq, 0, 255, Core.NORM_INF);
		List<Mat> eq = new ArrayList<Mat>();
		eq.add(hsv.get(0));//max=179
		eq.add(saturation);
		eq.add(value);
		return eq;
	}

	public static List<Mat> getHSVPlanes(Mat src){
		Mat hsv = new Mat();
		Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
		//Mat [ 640*480*CV_8UC3, isCont=true, isSubmat=false, nativeObj=0x7f3fdc184880, dataAddr=0x7f3fdc28e540 ]
		return getChannels(hsv);
	}

	public static int lowerCap(Mat src){
		Mat hist = histogram(src);
		int target = (int) (src.cols() * src.rows() * CAP_PERCENT);//153.6; 307200
		int i=0;
		for(int sum=0;i<256 && sum < target;i++){
			sum += hist.get(i, 0)[0];
		}
		i--;
		System.out.println();
		System.out.println(i);
		return i;//32 11
	}

	public static Mat threshold(Mat src){
		Mat dst1 = new Mat();
		Mat dst = new Mat();
		int max = upperCap(src);//calculate % of hist
		int min = lowerCap(src);
		//Imgproc.threshold(src, dst1, max, 255, Imgproc.THRESH_TRUNC);
		//Imgproc.threshold(dst1, dst, min, 255, Imgproc.THRESH_TOZERO);

		src.copyTo(dst1);
		Mat mask = new Mat();
		Core.inRange(dst1, new Scalar(0), new Scalar(min), mask);
		dst1.setTo(new Scalar(min), mask);

		dst1.copyTo(dst);
		mask = new Mat();
		Core.inRange(dst, new Scalar(max), new Scalar(255), mask);
		dst.setTo(new Scalar(max), mask);

		//src.setTo()
		//now replace
		minMax(src);
		minMax(dst);
		return dst;
	}

	public static int upperCap(Mat src){
		Mat hist = histogram(src);
		int target = (int) (src.cols() * src.rows() * CAP_PERCENT);//153.6; 307200
		int i=255;
		for(int sum=0;i>=0 && sum < target;i--){
			sum += hist.get(i, 0)[0];
		}
		i++;
		System.out.println();
		System.out.println(i);
		return i;//255 217
	}

}