package br.usp.larc.sembei.capacitysharing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import android.location.Criteria;
import android.media.MediaCodec.CryptoException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import br.usp.larc.sembei.capacitysharing.bluetooth.DeviceListActivity;
import br.usp.larc.sembei.capacitysharing.crypto.CryptoProvider;
import br.usp.larc.sembei.capacitysharing.crypto.MSSCryptoProvider;
import br.usp.larc.sembei.capacitysharing.crypto.util.FileManager;

public class GatewayActivity extends SupplicantActivity {

	private MSSCryptoProvider mss;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// It is important to call setContentView before super.onCreate
		setContentView(R.layout.activity_gateway);
		mss = new MSSCryptoProvider(GatewayActivity.this);				
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	 @Override
     public synchronized void onResume() {
        super.onResume();
     }

	protected void makeHttpRequest(String url){
		System.out.println("HAHAHAHA");
		System.out.println(remaining_data);
		if (remaining_data > 0) {
			new RequestTask(url).execute();			
		} else {
			new LoginTask().execute();
		}
	}
	
	private String makePostHttpRequest(String uri, List<NameValuePair> pairs) {
		HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
        	HttpPost post = new HttpPost(getString(R.string.PROXY_URL) + uri);

        	
        	post.setEntity(new UrlEncodedFormEntity(pairs));

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
            //
        } catch (IOException e) {
        	Log.e("IOException", e.getMessage());
            //
        }
        return responseString;
	}

	private class LoginTask extends AsyncTask<Void, String, String>{

		@Override
		protected void onPreExecute() {
			showToastMessage("Starting login");
			super.onPreExecute();
		}

	    @Override
	    protected String doInBackground(Void... uri) {
	    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        	addRequestParameters(pairs);
	        return makePostHttpRequest("/login", pairs);
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	super.onPostExecute(result);
			try {
				JSONObject requestJson = new JSONObject(result);
				String nonce = requestJson.getString("nonce");
				String hmac = requestJson.getString("hmac");

				Log.i("LOGIN", "nonce: " + nonce);
				Log.i("LOGIN", "hmac: " + hmac);
				
				new CheckloginTask(hmac, nonce).execute();
				
				hideKeyboard();
			} catch (JSONException | NullPointerException e) {
				// 
				showToastMessage("Error on the request");
				e.printStackTrace();
			}
	    }

		private void addRequestParameters(List<NameValuePair> pairs) {
			//		id: <NUM>
			//		token: <TEXT> 20 bytes // encripto NTRU //id, counter, session_key
			//		sig: <TEXT> // assinar ciphertext do token
			//		supplicant: (‘client’ | ‘gateway’)
			// TODO 
			FileManager fileManager = new FileManager(GatewayActivity.this);
			String id = fileManager.readFile(RegisterActivity.GATEWAY_ID);
			String token = "iiiiiiiiiiiiiiiiiiii";
			String pkey = fileManager.readFile(MainActivity.NTRU_PKEY);
			String skey = "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";
			
			String sig;
			
			token = mss.asymmetric_encrypt(token, pkey);
			// TODO APAGAR
			System.out.println(mss.asymmetric_decrypt(token, skey));
			System.out.println("TOKEN:" + token);
			sig = mss.sign(token);
			System.out.println("SIG:" + sig);

			Log.i("TEST", "mss.verify - token: " + token);
			Log.i("TEST", "mss.verify - sig: " + sig);
			Log.i("TEST", "mss.verify - pkey: " + mss.getPkey());
			Log.i("TEST", "mss.verify - " + mss.verify(token, sig, mss.getPkey()));

			Log.i("TEST", "mss.verify " + mss.verify(token, sig, mss.getPkey()));
			
			pairs.add(new BasicNameValuePair("id", id));
			pairs.add(new BasicNameValuePair("token", token));
			pairs.add(new BasicNameValuePair("sig", sig));
			pairs.add(new BasicNameValuePair("supplicant", "gateway"));
		}
	}
	private class CheckloginTask extends AsyncTask<String, String, String>{
		// TODO fix "session_key"
		public static final String SESSION_KEY = "UPB5iiqKPo37pi0whIwr/g==";
		
		private String id;
		private String hmac;
		private String nonce;

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
			Log.i("CASH_CHECKLOGIN", "nonce: " + nonce);
			Log.i("CASH_CHECKLOGIN", "SESSION_KEY: " + SESSION_KEY);
			Log.i("CASH_CHECKLOGIN", "hmac: " + hmac);

	    	Log.i("CASH_CHECKLOGIN", String.valueOf(mss.verify_hmac(nonce, SESSION_KEY, hmac)));
// TODO Apagar comentarios abaixo (if-else) quando verify_hmac estiver corrigido
//			if (mss.verify_hmac(nonce, SESSION_KEY, hmac)) {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				addRequestParameters(pairs);
				return makePostHttpRequest("/checklogin", pairs);
//			} else{
//				return null;
//			}
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	super.onPostExecute(result);
			try {
				JSONObject requestJson = new JSONObject(result);
				String checklogin = requestJson.getString("checklogin");
				String hmac = requestJson.getString("hmac");

				Log.i("LOGIN", "checklogin: " + checklogin);
				Log.i("LOGIN", "hmac: " + hmac);
				
		    	Log.i("CASH", "verify_hmac: " + String.valueOf(mss.verify_hmac(checklogin, SESSION_KEY, hmac)));
		    	
		    	// TODO replace with real IV
				String encoded_iv;
				byte[] iv = {0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf};
				encoded_iv = Base64.encodeToString(iv, Base64.DEFAULT);			
				
				String decrypted_checklogin = mss.symmetric_decrypt(checklogin, encoded_iv, SESSION_KEY);
		    	Log.i("CASH", "decrypt checklogin: " + decrypted_checklogin);
				
				hideKeyboard();
			} catch (JSONException | NullPointerException e) {
				// 
				showToastMessage("Error on the request");
				e.printStackTrace();
			}
	    }

		private void addRequestParameters(List<NameValuePair> pairs) {
			// TODO replace with real IV
			String encoded_iv;
			byte[] iv = {0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf};
			encoded_iv = Base64.encodeToString(iv, Base64.DEFAULT);			
			
			int new_nonce;
			try {				
				new_nonce = Integer.parseInt(mss.symmetric_decrypt(nonce, encoded_iv, SESSION_KEY)) + 1;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				new_nonce = -1;
			}
			
			Log.i("CASH", "CHECKLOGIN - new_nonce - " + String.valueOf(new_nonce));
			Log.i("CASH", "CHECKLOGIN - encoded_iv - " + encoded_iv);
			Log.i("CASH", "CHECKLOGIN - SESSION_KEY - " + SESSION_KEY);
			
			String encrypted_nonce = mss.symmetric_encrypt(String.valueOf(new_nonce) , encoded_iv, SESSION_KEY);

			pairs.add(new BasicNameValuePair("id", id));
			pairs.add(new BasicNameValuePair("nonce", encrypted_nonce));
			pairs.add(new BasicNameValuePair("hmac", mss.get_hmac(encrypted_nonce, SESSION_KEY)));
		}
	}
	
	private class RequestTask extends AsyncTask<Void, String, String>{

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
	    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        	addRequestParameters(pairs);
	        return makePostHttpRequest("/redirect", pairs);
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	super.onPostExecute(result);
			try {
				updateStatus(R.string.online);

				JSONObject requestJson = new JSONObject(result);
				JSONObject response = new JSONObject(requestJson.getString("response"));
				String hmac = requestJson.getString("hmac");
				String remainingData = response.getString("remaining_data");
				String content = response.getString("content");

				writeRemainingData(remainingData);
				
				byte[] data = Base64.decode(content, Base64.DEFAULT);
				String html = new String(data, "UTF-8");

				renderString(formatHtmlLink(html));
				hideKeyboard();
			} catch (JSONException | UnsupportedEncodingException
					| NullPointerException e) {
				// 
				e.printStackTrace();
				renderString(e.getMessage());
			}
	    }

		private String formatHtmlLink(String html) {
			return html.replaceAll("href=\"(?!http)", "href=\""+ url);
		}

		private void updateStatus(int text) {
			TextView status = (TextView) findViewById(R.id.connection_status);
	    	status.setText(text);
		}

		private void renderString(String html) {
			WebView webview = (WebView) findViewById(R.id.webView);
			webview.loadDataWithBaseURL(null, html, "text/html", "utf-8", "");
//			webview.loadData(html, "text/html; charset=UTF-8", null);
		}

		private void addRequestParameters(List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair("id", id));
			pairs.add(new BasicNameValuePair("hmac", mss.get_hmac(url, CheckloginTask.SESSION_KEY)));
			// TODO: create JSON request : <TEXT> (	url: <TEXT>, method: "get" | “post”,  params: <JSON> )
			pairs.add(new BasicNameValuePair("request", "XY5/3S5Zs8vrwL+8+uSKBVx4q9u3heOdAUdyKLpyARzNdC3vu9UEF3Fzpj+7aFq+2vHid9YbzpD4YedjCVaneSS/KPh1m47pP5/B4os5GmZqm+85+dG8uk5WKZjQx9eM"));
		}

		private void writeRemainingData(String id) {
			FileManager fileManager = new FileManager(GatewayActivity.this);
			Bundle extras = getIntent().getExtras();
			String supplicant = extras.getString(MainActivity.SUPPLICANT);
			if (supplicant.equals(MainActivity.CLIENT)) {
				fileManager.writeToFile(RegisterActivity.CLIENT_ID, id);
			} else if (supplicant.equals(MainActivity.GATEWAY)) {
				fileManager.writeToFile(RegisterActivity.GATEWAY_ID, id);

			}			
		}
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
	            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
	        }
	        if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	        }
			return true;
		}else if(id == R.id.scan){
           // Launch the DeviceListActivity to see devices and do scan
           Intent serverIntent = new Intent(this, DeviceListActivity.class);
           startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
           return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
