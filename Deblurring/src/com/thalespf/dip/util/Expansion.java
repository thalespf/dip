package com.thalespf.dip.util;


public class Expansion {
	
	public static void globalExpansion2(double[] magnitude, int[] pixels, int width, int height) {
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		
		int x_y;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				x_y = x + y*width;
				
				if(magnitude[x_y] < min) min = magnitude[x_y];
				if(magnitude[x_y] > max) max = magnitude[x_y];
			}
		}
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				x_y = x + y*width;
				
				int lum;
				
				lum = (int)(255.0*((magnitude[x_y])-min) / (max-min) + 0.5);

				if(lum < 0) lum = 0;
				else if(lum > 255) lum = 255;

				pixels[x_y] = 0xFF000000 | lum << 16 | lum << 8 | lum;
			}
		}
	}

	public static void globalExpansion(int[] pixels, int width, int height) {
		int min = Integer.MAX_VALUE;
		int max = -Integer.MAX_VALUE;
		
		int x_y;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				x_y = x + y*width;
				
				if(pixels[x_y] == 0)
					continue;
				
				if((pixels[x_y] & 0xFF) < min) min = pixels[x_y] & 0xFF;
				if((pixels[x_y] & 0xFF) > max) max = pixels[x_y] & 0xFF;
			}
		}
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				x_y = x + y*width;
				
				if(pixels[x_y] == 0)
					continue;
				
				int lum;
				
				lum = (int)(255.0*((pixels[x_y] & 0xFF)-min) / (max-min) + 0.5);

				if(lum < 0) lum = 0;
				else if(lum > 255) lum = 255;

				pixels[x_y] = 0xFF000000 | lum << 16 | lum << 8 | lum;
			}
		}
	}
}
