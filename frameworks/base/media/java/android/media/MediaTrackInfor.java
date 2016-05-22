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
	* ���ڱ���TrackInfor����
	*/
	public int mType = UNKNOW;

	public int mCodeId = -1;
	
	/*
	* ���ڱ����������
	*/
	public String mCodeName = "";

	/*
	* ��Ƶ֡��
	*/
	int          mFrameRate = -1;

	/*
	* ��Ƶ�ֱ���
	*/
	int          mWidth = -1;
	int          mHeight = -1;

	/*
	* ��Ƶͨ����
	*/
	public int    mChannel  = -1;

	/*
	* ��Ƶ������
	*/
	public int    mSample = -1;


	/*
	*  ��Ļ����
	*/
	public int    mSubtitleType = -1;

	/*
	*
	*/ 
	public String mLanguage = "";

	/*
	* audio/subitle ����ֵ����ͨ�����������л�����/��Ļ��ȡֵ��0��ʼ,��ֵֻ��audio/subtitle������
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

