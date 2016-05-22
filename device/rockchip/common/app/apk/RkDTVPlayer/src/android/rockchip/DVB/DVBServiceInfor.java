/************************************************************
  Copyright (C), 2010-2020, Rockchip Electronics. Co., Ltd.
  Description:    Rockchip DVB      
  Version:        1.0
  History:        
      <author>     <time>    <version >    <desc>
      aiyoujun   2010/11/08     1.0      build this moudle  
***********************************************************/

package android.rockchip.DVB;

import java.lang.String;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;

public class DVBServiceInfor implements Parcelable
{
	private int mServiceId;                       
	private int mFreq;                   
	private int mType;                      
	private int mCollect;                      
	private int mEncrypt;   
	private int mFree;                
	private String  mName;
	private byte[]  mNameArray;	
	
	public DVBServiceInfor(int serviceId,int freq,int type,int collect,int encrypt,int free,String name, byte[] nameArray)
	{
	    mServiceId = serviceId;                       
		mFreq = freq;                   
		mType = type;                      
		mCollect = collect;                      
		mEncrypt = encrypt;   
		mFree = free;                
		mName = name;
		mNameArray = nameArray;		
	}
	

	public int getServiceId()
	{
		return mServiceId;
	}

	public int getFreq()
	{
		return mFreq;
	}
	
	public int getType()
	{
		return mType;
	}

	public int getCollect()
	{
		return mCollect;
	}

	public int getEncrypt()
	{
		return mEncrypt;
	}

	public int getLCN()
	{
		return mFree;
	}

	public String getName()
	{
		return mName;
	}

	public byte[] getNameArray()
	{
		return mNameArray;
	}	

	private DVBServiceInfor(Parcel in)
	{
	    mServiceId = in.readInt();
		mFreq = in.readInt();
		mType = in.readInt();
		mCollect = in.readInt();
		mEncrypt = in.readInt();
		mFree = in.readInt();
		mName = in.readString();
		for(int i = 0; i < 32; i++)
	    {
	        mNameArray[i] =  in.readByte();
	    }
	}

	public static final Parcelable.Creator<DVBServiceInfor> CREATOR =
    new Parcelable.Creator<DVBServiceInfor>() 
	{
       	public DVBServiceInfor createFromParcel(Parcel in) 
        	{
            	return new DVBServiceInfor(in);
        	}
        
    	public DVBServiceInfor[] newArray(int size) 
		{
            	return new DVBServiceInfor[size];
    	}
	};
	
	public void writeToParcel(Parcel out, int flags) 
	{
    	out.writeInt(mServiceId);
    	out.writeInt(mFreq);
       	out.writeInt(mType);
    	out.writeInt(mCollect);
    	out.writeInt(mEncrypt);
		out.writeInt(mFree);
    	out.writeString(mName);
		for(int i = 0; i < 32; i++)
	    {
	         out.writeByte(mNameArray[i]);
	    }		
   	}	
	public int describeContents() 
	{
    	return 0;
	}	
}
