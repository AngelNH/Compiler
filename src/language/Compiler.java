package language;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import language.symbols.StringParser;

public class Compiler {
	public static void main(String[] args) {
		String input="";
		int sizeN=0;
		
		ArrayList<Instruction> summary = new ArrayList<Instruction>(); //Lista de prototipo de instrucciones
		ArrayList<String> names = new ArrayList<String>(); //Lista de los nombres de los prototipos de las intrucciones
		ArrayList<String> program = new ArrayList<String>(); //El programa del archivo ensamblador
		ArrayList<String> errores = new ArrayList<String>();
		
		ArrayList<LineInstruction> unresolvedInstructions = new ArrayList<LineInstruction>();
		ArrayList<LineInstruction> resolvedInstructions = new ArrayList<LineInstruction>();
		ArrayList<LineInstruction> instructions = new ArrayList<>();
		
		// C:\\Users\\inqui\\OneDrive\\Documentos\\ITESO\\5 Semestre\\Lenguajes Formales\\Asm-Instr.txt
		File diccionario = new File("C:\\Users\\Juan\\Desktop\\Lenguajes\\Asm-Instr.txt");
		File programa = new File("C:\\Users\\Juan\\Documents\\Keil uVision Projects\\Ejem 1\\Prueba.a51");
	
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
			System.out.println("Se ha cargado el diccionario con �xito.");
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		try(Scanner in = new Scanner(programa)) {
			//in = new Scanner(file);
			while(in.hasNext()){
				String line = in.nextLine();
				if( !StringParser.ignoreLine(line) )
					program.add(line);
			}
			System.out.println("Se ha cargado el programa con �xito.");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		ListIterator<String> ite = program.listIterator();
		
		String lineaActual;
		String []tokens;
		List<Instruction> candidatos;
		boolean flag = false;
		
		int nextAddress = 0;
		while(ite.hasNext()) { //Crea tabla de s�mbolos
			lineaActual = ite.next();
			String tag = StringParser.hasTag(lineaActual);
			if( tag != null ) {
				lineaActual = lineaActual.substring(lineaActual.indexOf(':')+1);
			}
			
			tokens = StringParser.getTokens(lineaActual);
			try {
				candidatos = summary.subList(names.indexOf(tokens[0]), names.lastIndexOf(tokens[0])+1); //si est� bien, tokens[0] deber�a ser una instrucci�n
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

						break; //Este ISA s�lo tiene una respuesta correcta 
					}
				}
				if( !flag ) {
					errores.add(lineaActual);
				}
			}
			catch(IndexOutOfBoundsException | IllegalArgumentException ex) {
				errores.add(lineaActual);
			}
		}
		
		if( !errores.isEmpty() ) //Para qu� continuar si hay de todas formas hay errores
			return;
		
		if( !unresolvedInstructions.isEmpty() ) { //hay que resolver la tabla de s�mbolos
			for( int i=0; i<unresolvedInstructions.size(); i++) {
				if( unresolvedInstructions.get(i).getInstruction().canSolveSymbols(unresolvedInstructions.get(i)) ) {
					//mandar a llamar a la de resolver
					unresolvedInstructions.get(i).getInstruction().solveInstruction(unresolvedInstructions.get(i), null);
					resolvedInstructions.add(unresolvedInstructions.remove(i));
				}
				else
				{
					errores.add(unresolvedInstructions.get(i).getLinea());
				}
			}
		}
	}
}
