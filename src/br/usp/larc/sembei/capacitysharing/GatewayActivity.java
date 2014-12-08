package br.usp.larc.sembei.capacitysharing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import br.usp.larc.sembei.capacitysharing.bluetooth.DeviceListActivity;
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
			new RequestTask().execute(url);			
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
        	showToastMessage(e.getMessage());
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
				JSONObject response = new JSONObject(requestJson.getString("response"));
				String hmac = requestJson.getString("hmac");
				String remainingData = response.getString("remaining_data");
				String content = response.getString("content");

				byte[] data = Base64.decode(content, Base64.DEFAULT);
				String html = new String(data, "UTF-8");

				hideKeyboard();
			} catch (JSONException | UnsupportedEncodingException
					| NullPointerException e) {
				// 
				e.printStackTrace();
			}
	    }

		private void addRequestParameters(List<NameValuePair> pairs) {
			//		id: <NUM>
			//		token: <TEXT> 20 bytes // encripto NTRU //id, counter, session_key
			//		sig: <TEXT> // assinar ciphertext do token
			//		supplicant: (‘client’ | ‘gateway’)
			// TODO
			String id = "1";
			String token = "iiiiiiiiiiiiiiiiiiii";
			String pkey = "AAhlAikCsQWSAqwDYgTrBKsDNQOcA/MC4AMzBNQA4gICA/IFmwa8BFACNALsBFQDVwQnA5wD/gQpBYMHRQDWATYCQAUZAoAEBgC5Bd0FJwRUAZ0EogLFBScGfwBxAYoAaAURACUGkgTvBGID8AXZAwAB2wFjB3YHUweTB8IGvAArBcEEhQeUAbcCJwVeAS4GBwS/AWUE6AT1BQkHDAOlBi0FvQNMB5MH9wBwA3kBWgbtB0IBVgOSAiAFWAOcAlcCigcyBQ0A1gZTAgoHdgetAUQF0QBTBGwHHAbWADUC6QZWAjUEogZNBd4CHwdxANkFCgUuB9AFFAA1Ac0HLAMcBCMFawJIAiQFTgD9AaAAUwNxBbsBcQObBvQCXwCbBLAA/AAcBMUEwgWPAUsGVgQfBT0HxAXgB/gBNQRUBt4CfwNCAokFVgc0AzYAuAD8ADMGdgQwAMkERgQNAGcDmwB8AZAFBAd/AgQDPQebAAsE6QOnAAQEpABcBvIHBgeSA5kHmwBsAv4GoAb3A/YApgB5BTwEXQAnB2EAagNMA7MEjwDDBqEDHAHzAdUDlgI9At0ChQWhBSoBlwNsAvQBJwatA6sDgwfbBIMHQwEjABYFrQMeAyMFUQcgAowEuQTzB7kGvwMeA7wGMQCSBnwCegOlAQoBIQPuBgwG/QanAgIGWQW/AYwBiQXNB+gGXQJ9BPsFmgTpAnIE3ACRAkcAdwf+BM8CyQHbArQEtwI0BpEA9gJFBgsCyADgAVwARQKtBWsADgKYBiQGvgYABAEEWgdCBy8HKAbVA6MCSAa2A68FgQZGBuEC8gajAC0AlQYVBI4CAQD5B/MBvgE3B0YAcwTrA2EBnAMlBLYC5QOvBQEBPQB/BDoEmgPuBzcAewQQAJUE+gJ2AWkEhAPEBwYD0ASMBiAFbQRZAB8GSwMFBOkEAQExApIA2gV6BwEDLwYZAAcEBQUqB6kC2gc3AKIC6ATUBycCiwXkB6YDWgD5AxAHRgQWBUkA1QYKBB8ERwHoAPgBcwfbADQCogIcASMELwV9BzMEAQXSBNECJgLdAwsEqwIXByMCYgdJBO0AigOdBh4BJAU9A/EEhwPHBUoDxAfbBUsBJgG2BkoA3ACsBJkD+gc7Ak0BTwF7AOwB3wBwA9IFagISAtUHeABQBPYCRQMrA1wEVAXeB2gH5wbIBgsHYwDaADcA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";
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
	
	private class RequestTask extends AsyncTask<String, String, String>{

		private String url;

		@Override
		protected void onPreExecute() {
	    	updateStatus(R.string.loading);
			super.onPreExecute();
		}

	    @Override
	    protected String doInBackground(String... uri) {
	    	setUrl(uri[0]);
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
			return html.replaceAll("href=\"(?!http)", "href=\""+getUrl());
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
			pairs.add(new BasicNameValuePair("id", "2"));
			pairs.add(new BasicNameValuePair("hmac", "WsLhjwwJ/azPBllA2l7LIQ=="));
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
		
		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
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
