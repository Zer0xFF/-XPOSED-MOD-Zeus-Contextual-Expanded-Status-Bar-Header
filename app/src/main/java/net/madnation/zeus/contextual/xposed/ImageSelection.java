package net.madnation.zeus.contextual.xposed;

import java.io.File;

/**
 * Created by alawi on 15/06/2016.
 */
public class ImageSelection
{
	private File imagefiles;
	private boolean _isSelected;

	public ImageSelection(File file)
	{
		imagefiles = file;
		_isSelected = false;
	}

	public File get()
	{
		return imagefiles;

	}

	public boolean isSelected()
	{
		return _isSelected;
	}

	public void setSelection(boolean isSelected)
	{
		this._isSelected = isSelected;
	}

	public void toggleSelection()
	{
		_isSelected = !_isSelected;
	}
}
