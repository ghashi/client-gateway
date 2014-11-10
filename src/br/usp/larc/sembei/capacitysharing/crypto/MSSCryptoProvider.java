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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verify(String message, String signature) {
		// TODO Auto-generated method stub
		return false;
	}
	
	// Benchmark
	public static native long keyGen(int mark);
	public static native long sign(int mark);
	public static native long verify(int mark);

}
