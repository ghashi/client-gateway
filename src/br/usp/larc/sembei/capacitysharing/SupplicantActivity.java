package br.usp.larc.sembei.capacitysharing;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import br.usp.larc.sembei.capacitysharing.bluetooth.BluetoothService;
import br.usp.larc.sembei.capacitysharing.bluetooth.DeviceListActivity;
import br.usp.larc.sembei.capacitysharing.crypto.MSSCryptoProvider;
import br.usp.larc.sembei.capacitysharing.crypto.util.FileManager;

public abstract class SupplicantActivity extends Activity {
	// Name of the connected device
	public String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	public StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	public BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	public BluetoothService mChatService = null;

	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "gateway";
	public static final String TOAST = "gateway_toast";

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String HANDSHAKE_OK = "HANDSHAKE_OK";
	public static final String HANDSHAKE_FAILED = "HANDSHAKE_FAILED";
	public static final int REMAINING_DATA = 10000000;

	protected MSSCryptoProvider mss;
	private int remainingData = 0;

	/**
	 * Process bluetooth message
	 */
	protected abstract void processMessage(String readMessage);

	protected abstract void makeHttpRequest(String url);

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.supplicant, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mss = new MSSCryptoProvider(SupplicantActivity.this);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		setSearchListener();
		configureWebView();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			showToastMessage("Please, enable bluetooth");
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
	}

	/**
	 * BLUETOOTH
	 */
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		private String buffer;
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					showToastMessage(getString(R.string.title_connected_to));
					break;
				case BluetoothService.STATE_CONNECTING:
					showToastMessage(getString(R.string.title_connecting));
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					showToastMessage(getString(R.string.title_not_connected));
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);

				Log.i("CASH", "mHandler.handleMessage at=MESSAGE_WRITE message.lenght()="+String.valueOf(writeMessage.length()) + "\nmessage=" + writeMessage );

				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);

				// restart buffer when new JSON is received
				if(readMessage.contains("{")){
					buffer = "";
				} 
				buffer = buffer.concat(readMessage);
				// call processMessage() when end of JSON is found
				if(buffer.contains("\"}")){
					Log.i("CASH", "mHandler.handleMessage at=MESSAGE_READ message.lenght()="+String.valueOf(buffer.length()) + "\nmessage=" + buffer);
					processMessage(buffer);
					buffer = null;
				}

				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	private void setupChat() {
		setSearchListener();

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	protected void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	protected void sendBluetoothMessage(String message) {
		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
		}
	}

	/**
	 * REQUESTS
	 */

	/**
	 * 
	 * @return [token, sig]
	 */
	protected String[] getLoginParams() {
		// id: <NUM>
		// token: <TEXT> 20 bytes // encripto NTRU //id, counter, session_key
		// sig: <TEXT> // assinar ciphertext do token
		// supplicant: (‘client’ | ‘gateway’)
		// TODO replace with real token
		FileManager fileManager = new FileManager(SupplicantActivity.this);
		String token = "iiiiiiiiiiiiiiiiiiii";
		String pkey = fileManager.readFile(MainActivity.NTRU_PKEY);
		String sig;

		token = mss.asymmetric_encrypt(token, pkey);
		sig = mss.sign(token);

		Log.i("CASH", "SupplicantActivity.getLoginParams at=mss.verify token="
				+ token + "\n" + "sig=" + sig + "\n" + "pkey=" + mss.getPkey()
				+ "\n" + "result=" + mss.verify(token, sig, mss.getPkey()));

		String[] res = new String[2];
		res[0] = token;
		res[1] = sig;

		return res;
	}

	/**
	 * @return [encrypted_nonce, checkloginHmac]
	 */
	protected String[] getCheckLoginParams(String nonce) {
		// TODO replace with real IV
		String encoded_iv;
		byte[] iv = { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa,
				0xb, 0xc, 0xd, 0xe, 0xf };
		encoded_iv = Base64.encodeToString(iv, Base64.DEFAULT);

		int new_nonce;
		try {
			new_nonce = Integer.parseInt(mss.symmetric_decrypt(nonce,
					encoded_iv, GatewayActivity.SESSION_KEY)) + 1;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			new_nonce = -1;
		}
		String encrypted_nonce = mss.symmetric_encrypt(
				String.valueOf(new_nonce), encoded_iv,
				GatewayActivity.SESSION_KEY);
		String checkloginHmac = mss.get_hmac(encrypted_nonce,
				GatewayActivity.SESSION_KEY);

		Log.i("CASH",
				"SupplicantActivity.getCheckLoginParams at=mss.symmetric_encrypt \n"
						+ "new_nonce=" + String.valueOf(new_nonce) + "\n"
						+ "encoded_iv=" + encoded_iv + "\n" + "SESSION_KEY - "
						+ GatewayActivity.SESSION_KEY + "\n" + "result="
						+ encrypted_nonce + "\n");

		String[] res = new String[2];
		res[0] = encrypted_nonce;
		res[1] = checkloginHmac;
		return res;
	}

	/**
	 * return [encrypted_request, request_hmac]
	 * 
	 * @param url
	 * @return
	 */
	protected String[] getRedirectParams(String url) {
		JSONObject request = new JSONObject();
		try {
			request.put("url", url);
			request.put("method", "get");
			request.put("params", "");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// TODO replace with real IV
		String encoded_iv;
		byte[] iv = { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa,
				0xb, 0xc, 0xd, 0xe, 0xf };
		encoded_iv = Base64.encodeToString(iv, Base64.DEFAULT);

		String encrypted_request = mss.symmetric_encrypt(request.toString(),
				encoded_iv, GatewayActivity.SESSION_KEY);
		String request_hmac = mss.get_hmac(encrypted_request,
				GatewayActivity.SESSION_KEY);

		String[] res = new String[2];
		res[0] = encrypted_request;
		res[1] = request_hmac;

		return res;
	}

	protected boolean verifyCheckLoginResponse(String result) {
		try {
			JSONObject requestJson = new JSONObject(result);
			String checklogin = requestJson.getString("checklogin");
			String hmac = requestJson.getString("hmac");

			Log.i("CASH",
					"CheckloginTask.onPostExecute at=mss.verify_hmac checklogin="
							+ checklogin
							+ "\n"
							+ "hmac: "
							+ hmac
							+ "\n"
							+ "result="
							+ String.valueOf(mss.verify_hmac(checklogin,
									GatewayActivity.SESSION_KEY, hmac)));

			if (mss.verify_hmac(checklogin, GatewayActivity.SESSION_KEY, hmac)) {
				// TODO replace with real IV
				String encoded_iv;
				byte[] iv = { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9,
						0xa, 0xb, 0xc, 0xd, 0xe, 0xf };
				encoded_iv = Base64.encodeToString(iv, Base64.DEFAULT);

				String decrypted_checklogin = mss.symmetric_decrypt(checklogin,
						encoded_iv, GatewayActivity.SESSION_KEY);
				Log.i("CASH",
						"CheckloginTask.onPostExecute decrypted_checklogin="
								+ decrypted_checklogin + "\n");
				return decrypted_checklogin.equals(HANDSHAKE_OK);
			} else {
				showToastMessage("CheckLogin failed: can't verify hmac");
			}
			hideKeyboard();
		} catch (JSONException | NullPointerException e) {
			//
			showToastMessage("Error on the request");
			e.printStackTrace();
		}
		return false;
	}

	protected void processRedirectResponse(String result, String url) {
		try {
			updateStatus(R.string.online);

			JSONObject requestJson = new JSONObject(result);
			String hmac = requestJson.getString("hmac");

			if (mss.verify_hmac(requestJson.getString("response"),
					GatewayActivity.SESSION_KEY, hmac)) {
				// TODO replace with real IV
				String encoded_iv;
				byte[] iv = { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9,
						0xa, 0xb, 0xc, 0xd, 0xe, 0xf };
				encoded_iv = Base64.encodeToString(iv, Base64.DEFAULT);

				String decrypted_response = mss.symmetric_decrypt(
						requestJson.getString("response"), encoded_iv,
						GatewayActivity.SESSION_KEY);
				JSONObject response = new JSONObject(decrypted_response);
				String remainingData = response.getString("remaining_data");
				String content = response.getString("content");

				updateRemainingData(Integer.valueOf(remainingData));

				byte[] data = Base64.decode(content, Base64.DEFAULT);
				String html = new String(data, "UTF-8");

				renderString(formatHtmlLink(html, url));
				hideKeyboard();
			} else {
				showToastMessage("Redirect failed: can't verify hmac");
			}
		} catch (JSONException | UnsupportedEncodingException
				| NullPointerException e) {
			//
			e.printStackTrace();
			renderString(e.getMessage());
		}
	}

	/**
	 * MISC
	 */
	private void setSearchListener() {
		SearchView searchView = (SearchView) findViewById(R.id.url_bar);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.i("CASH", "onQueryTextSubmit: " + query);

				if (!mBluetoothAdapter.isEnabled()) {
					showToastMessage("Please, enable bluetooth");
				} else {
					makeHttpRequest(query);
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
	}

	protected void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		// check if no view has focus:
		View view = this.getCurrentFocus();
		if (view != null) {
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void showToastMessage(String text) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	protected void updateRemainingData(int remainingData) {
		setRemainingData(remainingData);
		TextView tv = (TextView) findViewById(R.id.remaining_data);
		tv.setText(String.valueOf(getRemainingData()));
	}

	protected int getRemainingData() {
		return remainingData;
	}

	protected void setRemainingData(int remainingData) {
		this.remainingData = remainingData;
	}

	/**
	 * WEBVIEW
	 */
	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			makeHttpRequest(url);
			return true;
		}
	}

	private void configureWebView() {
		WebView webview = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webview.setWebViewClient(new MyWebViewClient());
	}

	protected String formatHtmlLink(String html, String url) {
		return html.replaceAll("href=\"(?!http)", "href=\"" + url);
	}

	protected void updateStatus(int text) {
		TextView status = (TextView) findViewById(R.id.connection_status);
		status.setText(text);
	}

	protected void renderString(String html) {
		WebView webview = (WebView) findViewById(R.id.webView);
		webview.loadDataWithBaseURL(null, html, "text/html", "utf-8", "");
		// webview.loadData(html, "text/html; charset=UTF-8", null);
	}
}
