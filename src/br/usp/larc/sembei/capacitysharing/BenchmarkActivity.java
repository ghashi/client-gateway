package br.usp.larc.sembei.capacitysharing;

import java.util.Locale;

import br.usp.larc.sembei.capacitysharing.crypto.CryptoProvider;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BenchmarkActivity extends Activity {

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
		setContentView(R.layout.activity_benchamark);
		
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
		getMenuInflater().inflate(R.menu.benchamark, menu);
		
		/*
		 * Beginning of the modified area
		 */
		
		
		findViewById(R.id.button_bench_keygen).setOnTouchListener(
				benchmarkKeyGenListener);
		
		findViewById(R.id.button_bench_sign).setOnTouchListener(
				benchmarkSignListener);
		
		findViewById(R.id.button_bench_verify).setOnTouchListener(
				benchmarkVerifyListener);
		
		/*
		 * End of the modified area
		 */
		
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
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.benchmark_title_keygen).toUpperCase(l);
			case 1:
				return getString(R.string.benchmark_title_signing).toUpperCase(l);
			case 2:
				return getString(R.string.benchmark_title_verification).toUpperCase(l);
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

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_benchamark,
					container, false);
			return rootView;
		}
	}

	/*
	 * Beginning of the modified area
	 */
	
	View.OnTouchListener benchmarkKeyGenListener = new View.OnTouchListener() {

		public boolean onTouch(View arg0, MotionEvent arg1) {
			long elapsedTime = CryptoProvider.benchKeyGen();
			TextView output = (TextView) findViewById(R.id.benchmark_output);
			output.setText("Key Generated in " + elapsedTime + "ms");
			return true;
		}
		
	};
	
	View.OnTouchListener benchmarkSignListener = new View.OnTouchListener() {

		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}
		
	};
	
	View.OnTouchListener benchmarkVerifyListener = new View.OnTouchListener() {

		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}
		
	};
	
	/*
	 * End of the modified area
	 */
	
}
