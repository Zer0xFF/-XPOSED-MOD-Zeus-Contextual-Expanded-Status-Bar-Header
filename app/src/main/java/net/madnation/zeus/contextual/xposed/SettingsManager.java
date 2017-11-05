package net.madnation.zeus.contextual.xposed;

import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SettingsManager
{
	static final String PREF_ENABLE_HOURLY_UPDATE = "EnableHourlyUpdate";
	static final String PREF_ENABLE_CUSTOM_IMAGES = "EnableCustomImages";
 	private final FileObserver _fileObserver;


	private String path = Environment.getExternalStorageDirectory().getPath() + "/ZCESH_BG/";
	private String _configFile = "config.json";
	private HashMap<String, Boolean> boolPref = new HashMap<>();
	private boolean _isReloadRequired;
	private boolean _isModified;

	public SettingsManager()
	{
		loadSettings();
		_fileObserver = new FileObserver(path)
		{
			@Override
			public void onEvent(int event, String path)
			{
				switch(event)
				{
					case FileObserver.MODIFY:
					case FileObserver.CREATE:
					case FileObserver.DELETE:
						if(path.endsWith(_configFile))
						{
							_isReloadRequired = true;
						}
						_isModified = true;
				}
			}
		};
		_fileObserver.startWatching();
	}

	private void saveSettings()
	{
		JSONObject jsonArr = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try
		{
			for(Map.Entry<String, Boolean> pref : boolPref.entrySet())
			{
				jsonData.put(pref.getKey(), pref.getValue());
			}
			jsonArr.put("bool", jsonData);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		writeToFile(jsonArr.toString());
	}

	private boolean loadSettings()
	{
		String file = readFromFile();
		try
		{
			JSONObject jsonArr = new JSONObject(file);
			JSONObject jsonData = jsonArr.getJSONObject("bool");
			Iterator it = jsonData.keys();
			while(it.hasNext())
			{
				String prefname = it.next().toString();
				boolPref.put(prefname, jsonData.getBoolean(prefname));
			}
			_isReloadRequired = false;
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			_isReloadRequired = true;
		}
		return !_isReloadRequired;
	}

	private void writeToFile(String data)
	{
		File dir = new File(path);
		if(!dir.exists())
			dir.mkdirs();
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(path, _configFile));
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		}
		catch(IOException e)
		{
			Log.e("Zeus_SettingMan_Except", "File write failed: " + e.toString());
		}
	}

	private String readFromFile()
	{

		String ret = "";

		try
		{
			FileInputStream fis = new FileInputStream(new File(path, _configFile));
			if(fis != null)
			{
				InputStreamReader inputStreamReader = new InputStreamReader(fis);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while((receiveString = bufferedReader.readLine()) != null)
				{
					stringBuilder.append(receiveString);
				}

				fis.close();
				ret = stringBuilder.toString();
			}
		}
		catch(FileNotFoundException e)
		{
			Log.e("Zeus_SettingMan", "File not found: " + e.toString());
		}
		catch(IOException e)
		{
			Log.e("Zeus_SettingMan", "Can not read file: " + e.toString());
		}

		return ret;
	}

	void setBooleanPref(String name, boolean value)
	{
		if(isReloadRequired())
		{
			reload();
		}

		boolPref.put(name, value);
		saveSettings();
	}

	boolean getBooleanPref(String name, boolean defVal)
	{
		if(isReloadRequired())
		{
			reload();
		}

		return boolPref.get(name) == null ? defVal : boolPref.get(name);
	}

	private boolean reload()
	{
		boolPref = new HashMap<>();
		return loadSettings();
	}

	private boolean isReloadRequired()
	{
		return _isReloadRequired;
	}

	boolean isModified()
	{
		Boolean res = _isModified;
		_isModified = false;
		return res;
	}
}
