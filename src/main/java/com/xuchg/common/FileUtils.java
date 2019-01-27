package com.xuchg.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

import com.xuchg.window.MainWindow;

/**
 * 文件操作工具类
 * @author xuchg
 *
 */
public class FileUtils {

	/**
	 * 读文件
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static String readFile(File file) throws IOException{
		StringBuilder result = new StringBuilder("");
		Charset charset = Charset.forName(MainWindow.charset);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),charset));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if(StringUtils.isNotBlank(result.toString())){
				result.append("\n");
			}
			result.append(line);
		}
		reader.close();
		return result.toString();
	}
	
	/**
	 * 写文件
	 * @param file
	 * @param lines
	 * @throws IOException
	 */
	public static void writeFile(File file,String lines) throws IOException{
		Charset charset = Charset.forName(MainWindow.charset);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),charset));
		String[] lineArr = lines.split("\n");
		for(int i=0;i<lineArr.length;i++){
			writer.write(lineArr[i]);
			if(i < lineArr.length - 1){
				writer.newLine();
			}
		}
		writer.flush();
		writer.close();
	}
	
	/**
	 * @param file
	 * @param lines
	 * @param charset
	 * @throws IOException
	 */
	public static void writeFile(File file,String lines,Charset charset) throws IOException{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),charset));
		String[] lineArr = lines.split("\n");
		for(int i=0;i<lineArr.length;i++){
			writer.write(lineArr[i]);
			if(i < lineArr.length - 1){
				writer.newLine();
			}
		}
		writer.flush();
		writer.close();
	}
}
