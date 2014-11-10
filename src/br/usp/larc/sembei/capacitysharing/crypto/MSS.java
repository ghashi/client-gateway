package br.usp.larc.sembei.capacitysharing.crypto;

public class MSS {
	
	public static native String[] keyGen(byte[] seed);
	public static native String[] sign(String message, String skey);
	public static native boolean verify(String message, String signature, String pkey);

}
