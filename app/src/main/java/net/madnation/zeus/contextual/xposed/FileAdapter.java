package net.madnation.zeus.contextual.xposed;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class FileAdapter extends BaseAdapter
{
	private final ArrayList<ImageSelection> images;
	private final int layoutID;
	private final Context context;
	private final int dimen;

	public FileAdapter(Context context, int layoutID, int dimen, ArrayList<ImageSelection> images)
	{
		this.images = images;
		this.layoutID = layoutID;
		this.context = context;
		this.dimen = dimen;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ImageView i;
		View v;

		if(convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			convertView = inflater.inflate(layoutID, null);
			i = (ImageView) convertView.findViewById(R.id.imageView_file);
			v = convertView.findViewById(R.id.file_foreground);

			i.setLayoutParams(new RelativeLayout.LayoutParams(dimen, dimen));
			v.setLayoutParams(new RelativeLayout.LayoutParams(dimen, dimen));
		}
		else
		{
			i = (ImageView) convertView.findViewById(R.id.imageView_file);
			v = convertView.findViewById(R.id.file_foreground);
		}
		i.setImageURI(Uri.fromFile(images.get(position).get()));

		if(images.get(position).isSelected())
		{
			v.setVisibility(View.VISIBLE);
		}
		else
		{
			v.setVisibility(View.INVISIBLE);
		}


		return convertView;
	}

	@Override
	public final int getCount()
	{
		return images.size();
	}

	public final Object getItem(int position)
	{
		return images.get(position);
	}

	public final long getItemId(int position)
	{
		return position;
	}

}