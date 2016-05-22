package android.bluray;

/*
*  this file is defined by hh@rock-chips.com
*  BlurayManager���ڶ�����������ĺ�ͻ�ȡ��Ҫ��UI����ʾ�����������Ϣ
*  BlurayManager�е���ز�����Ҫͨ��MediaPlayer����غ���ʵ�֣��䶨�����ر����ͺ��ֵ��Ҫ
*  ��rkBoxPlayerͷ�ļ��еĺ��Ӧ�����BlurayManager�еĺ�ͱ���������ı䣬����ᵼ�²���ʧ��
*  ���ļ��ж���Ĳ���ֻ���ڲ�������ʱ��Ч
*/
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.File;
import android.bluray.*;
import android.net.Uri;

import android.util.Log;

public class BlurayManager
{
	private final static String TAG = "BlurayManager";
	private MediaPlayer mMediaPlayer = null;
	
	// �������������Ϣ��ʼ����Ҫ��RkFrameManage.h�еĶ�Ӧ
	private static final int BLURAY_BASE = 7000;
	// ��ȡ��ǰ���ڲ�����Ƶ��Ϣ
	public static final int BLURAY_GET_VIDEO_INFOR = BLURAY_BASE;
	// ��ȡ��ǰ���ڲ��ŵ�������Ϣ
	public static final int BLURAY_GET_AUDIO_INFOR = BLURAY_BASE+1;
	// ��ȡ��ǰ���ڲ��ŵ���Ļ��Ϣ
	public static final int BLURAY_GET_SUBTITLE_INFOR = BLURAY_BASE+2;

	// ��ȡ��ǰ���ŵ�Title����������
	public static final int BLURAY_GET_AUDIO_TRACK = BLURAY_BASE+3;
	// ��ȡ��ǰ����Title��������Ļ��Ϣ
	public static final int BLURAY_GET_SUBTITLE_TRACK = BLURAY_BASE+4;
	// �����л�
	public static final int BLURAY_SET_AUDIO_TRACK = BLURAY_BASE+5;
	// ��Ļ�л�
	public static final int BLURAY_SET_SUBTITLE_TRACK = BLURAY_BASE+6;
	
	// �������غ���ʾ(ֻ��Pop-up Menu�ܹ����ػ�����ʾ,Always-on Menu����������)
	// BLURAY_SURFACE_SHOW/BLURAY_SURFACE_HIDE
	public static final int BLURAY_SET_IG_VISIBLE = BLURAY_BASE+7;
	public static final int BLURAY_GET_IG_VISIBLE = BLURAY_BASE+8;
	
	// ��Ļ���غ���ʾ
	// ��������BLURAY_SURFACE_SHOW/BLURAY_SURFACE_HIDE
	public static final int BLURAY_SET_SUBTITLE_VISIBLE = BLURAY_BASE+9;
	public static final int BLURAY_GET_SUBITTLE_VISIBLE = BLURAY_BASE+10;
	
	// ������ť����
	// �������� BLURAY_IG_BUTTON_MOVE_UP
	//          BLURAY_IG_BUTTON_MOVE_DOWN
	//          BLURAY_IG_BUTTON_MOVE_LEFT
	//          BLURAY_IG_BUTTON_MOVE_RIGHT
	//          BLURAY_IG_BUTTON_ACTIVATE
	public static final int BLURAY_SET_IG_OPERATION = BLURAY_BASE+11;

	// ��ȡ��ǰTitle�����½���
	public static final int BLURAY_GET_CHAPTER = BLURAY_BASE+12;
	// ���ŵ�ǰTitle��ĳһ�½�
	public static final int BLURAY_PLAY_CHAPTER = BLURAY_BASE+13;
	// ��ȡ��ǰ���ڲ��ŵ��½�
	public static final int BLURAY_GET_CURRENT_CHAPTER = BLURAY_BASE+14;

	// ��ȡ��ǰTitle��Angle��
	public static final int BLURAY_GET_NUMBER_OF_ANGLE = BLURAY_BASE+15;
	// ��ȡ��ǰTitle�����ڲ��ŵ�Angle
	public static final int BLURAY_GET_CURRENT_ANGLE = BLURAY_BASE+16;
	// ���ŵ�ǰTitle��Angle not support
	public static final int BLURAY_PLAY_ANGLE = BLURAY_BASE +17;

	/* 
	*  �����Title��Ϊ3��: FirstPlay(AutoRun) Title��Top Menu Title����ͨ��Title
	*  FirstPlay(AutoRun) Title Ϊ�����������ʱ�Զ���ʼ���ŵ�Title
	*  Top Menu Title Ϊ���������Ӧ��Title
	*  ��ͨ��Title Ϊ��������Ƶ���ŵ�Title
	*/
	// ��ȡTitle����,�˴���Title����������FirstPlay(AutoRun) Title��Top Menu Title
	public static final int BLURAY_GET_NUMBER_OF_TITLE = BLURAY_BASE+18;
	// ��ȡ��ǰ���ڲ��ŵ�Title
	public static final int BLURAY_GET_CURRENT_TITLE = BLURAY_BASE+19;
	// ���ò���ĳ��Title
	public static final int BLURAY_PLAY_TITLE = BLURAY_BASE+20;
	// ���ò���Top Title
	public static final int BLURAY_PLAY_TOP_TITLE = BLURAY_BASE+21;

	// ������ǰ�������ݣ����ź�������
	public static final int BLURAY_SKIP_CURRENT_CONTEXT = BLURAY_BASE+22;
	
	public static final int BLURAY_PLAY_SEEK_TIME_PLAY = BLURAY_BASE+23;

	// ���ڲ�ѯ�Ƿ��н����������˵�
	public static final int BLUERAYP_PLAY_QUERY_NAVIGATION_MENU = BLURAY_BASE+24;


	// ��Ļ/��������ʾ
	public static final int BLURAY_SURFACE_SHOW = 1;
	// ��Ļ/����������
	public static final int BLURAY_SURFACE_HIDE = 0;

	// ������ť�ƶ�����ͼ��ֻ��������Ϣ��������Ϣ������
	// �����ƶ�
	public static final int BLURAY_IG_BUTTON_MOVE_UP = 0;  
	// �����ƶ�
	public static final int BLURAY_IG_BUTTON_MOVE_DOWN = 1;
	// �����ƶ�
	public static final int BLURAY_IG_BUTTON_MOVE_LEFT = 2;
	// �����ƶ�
	public static final int BLURAY_IG_BUTTON_MOVE_RIGHT = 3;
	// ���ǰ��ť
	public static final int BLURAY_IG_BUTTON_ACTIVATE = 4;


	// �����ɹ�
	public static final int BLURAY_OPERATION_SUCCESS = 0;
	// ����ʧ��
	public static final int BLURAY_OPERATION_FAIL = 1;
	// ��ֹ��ǰ����
	public static final int BLURAY_OPERATION_FAIL_OPEATION_DISABLE = 2;
	// ����������
	public static final int BLURAY_OPERATION_FAIL_PARAMETER_ERROR = 3;
	// BDJģʽ����֧�ֵ�ǰ�˲���
	public static final int BLURAY_OPERATION_FAIL_BDJ = 3;
  
	// add by hh for bluray
	private static final int BLURAY_OPERATION_BASE = 8000;
	public static final int MEDIA_BLURAY_ISO_MOUNT_START  = BLURAY_OPERATION_BASE;
    public static final int MEDIA_BLURAY_ISO_MOUNT_END = BLURAY_OPERATION_BASE+1;
    public static final int MEDIA_BLURAY_ISO_UNMOUNT_START = BLURAY_OPERATION_BASE+2;
    public static final int MEDIA_BLURAY_ISO_UNMOUNT_END = BLURAY_OPERATION_BASE+3;
    public static final int MEDIA_BLURAY_ISO_MOUNT_FAIL = BLURAY_OPERATION_BASE+4;
    public static final int MEDIA_BLURAY_ISO_UNSUPPORT_FORMAT = BLURAY_OPERATION_BASE+5;
    public static final int MEDIA_BLURAY_ISO_PLAY_START = BLURAY_OPERATION_BASE+6;
    public static final int MEDIA_BLURAY_PLAY_NEXT  = BLURAY_OPERATION_BASE+7;
	
	public BlurayManager(MediaPlayer player)
	{
		mMediaPlayer = player;
	}

	public static boolean existInBackUp(String path,String name,boolean isDirectory)
	{
		if((path == null) || (name == null))
			return false;
		File backup = new File(path);
		if(!backup.exists() || !backup.isDirectory())
		{
			return false;
		}
		File file = new File(path+File.separator+name);
		if(file.exists())
		{
			if(isDirectory)
			{
				if(file.isDirectory())
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				if(file.isFile())
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}
	
	public static boolean isBDDirectory(String pathStr)
	{
		if(pathStr == null)
		{
			return false;
		}

		// ��ֹĿ¼·���д���ת���ַ�
		Uri uri = Uri.parse(pathStr);
		String path = pathStr;
		String scheme = uri.getScheme();
        if(scheme == null || scheme.equals("file"))
    	{
    		String temp = uri.toString();
			String prefix = new String("file://");
			if(temp != null && temp.startsWith(prefix))
			{
				path = temp.substring(prefix.length());
			}
    	}
		

		Log.d(TAG,"isBDDirectory(), pathStr = "+pathStr);
 		Log.d(TAG,"isBDDirectory(), path = "+path);
		
	  File file = new File(path);
		if(file.isDirectory())
		{
			String bdmvName = path+File.separator+"BDMV";
			String backup = bdmvName+File.separator+"BACKUP";
			File bdmv = new File(bdmvName);
			if(bdmv.exists() && bdmv.isDirectory())
			{
				/*
				String index = bdmvName+File.separator+"index.bdmv";
				File indexFile = new File(index);
				if(!indexFile.exists() && !existInBackUp(backup,"index.bdmv",false))
				{
					return false;
				}
				String moiveobject = bdmvName+File.separator+"MovieObject.bdmv";
				File moiveobjectFile = new File(moiveobject);
				if(!moiveobjectFile.exists() && !existInBackUp(backup,"MovieObject.bdmv",false))
				{
					return false;
				}*/
				String stream = bdmvName+File.separator+"STREAM";
				File streamFile = new File(stream);
				if(!streamFile.exists() && !existInBackUp(backup,"STREAM",true))
				{
					return false;
				}
				String playlist = bdmvName+File.separator+"PLAYLIST";
				File playlistFile = new File(playlist);
				if(!playlistFile.exists() && !existInBackUp(backup,"PLAYLIST",true))
				{
					return false;
				}
				String clip = bdmvName+File.separator+"CLIPINF";
				File clipFile = new File(clip);
				if(!clipFile.exists() && !existInBackUp(backup,"CLIPINF",true))
				{
					return false;
				}
				return true;
 			}
			else
			{
				return false;
			}
		}
		return false;
	}

	// ��ȡ��ǰ���ڲ��ŵ���Ƶ�����Ϣ
	public BlurayVideoInfor  getVideoInfor()
	{
		if(mMediaPlayer != null)
		{
			Parcel reply = mMediaPlayer.getParcelParameter(BLURAY_GET_VIDEO_INFOR);
			if(reply != null)
			{
				BlurayVideoInfor videoInfo[] = reply.createTypedArray(BlurayVideoInfor.CREATOR);
				if(videoInfo != null)
				{
					for(int i = 0; i < videoInfo.length; i++)
					{
						videoInfo[i].tostring();
					}
					
					return videoInfo[0];
				}
				
			}
		}
		return null;
	}

	// ��ȡ��ǰ���ڲ��ŵ���Ƶ��Ϣ
	public BlurayAudioInfor  getAudioInfor()
	{
		if(mMediaPlayer != null)
		{
			Parcel reply = mMediaPlayer.getParcelParameter(BLURAY_GET_AUDIO_INFOR);
			if(reply != null)
			{
				BlurayAudioInfor audioInfo[] = reply.createTypedArray(BlurayAudioInfor.CREATOR);
				if((audioInfo != null) && (audioInfo.length > 0))
				{
					for(int i = 0; i < audioInfo.length; i++)
					{
						audioInfo[i].tostring();
					}
					
					return audioInfo[0];
				}
			}
		}
		return null;
	}

	// ��ȡ��ǰ���ڲ�����ʾ����Ļ��Ϣ
	public BluraySubtitleInfor  getSubtitleInfor()
	{
		if(mMediaPlayer != null)
		{
			Parcel reply = mMediaPlayer.getParcelParameter(BLURAY_GET_SUBTITLE_INFOR);
			if(reply != null)
			{
				BluraySubtitleInfor subtitleInfo[] = reply.createTypedArray(BluraySubtitleInfor.CREATOR);;
				if((subtitleInfo != null) && (subtitleInfo.length > 0))
				{
					for(int i = 0; i < subtitleInfo.length; i++)
					{
						subtitleInfo[i].tostring();
					}
					
					return subtitleInfo[0];
				}
			}
		}
		return null;
	}

	// ��ȡ��ǰ��Ƶ����������
	public BlurayAudioInfor[]  getAudioTrack()
	{
		if(mMediaPlayer != null)
		{
			Parcel reply = mMediaPlayer.getParcelParameter(BLURAY_GET_AUDIO_TRACK);
			if(reply != null)
			{
				BlurayAudioInfor trackInfo[] = reply.createTypedArray(BlurayAudioInfor.CREATOR);
				if((trackInfo != null) && (trackInfo.length > 0))
				{
					Log.d(TAG,"Audio Track Count = "+trackInfo.length);
					for(int i = 0; i < trackInfo.length; i++)
					{
						trackInfo[i].tostring();
					}
				}
				return trackInfo;
			}
		}
		return null;
	}

	// ��ȡ��ǰ��Ƶ��������Ļ
	public BluraySubtitleInfor[]  getSubtitleTrack()
	{
		if(mMediaPlayer != null)
		{
            Parcel reply = mMediaPlayer.getParcelParameter(BLURAY_GET_SUBTITLE_TRACK);
			if(reply != null)
			{
				BluraySubtitleInfor trackInfo[] = reply.createTypedArray(BluraySubtitleInfor.CREATOR);
				if((trackInfo != null) && (trackInfo.length > 0))
				{
					Log.d(TAG,"Subtitle Track Count = "+trackInfo.length);
					for(int i = 0; i < trackInfo.length; i++)
					{
						trackInfo[i].tostring();
					}
				}
				return trackInfo;
			}

		}
		return null;
	}

	/*
	* ���ò���ĳ������,�л����죬,�ú������������Ĵ����߳������л����ú���Ϊ�������������л������ͨ��MediaPlayer��onInfo����
	* indexΪBlurayAudioInfor��mIndex��Ӧ��ֵ
	*/
	public boolean  setAudioTrack(int index)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_SET_AUDIO_TRACK,index);
		}

		return false;
	}

	/* 
	*  ���ò���ĳ����Ļ,�л���Ļ,�ú������������Ĵ����߳������л����ú���Ϊ�������������л������ͨ��MediaPlayer��onInfo����
	*  indexΪBluraySubtitleInfor��mIndex��Ӧ��ֵ
	*/
	public boolean  setSubtitleTrack(int index)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_SET_SUBTITLE_TRACK,index);
		}

		return false;
	}

	/*
	*   ������Ļ����ʾ������
	*   visible: BLURAY_SURFACE_SHOW/BLURAY_SURFACE_HIDE
	*/
	public boolean setSubtitleSurfaceVisible(int visible)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_SET_SUBTITLE_VISIBLE,visible);
		}

		return false;
	}

	/*
	*   ��ȡ��Ļ����ʾ/����״̬
	*   return: BLURAY_SUBTITLE_SHOW/BLURAY_SUBTITLE_HIDE
	*/
	public int getSubtitleSurfaceVisible()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(BLURAY_GET_SUBITTLE_VISIBLE);
		}

		return BLURAY_SURFACE_HIDE;
	}

	/*
	*   ���õ���������ʾ������
	*   visible: BLURAY_SURFACE_SHOW/BLURAY_SURFACE_HIDE
	*/
	public boolean setIGSurfaceVisible(int visible)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_SET_IG_VISIBLE,visible);
		}

		return false;
	}

	/*
	*   ��ȡ����������ʾ/����״̬
	*   return: BLURAY_SURFACE_SHOW/BLURAY_SURFACE_HIDE
	*/
	public int getIGSurfaceVisible()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(BLURAY_GET_IG_VISIBLE);
		}

		return BLURAY_SURFACE_HIDE;
	}

	/*
	* ���⵼����ť����,ֻ����ʾ�����˵�ʱ����������Ч
	*
	*/
	public boolean  moveNavigationButton(int direction)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_SET_IG_OPERATION,direction);
		}

		return false;
	}

	/*
	* ����:��ȡ��ǰTitle���½�����
	*/
	public int getNumberOfChapter()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(BLURAY_GET_CHAPTER);
		}
		return 0;
	}

	/*
	* ����:���ŵ�ǰTitle��ĳ���½�,
	* param: chapter: 0~getChapterCount()-1
	* ˵��: �ú�����������ⷢ����Ϣ���󲥷ŵ�ǰTitleĳ���½ڣ��ú���Ϊ����������
	*       ����ֵֻ��ʾ�����ͳɹ�����ʧ�ܣ��Ƿ񲥷�ĳ���½ڳɹ�����ʧ�ܽ�ͨ��MediaPlayer.OnInfoListener����
	*/
	public boolean playChapter(int chapter)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_PLAY_CHAPTER,chapter);
		}
		
		return false;
	}

	/*
	* ����:��ȡ��ǰTitle���½�����
	*/
	public int getCurrentChapter()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(BLURAY_GET_CURRENT_CHAPTER);
		}
		
		return 0;
	}

	/*
	* ����:��ȡ��ǰTitle��Angle����
	*/
	public int getNumberOfAngle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(BLURAY_GET_NUMBER_OF_ANGLE);
		}
		
		return 0;
	}

	/*
	* ����:��ȡ��ǰTitle�����ڲ��ŵ�Angle
	*/
	public int getCurrentAngle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(BLURAY_GET_CURRENT_ANGLE);
		}
		
		return 0;
	}

	/*
	* ����:���ŵ�ǰTitle��ĳ��Angle��Ӧ����Ƶ
	*/
	public boolean playAngle(int angle)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_PLAY_ANGLE,angle);
		}
		
		return false;
	}

	/*
	* ����:��ȡTitle����
	*/
	public int getNumberOfTitle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(BLURAY_GET_NUMBER_OF_TITLE);
		}
		
		return 0;
	}

	/*
	* ����:��ȡ��ǰ���ڲ��ŵ�Title
	*/
	public int getCurrentTitle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(BLURAY_GET_CURRENT_TITLE);
		}
		
		return 0;
	}

	/*
	* ����:���ŵ�ǰTitle��ĳ��Angle��Ӧ����Ƶ
	*/
	public boolean playTitle(int title)
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_PLAY_TITLE,title);
		}
		
		return false;
	}

	/*
	* ��ת���������沥��
	*/
	public boolean playTopTitle()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_PLAY_TOP_TITLE,0);
		}
		
		return false;
	}

	/*
	* ������ǰ�������ݣ����ź�������
	*/
	public boolean playNextContent()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.setParameter(BLURAY_SKIP_CURRENT_CONTEXT,0);
		}

		return false;
	}

	/*
	* ��ѯ�Ƿ��н����������˵�
	* 0: û�е����˵�  1: �е����˵�
	*/
	public int queryNavigationMenu()
	{
		if(mMediaPlayer != null)
		{
			return mMediaPlayer.getIntParameter(BLUERAYP_PLAY_QUERY_NAVIGATION_MENU);
		}

		return 0;
	}
}

