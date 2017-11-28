package language;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import javax.swing.plaf.synth.SynthSeparatorUI;

import language.symbols.StringParser;

public class Compiler {
	
	private static File diccionario = new File(Compiler.class.getResource("Asm-Instr.txt").getPath());
	
	public static List<LineInstruction> compile(File programa, List<Integer> errorBuffer) {
		ArrayList<Instruction> summary = new ArrayList<Instruction>(); //Lista de prototipo de instrucciones
		ArrayList<String> names = new ArrayList<String>(); //Lista de los nombres de los prototipos de las intrucciones
		ArrayList<String> program = new ArrayList<String>(); //El programa del archivo ensamblador
		//ArrayList<String> errores = new ArrayList<>();
		
		ArrayList<LineInstruction> unresolvedInstructions = new ArrayList<LineInstruction>();
		ArrayList<LineInstruction> resolvedInstructions = new ArrayList<LineInstruction>();
		ArrayList<LineInstruction> instructions = new ArrayList<>();
		
		String input="";
		int sizeN=0;
		
		try(Scanner in = new Scanner(diccionario)) {
			//in = new Scanner(file);
			while(in.hasNext()){
				input = in.nextLine();
				
				String[] ics = input.split("-");
				
				sizeN=Integer.parseInt(ics[2]);
				Instruction inns= new Instruction(ics[0],ics[1],sizeN);
				summary.add(inns);
				names.add(inns.instrName);
				
				System.out.println(input);
			}
			System.out.println("Se ha cargado el diccionario con éxito.");
		} 
		catch (FileNotFoundException e) {
			//e.printStackTrace();
			errorBuffer.add(-2); //-2 significa file not found
			return instructions;
		}
		
		try(Scanner in = new Scanner(programa)) {
			//in = new Scanner(file);
			while(in.hasNext()){
				program.add(in.nextLine());
			}
			System.out.println("Se ha cargado el programa con éxito.");
		}
		catch (FileNotFoundException e) {
			//e.printStackTrace();
			errorBuffer.add(-2); //-2 significa file not found
			return instructions;
		}
		
		boolean flag = false;
		String lineaActual;
		String []tokens;
		List<Instruction> candidatos;
		ListIterator<String> ite = program.listIterator();
		
		int nextAddress = 0;
		while(ite.hasNext()) { //Crea tabla de símbolos
			lineaActual = ite.next();
			if( StringParser.ignoreLine(lineaActual) ) {	continue;	}
			
			String tag = StringParser.hasTag(lineaActual);
			if( tag != null ) {
				lineaActual = lineaActual.substring(lineaActual.indexOf(':')+1);
			}
				
			lineaActual = lineaActual.toUpperCase();
			lineaActual = StringParser.cleanInstruction(lineaActual);
			tokens = StringParser.getTokens(lineaActual);
			try {
				candidatos = summary.subList(names.indexOf(tokens[0]), names.lastIndexOf(tokens[0])+1); //si está bien, tokens[0] debería ser una instrucción
				flag = false;
				for(int i=0;i<candidatos.size();i++) {
					LineInstruction li = candidatos.get(i).isThisInstruction(tokens, lineaActual, ite.nextIndex()-1,nextAddress);
					if(li != null)
					{
						li.setTag(tag);
						nextAddress += li.getInstruction().bytes;
						instructions.add(li);
						flag |= true;
						if( li.isNeedsResolution() ) {
							unresolvedInstructions.add(li);
						}else {
							resolvedInstructions.add(li);
						}

						break; //Este ISA sólo tiene una respuesta correcta 
					}
				}
				if( !flag ) {
					errorBuffer.add(ite.nextIndex());
				}
			}
			catch(IndexOutOfBoundsException | IllegalArgumentException ex) {
				errorBuffer.add(ite.nextIndex());
			}
		}
		
		/*if( !errorBuffer.isEmpty() ) //Para qué continuar si hay de todas formas hay errores
			return instructions;*/
		
		if( !unresolvedInstructions.isEmpty() ) { //hay que resolver la tabla de símbolos
			for( int i=0; i<unresolvedInstructions.size(); i++) {
				if( unresolvedInstructions.get(i).getInstruction().canSolveSymbols(unresolvedInstructions.get(i)) ) {
					//mandar a llamar a la de resolver
					try{
						unresolvedInstructions.get(i).getInstruction().solveInstruction(unresolvedInstructions.get(i), instructions);
					}
					catch(Exception ex) {
						errorBuffer.add(unresolvedInstructions.get(i).getNumLinea()+1);
					}
					resolvedInstructions.add(unresolvedInstructions.get(i));
				}
				else
				{
					errorBuffer.add(unresolvedInstructions.get(i).getNumLinea()+1);
				}
			}
		}
		
		instructions.sort((LineInstruction l1, LineInstruction l2)->Integer.compare(l1.getAddress(), l2.getAddress()));
		
		errorBuffer.sort(Integer::compare);
		return instructions;
	}
	
	public static String generateHex1(List<LineInstruction> line) {
		String rawLine = "";
		String val = "";
		int nextAddress = 0, len = 0;
		boolean check = true;
		
		for(LineInstruction li : line) {
			for( int i = 0; i < li.getHex().length(); i+=2 ) {
				len++;
				rawLine += li.getHex().substring(i, i+2);
				check = true;
				if( len > 15 ){
					val += String.format(":%02X%04X00%s%s\n",len,nextAddress,rawLine,checkSum(String.format(":%02X%04X00%s",len,nextAddress,rawLine)));
					nextAddress += len;
					len = 0;
					rawLine = "";
					check = false;
				}
			}
		}
		
		if( check ) {
			val += String.format(":%02X%04X00%s%s\n",len,nextAddress,rawLine,checkSum(String.format(":%02X%04X00%s",len,nextAddress,rawLine)));
		}
		
		val += ":00000001FF\n";
		
		return val.toUpperCase();
	}
	
	public static String generateHex(List<LineInstruction> line) {
		generateHex1(line);
		String hex="";
		String direction="0000";
		String hexline = direction+"00";
		int eol =0;
		int pc = 0;

		for(LineInstruction c : line){
			//System.out.println("Instruccion: "+ c.instr);
			//System.out.println("Codigo: "+ c.getCode());
			//System.out.println("Bytes: "+ c.bytes);
			if(eol<16){
				hexline+=c.getHex();
				eol+=c.getInstruction().bytes;
				pc+=c.getInstruction().bytes;
			}else if(eol >= 16){
				//line full
				hexline = ":10"+hexline;
				hexline += checkSum(hexline);
				hex+=hexline+"\n";
				eol = 0;
				direction = "00"+ Integer.toHexString(pc);//just in case is only 2 numbers
				hexline = direction+"00";
				
				hexline+=c.getHex();
				eol+=c.getInstruction().bytes;
				pc+=c.getInstruction().bytes;
			}
		}
		//there are no instructions left
		if(eol==0) {
			hexline=":00000001FF";
		}else {
			hexline = ":0"+Integer.toHexString(eol)+hexline;
			hexline+= checkSum(hexline);
			hex+=hexline+"\n";
			hexline=":00000001FF";
		}
		hex+=hexline;
		return hex.toUpperCase();
	}

	private static String checkSum(String hexline) {
		String cs="";
		int checksum=0;
		int actual=1;
		String aux="";
		int x=0;
		boolean flag=true;
		
		while(hexline.length()!=actual){
			if(flag){//first digit
				aux=""+hexline.charAt(actual);
				flag=false;
				actual++;
			}else {// second digit
				flag=true;
				aux+=""+hexline.charAt(actual);
				actual++;
				//now we have it formed
				x=Integer.parseInt(aux, 16);//hex2decimal(aux);
				checksum+=x;
			}
		}
		
		checksum = ~checksum;
		checksum++;
		aux=Integer.toHexString(checksum);
		cs =""+aux.charAt(6)+aux.charAt(7);
		return cs;
	}
}
