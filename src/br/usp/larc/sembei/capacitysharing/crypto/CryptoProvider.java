package br.usp.larc.sembei.capacitysharing.crypto;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.widget.Toast;

public abstract class CryptoProvider {

	// Digital Signature
	public abstract void keyGen();
	public abstract String sign(String message);
	public abstract boolean verify(String message, String signature);
	
	// HMAC
	public native String get_hmac(String message, String key);
	public native String verify_hmac(String message, String key, String tag);
	
	// Symmetric Encryption
	public native String symmetric_encrypt(String plaintext, String key);
	public native String symmetric_decrypt(String ciphertext, String key);
	
	// Symmetric Encryption
	public native String asymmetric_encrypt(String plaintext, String pkey);
	public native String asymmetric_decrypt(String ciphertext, String skey);
	
}
