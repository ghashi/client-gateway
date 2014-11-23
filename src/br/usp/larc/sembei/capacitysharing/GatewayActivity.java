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

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;
import android.widget.TextView;

public class GatewayActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gateway);

		configureWebView();
		setSearchListener();
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
		}
		return super.onOptionsItemSelected(item);
	}

	private void setSearchListener(){
		SearchView searchView = (SearchView) findViewById(R.id.url_bar);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				makeHttpRequest(query);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}
		});
	}
	private void configureWebView() {

		WebView webview = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webview.setWebViewClient(new MyWebViewClient());
	}

	private void makeHttpRequest(String url){
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
	        	HttpPost post = new HttpPost(getString(R.string.AAAS_URL));

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

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			makeHttpRequest(url);
			return true;
		}
	}

  private void hideKeyboard() {
	    InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

	    // check if no view has focus:
	    View view = this.getCurrentFocus();
	    if (view != null) {
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
}
