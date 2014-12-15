package br.usp.larc.sembei.capacitysharing.crypto;

import java.security.SecureRandom;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import br.usp.larc.sembei.capacitysharing.MainActivity;
import br.usp.larc.sembei.capacitysharing.crypto.util.FileManager;

public class MSSCryptoProvider extends CryptoProvider {

	private final String skeyFile = "mss_skey";
	private final String pkeyFile = "mss_pkey";

	private FileManager fileManager;

	public MSSCryptoProvider(Activity activity) {
		fileManager = new FileManager(activity);
	}

	public boolean hasKeyPair() {
		return(fileManager.existFile(skeyFile) && fileManager.existFile(pkeyFile));
	}

	@Override
	public void keyGen() {
		byte[] seed = new byte[16];
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(seed);
		String[] keys = MSS.keyGen(seed);
		Log.d("CASH", keys[0]);
		Log.d("CASH", keys[1]);
		fileManager.writeToFile(skeyFile, keys[0]);
		fileManager.writeToFile(pkeyFile, keys[1]);
	}

	@Override
	public String sign(String message) {
		String skey = fileManager.readFile(skeyFile);
		Log.d("CASH", "antes skey: " + skey);
//		Log.d("CASH", "message: " + message);
		String digest = get_hash(message);
//		Log.d("CASH", "digest: " + digest);
		String[] sigState = MSS.sign(digest, skey);
		String signature = sigState[0];
		skey = sigState[1];
//		Log.d("CASH", "signature: " + signature);
		Log.d("CASH", "depois skey: " + skey);
		fileManager.writeToFile(skeyFile, skey);
		return signature;
	}

	public String getPkey(){
		return fileManager.readFile(pkeyFile);
	}

	public String getSkey(){
		return fileManager.readFile(skeyFile);
	}
	
	public int getSignatureIndex(){
		byte[] skey = Base64.decode(fileManager.readFile(skeyFile), Base64.NO_WRAP);
		Log.i("CASH", "MSSCryptoProvider.getSignatureIndex (skey[1]) << 8)=" + String.valueOf(Integer.valueOf(skey[1]) << 8) + "\n" +
				"skey[0]=" + String.valueOf(Integer.valueOf(skey[0])));
		int count = (Integer.valueOf(skey[1]) << 8) | Integer.valueOf(skey[0]);
		return count;
	}

	@Override
	public boolean verify(String message, String signature, String pkey) {
		String digest = get_hash(message);
//		Log.d("CASH", "digest: " + digest);
		return MSS.verify(digest, signature, pkey);
	}

	public native String generateCSR(int id, String cname, String authKey, String tokenKey, String mssKey);
	public native boolean readCSR(String csr, int id, String cname, String authKey, String tokenKey, String mssKey);
	public native boolean readCert(String certificate, int id, String cname, String time, String valid, String authKey, String tokenKey, String certSignature, String caPkey);

	// Benchmark
	public static native long keyGen(int mark);
	public static native long sign(int mark);
	public static native long verify(int mark);

}
