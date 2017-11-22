package language;

public class SpecialRegister {
	String symbol;
	String sDir;
	int iDir;
	String bitEnable;
	
	public SpecialRegister(String s, String d, String be) {
		this.symbol=s;
		this.sDir=d;
		this.iDir=Integer.parseInt(d,16);
		this.bitEnable=be;
	}
}
