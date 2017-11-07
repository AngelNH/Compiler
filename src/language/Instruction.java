package language;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class Instruction {
	//Necessary*************
	public String instr;
	public String code;
	public int bytes;
	public String instrName; // to have the name of the instruction example: MOV A,#01 instrName = MOV ,instr MOV A,##
	//*************************************************************************
	public ArrayList<SpecialRegister> specialRBit;
	public ArrayList<SpecialRegister> specialRByte;
	
	public Instruction(){
		this.instr="";
		this.code="";
		this.bytes=0;
		this.specialRBit= new ArrayList<SpecialRegister>();
		this.specialRByte= new ArrayList<SpecialRegister>();
		this.LoadSpecialRegisters();
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
			this.specialRBit= new ArrayList<SpecialRegister>();
			this.specialRByte= new ArrayList<SpecialRegister>();
			this.LoadSpecialRegisters();
	}
	public void LoadSpecialRegisters(){//this one maybe outside checkagain
		Scanner in;
		String input;
		SpecialRegister sr;
		int i=0;
		File file = new File("C:\\Users\\inqui\\OneDrive\\Documentos\\ITESO\\5 Semestre\\Lenguajes Formales\\Asm-SR.txt");
		System.out.println("Inicia carga de registros especiales ...");
		
		try {
			in = new Scanner(file);
			i=0;
			while(in.hasNext()){
				i=0;
				input = in.nextLine();
				String[] ics = input.split("-");
				sr = new SpecialRegister(ics[0],ics[1],ics[2]);
				if(ics[2].equals("0")){//save the non bit directional in SRbyte
					this.specialRByte.add(sr);
				}else{//save the bit directional in SRbit
					this.specialRBit.add(sr);
				}
				
				System.out.println(input);
				//System.out.println();
			}
			System.out.println("Se han cargado los registros escpeciales con éxito.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isOne(String s){
		int i=0;
		String name = "";
		while(s.charAt(i)!= ' '){
			name+=s.charAt(i);
			i++;
		}
		if(name.equals(this.instrName))return true;
		return false;
	}
	
	public boolean waitsOriginBit(){
		return true;
	}
	//*********************
	// "&" REL
	// "$" BIT
	// "%" DIR
	// "+" DATOS
	public boolean isThisInstruction(String s){
		//"ADD A,#34H"
		//TODO check the instruction and see that it follows the instruction kind
		String get="";
		int i=0;
		while(this.instr.charAt(i)!=' '){
			get+= this.instr.charAt(i);//get the name
			i++;
		}
		i++;
		String actual=""+s.charAt(i);
		String ins = this.instr;
		if(ins!="A"){
			switch (ins){
			case "&":				//REL
				//TODO match the string with a number in Hexadecimal or Letters with numbers
				break;
			case "$":				//BIT
				//TODO match the string with a number in Hexadecimal
				break;
			case "%":				//DIR
				//TODO match the String with a number in Hexadecimal
				break;
			case "+":				//DATOS
				//TODO match the String with a number in Hexadecimal
				break;
			}
		}
		
		return true;
	}
	
	public String toString(){
		return "Name: "+this.instrName+" Code: "+this.code+" Number of bytes: "+this.bytes;
	}
	// REL &
	public boolean isRelative(String s){
		// example: JNZ ETIQUETA1
		int actual=0;
		while(s.length()!=actual){
			//check if it is number, or letter
			if(!(s.charAt(actual)>=48 && s.charAt(actual)<=57 || s.charAt(actual)>=65 && s.charAt(actual)<=90))//check again for the numbers, with H and without
				return false;
			actual++;
		}
		return true;
	}
	/**
	 * 
	 * @param s
	 * @return String: the direction of the bit 
	 */
	public String isBit(String s){
		int actual=0;
		int i=0;
		if(s.charAt(actual)>=48 && s.charAt(actual)<=57 && s.charAt(actual+1)>=48 && s.charAt(actual+1)<=57 && s.charAt(actual+2)=='H'){ //Example: 56H valid return 56
			return ""+s.charAt(actual)+s.charAt(actual+1);
		}else if((s.charAt(actual)>=48 && s.charAt(actual)<=57 && s.charAt(actual+1)>=48 && s.charAt(actual+1)<=57)){ //Example 56 valid, return 38
			String num=""+s.charAt(actual)+s.charAt(actual+1);
			int x = Integer.parseInt(num);
			return Integer.toHexString(x);
		}
		
		//its not a number.
		//check if its one of the special register with a bit direction enable Example: SETB PSW.1
		String check="";
		String bit="";
		int y=0;
		int z=0;
		actual=0;
		while(s.length()!=actual){
			if(s.charAt(actual)!='.'){
				check+=s.charAt(actual);
				actual++;
			}else{
				break;
			}
		}
		Iterator<SpecialRegister> it= this.specialRBit.iterator();
		while(it.hasNext()){
			SpecialRegister sr =it.next();
			if(sr.simbol.equals(check)){
				actual++;
				bit = ""+s.charAt(actual);//has to be a number.
				if(bit.charAt(0)>=48 && bit.charAt(0)<=55 ){//if its a number...
					y=sr.dir;
					z=Integer.parseInt(bit,16);
					y= y+z;
				}
				return ""+Integer.toHexString(y);
			}	
		}
		//***********************
		//missing to check the specialRBytes
		return "FALSE";
	}

}
