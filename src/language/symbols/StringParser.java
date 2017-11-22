package language.symbols;

import java.util.ArrayList;
import java.util.Arrays;

public class StringParser {
	
	public static boolean ignoreLine(String line) {
		line = line.trim();
		
		if( line.isEmpty() ) //ignorar si la la línea está vacía
			return true;
		
		if( line.charAt(0)==';' ) //ignorar si la línea es un un comentario
			return true;
		
		return false;
	}
	
	public static String []getTokens(String line){
		ArrayList<String> str = new ArrayList<>();
		String temp;
		String []array;
		
		
		for(String elem : line.split(" ")) { //Obtiene todas las palabras separadas por espacios
			temp = elem.trim();
			if( !elem.isEmpty() )
				str.add(temp);
		}
		
		for(int i=0; i<str.size(); i++) { //Si hay comas, las agrega como tokens
			temp = str.get(i);
			array = temp.split("((?<=,)|(?=,))");
			if( array.length > 1) {
				str.remove(i);
				str.addAll(i,Arrays.asList(temp.split("((?<=,)|(?=,))")));
				i += array.length-1;
			}
		}
		
		return str.toArray(new String[0]);
	}
}
