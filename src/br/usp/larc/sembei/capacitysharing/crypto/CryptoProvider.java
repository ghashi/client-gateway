package br.usp.larc.sembei.capacitysharing.crypto;

public class CryptoProvider {

	public static native long benchKeyGen(int mark);
	public static native long benchSign(int mark);
	public static native long benchVerify(int mark);
	
}
