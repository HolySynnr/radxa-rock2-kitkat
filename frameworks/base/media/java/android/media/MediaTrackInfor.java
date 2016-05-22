package android.media;




/*
* this file is defined by hh@rock-chips.com to get video/audio/subtitle's Tracks information
* 
*
*/
import android.os.Parcel;
import android.os.Parcelable;

import android.util.Log;


public class MediaTrackInfor implements Parcelable
{
	public static final int UNKNOW = 0;
	public static final int VIDEO = 1;
	public static final int AUIDO = 2;
	public static final int SUBTITLE = 3;

	public String TAG = "MediaTrackInfor";

	/*
	* 用于保存TrackInfor内型
	*/
	public int mType = UNKNOW;

	public int mCodeId = -1;
	
	/*
	* 用于保存编码类型
	*/
	public String mCodeName = "";

	/*
	* 视频帧率
	*/
	int          mFrameRate = -1;

	/*
	* 视频分辨率
	*/
	int          mWidth = -1;
	int          mHeight = -1;

	/*
	* 音频通道数
	*/
	public int    mChannel  = -1;

	/*
	* 音频采用率
	*/
	public int    mSample = -1;


	/*
	*  字幕内型
	*/
	public int    mSubtitleType = -1;

	/*
	*
	*/ 
	public String mLanguage = "";

	/*
	* audio/subitle 索引值，可通过该索引来切换音轨/字幕，取值从0开始,该值只对audio/subtitle有意义
	*/
	public int   mIndex = 0;
	

	private MediaTrackInfor(Parcel in)	
	{		
		mType = in.readInt();
		mCodeId = in.readInt();
		mCodeName = in.readString();

		Log.d(TAG,"MediaTrackInfor, type = "+mType+",mCodeId = "+mCodeId+",mCodeName = "+mCodeName);
		if(mType == VIDEO)
		{
			mFrameRate = in.readInt();		
			mWidth = in.readInt();
			mHeight = in.readInt();
			Log.d(TAG,"MediaTrackInfor, mWidth = "+mWidth+",mHeight = "+mHeight);
			Log.d(TAG,"MediaTrackInfor, mFrameRate = "+mFrameRate);
		}
		else if(mType == AUIDO)
		{
			mIndex = in.readInt();
			mChannel = in.readInt();
			mSample = in.readInt();		
			Log.d(TAG,"MediaTrackInfor, mChannel = "+mType+",mSample = "+mSample);
		}
		else if(mType == SUBTITLE)
		{
			mIndex = in.readInt();
			mSubtitleType = in.readInt();
			Log.d(TAG,"MediaTrackInfor, mSubtitleType = "+mSubtitleType);
		}
		mLanguage = in.readString();
		Log.d(TAG,"MediaTrackInfor, mLanguage = "+mLanguage);
	}

	public static final Parcelable.Creator<MediaTrackInfor> CREATOR =            
		new Parcelable.Creator<MediaTrackInfor>()     	
	{       	
		public MediaTrackInfor createFromParcel(Parcel in)         	
		{            	
			return new MediaTrackInfor(in);        	
		}  
		
		public MediaTrackInfor[] newArray(int size) 		
		{            	
			return new MediaTrackInfor[size];        	
		}    	
	};		

	public void writeToParcel(Parcel out, int flags) 	
	{        	
		out.writeInt(mType);
		out.writeString(mCodeName);	
		if(mType == VIDEO)
		{
			out.writeInt(mFrameRate);
			out.writeInt(mWidth);
			out.writeInt(mHeight);
			out.writeInt(mFrameRate);
		}
		else if(mType == AUIDO)
		{	
			out.writeInt(mIndex);
			out.writeInt(mChannel);
			out.writeInt(mSample);
		}
		else if(mType == SUBTITLE)
		{
			out.writeInt(mIndex);
			out.writeInt(mSubtitleType);
		}
		out.writeString(mLanguage);
		
	}	  

	public int describeContents() 	
	{        	
		return 0;    	
	}
}

