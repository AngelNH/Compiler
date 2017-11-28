package language;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import language.symbols.StringParser;

public class Instruction {
	
	private static List<SpecialRegister> specialRegisterBit = parseSFR(Instruction.class.getResource("Asm-SR.txt").getPath(),1);
	private static List<SpecialRegister> specialRegisterByte = parseSFR(Instruction.class.getResource("Asm-SR.txt").getPath(),0);
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
	public LineInstruction isThisInstruction(String []tokens, String instruction, int numLine, int address){
		//"ADD A,#34H"
		ArrayList<String> transiciones = new ArrayList<String>();
		boolean flag = true;
		LineInstruction li = new LineInstruction(this,instruction,numLine,false,address);
				
		transiciones.addAll(Arrays.asList(StringParser.getTokens(this.instr))); //Transiciones esperadas (operandos)
		if( tokens.length != transiciones.size() )
			return null;
		
		for(int i=0;i<transiciones.size();i++) {
			if( transiciones.get(i).equals("&") || transiciones.get(i).equals("$") || transiciones.get(i).equals("%") || transiciones.get(i).equals("#+") ) {
				li.getDefinitions().add(transiciones.get(i));
				li.getProvided().add(tokens[i]);
				li.setNeedsResolution(true);
				flag &= li.getInstruction().canSolveSymbols(li);
			}
			else {
				flag &= tokens[i].equalsIgnoreCase(transiciones.get(i));
			}
			
			if( !flag )
				return null;
		}
		
		solveFixedInstruction(li);
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
	
	private void solveFixedInstruction(LineInstruction li) {
		String hex = li.getInstruction().code;
		if( !li.isNeedsResolution() )
			li.setHex(hex);
	}
	
	public void solveInstruction(LineInstruction li, List<LineInstruction> others) throws Exception {
		String theHex=""+code;
		String []especial = new String[2];
		if( bytes > 1 ) {//significa que hay que ir metiendo los operandos resueltos
			for( int i = 0; i < li.getProvided().size(); i++ ) {
				if( li.getDefinitions().get(i).equals("&") ) {
					String temp = solveRelative(li.getProvided().get(i),li,others);
					if( temp == null )
						throw new Exception("No encontrado");
					else {
						theHex += temp;
					}
				}
				else if( li.getDefinitions().get(i).equals("$") ) {
					theHex += solveBit(li.getProvided().get(i));
				}
				else if( li.getDefinitions().get(i).equals("#+") ) {
					theHex += solveDirect(li.getProvided().get(i));
				}
				else if( li.getDefinitions().get(i).equals("%") ) {
					if( li.getInstruction().code.equalsIgnoreCase("85") ){
						especial[i] = solveInmediate(li.getProvided().get(i));
					}
					else
						theHex += solveInmediate(li.getProvided().get(i));
				}
			}
		}
		
		li.setNeedsResolution(false);
		if( li.getInstruction().code.equalsIgnoreCase("85") ) {
			li.setHex(theHex+especial[1]+especial[0]);
		}else if( !li.getInstruction().instrName.equalsIgnoreCase("ajmp") && !li.getInstruction().instrName.equalsIgnoreCase("ljmp") 
				&& !li.getInstruction().instrName.equalsIgnoreCase("acall") && !li.getInstruction().instrName.equalsIgnoreCase("lcall") ){
			li.setHex(theHex); //Esas instrucciones requieren un procesamiento especial
		}
	}
	
	// REL &
	public boolean isRelative(String s){
		
		return s.matches("^[a-zA-Z_][a-zA-Z_0-9]*");
	}
	
	public String solveRelative(String provided, LineInstruction current, List<LineInstruction> others) {
		int direccion = 0;
		
		if( current.getInstruction().instrName.equalsIgnoreCase("ajmp") || current.getInstruction().instrName.equalsIgnoreCase("acall") ) { //Procesamiento individual de 
			for( LineInstruction li : others ) { //%[argument_index$][flags][width][.precision]conversion
				if( li.getTag() != null && li.getTag().equalsIgnoreCase(provided) ) {
					byte mask1 = (byte) Integer.parseInt(current.getInstruction().code,16);
					byte mask2 = (byte) li.getAddress();
					byte mask3 = (byte) (mask2>>8);
					mask3 &= 0x07;
					mask3 <<= 5; //parte alta de la dirección
					mask1 &= 0x1F;
					mask1 = (byte) (mask1 | mask3); //primer byte
					mask2 = (byte) (mask2 & 0xFF);
					current.setHex(String.format("%02X%02X",mask1,mask2));
					return "";
				}
			}
		}
		else if( current.getInstruction().instrName.equalsIgnoreCase("ljmp") || current.getInstruction().instrName.equalsIgnoreCase("lcall") ) {
			for( LineInstruction li : others ) { //%[argument_index$][flags][width][.precision]conversion
				if( li.getTag() != null && li.getTag().equalsIgnoreCase(provided) ) {
					current.setHex(String.format("%02X%04X",Byte.parseByte(current.getInstruction().code,16),li.getAddress()));
					return "";
				}
			}
		}
		
		for( LineInstruction li : others ) { //%[argument_index$][flags][width][.precision]conversion
			if( li.getTag() != null && li.getTag().equalsIgnoreCase(provided) ) {
				direccion = (li.getAddress())-(current.getAddress()+current.getInstruction().bytes);
				return String.format("%08X", direccion).substring(6, 8);
			}
		}
		
		return null;
	}
	
	public boolean isBit(String provided){
		for(SpecialRegister sr : specialRegisterBit) {
			if( provided.matches(String.format("(?i)(%s)[.0-7]??",sr.symbol)) ) {
				return true;
			}
		}
		
		try{
			int test=-1;
			int base = 0;
			if( provided.endsWith("H") ){
				base = 16;
			}else if( provided.endsWith("B") ){
				base = 2;
			}else if( provided.endsWith("D") ){
				base = 10;
			}
			
			test = Integer.parseUnsignedInt(provided.substring(0,provided.length()-1), base);
			if( test > -1 && test < 256 )
				return true;
			
			return false;
		}
		catch(NumberFormatException ex) {
			return false;
		}
	}
	
	public String solveBit(String provided) {
		for(SpecialRegister sr : specialRegisterBit) {
			if( provided.matches(String.format("(?i)(%s)[.0-7]??",sr.symbol)) ) {
				int suma=0;
				if(provided.matches(String.format("(?i)(%s)[.0-7]{1}",sr.symbol))){
					
					suma = Integer.parseInt(provided.substring(provided.lastIndexOf(".")+1));
				}
				
				int base = sr.iDir;
				return Integer.toHexString(base+suma);
			}
		}
		
		int base = 0;
		if( provided.endsWith("H") ){
			base = 16;
		}else if( provided.endsWith("B") ){
			base = 2;
		}else if( provided.endsWith("D") ){
			base = 10;
		}
		
		return String.format("%02X",Integer.parseUnsignedInt(provided.substring(0,provided.length()-1), base));
	}
	
	public boolean isDirect(String str) {
		return str.matches("(?i)[#0-9a-f]+?.[hbd]");
	}
	
	public String solveDirect(String provided) {
		int entero = 0;
		provided = provided.toLowerCase();
		provided = provided.substring(1);
		if( provided.matches(".^[hbd]") ) {
			entero = Integer.parseInt(provided, 16);
		}
		else if( provided.endsWith("h") ) {
			entero = Integer.parseInt(provided.substring(0, provided.indexOf("h")), 16);
		}
		else if( provided.endsWith("b") ) {
			entero = Integer.parseInt(provided.substring(0, provided.indexOf("b")), 2);
		}
		else if( provided.endsWith("d") ) {
			entero = Integer.parseInt(provided.substring(0, provided.indexOf("d")), 10);
		}
		
		return String.format("%02X",entero);
	}
	
	public boolean isInmediate(String str) {
		List<SpecialRegister> allRegisters = new ArrayList<>();
		allRegisters.addAll(specialRegisterBit);
		allRegisters.addAll(specialRegisterByte);
		
		for( SpecialRegister sr : allRegisters ) {
			if( sr.symbol.equalsIgnoreCase(str) )
				return true;
		}
		
		return str.matches("(?i)[[0-9]+?].[hbd]");
	}
	
	public String solveInmediate(String provided) {
		int entero = 0;
		provided = provided.toLowerCase();
		if( provided.matches(".^[hbd]") ) {
			entero = Integer.parseInt(provided, 16);
		}
		else if( provided.endsWith("h") ) {
			entero = Integer.parseInt(provided.substring(0, provided.indexOf("h")), 16);
		}
		else if( provided.endsWith("b") ) {
			entero = Integer.parseInt(provided.substring(0, provided.indexOf("b")), 2);
		}
		else if( provided.endsWith("d") ) {
			entero = Integer.parseInt(provided.substring(0, provided.indexOf("d")), 10);
		}
		else { //Busca en los registros
			List<SpecialRegister> allRegisters = new ArrayList<>();
			allRegisters.addAll(specialRegisterBit);
			allRegisters.addAll(specialRegisterByte);
			
			for( SpecialRegister sr : allRegisters ) {
				if( sr.symbol.equalsIgnoreCase(provided) ) {
					entero = sr.iDir;
				}
			}
		}
		
		return Integer.toHexString(entero);
	}

}
