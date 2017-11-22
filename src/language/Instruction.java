package language;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Instruction {
	
	private static List<SpecialRegister> specialRegisterBit = parseSFR("C:\\Users\\Juan\\Desktop\\Lenguajes\\Asm-SR.txt",0);
	private static List<SpecialRegister> specialRegisterByte = parseSFR("C:\\Users\\Juan\\Desktop\\Lenguajes\\Asm-SR.txt",1);
	//Necessary*************
	public String instr;
	public String code;
	public int bytes;
	public String instrName; // to have the name of the instruction example: MOV A,#01 instrName = MOV ,instr MOV A,##
	//*************************************************************************
	
	public Instruction(){
		this.instr="";
		this.code="";
		this.bytes=0;
	}

	public Instruction(String instr, String code, int bytes) {
		this.instr=instr;
		this.code=code;
		this.bytes=bytes;
		// initialize the values with the default that is 555

		int i=0;
		this.instrName="";
		while(instr.charAt(i)!=' '){
			this.instrName+=instr.charAt(i);
			i++;
		}
	}
	
	private static List<SpecialRegister> parseSFR(String ruta, int opc) { //opc decide si va a filtrar los bits o bytes. bits = 0, bytes = 1.
		ArrayList<SpecialRegister> array = new ArrayList<>();
		try(Scanner in = new Scanner(new File(ruta))){
			while(in.hasNextLine()) {
				String []ics = in.nextLine().split("-");
				if(opc == 0 && ics[2].equals("0")) {
					array.add(new SpecialRegister(ics[0],ics[1],ics[2]));
				}
				else if(opc == 1 && ics[2].equals("1")) {
					array.add(new SpecialRegister(ics[0],ics[1],ics[2]));
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return array;
	}
	
	//*********************
	// "&" REL
	// "$" BIT
	// "%" DIR
	// "#+" DATOS
	/**
	 * @param tokens: instrucción filtrada en tokens
	 * @return si lo que espera la instrucción tentativamente corresponde a la instrucción tokenizada
	 * */
	public LineInstruction isThisInstruction(String []tokens, String instruction, int numLine){
		//"ADD A,#34H"
		ArrayList<String> transiciones = new ArrayList<String>();
		boolean flag = true;
		LineInstruction li = new LineInstruction(this,instruction,numLine,false);
				
		transiciones.addAll(Arrays.asList(this.instr.split(" |,"))); //Transiciones esperadas (operandos)
		if( tokens.length != transiciones.size() )
			return null;
		
		for(int i=0;i<transiciones.size();i++) {
			if( transiciones.get(i).equals("&") || transiciones.get(i).equals("$") || transiciones.get(i).equals("%") || transiciones.get(i).equals("#+") ) { //Omite la comparación directa cuando se trata de &,$,%,+
				li.getDefinitions().add(transiciones.get(i));
				li.getProvided().add(tokens[i]);
				li.setNeedsResolution(true);
			}
			else {
				flag &= tokens[i].equals(transiciones.get(0));
			}
			
			if( !flag )
				return null;
		}
		
		
		return li;
	}
	
	public String toString(){
		return String.format("[Name: %s Code: %s Number of bytes: %s]", this.instrName,this.code,this.bytes);
	}
	
	public boolean canSolveSymbols(LineInstruction is) {
		boolean killme = true;
		for( int i=0; i<is.getDefinitions().size(); i++ ) {
			if( is.getDefinitions().get(i).equals("&") ) {
				killme &= this.isRelative(is.getProvided().get(i));
			}
			else if( is.getDefinitions().get(i).equals("$") ) {
				killme &= this.isBit(is.getProvided().get(i));
			}
			else if( is.getDefinitions().get(i).equals("#+") ) {
				killme &= this.isDirect(is.getProvided().get(i));
			}
			else if( is.getDefinitions().get(i).equals("%") ) {
				killme &= this.isInmediate(is.getProvided().get(i));
			}
		}
		
		return killme;
	}
	
	// REL &
	public boolean isRelative(String s){
		// example: JNZ ETIQUETA1
		return s.matches("^[a-zA-Z0-9_-]");
	}
	
	
	public boolean isBit(String s){
		
		
		return false;
	}
	
	public boolean isDirect(String str) {
		return false;
	}
	
	public boolean isInmediate(String str) {
		return false;
	}

}
