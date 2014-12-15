package br.usp.larc.sembei.capacitysharing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import br.usp.larc.sembei.capacitysharing.crypto.util.FileManager;

public class GatewayActivity extends SupplicantActivity {

	// TODO fix "session_key"
	public static final String SESSION_KEY = "UPB5iiqKPo37pi0whIwr/g==";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// It is important to call setContentView before super.onCreate
		setContentView(R.layout.activity_gateway);
		super.onCreate(savedInstanceState);

		// TODO APAGAR
		SearchView sw = ((SearchView) findViewById(R.id.url_bar));
		sw.setQuery("http://www.uol.com.br", false);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
	}

	protected String makePostHttpRequest(String uri, List<NameValuePair> pairs) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		String responseString = null;
		try {
			HttpPost post = new HttpPost(getString(R.string.PROXY_URL) + uri);

			post.setEntity(new UrlEncodedFormEntity(pairs));

			response = httpclient.execute(post);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", e.getMessage());
			//
		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
			//
		}
		return responseString;
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gateway, menu);
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
		} else if (id == R.id.discoverable) {
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void processMessage(String readMessage) {
		new RedirectTask().execute(readMessage);
	}

	protected void makeHttpRequest(String url) {
		Log.i("CASH", "SupplicantActivity.makeHttpRequest " + "remainingData="
				+ getRemainingData() + " url=" + url);
		if (getRemainingData() > 0) {
			new RequestTask(url).execute();
		} else {
			new LoginTask().execute(url);
		}
	}

	/********************
	 * 
	 * REDIRECT
	 * 
	 ********************/
	private class RedirectTask extends AsyncTask<String, String, String> {
		private String action;
		
		@Override
		protected String doInBackground(String... arg) {
			String response = null;
			JSONObject requestJson;
			try {
				requestJson = new JSONObject(arg[0]);
				action = requestJson.getString("action");

				switch (action) {
				case ClientActivity.LOGIN_ACTION:
					String login_id = requestJson.getString("id");
					String token = requestJson.getString("token");
					String sig = requestJson.getString("sig");
					response = makeLoginRequest(login_id, token, sig);
					break;
				case ClientActivity.CHECKLOGIN_ACTION:
					String checklogin_id = requestJson.getString("id");
					String nonce = requestJson.getString("nonce");
					String hmac = requestJson.getString("hmac");
					response = makeCheckLoginRequest(checklogin_id, nonce, hmac);				
					break;
				case ClientActivity.REDIRECT_ACTION:
					String redirect_id = requestJson.getString("id");
					String request = requestJson.getString("request");
					String redirect_hmac = requestJson.getString("hmac");
					response = makeRedirectRequest(redirect_id, request, redirect_hmac);
					Log.i("CASH", "RedirectTask.doInBackground at=REDIRECT_ACTION response.size="+String.valueOf(response.length()));
					break;
				default:
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				JSONObject responseJson = new JSONObject(result);
				responseJson.put("action", action);
				sendBluetoothMessage(responseJson.toString());
			} catch (NullPointerException | JSONException e) {
				showToastMessage("Can't process received message");
				e.printStackTrace();
			}
		}
	}
	
	/********************
	 * 
	 * LOGIN
	 * 
	 ********************/
	private class LoginTask extends AsyncTask<String, String, String> {

		private String next_url;

		@Override
		protected void onPreExecute() {
			showToastMessage("Starting login");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... url) {
			next_url = url[0];

			FileManager fileManager = new FileManager(GatewayActivity.this);
			String id = fileManager.readFile(RegisterActivity.GATEWAY_ID);

			String[] params = new String[2];
			params = getLoginParams();

			return makeLoginRequest(id, params[0], params[1]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				JSONObject requestJson = new JSONObject(result);
				String nonce = requestJson.getString("nonce");
				String hmac = requestJson.getString("hmac");

				Log.i("CASH", "LoginTask.onPostExecute nonce=" + nonce + "\n"
						+ "hmac=" + hmac + "\n");

				new CheckloginTask(hmac, nonce).execute(next_url);
			} catch (JSONException | NullPointerException e) {
				//
				showToastMessage("Error on the request");
				e.printStackTrace();
			}
		}
	}
	
	private String makeLoginRequest(String id, String token, String sig) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("id", id));
		pairs.add(new BasicNameValuePair("token", token));
		pairs.add(new BasicNameValuePair("sig", sig));
		pairs.add(new BasicNameValuePair("supplicant", "gateway"));

		return makePostHttpRequest("/login", pairs);
	}
	
	/********************
	 * 
	 * CHECKLOGIN
	 * 
	 ********************/
	private class CheckloginTask extends AsyncTask<String, String, String> {
		private String id;
		private String hmac;
		private String nonce;
		private String next_url;

		public CheckloginTask(String hmac, String nonce) {
			FileManager fileManager = new FileManager(GatewayActivity.this);
			this.id = fileManager.readFile(RegisterActivity.GATEWAY_ID);

			this.hmac = hmac;
			this.nonce = nonce;
		}

		@Override
		protected void onPreExecute() {
			showToastMessage("Sending checklogin");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... arg) {
			next_url = arg[0];

			Log.i("CASH",
					"CheckloginTask.doInBackground before=mss.verify_hmac() nonce="
							+ nonce
							+ "\n"
							+ "SESSION_KEY="
							+ SESSION_KEY
							+ "\n"
							+ "hmac: "
							+ hmac
							+ "\n"
							+ "mss.verify_hmac="
							+ String.valueOf(mss.verify_hmac(nonce,
									SESSION_KEY, hmac)) + "\n");

			if (mss.verify_hmac(nonce, SESSION_KEY, hmac)) {
				String[] params = new String[2];
				params = getCheckLoginParams(nonce);

				return makeCheckLoginRequest(id, params[0], params[1]);
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (verifyCheckLoginResponse(result)) {
				showToastMessage("CheckLogin successful!");
				updateRemainingData(REMAINING_DATA);
				new RequestTask(next_url).execute();
			} else {
				showToastMessage("CheckLogin failed: " + HANDSHAKE_FAILED);
			}
		}
	}
	
	private String makeCheckLoginRequest(String id, String encrypted_nonce,
			String hmac) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("id", id));
		pairs.add(new BasicNameValuePair("nonce", encrypted_nonce));
		pairs.add(new BasicNameValuePair("hmac", hmac));

		return makePostHttpRequest("/checklogin", pairs);
	}
	/********************
	 * 
	 * REQUEST
	 * 
	 ********************/
	private class RequestTask extends AsyncTask<Void, String, String> {

		private String url;
		private String id;

		public RequestTask(String url) {
			FileManager fileManager = new FileManager(GatewayActivity.this);
			this.id = fileManager.readFile(RegisterActivity.GATEWAY_ID);

			this.url = url;
		}

		@Override
		protected void onPreExecute() {
			updateStatus(R.string.loading);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... uri) {
			String[] params = new String[2];
			params = getRedirectParams(url);

			return makeRedirectRequest(id, params[0], params[1]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			processRedirectResponse(result, url);
		}
	}
	
	private String makeRedirectRequest(String id, String encrypted_request,
			String request_hmac) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("id", id));
		pairs.add(new BasicNameValuePair("request", encrypted_request));
		pairs.add(new BasicNameValuePair("hmac", request_hmac));

		return makePostHttpRequest("/redirect", pairs);
	}
}
