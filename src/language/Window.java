package language;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class Window extends Application {
	File program = null;
	String salida = "";
	
	public void setSalida(String str){
		salida = str;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		Platform.setImplicitExit(true);
		stage.setOnCloseRequest((ae) -> {
			Platform.exit();
			System.exit(0);
		});

		GridPane grid = new GridPane();
		
		grid.setHgap(20);
		grid.setPrefSize(1500, 800);
		grid.setVgap(15);
		grid.setPadding(new Insets(15));

		Scene scene = new Scene(grid);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Button btLoad = new Button("Load");

		Button btSave = new Button("Save");

		Button btCompile = new Button("Compile");

		Label lb1 = new Label("Compiler");
		Label lb2 = new Label("Assembler");
		Label lb3 = new Label("Intel HEX-80");

		TextArea taSrc = new TextArea();
		TextArea taTgt = new TextArea();
		TextField tfError = new TextField();

		lb1.setStyle("-fx-font-size: 36px");
		lb2.setStyle("-fx-font-size: 24px");
		lb3.setStyle("-fx-font-size: 24px");

		btSave.setPrefWidth(130);
		btLoad.setPrefWidth(130);
		btCompile.setPrefWidth(130);
		taSrc.setPrefSize(730, 750);
		taTgt.setPrefSize(730, 750);
		taTgt.setStyle("-fx-font-size: 22px");
		taSrc.setEditable(false);
		taTgt.setEditable(false);
		tfError.setEditable(false);
		grid.add(lb1, 0, 0, 3, 1);
		grid.add(lb2, 0, 1, 2, 1);
		grid.add(lb3, 2, 1, 1, 1);
		grid.add(taSrc, 0, 2, 2, 1);
		grid.add(taTgt, 2, 2, 1, 1);
		grid.add(btCompile, 1, 4, 1, 1);
		grid.add(btLoad, 0, 4, 1, 1);
		grid.add(btSave, 2, 4, 1, 1);
		grid.add(tfError, 0, 3, 3, 1);

		GridPane.setHalignment(lb1, HPos.CENTER);
		GridPane.setHalignment(btSave, HPos.CENTER);
		GridPane.setHalignment(lb2, HPos.CENTER);
		GridPane.setHalignment(lb3, HPos.CENTER);
		GridPane.setHalignment(btLoad, HPos.LEFT);
		GridPane.setHalignment(btCompile, HPos.CENTER);
		stage.setScene(scene);
		stage.show();
		stage.setTitle("Compiler");
		stage.setResizable(false);

		btLoad.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				FileChooser.ExtensionFilter extFilterA51 = new FileChooser.ExtensionFilter("A51 files (*.a51)",
						"*.A51");
				FileChooser.ExtensionFilter extFilterASM = new FileChooser.ExtensionFilter("ASM files (*.asm)",
						"*.ASM");
				fileChooser.getExtensionFilters().addAll(extFilterA51, extFilterASM);
				program = fileChooser.showOpenDialog(null);
				String code = "";
				try {
					code = readFromFile(program);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				taSrc.setText(code);
			}
		});

		btCompile.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				List<LineInstruction> instructions = new ArrayList<LineInstruction>();
				List<Integer> errorBuffer = new ArrayList<Integer>();
				instructions = Compiler.compile(program, errorBuffer);
				//String temp = "";
				/*if(!errorBuffer.isEmpty()){
					for(LineInstruction li : instructions){
						temp += li.getHex80();
					}
				}*/
				if(errorBuffer.isEmpty()){
					salida = Compiler.generateHex1(instructions);
					taTgt.setText(salida);
				}
				else{
					tfError.setText("Error, revisar líneas " + errorBuffer);
				}
				//taTgt.setText(temp);
			}
		});

		btSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				FileChooser.ExtensionFilter extFilterHEX = new FileChooser.ExtensionFilter("HEX file (*.hex)",
						"*.hex");
				fileChooser.getExtensionFilters().addAll(extFilterHEX);
				fileChooser.setTitle("Save HEX");
				File file = fileChooser.showSaveDialog(stage);
				try {
					System.out.println(salida);
					writeToFile(file, salida);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	public static String readFromFile(File file) throws IOException {
		// Creamos la ruta al carchivo o directorio
		Path path = Paths.get(file.getAbsolutePath());
		// Abrir canal del Path para lecctura
		FileChannel inChannel = FileChannel.open(path, StandardOpenOption.READ);

		// Crear ByteBuffer
		ByteBuffer bb = ByteBuffer.allocate((int) file.length());
		int bytesRead = inChannel.read(bb);
		String code = "";
		bb.flip();
		while (bytesRead != -1) {
			while (bb.hasRemaining()) {
				code += ((char) bb.get());
			}
			bb.clear();
			bytesRead = inChannel.read(bb);
		}
		inChannel.close();
		return code;
	}

	public static void writeToFile(File file, String code) throws IOException {
		String []lineas = code.split(":");
		try(BufferedWriter asd = new BufferedWriter(new FileWriter(file))){
			for( String linea : lineas ){
				if( linea.length() > 0 ){
					asd.write(":"+linea);
					//asd.newLine();
				}
			}
		}
		
	}

	public static void main(String[] args) {
		launch(args);
	}

}