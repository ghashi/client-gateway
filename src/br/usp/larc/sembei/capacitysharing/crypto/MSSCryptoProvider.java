package br.usp.larc.sembei.capacitysharing.crypto;

import java.security.SecureRandom;

import android.app.Activity;
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
		Log.d("CASH", skey);
		String[] sigState = MSS.sign(message, skey);
		String signature = sigState[0];
		skey = sigState[1];
		Log.d("CASH", signature);
		Log.d("CASH", skey);
		fileManager.writeToFile(skeyFile, skey);
		return signature;
	}

	@Override
	public boolean verify(String message, String signature, String pkey) {
		return MSS.verify(message, signature, pkey);
	}
	
	// Benchmark
	public static native long keyGen(int mark);
	public static native long sign(int mark);
	public static native long verify(int mark);

}
