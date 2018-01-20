package br.serpro.supss.processamentoimagem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Object-oriented wrapper for native-runned OpenCV matrixes representing raster images with 
 * multiple color channels.
 * @author Estêvão Chaves Monteiro
 * @since 23/08/17
 * @see http://opencv.org
 */
public class ChanneledRaster {
	
	private static final double QUANTILE = 2;
	private static final double WHITE_BALANCE_MARGIN = 0.5;
	private static final int DYNAMIC_RANGE = 255;
	
	private final Mat mat;

	public ChanneledRaster(Mat mat){
		this.mat = mat;
	}
	
	public ChanneledRaster(String file){
		this.mat = Imgcodecs.imread(file);
		if(mat.empty()){
			throw new IllegalArgumentException("imagem não encontrada");
		}
	}
	
	private void checkSingleChannel(){
		if(this.mat.channels() > 1){
			throw new IllegalStateException("Image has " + this.mat.channels() + " channels.");
		}
	}

	public ChanneledRaster balanceWhite(double adjust){
		final List<Mat> src = splitChannelsMats();
		return new ChanneledRaster(balanceWhite(src, 0, DYNAMIC_RANGE, 0, DYNAMIC_RANGE, QUANTILE, adjust));
	}

	public ChanneledRaster balanceWhite(){
		return balanceWhite(1.0);
	}

	public ChanneledRaster convertColor(int colorConversionCode){//TODO enum for Imgproc.COLOR_HSV2BGR Imgproc.COLOR_BGR2HSV
		Mat dst = new Mat();
		Imgproc.cvtColor(this.mat, dst, colorConversionCode);
		return new ChanneledRaster(dst);
	}

	/**
	 * Beware possible artifacts.
	 * @return
	 */
	public ChanneledRaster equalizeChannels(){
		List<Mat> channels = splitChannelsMats();
		Mat b = new ChanneledRaster(channels.get(0)).equalizeSingleChannel().getMat();
		Mat g = new ChanneledRaster(channels.get(1)).equalizeSingleChannel().getMat();
		Mat r = new ChanneledRaster(channels.get(2)).equalizeSingleChannel().getMat();
		return newInstance(b, g, r);
	}
	
	/**
	 * Beware possible artifacts.
	 * @return
	 */
	public ChanneledRaster equalizeSingleChannel(){
		checkSingleChannel();
		Mat dst = new Mat();
		Imgproc.equalizeHist(this.mat, dst);
		//Imgproc.equalizeHist(threshold(src), dst);
		//minMax(this.mat);
		//minMax(dst);
		return new ChanneledRaster(dst);
	}

	public Mat getMat(){
		return this.mat;
	}
	
	public int getValueSingleChannel(int col, int row){
		checkSingleChannel();
		byte[] vals = new byte[1];
		this.mat.get(row, col, vals);
		return Byte.toUnsignedInt(vals[0]);
	}
	
	public ChanneledRaster grayScale(){
		Mat dst = new Mat();
		Imgproc.cvtColor(this.mat, dst, Imgproc.COLOR_BGR2GRAY);
		return new ChanneledRaster(dst);
	}
	
	public ChanneledRaster histogram(){
		List<Mat> l = new ArrayList<Mat>();
		l.add(this.mat);
		Mat mask = new Mat();
		Mat hist = new Mat();
		Imgproc.calcHist(l, new MatOfInt(0), mask, hist, new MatOfInt(256), new MatOfFloat(0, 256));
		return new ChanneledRaster(hist);
	}

	public void save(String file){
		save(this.mat, file);
	}

	public List<ChanneledRaster> splitChannels(){
		List<Mat> planes = new ArrayList<Mat>();
		Core.split(this.mat, planes);
		List<ChanneledRaster> r = new ArrayList<ChanneledRaster>();
		for(Mat mat : planes){
			r.add(new ChanneledRaster(mat));
		}
		return r;
	}

	public List<Mat> splitChannelsMats(){
		List<Mat> planes = new ArrayList<Mat>();
		Core.split(this.mat, planes);
		return planes;
	}

	private static Mat merge(Mat channel1, Mat channel2, Mat channel3){
		List<Mat> channels = new ArrayList<Mat>();
		channels.clear();
		channels.add(channel1);
		channels.add(channel2);
		channels.add(channel3);
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
	
	public static ChanneledRaster newInstance(ChanneledRaster channel1, ChanneledRaster channel2, ChanneledRaster channel3){
		return newInstance(channel1.getMat(), channel2.getMat(), channel3.getMat());
	}
	
	public static ChanneledRaster newInstance(Mat channel1, Mat channel2, Mat channel3){
		return new ChanneledRaster(merge(channel1, channel2, channel3));
	}
	
	private static void save(Mat mat, String file){
		File img = new File(file);
		//img.mkdirs();
		Imgcodecs.imwrite(file, mat);
		System.out.println(img.exists() ? img.getAbsolutePath() : "Failed to save file '" + file + "'");
	}

	/**
	 * 
	 * @param channels
	 * @param dst
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @param p
	 * @see https://github.com/opencv/opencv_contrib/blob/master/modules/xphoto/src/simple_color_balance.cpp
	 */
	private static Mat balanceWhite(final List<Mat> channels, final double inputMin, final double inputMax, 
			final double outputMin, final double outputMax, final double p, double adjust){
		
		int depth = 2;//depth of the histogram tree
		final Mat ref = channels.get(0);//referential matrix for dimensions etc.
		if (ref.depth() != CvType.CV_8U){
			depth++;
		}
		int bins = 16;//number of bins at each histogram level
		//Iterate over all image channels:
		for (int i=0; i<channels.size(); i++){
			balanceWhiteChannel(channels.get(i), inputMin, inputMax, outputMin, outputMax, p, bins, depth, adjust);
		}
		//Merge modified matrixes into single multi-channel matrix:
		Mat out = new Mat();
		out.create(ref.size(), CvType.makeType(ref.depth(), channels.size()));
		Core.merge(channels, out);
		return out;
	}
	
	private static void balanceWhiteChannel(Mat mat, final double inputMin, final double inputMax, 
			final double outputMin, final double outputMax, double p, int bins, int depth, double adjust){
		
		int[] hist = histWhiteBalance(mat, inputMin, inputMax, bins, depth);
		
		double minValue = inputMin - WHITE_BALANCE_MARGIN;//-0.5
		double maxValue = inputMax + WHITE_BALANCE_MARGIN;//255.5
		double interval = (maxValue - minValue) / bins;//16
		int total = (int) (mat.total());//307200
		int p1 = 0;
		int p2 = bins - 1;
		int n1 = 0;
		int n2 = total;
		final double s1 = p;//lower quantile
		final double s2 = p;//higher quantile
		//Search for s1 and s2:
		for (int j = 0; j < depth; j++){
			while (p1 < 256 && n1 + hist[p1] < s1 * total / 100.0){
				n1 += hist[p1++];
				minValue += interval;
			}
			p1 *= bins;
			while (p2 > -1 && n2 - hist[p2] > (100.0 - s2) * total / 100.0){
				n2 -= hist[p2--];
				maxValue -= interval;
			}
			p2 = (p2 + 1) * bins - 1;
			interval /= bins;
		}
		//minValue = 76.5; maxValue = 255.5
		//Process the image for balanced whites:
		Core.subtract(mat, new Scalar(minValue), mat);//mat - minValue
		//adjust = 1.2;//1.0
		double factor = Math.pow((outputMax - outputMin)/(maxValue - minValue), adjust);
		Core.multiply(mat, new Scalar(factor), mat);//mat * factor
		if(outputMin != 0){
			Core.add(mat, new Scalar(outputMin), mat);//mat + outputMin
		}
	}
	
	private static int[] histWhiteBalance(final Mat src, final double inputMin, final double inputMax, 
			final int bins, final int depth){
		int range = (int) Math.pow(bins, depth);//number of elements in histogram tree
		final int[] hist = new int[range];
		
		for(int c=0;c<src.cols();c++){
			for(int r=0;r<src.rows();r++){
				int val = new ChanneledRaster(src).getValueSingleChannel(c, r);
				double minValue = inputMin - WHITE_BALANCE_MARGIN;//-0.5
				double maxValue = inputMax + WHITE_BALANCE_MARGIN;//255.5
				double interval = (maxValue - minValue)/bins;//16
				int pos = 0;
				for (int j = 0; j < depth; j++){
					int bin = (int) ((val - minValue + 1e-4)/interval);
					hist[pos + bin]++;
					pos = (pos + bin) * bins;
					minValue = minValue + bin * interval;
					maxValue = minValue + interval;
					interval /= bins;
				}
			}
		}
		return hist;
	}
	
	/**
	 * Balance saturation and value, keeping the original hue.
	 * @return
	 */
	public ChanneledRaster balanceHSV(double adjust){
		List<ChanneledRaster> hsv = convertColor(Imgproc.COLOR_BGR2HSV).splitChannels();
		ChanneledRaster hue = hsv.get(0);
		ChanneledRaster saturation = hsv.get(1).balanceWhite();
		ChanneledRaster value = hsv.get(2).balanceWhite(adjust);
		return ChanneledRaster.newInstance(hue, saturation, value).convertColor(Imgproc.COLOR_HSV2BGR);		
	}
	
	public ChanneledRaster balanceHSV(){
		return balanceHSV(1.0);
	}
	
	public ChanneledRaster balanceSaturation(){
		List<ChanneledRaster> hsv = convertColor(Imgproc.COLOR_BGR2HSV).splitChannels();
		ChanneledRaster hue = hsv.get(0);
		ChanneledRaster saturation = hsv.get(1).balanceWhite();
		ChanneledRaster value = hsv.get(2);
		return ChanneledRaster.newInstance(hue, saturation, value).convertColor(Imgproc.COLOR_HSV2BGR);		
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Results in artifacts.
	 */
	public ChanneledRaster equalizeSaturation(){
		List<ChanneledRaster> hsv = convertColor(Imgproc.COLOR_BGR2HSV).splitChannels();
		ChanneledRaster hue = hsv.get(0);
		ChanneledRaster saturation = hsv.get(1).equalizeSingleChannel();
		ChanneledRaster value = hsv.get(2);
		return ChanneledRaster.newInstance(hue, saturation, value).convertColor(Imgproc.COLOR_HSV2BGR);		
	}
	
	/**
	 * Balance the luma plane, keeping the original chroma planes.
	 * @return
	 */
	public ChanneledRaster balanceLuma(){
		List<ChanneledRaster> yuv = convertColor(Imgproc.COLOR_BGR2YUV).splitChannels();
		ChanneledRaster luma = yuv.get(0).balanceWhite();
		return ChanneledRaster.newInstance(luma, yuv.get(1), yuv.get(2)).convertColor(Imgproc.COLOR_YUV2BGR);		
	}

	public ChanneledRaster balanceLightness(double adjust){
		List<ChanneledRaster> lab = convertColor(Imgproc.COLOR_BGR2Lab).splitChannels();
		ChanneledRaster lightness = lab.get(0).balanceWhite();
		return ChanneledRaster.newInstance(lightness, lab.get(1), lab.get(2)).convertColor(Imgproc.COLOR_Lab2BGR);
	}

	public ChanneledRaster balanceLightness(){
		return balanceLightness(1.0);
	}
}