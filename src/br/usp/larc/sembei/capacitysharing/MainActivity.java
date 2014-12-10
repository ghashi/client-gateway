package br.usp.larc.sembei.capacitysharing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import br.usp.larc.sembei.capacitysharing.crypto.MSSCryptoProvider;
import br.usp.larc.sembei.capacitysharing.crypto.util.FileManager;
import br.usp.larc.sembei.capacitysharing.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity {

	private static final String TAG = "Client-Gateway";

	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	protected static final String GATEWAY = "gateway";

	protected static final String CLIENT = "client";

	protected static final String SUPPLICANT = "supplicant";

	public static final String NTRU_PKEY = "ntru_pkey";

	public static final String ECDSA_PKEY = "ecdsa_pkey";

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onResume() {
		super.onResume();
		
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, "Getting ntru_pkey and ecdsa_pkey", duration);
		toast.show();
		
		new GetKeysTask("/ntru_pkey", NTRU_PKEY).execute();
		new GetKeysTask("/ecdsa_pkey", ECDSA_PKEY).execute();

		
		// DESCOMENTAR PARA TESTAR CRYPTO LIB
//		new TestTask().execute();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		System.loadLibrary("crypto-library");

		setContentView(R.layout.activity_main);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		// findViewById(R.id.benchmark_button).setOnTouchListener(
		// mDelayHideTouchListener);

		/*
		 * Beginning of the modified area
		 */

		// CryptoProvider cp = new MSSCryptoProvider(MainActivity.this);
		// String key = cp.symmetric_keyGen();
		// String iv = cp.symmetric_ivGen();
		// String ciphertext = cp.symmetric_encrypt("teste---DEU!", iv, key);

		// Toast toast = Toast.makeText(getApplicationContext(),
		// cp.symmetric_decrypt(ciphertext, iv, key), Toast.LENGTH_LONG);
		// toast.show();
		// if(cp.verify_hmac("TaNaNannn", new String(key), tag)) {
		// toast = Toast.makeText(getApplicationContext(), "Belezoca!",
		// Toast.LENGTH_LONG);
		// toast.show();
		// if(!cp.verify_hmac("TaNaNannm", new String(key), tag))
		// toast = Toast.makeText(getApplicationContext(), "MESMO!",
		// Toast.LENGTH_LONG);
		// else
		// toast = Toast.makeText(getApplicationContext(), "#sqn",
		// Toast.LENGTH_LONG);
		// }
		// else
		// toast = Toast.makeText(getApplicationContext(), "Merdão!",
		// Toast.LENGTH_LONG);
		// toast.show();

		findViewById(R.id.client_button).setOnClickListener(clientListener);

		findViewById(R.id.gateway_button).setOnClickListener(gatewayListener);

		findViewById(R.id.benchmark_button).setOnClickListener(
				benchmarkListener);

		/*
		 * End of the modified area
		 */
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	/*
	 * Beginning of the modified area
	 */

	View.OnClickListener clientListener = new View.OnClickListener() {

		public void onClick(View view) {
			MSSCryptoProvider mss = new MSSCryptoProvider(MainActivity.this);
			if (!mss.hasKeyPair()) {
				Intent clientRegisterIntent = new Intent(MainActivity.this,
						RegisterActivity.class);
				Bundle extras = new Bundle();
				extras.putString(SUPPLICANT, CLIENT);
				clientRegisterIntent.putExtras(extras);
				startActivity(clientRegisterIntent);
			} else {
				startActivity(new Intent(MainActivity.this,
						ClientActivity.class));
			}
		}

	};

	View.OnClickListener gatewayListener = new View.OnClickListener() {

		public void onClick(View v) {
			MSSCryptoProvider mss = new MSSCryptoProvider(MainActivity.this);
			if (!mss.hasKeyPair()) {
				Intent gatewayRegisterIntent = new Intent(MainActivity.this,
						RegisterActivity.class);
				Bundle extras = new Bundle();
				extras.putString(SUPPLICANT, GATEWAY);
				gatewayRegisterIntent.putExtras(extras);
				startActivity(gatewayRegisterIntent);
			} else {
				startActivity(new Intent(MainActivity.this,
						GatewayActivity.class));
			}
		}
	};

	View.OnClickListener benchmarkListener = new View.OnClickListener() {

		public void onClick(View v) {
			startActivity(new Intent(MainActivity.this, BenchmarkActivity.class));
		}

	};

	private class TestTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			test();
			return null;
		}
	}
	
	private class GetKeysTask extends AsyncTask<Void, String, String> {

		private String url;
		private String filename;
		
		public GetKeysTask(String url, String filename) {
			this.url = url;
			this.filename = filename;
		}
		
		@Override
		protected String doInBackground(Void... arg) {
			HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	        	HttpGet post = new HttpGet(getString(R.string.AAAS_URL) + url);

	            response = httpclient.execute(post);

	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	        	Log.e("ClientProtocolException", e.getMessage());
	            //TODO Handle problems..
	        } catch (IOException e) {
	        	Log.e("IOException", e.getMessage());
	            //TODO Handle problems..
	        }
	        return responseString;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				FileManager fileManager = new FileManager(MainActivity.this);
				fileManager.writeToFile(filename, result);
				
				Log.i("CASH_START", filename + ": " + result);
				
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, filename + " saved!", duration);
				toast.show();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
		}
	}

	private boolean test() {
	MSSCryptoProvider mss;
		mss = new MSSCryptoProvider(MainActivity.this);
		
		/**
		 * Digital Signature
		 */
		String msg = "Test message";
		mss.keyGen();
		Log.i("TEST", "mss.getPkey(): " + mss.getPkey());
		Log.i("TEST", "mss.getSkey() - before: " + mss.getSkey());
		String signature = mss.sign(msg);
		Log.i("TEST", "signed_msg: " + signature);
		Log.i("TEST", "verify: " + mss.verify(msg, signature, mss.getPkey()));
		Log.i("TEST", "mss.getSkey() - after: " + mss.getSkey());

		/**
		 * HMAC
		 */
		String key = mss.symmetric_keyGen();
		Log.i("TEST", "session_key: " + key);
		String hmac = mss.get_hmac(msg, key);
		Log.i("TEST", "mss.verify_hmac: " + mss.verify_hmac(msg, key, hmac));
		
		/**
		 * SYMMETRIC 
		 */
		String iv = mss.symmetric_ivGen();
		String sym_enc_msg = mss.symmetric_encrypt(msg, iv, key);
		Log.i("TEST", "mss.symmetric_encrypt: " + sym_enc_msg);
		String sym_dec_msg = mss.symmetric_decrypt(sym_enc_msg, iv, key);
		Log.i("TEST", "mss.symmetric_decrypt: " + sym_dec_msg);

		/**
		 * ASYMMETRIC 
		 */
		String pkey = "AAhlAikCsQWSAqwDYgTrBKsDNQOcA/MC4AMzBNQA4gICA/IFmwa8BFACNALsBFQDVwQnA5wD/gQpBYMHRQDWATYCQAUZAoAEBgC5Bd0FJwRUAZ0EogLFBScGfwBxAYoAaAURACUGkgTvBGID8AXZAwAB2wFjB3YHUweTB8IGvAArBcEEhQeUAbcCJwVeAS4GBwS/AWUE6AT1BQkHDAOlBi0FvQNMB5MH9wBwA3kBWgbtB0IBVgOSAiAFWAOcAlcCigcyBQ0A1gZTAgoHdgetAUQF0QBTBGwHHAbWADUC6QZWAjUEogZNBd4CHwdxANkFCgUuB9AFFAA1Ac0HLAMcBCMFawJIAiQFTgD9AaAAUwNxBbsBcQObBvQCXwCbBLAA/AAcBMUEwgWPAUsGVgQfBT0HxAXgB/gBNQRUBt4CfwNCAokFVgc0AzYAuAD8ADMGdgQwAMkERgQNAGcDmwB8AZAFBAd/AgQDPQebAAsE6QOnAAQEpABcBvIHBgeSA5kHmwBsAv4GoAb3A/YApgB5BTwEXQAnB2EAagNMA7MEjwDDBqEDHAHzAdUDlgI9At0ChQWhBSoBlwNsAvQBJwatA6sDgwfbBIMHQwEjABYFrQMeAyMFUQcgAowEuQTzB7kGvwMeA7wGMQCSBnwCegOlAQoBIQPuBgwG/QanAgIGWQW/AYwBiQXNB+gGXQJ9BPsFmgTpAnIE3ACRAkcAdwf+BM8CyQHbArQEtwI0BpEA9gJFBgsCyADgAVwARQKtBWsADgKYBiQGvgYABAEEWgdCBy8HKAbVA6MCSAa2A68FgQZGBuEC8gajAC0AlQYVBI4CAQD5B/MBvgE3B0YAcwTrA2EBnAMlBLYC5QOvBQEBPQB/BDoEmgPuBzcAewQQAJUE+gJ2AWkEhAPEBwYD0ASMBiAFbQRZAB8GSwMFBOkEAQExApIA2gV6BwEDLwYZAAcEBQUqB6kC2gc3AKIC6ATUBycCiwXkB6YDWgD5AxAHRgQWBUkA1QYKBB8ERwHoAPgBcwfbADQCogIcASMELwV9BzMEAQXSBNECJgLdAwsEqwIXByMCYgdJBO0AigOdBh4BJAU9A/EEhwPHBUoDxAfbBUsBJgG2BkoA3ACsBJkD+gc7Ak0BTwF7AOwB3wBwA9IFagISAtUHeABQBPYCRQMrA1wEVAXeB2gH5wbIBgsHYwDaADcA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";
		String skey = "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";
		String asym_enc_msg = mss.asymmetric_encrypt(msg, pkey);
		Log.i("TEST", "mss.asymmetric_encrypt: " + asym_enc_msg);
		String asym_dec_msg = mss.asymmetric_decrypt(asym_enc_msg, skey);
		Log.i("TEST", "mss.asymmetric_decrypt: " + asym_dec_msg);
		
		/**
		 * Hash
		 */
		Log.i("TEST", "mss.get_hash: " + mss.get_hash(msg));
		
		/**
		 * CSR
		 */
		String csr = mss.generateCSR(
				1,
				"Gateway",
				"MTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExCg==",
				mss.getPkey(),
				mss.getSkey());
		Log.i("TEST", "mss.csr: " + csr);
		
		return true;
	}

	
	/*
	 * End of the modified area
	 */
}
