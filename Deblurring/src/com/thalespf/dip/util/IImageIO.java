
package com.thalespf.dip.util;

import java.io.IOException;

public interface IImageIO {

	//int
	public void savePixels(int[] pixels, int width, int height,
			String dir, String name);

	//byte
	public void savePixels(byte[] pixels, int width, int height,
			String dir, String name);

	public void savePixels(int[] pixels, int x0, int y0, int x1,
			int y1, int width, String dir, String name);

	public void savePixels(byte[] pixels, int[] map, int x0, int y0,
			int x1, int y1, int width, int id1, int id2, String dir, String name);

	public Object getImageObject() throws IOException;

	public String createOutputDir();

}