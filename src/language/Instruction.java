package language;

public class Instruction {
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

}
