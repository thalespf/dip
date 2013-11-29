package com.thalespf.dip.util;

import java.io.File;

public class Config {
	public static final String PHOTOS_DIR = "/photos";
	public static final String DICTIONARY_DIR = "/dictionary";
	public static String TEST_OUTPUT_DIR = "/test";

	public static String lettersDir = "/letters";
	public static File photosDir = new File(PHOTOS_DIR);
	public static File networksDir = new File("/networks");
	public static File dictionaryDir = new File("/dictionary");

	public static File rootDir = new File(".");
}
