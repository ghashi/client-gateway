package br.usp.larc.sembei.capacitysharing.crypto;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

public abstract class CryptoProvider {
	
	private final int securityLevel = 128;
	private final int symmetricKeySize = (securityLevel / 8);
	
	// Digital Signature
	public abstract void keyGen();
	public abstract String sign(String message);
	public abstract boolean verify(String message, String signature, String pkey);
	
	// HMAC
	public native String get_hmac(String message, String key);
	public native boolean verify_hmac(String message, String key, String tag);
	
	// Symmetric Encryption
	public String symmetric_keyGen() {
		byte[] key = new byte[symmetricKeySize];
		Random randomGenerator = new Random();
		randomGenerator.nextBytes(key);
		return Base64.encodeToString(key, Base64.DEFAULT);
	}
	public String symmetric_ivGen() {
		byte[] iv = new byte[symmetricKeySize];
		Random randomGenerator = new Random();
		randomGenerator.nextBytes(iv);
		return Base64.encodeToString(iv, Base64.DEFAULT);
	}
	public native String symmetric_encrypt(String plaintext, String iv, String key);
	public native String symmetric_decrypt(String ciphertext, String iv, String key);
	
	// Symmetric Encryption
	public native String asymmetric_encrypt(String plaintext, String pkey);
	public native String asymmetric_decrypt(String ciphertext, String skey);
	
	public native String generateCSR(int id, String cname, String authKey, String tokenKey, String mssKey);
	public native boolean readCSR(String csr, int id, String cname, String authKey, String tokenKey, String mssKey);
	public native boolean readCert(String certificate, int id, String cname, String time, String valid, String authKey, String tokenKey, String certSignature, String caPkey);
	
}
