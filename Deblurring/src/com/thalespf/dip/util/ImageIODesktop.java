package com.thalespf.dip.util;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Permite salvar imagens no disco.
 *
 */
public class ImageIODesktop implements IImageIO {
	private static int cont = 0;
	private static final DecimalFormat df = new DecimalFormat("0000");
	private final String inputImagePath;
	private String fileInputID;
	private String dirOut;

	/**
	 * Constroi um ImageIODesktop a partir de uma imagem de entrada
	 * 
	 * @param inputImagePath
	 */
	public ImageIODesktop(String inputImagePath) {
		this.inputImagePath = inputImagePath;
		dirOut = createOutputDir();
	}

	public ImageIODesktop() {
		inputImagePath = null;
		dirOut = createOutputDir();
	}

	@Override
	public Object getImageObject() throws IOException {
		File imageFile = new File(inputImagePath);
		BufferedImage bufferedImage = javax.imageio.ImageIO.read(imageFile);
		fileInputID = imageFile.getName();
		dirOut = createOutputDir();
		return bufferedImage;
	}

	/**
	 * Salva uma imagem no diretõrio Ledblind/fotos.
	 * 
	 * O nome do arquivo contera a hora em que ele foi salvo seguida de um nome escolhido pelo usuario na chamada
	 * do método.
	 * 
	 * @param image imagem a ser salva.
	 * @param name nome do arquivo resultante.
	 */
	public void saveBitmapToPNG(BufferedImage bufferedImage, String dir, String name) {
		try {
			Date date = new Date(System.currentTimeMillis());
			String currentTime = date.getHours() + "-" + date.getMinutes() + "-" + date.getSeconds();
			cont++;

			String outputDir = dirOut;
			if(dir != null) {
				outputDir = dirOut + "/" + dir + "/";
			}

			File file = new File("./" + outputDir, currentTime + "_" + df.format(cont) + "_" + name + ".png");
			if(!file.exists())
				file.mkdirs();
			javax.imageio.ImageIO.write(bufferedImage, "PNG", file);

			//System.out.println("Imagem " + file.getName() + " salva!");
		}
		catch(Exception e) {
			System.out.println("Erro ao salvar a imagem!");
			System.out.println(e.getMessage());
		}
	}

	@Override
	public String createOutputDir() {
		if(fileInputID != null)
			return Config.TEST_OUTPUT_DIR + "/" + fileInputID + "/";
		else
			return Config.TEST_OUTPUT_DIR + "/";
	}

	//int
	/* (non-Javadoc)
	 * @see br.com.domm.ledblind.desktop.utils.IImageIO#savePixels(int[], int, int, java.lang.String, java.lang.String)
	 */
	@Override
	public void savePixels(int[] pixels, int width, int height, String dir, String name) {
		
//		if(true)
//			return;
		
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = bufferedImage.getRaster();

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				raster.setSample(x, y, 0, (pixels[x + y*width] >> 16) & 0xFF);
				raster.setSample(x, y, 1, (pixels[x + y*width] >> 8) & 0xFF);
				raster.setSample(x, y, 2, pixels[x + y*width] & 0xFF);
				raster.setSample(x, y, 3, (pixels[x + y*width] >> 24) & 0xFF);
			}
		}

		saveBitmapToPNG(bufferedImage, dir, name);
	}
	
	public void savePixels2(byte[] pixels, int width, int height, String dir, String name) {
		
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = bufferedImage.getRaster();

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				raster.setSample(x, y, 0, pixels[x + y*width]);
			}
		}

		saveBitmapToPNG(bufferedImage, dir, name);
	}

	//byte
	/* (non-Javadoc)
	 * @see br.com.domm.ledblind.desktop.utils.IImageIO#savePixels(byte[], int, int, java.lang.String, java.lang.String)
	 */
	@Override
	public void savePixels(byte[] pixels, int width, int height, String dir, String name) {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = bufferedImage.getRaster();

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				raster.setSample(x, y, 0, pixels[x + y*width] & 0xFF);
			}
		}

		saveBitmapToPNG(bufferedImage, dir, name);
	}

	/* (non-Javadoc)
	 * @see br.com.domm.ledblind.desktop.utils.IImageIO#savePixels(int[], int, int, int, int, int, java.lang.String, java.lang.String)
	 */
	@Override
	public void savePixels(int[] pixels, int x0, int y0, int x1, int y1, int width, String dir, String name) {
		BufferedImage bufferedImage = new BufferedImage(x1-x0+1, y1-y0+1, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = bufferedImage.getRaster();

		for(int y = y0; y <= y1; y++) {
			for(int x = x0; x <= x1; x++) {
				raster.setSample(x, y, 0, pixels[x + y*width] & 0xFF);
			}
		}

		saveBitmapToPNG(bufferedImage, dir, name);
	}

	/* (non-Javadoc)
	 * @see br.com.domm.ledblind.desktop.utils.IImageIO#savePixels(byte[], int[], int, int, int, int, int, int, int, java.lang.String, java.lang.String)
	 */
	@Override
	public void savePixels(byte[] pixels, int[] map, int x0, int y0, int x1, int y1,
			int width, int id1, int id2, String dir, String name) {
		BufferedImage bufferedImage = new BufferedImage(x1-x0+1, y1-y0+1, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = bufferedImage.getRaster();

		int x_y;

		for(int x = x0; x <= x1; x++) {
			for(int y = y0; y <= y1; y++) {
				x_y = x + y*width;

				if(map[x_y] >= id1 && map[x_y] <= id2) {
					raster.setSample(x, y, 0, (pixels[x_y] & 0xFF));
				}
				else {
					raster.setSample(x, y, 0, 0xFF);
				}
			}
		}

		saveBitmapToPNG(bufferedImage, dir, name);
	}

	public static File openFile(final String extension, File dir) {
		FileFilter filter = new FileFilter() {
			@Override
			public String getDescription() {
				return extension;
			}

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(extension);
			}
		};

		JFileChooser jc = new JFileChooser(dir);
		jc.setFileFilter(filter);
		jc.setDialogType(JFileChooser.OPEN_DIALOG);

		int opt = jc.showDialog(null, "Abrir");

		if(opt != JFileChooser.APPROVE_OPTION)
			return null;

		return jc.getSelectedFile();
	}

	public static IImageIO createImageIO(String imagePath) {
		return new ImageIODesktop(imagePath);
	}
}
