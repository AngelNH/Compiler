package language;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Compiler {
	public static void main(String[] args) {
		int i=0;
		String input="";
		String inst="";
		String code="";
		String size="";
		int sizeN=0;
		Scanner in = new Scanner(System.in);
		ArrayList<Instruction> summary = new ArrayList<Instruction>();
		ArrayList<String> names = new ArrayList<String>();//Missing know which the collection
		ArrayList<String> summaryFinder = new ArrayList<String>();
		
		
		File file = new File("C:\\Users\\inqui\\OneDrive\\Documentos\\ITESO\\5 Semestre\\Lenguajes Formales\\Asm-Instr.txt");
		System.out.println("Inicia carga del diccionario...");
		
		
		try {
			in = new Scanner(file);
			i=0;
			while(in.hasNext()){
				i=0;
				inst="";
				code="";
				size="";
				input = in.nextLine();
				
				String[] ics = input.split("-");
				//System.out.println("Name: "+ics[0]);
				//System.out.println("Code: "+ics[1]);
				//System.out.println("Size: "+ics[2]);
				//check mistakes of charge
				
				sizeN=Integer.parseInt(ics[2]);
				Instruction inns= new Instruction(ics[0],ics[1],sizeN);
				summary.add(inns);
				names.add(inns.instrName);
				summaryFinder.add(ics[0]);
				
				System.out.println(input);
				//System.out.println();
			}
			System.out.println("Se ha cargado el diccionario con éxito.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//Now we have been charged the dictionary
		// start reading the codes.
		//String text="ADD A,#34H";
		String text = "INC A";
		Code c1 = new Code(text);
		int x=0;
		int index=0;
		//first check if it´s in the array (non variable instructions)
		x=c1.getIndex(summaryFinder);
		c1.code=summary.get(x).code;
		//test (erase)
		c1.dir="54";
		c1.datos="AF";
		if(x!=500){
			System.out.println("Codigo de operación de "+text+ " es :"+c1.getCode());
			System.out.println(summary.get(x).toString());
		}else if(isAnInstruction(text,summary)){//now for variable instructions example: MOV A,#30
			System.out.println("Es una instruccion variable");
		}
		System.out.println("Terminó la ejecuccion");
		//tests
		
	}
	
	public static boolean isAnInstruction(String text,ArrayList<Instruction> summary){
		boolean state = false;
		Iterator<Instruction> it= summary.iterator();
		while(it.hasNext()){
			Instruction actual =it.next();
			if(actual.isOne(text) && actual.isThisInstruction(text)){
				state= true;
				
				break;
			}else{
				state= false;
			}
		}
		return state;
	}
}
