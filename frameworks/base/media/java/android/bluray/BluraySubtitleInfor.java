package android.bluray;

/*
* this file is defined by hh@rock-chips.com
* Define the some Bluray Audio inforamtions  
*
*/
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.String;

import android.util.Log;

public class BluraySubtitleInfor  implements Parcelable
{
	// The Sutitle Code Type 编码
	public static final int Sutitle_Code_PG = 0x90;    // PG 图片字幕
	public static final int Sutitle_Code_TEXT = 0x92;  // Text 文字字幕

	// The Text Subtitle Character Code
	public static final int Sutitle_Character_UTF8 = 0x01;
	public static final int Sutitle_Character_UTF16BE = 0x02;
	public static final int Sutitle_Character_Shirf_JIS = 0x03;
	public static final int Sutitle_Character_KSC = 0x04;
	public static final int Sutitle_Character_GB18030= 0x05;
	public static final int Sutitle_Character_GB2312 = 0x06;
	public static final int Sutitle_Character_BIG5 = 0x07;

	// 位于playList中的位置，用于换台
	public int mIndex = -1;
	public int mType  = -1;
	// this field is valid only when mType == Sutitle_Code_TEXT
	public int mCharacter = -1;
	public int mPid = -1;
	public String mLang = null;
	
	private BluraySubtitleInfor(Parcel in)	
	{
		mIndex = in.readInt();
		mType = in.readInt();		
		mCharacter = in.readInt();
		mPid = in.readInt();
		mLang = in.readString();
	}

	public static final Parcelable.Creator<BluraySubtitleInfor> CREATOR =            
		new Parcelable.Creator<BluraySubtitleInfor>()     	
	{       	
		public BluraySubtitleInfor createFromParcel(Parcel in)         	
		{            	
			return new BluraySubtitleInfor(in);        	
		}  
		
		public BluraySubtitleInfor[] newArray(int size) 		
		{            	
			return new BluraySubtitleInfor[size];        	
		}    	
	};		

	public void writeToParcel(Parcel out, int flags) 	
	{
		out.writeInt(mIndex);
		out.writeInt(mType);		
		out.writeInt(mCharacter);
		out.writeInt(mPid);	
		out.writeString(mLang);	
	}	  

	public int describeContents() 	
	{        	
		return 0;    	
	}

	public void tostring()
	{
		Log.d("Bluray Subtitle","mIndex = "+mIndex);
		Log.d("Bluray Subtitle","Type = "+mType);
		Log.d("Bluray Subtitle","mCharacter = "+mCharacter);
		Log.d("Bluray Subtitle","mPid = "+mPid);
		Log.d("Bluray Subtitle","mLang = "+mLang);
	}
}

