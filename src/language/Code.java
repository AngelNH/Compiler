package language; 

import java.util.*;

public class Code extends Instruction{
	public String dir;
	public String datos;
	public String rel;
	public String hex80;
	
	public Code(String inst){
		super();
		this.dir="***";
		this.datos="***";
		this.rel="***";
		super.instr=inst;
		this.hex80=gethex80();
	}
	public int getIndex(ArrayList<String> list){
		if(list.contains(super.instr)){
			return list.indexOf(super.instr);
		}
		else{
			return 500;
		}
			
	}
	public String getCode(){
		if(dir!="***" && datos!="***"){ //operation of 3 bytes
			return this.code + this.dir + this.datos;
		}else if(dir!= "***" && rel!="***"){
			return this.code + this.dir + this.rel;
		}else if(dir!="***"){
			return this.code + this.dir;
		}else if(datos!="***"){
			return this.code + this.datos;
		}else if(rel != "***"){
			return this.code + this.rel;
		}else{
			return this.code;
		}
	}
	
	public String gethex80() {
		//TODO generate the HEX 80
		String out;
		return null;
	}
	
}
