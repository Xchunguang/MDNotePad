package com.xuchg.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

import com.xuchg.common.Constant;
import com.xuchg.common.EncodingDetect;
import com.xuchg.common.FileUtils;
import com.xuchg.window.MainWindow;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

/**
 * 请求 
 * @author xuchg
 */
public class NoteController {
	
	private static Boolean editable = false;
	
	private static Boolean openLangeFile = false;

	/**
	 * 打开选择文件窗口
	 */
	public void toOpenFile(){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("打开文件");
		if(MainWindow.oldFile != null){
			fileChooser.setInitialDirectory(MainWindow.oldFile.getParentFile());
		}
		File file = fileChooser.showOpenDialog(MainWindow.stage);
		if(file != null){
			openFile(file);
		}
	}
	
	/**
	 * 保存前的检验
	 * @throws IOException 
	 */
	public void toSaveFile() throws IOException{
		String value = getValue();
		if(MainWindow.newFile && StringUtils.isNoneBlank(value)){//新文件且编辑过
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("保存文件");
			FileChooser.ExtensionFilter mdFilter = new FileChooser.ExtensionFilter("README files (*.md)", "*.md");
			FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
			FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("所有文件", "*.*");
			fileChooser.getExtensionFilters().add(mdFilter);
	        fileChooser.getExtensionFilters().add(txtFilter);
	        fileChooser.getExtensionFilters().add(allFilter);
			File file = fileChooser.showSaveDialog(MainWindow.stage);
			if(file != null){
				if(file.exists()){
					saveFile(value,file);
				}else{
					file.createNewFile();
					saveFile(value,file);
				}
			}
		}else if(!MainWindow.newFile && !value.equals(MainWindow.oldValue)){
			saveFile(value,MainWindow.oldFile);
		}else if(!MainWindow.newFile && !MainWindow.save){
			saveFile(value,MainWindow.oldFile);
		}
	}
	
	/**
	 * 保存文件
	 * @param create true表示新创建的文件，false表示旧文件保存
	 */
	public void saveFile(String value,File file){
		try {
			FileUtils.writeFile(file, value);
			MainWindow.oldFile = file;
			MainWindow.newFile = false;
			MainWindow.fileName = file.getName();
			MainWindow.oldValue = value;
			MainWindow.save = true;
			MainWindow.stage.setTitle(file.getName() + " - MDNotePad");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 打开文件
	 * @param file
	 */
	public void openFile(File file){
		try {
			//预测文件编码
			String encode = EncodingDetect.getJavaEncode(file.getAbsolutePath());
			MainWindow.charset = encode;
			MainWindow.jsObject.call("changeCodeDis", encode);
			
			long fileLength = file.length();
			//文件过大提醒
			if(fileLength > 1024000){
				openLangeFile = false;
				Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,"是否打开该大文本？");
				confirmation.setHeaderText("");
				confirmation.showAndWait().ifPresent(response ->{
					if(response == ButtonType.OK){
						openLangeFile = true;
					}
				});
				if(!openLangeFile){
					return ;
				}
			}
			
			String info = FileUtils.readFile(file);
			MainWindow.oldValue = info;
			MainWindow.fileName = file.getName();
			MainWindow.newFile = false;
			MainWindow.oldFile = file;
			MainWindow.save = true;
			MainWindow.stage.setTitle(file.getName() + " - MDNotePad");
			MainWindow.jsObject.call("initText", info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 切换编码，打开文件，无序判断文件大小
	 * @param file
	 */
	public void openFile(File file,String encode){
		try {
			//预测文件编码
			MainWindow.charset = encode;
			
			String info = FileUtils.readFile(file);
			MainWindow.oldValue = info;
			MainWindow.fileName = file.getName();
			MainWindow.newFile = false;
			MainWindow.oldFile = file;
			MainWindow.save = true;
			MainWindow.stage.setTitle(file.getName() + " - MDNotePad");
			MainWindow.jsObject.call("initText", info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 创建新文件
	 */
	public void createNewFile(){
		Boolean result = checkEdit("文件未保存，是否继续？");
		if(result){
			initFile();
		}
	}
	
	/**
	 * 检查是否编辑
	 */
	public String checkIfEdit(){
		String result = "n";
		String value = getValue();
		if(MainWindow.newFile){
			if(StringUtils.isNotBlank(value)){
				MainWindow.save = false;
				MainWindow.stage.setTitle("* 未保存 - MDNotePad");
				result = "y";
			}
		}else{
			if(!value.equals(MainWindow.oldValue)){
				MainWindow.save = false;
				MainWindow.stage.setTitle("* " + MainWindow.stage.getTitle());
				result = "y";
			}
		}
		return result;
	}
	
	
	/**
	 * 获取当前编辑器的值
	 * @return
	 */
	private String getValue(){
		String value = (String) MainWindow.jsObject.call("getValue");
		return value;
	}
	
	/**
	 * 初始化创建文件
	 */
	private void initFile(){
		MainWindow.fileName = "";
		MainWindow.newFile = true;
		MainWindow.charset = Constant.Charset.GB2312;
		MainWindow.oldValue = "";
		MainWindow.save = true;
		MainWindow.stage.setTitle("未保存 - MDNotePad");
		MainWindow.jsObject.call("initText", "");
	}
	
	/**
	 * 修改编码
	 * @param encode
	 */
	public void changeEncode(String encode){
		MainWindow.charset = encode;
		if(!MainWindow.newFile){//已经打开的文件，修改编码则需要重新打开文件
			openFile(MainWindow.oldFile,encode);
			MainWindow.save = false;
		}
	}
	
	/**
	 * 导出成html
	 * @throws IOException 
	 */
	public void downHtml() throws IOException{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("保存成HTML");
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html");
		FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("所有文件", "*.*");
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.getExtensionFilters().add(allFilter);
		File file = fileChooser.showSaveDialog(MainWindow.stage);
		if(file != null){
			String html = MainWindow.jsObject.call("getHtml", "").toString();
			String prefix = "<!DOCTYPE html><html><body><head> <meta charset='utf-8'/><title>MDNotePad</title></head>";
			String end = "</body></html>";
			html = prefix + html + end;
			Charset charset =  Charset.forName(Constant.Charset.UTF8);
			if(file.exists()){
				FileUtils.writeFile(file, html,charset);
			}else{
				file.createNewFile();
				FileUtils.writeFile(file, html,charset);
			}
		}
	}
	
	/**
	 * 退出
	 */
	public void exit(){
		Boolean exec = checkEdit("未保存，是否退出？");
		if(exec){
			MainWindow.stage.close();
		}
	}
	
	/**
     * 打开github
     * @throws URISyntaxException
     * @throws IOException
     */
    public void openGithub() throws URISyntaxException, IOException{
    	String url = "https://github.com/Xchunguang/MDNotePad";
    	System.setProperty("java.awt.headless", "false");
    	URI address = new URI(url);
    	Desktop d = Desktop.getDesktop();
    	d.browse(address);
    }
	
	/**
	 * 检查当前文件是否编辑过，并请用户判断是否继续
	 * 新建文件，文本内容不为空则属编辑态
	 * 打开文件，旧内容与新内容不同则属编辑态
	 * @return
	 */
	public Boolean checkEdit(String info){
		editable = false;
		String value = getValue();
		if((MainWindow.newFile && StringUtils.isNotBlank(value)) || (!MainWindow.newFile)&&!value.equals(MainWindow.oldValue)){
			Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,info);
			confirmation.setHeaderText("");
			confirmation.showAndWait().ifPresent(response ->{
				if(response == ButtonType.OK){
					editable = true;
				}
			});
		}else{
			editable = true;
		}
		return editable;
	}
	
}
