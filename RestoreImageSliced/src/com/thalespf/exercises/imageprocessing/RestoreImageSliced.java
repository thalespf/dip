package com.thalespf.exercises.imageprocessing;

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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * Reconstruct a original image from various image slice pieces out of order. 
 * 
 * The method used for similarity is based on the shortest distance between 
 * the pixels on a window.
 * 
 * @author Thales Ferreira / l.thales.x@gmail.com
 * 
 */
public class RestoreImageSliced {

	private static int sliceQuant = 0;
	private static final int SLICE_WIDTH = 50;
	private static Raster[] slices;
	private static int[] imageOrdered;
	private static int imageHeight;
	private static final int WINDOW_WIDTH_SIZE = 5;

	public static void main(String[] args) {

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("images/windows2.png"));

			int imageWidth = img.getWidth();
			imageHeight = img.getHeight();
			sliceQuant = imageWidth / SLICE_WIDTH;
			
			imageOrdered = new int[sliceQuant];
			for (int i = 0; i < imageOrdered.length; i++) {
				imageOrdered[i] = i;
			}
			
			slices = getSlices(img);

			reOrderImage();

			saveResult(imageWidth, imageHeight);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void saveResult(int imageWidth, int imageHeight) throws IOException {
		BufferedImage imageResult = new BufferedImage(imageWidth,
				imageHeight, BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < imageHeight; i++) {
			for (int k = 0; k < slices.length; k++) {
				Raster r = slices[imageOrdered[k]];

				for (int j = 0; j < SLICE_WIDTH; j++) {
					int[] pixel = new int[3];
					r.getPixel(r.getSampleModelTranslateX() + j, r.getSampleModelTranslateY() + i, pixel);
					int rgb = pixel[0] << 16 | pixel[1] << 8 | pixel[2];
					imageResult.setRGB(k * SLICE_WIDTH + j, i, rgb);
				}
			}
		}

		ImageIO.write(imageResult, "png", new File("result.png"));
	}

	/**
	 * @param imageOrdered
	 * @param slices
	 */
	private static void reOrderImage() {
		for (int i = 0; i < slices.length - 1; i++) {
			Raster r = slices[imageOrdered[i]];
			int old = imageOrdered[i + 1];
			int value = calculateSimilarityByDifference(r, i);
			imageOrdered[i + 1] = imageOrdered[value];
			imageOrdered[value] = old;
		}
	}

	/**
	 * @param slice 
	 * @param sliceId 
	 * 
	 */
	private static int calculateSimilarityByDifference(Raster slice, int sliceId) {
		double[][] slicesDifference = new double[sliceQuant - sliceId - 1][imageHeight
				/ WINDOW_WIDTH_SIZE];

		Raster first = slice;
		for (int i = 0, j = sliceId;j + 1 < slices.length; i++, j++) {
			Raster r = slices[imageOrdered[j + 1]];

			double[] windowDifferences = windowDistanceByMean(first, r);
			slicesDifference[i] = windowDifferences;
		}

		for (int i = 0; i < slicesDifference.length; i++) {
			double[] ds = slicesDifference[i];
			Arrays.sort(ds);
		}

		int finded = 0;
		double windowMean = Double.MAX_VALUE;
		for (int i = 0; i < slicesDifference.length; i++) {
			double[] ds = slicesDifference[i];
			//finded = windowsByFirstLow(slicesDifference, finded, i, ds);
			if(differenceByMean(ds) < windowMean) {
				windowMean = differenceByMean(ds);
				finded = i;
			}
		}
		//double result[] = null;
		//result = slicesDifference[finded];
		//System.out.println(Arrays.toString(result));
		
		return finded + sliceId + 1;
	}

	/**
	 * @param windowDifference
	 * @return
	 */
	private static double differenceByMean(double[] windowDifference) {
		double t = 0;
		for (int i = 0; i < windowDifference.length; i++) {
			t += windowDifference[i];
		}
		return t / windowDifference.length;
	}

	/**
	 * @param pointA
	 * @param pointB
	 * @param first
	 * @param r
	 * @return
	 */
	private static double[] windowDistanceByMean(Raster first, Raster r) {
		double[] windowDistances = new double[imageHeight / WINDOW_WIDTH_SIZE];

		for (int i = 0, y = 0; y <= imageHeight - WINDOW_WIDTH_SIZE; y += WINDOW_WIDTH_SIZE, i++) {
			double[] windowDistance = calculateInWindowDifferenceByDistance(y,
					first, r);
			double windowDistanceByMean = differenceByMean(windowDistance);
			windowDistances[i] = windowDistanceByMean;
		}

		return windowDistances;
	}

	/**
	 * @param first
	 * @param second
	 * @param x
	 * @param y
	 * @return
	 */
	private static double[] calculateInWindowDifferenceByDistance(int y,
			Raster first, Raster second) {
		double[] windowDifference = new double[WINDOW_WIDTH_SIZE
				* WINDOW_WIDTH_SIZE];
		int[] pixelA = new int[first.getNumBands()];
		int[] pixelB = new int[second.getNumBands()];

		for (int j = 0; j < WINDOW_WIDTH_SIZE; j++) {
			for (int i = 0; i < WINDOW_WIDTH_SIZE; i++) {
				first.getPixel(first.getSampleModelTranslateX()
						+ (SLICE_WIDTH) - WINDOW_WIDTH_SIZE + i,
						first.getSampleModelTranslateY() + j + y, pixelA);

				second.getPixel(second.getSampleModelTranslateX() + i,
						second.getSampleModelTranslateY() + j + y, pixelB);

				double difference = pixelDistanceDifference(pixelA, pixelB);
				windowDifference[j * WINDOW_WIDTH_SIZE + i] = difference;
			}
		}
		return windowDifference;
	}

	/**
	 * Calculate the difference distance between two pixels.
	 * 
	 * @param pixelA
	 * @param pixelB
	 * @return
	 */
	private static double pixelDistanceDifference(int[] pixelA, int[] pixelB) {
		int rA = pixelA[0];
		int gA = pixelA[1];
		int bA = pixelA[2];

		int rB = pixelB[0];
		int gB = pixelB[1];
		int bB = pixelB[2];

		return Math.sqrt((rB - rA) * (rB - rA) + (gB - gA) * (gB - gA)
				+ (bB - bA) * (bB - bA));
	}

	/**
	 * Returns each slice of the input image
	 * 
	 * @param img
	 * @return
	 */
	private static Raster[] getSlices(BufferedImage img) {
		Raster[] slices = new Raster[sliceQuant];

		for (int i = 0; i < sliceQuant; i++) {
			slices[i] = img.getData(new Rectangle(i * SLICE_WIDTH, 0,
					SLICE_WIDTH, imageHeight));
		}

		return slices;
	}

}
