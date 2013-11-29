/*
 * Copyright 2013 Thales Ferreira
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thalespf.dip;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.commons.math3.analysis.function.Sinc;
import org.apache.commons.math3.complex.Complex;

import com.thalespf.dip.util.Expansion;
import com.thalespf.dip.util.IImageIO;
import com.thalespf.dip.util.ImageIODesktop;

/**
 * A class to blur and deblur images with motion blur. The blur method (convolution) 
 * use a PSF (point-spread function) defined in Gonzalez. 
 * The PSF (in domain space) is represented by a OTF in frquency domain that make linear motion blur.
 * 
 * The deblur use the Wiener method to make the image deconvolution.
 * 
 * This code is only for tests and needed design.
 * 
 * @author thales ferreira (l.thales.x@gmail.com)
 *
 */
public class DeblurringTest {

	private static final double T = 1.0;
	private static final double A = 0.1;
	private static final double B = 0.1;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//convolution
		blurTest();
		
		//deconvolution
		deblurTest();
	}

	private static void deblurTest() throws IOException {
		String imagePath = "image_blurred.png";
		IImageIO imageIO = ImageIODesktop.createImageIO(imagePath);
		Object imageObject = imageIO.getImageObject();
		if(imageObject == null || !(imageObject instanceof BufferedImage)) {
			throw new IllegalStateException("Nao foi possivel criar a imagem.");
		}

		BufferedImage bufferedImage = (BufferedImage) imageObject;
		WritableRaster raster = bufferedImage.getRaster();
		
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		
		double[] fft = new double[2 * width * height];
		FFTForwardInverseTest.fftForward(raster, width, height, fft);
		
		Complex[] complexImage = new Complex[width * height];
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				Complex c = new Complex(fft[2*(x + y*width)], fft[2*(x + y*width) + 1]);
				complexImage[x + y*width] = c;
				
			}
		}
		
		normalizeToMaxAbsValue(complexImage, width, height, 1);
		
		double[] degradation = new double[2*width * height];
		//motionBlur (funcao de transferencia)
		Complex[] complexPsf = motionBlur(degradation, width, height);
		
		normalizeToMaxAbsValue(complexPsf, width, height, 1);

		//deconvolve data
		double[] convoluted = new double[2 * width * height];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				Complex complexImg = complexImage[x + y*width];
				
				Complex complexDeg = complexPsf[x + y*width];
				
				Complex m = deconvolutionByWiener(complexImg, complexDeg);
				
				convoluted[2*(x + y*width)] = m.getReal();
				convoluted[2*(x + y*width) + 1] = m.getImaginary();
			}
		}
		
		double[] imageResult = new double[width * height];
		FFTForwardInverseTest.fftInverse(convoluted, imageResult, width, height);
		
		int[] image = new int[imageResult.length];
		Expansion.globalExpansion2(imageResult, image, width, height);
		
		ImageIODesktop imageIODesktop = new ImageIODesktop();
		imageIODesktop.savePixels(image, width, height, null, "image_deblurred");
	}
	
	private static Complex deconvolutionByWiener(Complex imagem, Complex psf) {
	    double K = Math.pow(1.07, 32)/10000.0;
	    double energyValue = Math.pow(psf.getReal(), 2) + Math.pow(psf.getImaginary(), 2);
	    double wienerValue = energyValue / (energyValue + K);
	    
	    Complex divided = imagem.divide(psf);
		Complex c = divided.multiply(wienerValue);
	    
	    return c;
	}
	

	private static Complex divideDeconvolutionOp(Complex complexImg,
			Complex complexDeg) {
		return complexImg.divide(complexDeg);
	}
	
    public static void normalizeToMaxAbsValue(Complex[] data, int width, int height, double value)
    {
        double max = Double.MIN_VALUE;

		for ( int x = 0; x < width; x++ ) {
            for ( int y = 0; y < height; y++ ) {
				Complex c = data[ x + y*width ];
            	double abs = Math.sqrt(c.getReal() * c.getReal() + c.getImaginary() * c.getImaginary() );
                max = Math.max( max, abs );
    		}
    	}

        for ( int x = 0; x < data.length; x++ ) {
        	data[x] = data[x].multiply( value / max );        	
        }
    }

	private static void blurTest() throws IOException {
		String imagePath = "image.jpg";
		IImageIO imageIO = ImageIODesktop.createImageIO(imagePath);
		Object imageObject = imageIO.getImageObject();
		if(imageObject == null || !(imageObject instanceof BufferedImage)) {
			throw new IllegalStateException("Nao foi possivel criar a imagem.");
		}

		BufferedImage bufferedImage = (BufferedImage) imageObject;
		WritableRaster raster = bufferedImage.getRaster();
		
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		
		double[] fft = new double[2 * width * height];
		FFTForwardInverseTest.fftForward(raster, width, height, fft);
		
		Complex[] ci = new Complex[width * height];
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				Complex c = new Complex(fft[2*(x + y*width)], fft[2*(x + y*width) + 1]);
				ci[x + y*width] = c;
				
			}
		}
		
		normalizeToMaxAbsValue(ci, width, height, 1);
		
		double[] degradation = new double[2*width*height];
		//motionBlur (funcao de transferencia)
		Complex[] complexPsf = motionBlur(degradation, width, height);
		
		normalizeToMaxAbsValue(complexPsf, width, height, 1);
		
		//convolve data
		double[] convoluted = new double[2 * width * height];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				double realImg = fft[2*(x + y*width)];
				double imgImg = fft[2*(x + y*width) + 1];
				Complex complexImage = new Complex(realImg, imgImg);
				
				double realDeg = degradation[2*(x + y*width)];
				double imgDeg = degradation[2*(x + y*width) + 1];
				Complex complexDeg = new Complex(realDeg, imgDeg);
				
				Complex m = complexImage.multiply(complexDeg);
				convoluted[2*(x + y*width)] = m.getReal();
				convoluted[2*(x + y*width) + 1] = m.getImaginary();
			}
		}
		
		double[] imageResult = new double[width * height];
		FFTForwardInverseTest.fftInverse(convoluted, imageResult, width, height);
		
		int[] image = new int[imageResult.length];
		Expansion.globalExpansion2(imageResult, image, width, height);

		ImageIODesktop imageIODesktop = new ImageIODesktop();
		imageIODesktop.savePixels(image, width, height, null, "image_blurred");
	}
	
	private static Complex[] motionBlur(double[] degradation, int width, int height) {
		Complex[] complex = new Complex[width * height];
		
		double[] temp = new double[2 * width * height];
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				double teta = Math.PI*((x - width/2)%width*A + (y - height/2)%height*B);
				
				Sinc sinc = new Sinc();
				
				double real = Math.cos(teta) * sinc.value(teta) * T;
				double imaginary = Math.sin(teta) * sinc.value(teta) * T;
				
				Complex c = new Complex(real, imaginary);
				Complex cConj = c.conjugate();
				
				temp[2*(x + y*width)] = cConj.getReal();
				temp[2*(x + y*width) + 1] = cConj.getImaginary();
			}
		}
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
	            int xTranslated = (x + width/2) % width;
	            int yTranslated = (y + height/2) % height;
	            
	            double real = temp[2*(yTranslated*width + xTranslated)];
	            double imaginary = temp[2*(yTranslated*width + xTranslated) + 1];
	            
	            degradation[2*(x + y*width)] = real;
	            degradation[2*(x + y*width) + 1] = imaginary;
	            
	            Complex c = new Complex(real, imaginary);
	            complex[y*width + x] = c;
			}
		}
		
		return complex;
	}

}

