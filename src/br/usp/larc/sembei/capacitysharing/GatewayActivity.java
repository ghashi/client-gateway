package br.usp.larc.sembei.capacitysharing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
//		webSettings.setJavaScriptEnabled(true);
//		webSettings.setDefaultTextEncodingName("utf-8");
		webview.setWebViewClient(new MyWebViewClient());
	}

	private void makeHttpRequest(String url){
		new RequestTask().execute(url);
	}

	private class RequestTask extends AsyncTask<String, String, String>{
		@Override
		protected void onPreExecute() {
	    	updateStatus(R.string.loading);
			super.onPreExecute();
		}

	    @Override
	    protected String doInBackground(String... uri) {
	        return makeHttpRequest(uri);
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	super.onPostExecute(result);
	    	updateStatus(R.string.online);
	        renderString(result);
	    }

		private void updateStatus(int text) {
			TextView status = (TextView) findViewById(R.id.connection_status);
	    	status.setText(text);
		}

		private void renderString(String html) {
			WebView webview = (WebView) findViewById(R.id.webView);
			webview.loadData(html, "text/html; charset=UTF-8", null);
		}

	    private String makeHttpRequest(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(uri[0]));
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
	}

  private class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        if (Uri.parse(url).getHost().equals("www.google.com.br")) {
//            // This is my web site, so do not override; let my WebView load the page
//            return false;
//        }
//        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        startActivity(intent);
    	System.out.println(url);
    	makeHttpRequest(url);
        return true;
    }
}
}
