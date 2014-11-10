package br.usp.larc.sembei.capacitysharing.crypto.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class FileManager {
	
	Activity activity;
	
	public FileManager(Activity activity) {
		this.activity = activity;
	}

	public boolean existFile(String filename) {
		File file = activity.getFileStreamPath(filename);
		return file.exists();
	}
	
	public void writeToFile(String filename, String content) {
		try {
			FileOutputStream fos = activity.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String readFile(String filename) {
		String content = new String();
		try {
			FileInputStream fin = activity.openFileInput(filename);
			int c;
			while((c = fin.read()) != - 1) {
				content += Character.toString((char) c);
			}
			fin.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
	
	
}
