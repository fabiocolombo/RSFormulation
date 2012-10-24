package util;

import java.io.File;

public class FileUtils {
	public static String getBasename(File file){
		return file.getPath().substring(file.getPath().lastIndexOf(File.pathSeparator)+1);
	}
}
