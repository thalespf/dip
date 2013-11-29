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

import com.thalespf.dip.util.Expansion;
import com.thalespf.dip.util.IImageIO;
import com.thalespf.dip.util.ImageIODesktop;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

/**
 * 
 * @author thales
 *
 */
public class FFTForwardInverseTest {
	public static void main(String[] args) throws IOException {
		forwardTest();
	}

	private static void forwardTest() throws IOException {
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

		//calculate the image fft
		double[] fft = new double[2 * width * height];
		fftForward(raster, width, height, fft);
		
		//calculate the spectro
		int[] spectro = new int[width * height];
		spectro(width, height, fft, spectro);
		imageIO.savePixels(spectro, width, height, null, "fft");
		
		double[] out = new double[width * height];
		fftInverse(fft, out, width, height);
		
		byte[] out2 = new byte[out.length];
		
		for (int i = 0; i < out.length; ++i) {
			long value = Math.round(out[i]);
			if (value < 0)
				out2[i] = (byte) 0;
			else if (value > 255)
				out2[i] = (byte) 255;
			else
				out2[i] = (byte) value;
		}

		ImageIODesktop imageIODesktop = new ImageIODesktop();
		imageIODesktop.savePixels2(out2, width, height, null, "fft_inverse");
	}

	public static void fftForward(WritableRaster raster,
			int width, int height, double[] fft) {
		DoubleFFT_2D fftDo = new DoubleFFT_2D(width, height);

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int r = raster.getSample(x, y, 0);

				fft[2*(x + y*width)] = r;
				fft[2*(x + y*width) + 1] = 0;
				
			}
		}
		
		fftDo.complexForward(fft);
	}

	public static void spectro(int width, int height, double[] fft,
			int[] spectro) {
		double[] magnitude = new double[width * height];
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				double real = fft[y*2*width + 2*x]; //parte real 
				double img = fft[y*2*width + 2*x+1]; //parte imaginaria
				
				magnitude[y*width + x] = Math.sqrt(real * real + img * img);
			}
		}
		
		Expansion.globalExpansion2(magnitude, spectro, width, height);
	}
	
	public static void fftInverse(double[] fft, double[] out, int width, int height) throws IOException {
		DoubleFFT_2D fftDo = new DoubleFFT_2D(width, height);
		fftDo.complexInverse(fft, true);
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				double real = fft[y*2*width + 2*x]; //parte real 
				out[y*width + x] = real;
			}
		}
	}

}