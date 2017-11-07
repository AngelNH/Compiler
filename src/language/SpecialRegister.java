package language;

public class SpecialRegister {
	String simbol;
	String sdir;
	int dir;
	String bitEnable;
	public SpecialRegister(String s, String d, String be) {
		this.simbol=s;
		//this.dir=d;
		this.sdir=d;
		this.dir=Integer.parseInt(d,16);
		this.bitEnable=be;
	}

}
