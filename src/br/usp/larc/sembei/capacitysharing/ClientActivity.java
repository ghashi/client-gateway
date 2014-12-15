package br.usp.larc.sembei.capacitysharing;

import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;
import br.usp.larc.sembei.capacitysharing.bluetooth.BluetoothService;
import br.usp.larc.sembei.capacitysharing.bluetooth.DeviceListActivity;
import br.usp.larc.sembei.capacitysharing.crypto.util.FileManager;

public class ClientActivity extends SupplicantActivity {

	public static final String LOGIN_ACTION = "login";
	public static final String CHECKLOGIN_ACTION = "checklogin";
	public static final String REDIRECT_ACTION = "redirect";

	private String next_url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// It is important to call setContentView before super.onCreate
		setContentView(R.layout.activity_client);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		super.onCreate(savedInstanceState);

		// TODO APAGAR
		SearchView sw = ((SearchView) findViewById(R.id.url_bar));
		sw.setQuery("http://capacity-sharing.neocities.org", false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.client, menu);
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
		} else if (id == R.id.action_bluetooth) {
			// Get local Bluetooth adapter
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			// If the adapter is null, then Bluetooth is not supported
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, "Bluetooth is not available",
						Toast.LENGTH_LONG).show();
			}
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}
			return true;
		} else if (id == R.id.scan) {
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void makeHttpRequest(String url) {
		next_url = url;
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		new SendMessageTask().execute(url);
	}

	protected void makeLoginRequest(String id, String token, String sig) {
		JSONObject request = new JSONObject();
		try {
			request.put("action", LOGIN_ACTION);
			request.put("id", id);
			request.put("token", token);
			request.put("sig", sig);
			request.put("supplicant", "client");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendBluetoothMessage(request.toString());
	}

	protected void makeCheckLoginRequest(String id, String encrypted_nonce,
			String hmac) {
		JSONObject request = new JSONObject();
		try {
			request.put("action", CHECKLOGIN_ACTION);
			request.put("id", id);
			request.put("nonce", encrypted_nonce);
			request.put("hmac", hmac);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendBluetoothMessage(request.toString());
	}

	protected void makeRedirectRequest(String id, String encrypted_request,
			String request_hmac) {
		JSONObject request = new JSONObject();
		try {
			request.put("action", REDIRECT_ACTION);
			request.put("id", id);
			request.put("request", encrypted_request);
			request.put("hmac", request_hmac);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		sendBluetoothMessage(request.toString());
	}

	@Override
	protected void processMessage(String readMessage) {
		JSONObject requestJson;
		try {
			requestJson = new JSONObject(readMessage);
			String action = requestJson.getString("action");

			switch (action) {
			case LOGIN_ACTION:
				break;
			case CHECKLOGIN_ACTION:
				if (verifyCheckLoginResponse(readMessage)) {
					showToastMessage("CheckLogin successful!");
					updateRemainingData(REMAINING_DATA);
				} else {
					showToastMessage("CheckLogin failed: " + HANDSHAKE_FAILED);
				}
				break;
			case REDIRECT_ACTION:
				Log.i("CASH", "APAGAR readMessage="+readMessage);
				processRedirectResponse(readMessage, next_url);
				break;
			default:
				showToastMessage("Can't process received message");
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		new ProcessMessageTask().execute(readMessage);
	}

	private void processLoginResponse(JSONObject requestJson) {
		try {
			String nonce = requestJson.getString("nonce");
			String hmac = requestJson.getString("hmac");

			Log.i("CASH",
					"ClientActivity.processMessage before=mss.verify_hmac() nonce="
							+ nonce
							+ "\n"
							+ "SESSION_KEY="
							+ GatewayActivity.SESSION_KEY
							+ "\n"
							+ "hmac: "
							+ hmac
							+ "\n"
							+ "mss.verify_hmac="
							+ String.valueOf(mss.verify_hmac(nonce,
									GatewayActivity.SESSION_KEY, hmac)) + "\n");

			if (mss.verify_hmac(nonce, GatewayActivity.SESSION_KEY, hmac)) {
				FileManager fileManager = new FileManager(ClientActivity.this);
				String id = fileManager.readFile(RegisterActivity.CLIENT_ID);

				String[] params = new String[2];
				params = getCheckLoginParams(nonce);

				makeCheckLoginRequest(id, params[0], params[1]);
			}
		} catch (JSONException | NullPointerException e) {
			showToastMessage("Error on the request");
			e.printStackTrace();
		}
	}
	
	private class SendMessageTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... arg) {
			FileManager fileManager = new FileManager(ClientActivity.this);
			String id = fileManager.readFile(RegisterActivity.CLIENT_ID);
			String[] params = new String[2];

			if (getRemainingData() > 0) {
				params = getRedirectParams(arg[0]);
				makeRedirectRequest(id, params[0], params[1]);
			} else {
				params = getLoginParams();
				makeLoginRequest(id, params[0], params[1]);
			}
			return null;
		}
	}
	
	private class ProcessMessageTask extends AsyncTask<String, Void, Void> {
		
		@Override
		protected Void doInBackground(String... arg) {
			JSONObject requestJson;
			try {
				requestJson = new JSONObject(arg[0]);
				String action = requestJson.getString("action");

				switch (action) {
				case LOGIN_ACTION:
					processLoginResponse(requestJson);
					break;
				case CHECKLOGIN_ACTION:
					if (verifyCheckLoginResponse(arg[0])) {
						makeHttpRequest(next_url);
					}
					break;
				case REDIRECT_ACTION:
					break;
				default:
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
