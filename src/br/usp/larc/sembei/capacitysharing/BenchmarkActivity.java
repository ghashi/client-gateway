package br.usp.larc.sembei.capacitysharing;

import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import br.usp.larc.sembei.capacitysharing.crypto.MSSCryptoProvider;

public class BenchmarkActivity extends Activity {
	
	private static final int BENCHMARK_KEYGEN = 0;
	private static final int BENCHMARK_SIGN = 1;
	private static final int BENCHMARK_VERIFY = 2;
	private static final int BENCHMARK_TOTAL = 3;
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_benchmark);
		
		Intent intent = getIntent();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.benchmark, menu);
		
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

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return BENCHMARK_TOTAL;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case BENCHMARK_KEYGEN:
				return getString(R.string.benchmark_title_keygen).toUpperCase(l);
			case BENCHMARK_SIGN:
				return getString(R.string.benchmark_title_sign).toUpperCase(l);
			case BENCHMARK_VERIFY:
				return getString(R.string.benchmark_title_verify).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private static int index = BENCHMARK_KEYGEN;
		private int view;

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment(index++);
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment(int index) {
			view = index % BENCHMARK_TOTAL;
		}
		
		public int getViewIndex() {
			return view;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {	
			
			View rootView = null;
			
			switch(view) {
				case BENCHMARK_KEYGEN:
					rootView = inflater.inflate(R.layout.fragment_benchmark_keygen,
							container, false);
					break;
				case BENCHMARK_SIGN:
					rootView = inflater.inflate(R.layout.fragment_benchmark_sign,
							container, false);
					break;
				case BENCHMARK_VERIFY:
					rootView = inflater.inflate(R.layout.fragment_benchmark_verify,
							container, false);
					break;
			}
			
			return rootView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			setRetainInstance(true);
			switch(view) {
				case BENCHMARK_KEYGEN:
					getActivity().findViewById(R.id.button_benchmark_keygen_start).setOnClickListener(
							benchmarkListener);
					break;
				case BENCHMARK_SIGN:
					getActivity().findViewById(R.id.button_benchmark_sign_start).setOnClickListener(
							benchmarkListener);
					break;
				case BENCHMARK_VERIFY:
					getActivity().findViewById(R.id.button_benchmark_verify_start).setOnClickListener(
							benchmarkListener);
					break;
			}
		}
		
		private class BenchmarkTask extends AsyncTask<Integer, Integer, Long> {
			

			private static final int BENCHMARK_MARK_KEYGEN = 2;
			private static final int BENCHMARK_MARK_SIGN = 1000;
			private static final int BENCHMARK_MARK_VERIFY = 1000;

			private int benchmark;
			
			public int[] getId(int benchmark) {
				int[] id = new int[2];
				switch(benchmark) {
	 				case R.id.button_benchmark_keygen_start:
	 					id[0] = R.id.benchmark_keygen_output;
	 					id[1] = R.id.keygen_spinner;
	 					break;
	 				case R.id.button_benchmark_sign_start:
	 					id[0] = R.id.benchmark_sign_output;
	 					id[1] = R.id.sign_spinner;
	 					break;
	 				case R.id.button_benchmark_verify_start:
	 					id[0] = R.id.benchmark_verify_output;
	 					id[1] = R.id.verify_spinner;
	 					break;
				}
				return id;
			}
			
		     protected void onProgressUpdate(Integer... progress) {
		    	 getActivity().setProgress(progress[0]);
		     }

		     protected void onPostExecute(Long elapsedTime) {
				TextView output = (TextView) getActivity().findViewById(getId(benchmark)[0]);
				switch(benchmark) {
 					case R.id.button_benchmark_keygen_start:
 						if(BENCHMARK_MARK_KEYGEN == 1)
 							output.setText("Key generated in " + elapsedTime + "ms");
 						else
 							output.setText(BENCHMARK_MARK_KEYGEN + " keys generated in " + elapsedTime + "ms");
 						break;
 					case R.id.button_benchmark_sign_start:
 						output.setText(BENCHMARK_MARK_SIGN + " messages signed in " + elapsedTime + "ms");
 						break;
 					case R.id.button_benchmark_verify_start:
 						output.setText(BENCHMARK_MARK_VERIFY + " signatures verified in " + elapsedTime + "ms");
 						break;
				}
				getActivity().findViewById(getId(benchmark)[1]).setVisibility(View.INVISIBLE);
		     }

			@Override
			protected Long doInBackground(Integer... params) {
				benchmark = params[0];
				long elapsedTime;
				
				switch(benchmark) {
	    	 		case R.id.button_benchmark_keygen_start:
	    	 			elapsedTime = MSSCryptoProvider.keyGen(BENCHMARK_MARK_KEYGEN);
	    	 			break;
	    	 		case R.id.button_benchmark_sign_start:
	    	 			elapsedTime = MSSCryptoProvider.sign(BENCHMARK_MARK_SIGN);
	    	 			break;
	    	 		case R.id.button_benchmark_verify_start:
	    	 			elapsedTime = MSSCryptoProvider.verify(BENCHMARK_MARK_VERIFY);
	    	 			break;
	    	 		default:
	    	 			elapsedTime = -1;
	    	 			break;
				}
				
				return elapsedTime;
			}
		 }
		
		View.OnClickListener benchmarkListener = new View.OnClickListener() {

			public void onClick(View v) {
				BenchmarkTask task = new BenchmarkTask();
				TextView output = (TextView) getActivity().findViewById(task.getId(v.getId())[0]);
				output.setText("");
				getActivity().findViewById(task.getId(v.getId())[1]).setVisibility(View.VISIBLE);
				task.execute(v.getId());
			}
			
			
		};
				
	}
}
