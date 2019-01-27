package com.xuchg.window;

import java.io.File;
import java.util.List;

import com.sun.javafx.webkit.WebConsoleListener;
import com.xuchg.common.Constant;
import com.xuchg.controller.NoteController;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class MainWindow extends Application {

		private double minWidth = 850.00;
		private double minHeight = 550.00;
		
		private double width = 900;
		private double height = 800;

		public static Stage stage;

		private static NoteController noteController = null;

		public static WebView webView;
		public static WebEngine webEngine;
		
		public static String charset = Constant.Charset.GB2312;
		
		public static JSObject jsObject = null;
		
		public static String fileName = "";
		
		public static Boolean newFile = true;
		
		public static String oldValue = "";
		
		public static File oldFile = null;
		
		public static Boolean save = true;

		@SuppressWarnings("static-access")
		@Override
		public void start(Stage arg0) throws Exception {
			StackPane layout = new StackPane();//布局
			layout.setId("main-view");
			layout.getStylesheets().add("/pages/css/main-view.css");

			noteController = new NoteController();
			stage =  new Stage();
			layout.getChildren().add(getBodyView());
			Scene scene = new Scene(layout,width,height);
			stage.setScene(scene);
			stage.setMinHeight(minHeight);
			stage.setMinWidth(minWidth);
			stage.getIcons().add(new Image("/pages/images/app01.png"));
			stage.centerOnScreen();
			stage.setTitle("MDNotePad");
			stage.show();
			stage.setOnCloseRequest((event)->{
				if(event.getEventType().equals(event.WINDOW_CLOSE_REQUEST)){
					Boolean exec = noteController.checkEdit("未保存，是否退出？");
					if(!exec){
						event.consume();
					}
				};
			});
		}

		public WebView getBodyView() {

			webView = new WebView();
			webView.setCache(false);
			
			webView.setOnDragDropped((event) -> {
				event.getTarget();
				// 修改鼠标样式
				List<File> files = event.getDragboard().getFiles();
				
				if(files.size() == 1){
					File curFile = files.get(0);
					noteController.openFile(curFile);
				}
			});
			
			webEngine = webView.getEngine();
			webView.setContextMenuEnabled(true);
			webEngine.setJavaScriptEnabled(true);
			webView.setFontSmoothingType(FontSmoothingType.GRAY);
			webEngine.load(MainWindow.class.getResource("/pages/index.html").toExternalForm());

			//设置数据目录
			String basePath = System.getProperty("user.home");
			String dataPath = basePath + "/.MDNotePad/temp";
			existsFile(dataPath);
			webEngine.setUserDataDirectory(new File(dataPath));

			//监听事件
			Worker<Void> woker = webEngine.getLoadWorker();
			woker.stateProperty().addListener((obs, oldValue, newValue) -> {
				if (newValue == Worker.State.SUCCEEDED) {
					jsObject = (JSObject) webEngine.executeScript("window");
					jsObject.setMember("noteController", noteController);
				}
			});

			//页面异常事件
			woker.exceptionProperty().addListener((ObservableValue<? extends Throwable> ov, Throwable t0, Throwable t1) -> {
				System.out.println("Received Exception: " + t1.getMessage());
			});

			//控制台监听事件
			WebConsoleListener.setDefaultListener((WebView curWebView, String message, int lineNumber, String sourceId) -> {
				if (message.contains("ReferenceError: Can't find variable")) {
					//                webEngine.reload();
				}
				System.out.println("Console: [" + sourceId + ":" + lineNumber + "] " + message);
			});

			return webView;
		}

		public static void existsFile(String filePath) {
			File file = new File(filePath);
			if (!file.exists()) {
				file.mkdir();
			}
		}
}
