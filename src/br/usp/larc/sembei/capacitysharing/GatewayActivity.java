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

public class GatewayActivity extends SupplicantActivity {
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// It is important to call setContentView before super.onCreate
		setContentView(R.layout.activity_gateway);
		
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
		new RequestTask().execute(url);
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
	        return makeHttpRequest(uri);
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

				byte[] data = Base64.decode(content, Base64.DEFAULT);
				String html = new String(data, "UTF-8");

				renderString(formatHtmlLink(html));
				hideKeyboard();
			} catch (JSONException | UnsupportedEncodingException
					| NullPointerException e) {
				// TODO Auto-generated catch
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

	    private String makeHttpRequest(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	        	HttpPost post = new HttpPost(getString(R.string.PROXY_URL) + "/redirect");

	        	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
	        	addRequestParameters(pairs);
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
	            //TODO Handle problems..
	        } catch (IOException e) {
	        	Log.e("IOException", e.getMessage());
	            //TODO Handle problems..
	        }
	        return responseString;
		}

		private void addRequestParameters(List<NameValuePair> pairs) {
			pairs.add(new BasicNameValuePair("id", "2"));
			pairs.add(new BasicNameValuePair("hmac", "WsLhjwwJ/azPBllA2l7LIQ=="));
			pairs.add(new BasicNameValuePair("request", "XY5/3S5Zs8vrwL+8+uSKBVx4q9u3heOdAUdyKLpyARzNdC3vu9UEF3Fzpj+7aFq+2vHid9YbzpD4YedjCVaneSS/KPh1m47pP5/B4os5GmZqm+85+dG8uk5WKZjQx9eM"));
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
