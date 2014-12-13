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

import br.usp.larc.sembei.capacitysharing.crypto.MSSCryptoProvider;
import br.usp.larc.sembei.capacitysharing.crypto.util.FileManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	public static final String CLIENT_ID = "client_id";
	public static final String GATEWAY_ID = "gateway_id";
	public static final String CLIENT_CERT = "client.cert";
	public static final String GATEWAY_CERT = "gateway.cert";

	private String gen_csr;
	private String gen_hmac;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		findViewById(R.id.register_button).setOnClickListener(registerListener);
		
		// TODO remove
		Bundle extras = getIntent().getExtras();
		String supplicant = extras.getString(MainActivity.SUPPLICANT);
		if (supplicant.equals(MainActivity.CLIENT)) {
			((EditText)findViewById(R.id.activation_code_edittext)).setText("02260b2ada9786f76f174ee0c2273f6d");
			((EditText)findViewById(R.id.user_id_edittext)).setText("4");
			((EditText)findViewById(R.id.user_name_edittext)).setText("client");			
		} else if (supplicant.equals(MainActivity.GATEWAY)) {
			((EditText)findViewById(R.id.activation_code_edittext)).setText("904d25e5e6b3cb6a6a131b9d6289202d");
			((EditText)findViewById(R.id.user_id_edittext)).setText("3");
			((EditText)findViewById(R.id.user_name_edittext)).setText("gateway");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	View.OnClickListener registerListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			TextView activationCodeView = (TextView) findViewById(R.id.activation_code_edittext);
			if (activationCodeView.getText().toString().equals("")) {
				Toast toast = Toast.makeText(
						RegisterActivity.this.getApplicationContext(),
						"Please, fill in your activation code", Toast.LENGTH_LONG);
				toast.show();
			} else {
				MSSCryptoProvider mss = new MSSCryptoProvider(RegisterActivity.this);
				new KeyGenTask(mss).execute();
			}
		}
	};
/**
 * Gera par de chaves MSS e chama geração de CSR
 * @author guilherme
 *
 */
	private class KeyGenTask extends
			AsyncTask<Void, Integer, Void> {

		private MSSCryptoProvider mss;

		public KeyGenTask(MSSCryptoProvider mss) {
			this.mss = mss;
		}

		@Override
		protected void onPreExecute() {
			 findViewById(R.id.home_spinner).setVisibility(View.VISIBLE);
			 Toast toast = Toast.makeText(
			 RegisterActivity.this.getApplicationContext(),
			 "Generating Key Pair", Toast.LENGTH_SHORT);
			 toast.show();
			super.onPreExecute();
		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			new CsrGenTask(mss).execute();
		}

		protected Void doInBackground(Void... params) {
			mss.keyGen();
			return null;
		}
	}

	/**
	 * Gera CSR e chama geração de HMAC
	 * @author guilherme
	 *
	 */
	private class CsrGenTask extends
			AsyncTask<Void, Integer, String> {

;
		private MSSCryptoProvider mss;

		public CsrGenTask(MSSCryptoProvider mss) {
			this.mss = mss;
		}

		@Override
		protected void onPreExecute() {
			 Toast toast = Toast.makeText(
			 RegisterActivity.this.getApplicationContext(),
			 "Generating CSR", Toast.LENGTH_SHORT);
			 toast.show();
			super.onPreExecute();
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			new HmacGenTask(mss).execute(result);
		}

		@SuppressLint("UseValueOf")
		@Override
		protected String doInBackground(Void... params) {
			// TODO trocar os 64 1 (authkey)

			String id = ((EditText) findViewById(R.id.user_id_edittext)).getText().toString();
			registerID(id);

			String csr = mss.generateCSR(
					new Integer(id),
					((EditText) findViewById(R.id.user_name_edittext)).getText().toString(),
					"MTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExCg==",
					mss.getPkey(),
					mss.getSkey());

			gen_csr = csr;

			return csr;
		}

		private void registerID(String id) {
			FileManager fileManager = new FileManager(RegisterActivity.this);
			Bundle extras = getIntent().getExtras();
			String supplicant = extras.getString(MainActivity.SUPPLICANT);
			if (supplicant.equals(MainActivity.CLIENT)) {
				fileManager.writeToFile(CLIENT_ID, id);
			} else if (supplicant.equals(MainActivity.GATEWAY)) {
				fileManager.writeToFile(GATEWAY_ID, id);

			}
		}
	}

	/**
	 * Gera HMAC e chama getCertificate
	 * @author guilherme
	 *
	 */
	private class HmacGenTask extends
			AsyncTask<String, Integer, String> {
		private MSSCryptoProvider mss;

		public HmacGenTask(MSSCryptoProvider mss) {
			this.mss = mss;
		}

		@Override
		protected void onPreExecute() {
			 Toast toast = Toast.makeText(
			 RegisterActivity.this.getApplicationContext(),
			 "Generating Hmac", Toast.LENGTH_SHORT);
			 toast.show();
			super.onPreExecute();
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new RequestTask().execute();
		}

		@Override
		protected String doInBackground(String... params) {
			String csr = params[0];

			String hmac = mss.get_hmac(csr,
					((EditText) findViewById(R.id.activation_code_edittext)).getText().toString());

			gen_hmac = hmac;

			return hmac;
		}
	}

	/**
	 * Obtem Certificate a partir de um Http request
	 * @author guilherme
	 *
	 */
	private class RequestTask extends AsyncTask<Void, String, String>{
		@Override
		protected void onPreExecute() {
			 Toast toast = Toast.makeText(
			 RegisterActivity.this.getApplicationContext(),
			 "Getting Certificate", Toast.LENGTH_SHORT);
			 toast.show();
			super.onPreExecute();
		}

	    @Override
	    protected String doInBackground(Void... params) {

	        return makeHttpRequest();
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	super.onPostExecute(result);
			try {
				JSONObject requestJson = new JSONObject(result);
				String certificate = requestJson.getString("certificate");

				registerCertificate(certificate);

				findViewById(R.id.home_spinner).setVisibility(View.INVISIBLE);
				Bundle extras = getIntent().getExtras();
				String supplicant = extras.getString(MainActivity.SUPPLICANT);

				if (supplicant.equals(MainActivity.CLIENT)) {
					startActivity(new Intent(RegisterActivity.this,
							ClientActivity.class));
				} else if (supplicant.equals(MainActivity.GATEWAY)) {
					startActivity(new Intent(RegisterActivity.this,
							GatewayActivity.class));
				}
			} catch (JSONException | NullPointerException e) {
				// TODO Auto-generated catch
				e.printStackTrace();
				Toast toast = Toast.makeText(
						RegisterActivity.this.getApplicationContext(),
						"Error on request", Toast.LENGTH_SHORT);
				toast.show();
				findViewById(R.id.home_spinner).setVisibility(View.INVISIBLE);

				Log.e("", "ERROR on RegisterActivity#RequestTask: ");
			}
	    }

	    private String makeHttpRequest() {
			HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	        	HttpPost post = new HttpPost(getString(R.string.AAAS_URL) + "/register");

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
			pairs.add(new BasicNameValuePair("id",
					((EditText) findViewById(R.id.user_id_edittext)).getText().toString()));
			pairs.add(new BasicNameValuePair("csr", gen_csr));
			pairs.add(new BasicNameValuePair("tag", gen_hmac));
		}

		private void registerCertificate(String cert) {
			FileManager fileManager = new FileManager(RegisterActivity.this);
			Bundle extras = getIntent().getExtras();
			String supplicant = extras.getString(MainActivity.SUPPLICANT);
			if (supplicant.equals(MainActivity.CLIENT)) {
				fileManager.writeToFile(CLIENT_CERT, cert);
			} else if (supplicant.equals(MainActivity.GATEWAY)) {
				fileManager.writeToFile(GATEWAY_CERT, cert);

			}
		}
	}


}
