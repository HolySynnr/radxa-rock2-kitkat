package android.bluray;

/*
*  this file is defined by hh@rock-chips.com
*  BlurayManager用于定义蓝光操作的宏和获取需要在UI上显示的蓝光相关信息
*  BlurayManager中的相关操作主要通过MediaPlayer的相关函数实现，其定义的相关变量和宏的值需要
*  与rkBoxPlayer头文件中的宏对应，因此BlurayManager中的宏和变量不允许改变，否则会导致操作失败
*  该文件中定义的操作只有在播放蓝光时有效
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
	
	// 蓝光相关设置消息起始，需要与RkFrameManage.h中的对应
	private static final int BLURAY_BASE = 7000;
	// 获取当前正在播放视频信息
	public static final int BLURAY_GET_VIDEO_INFOR = BLURAY_BASE;
	// 获取当前正在播放的音轨信息
	public static final int BLURAY_GET_AUDIO_INFOR = BLURAY_BASE+1;
	// 获取当前正在播放的字幕信息
	public static final int BLURAY_GET_SUBTITLE_INFOR = BLURAY_BASE+2;

	// 获取当前播放的Title的所有音轨
	public static final int BLURAY_GET_AUDIO_TRACK = BLURAY_BASE+3;
	// 获取当前播放Title的所有字幕信息
	public static final int BLURAY_GET_SUBTITLE_TRACK = BLURAY_BASE+4;
	// 音轨切换
	public static final int BLURAY_SET_AUDIO_TRACK = BLURAY_BASE+5;
	// 字幕切换
	public static final int BLURAY_SET_SUBTITLE_TRACK = BLURAY_BASE+6;
	
	// 导航隐藏和显示(只有Pop-up Menu能够隐藏或者显示,Always-on Menu不允许隐藏)
	// BLURAY_SURFACE_SHOW/BLURAY_SURFACE_HIDE
	public static final int BLURAY_SET_IG_VISIBLE = BLURAY_BASE+7;
	public static final int BLURAY_GET_IG_VISIBLE = BLURAY_BASE+8;
	
	// 字幕隐藏和显示
	// 操作参数BLURAY_SURFACE_SHOW/BLURAY_SURFACE_HIDE
	public static final int BLURAY_SET_SUBTITLE_VISIBLE = BLURAY_BASE+9;
	public static final int BLURAY_GET_SUBITTLE_VISIBLE = BLURAY_BASE+10;
	
	// 导航按钮操作
	// 操作参数 BLURAY_IG_BUTTON_MOVE_UP
	//          BLURAY_IG_BUTTON_MOVE_DOWN
	//          BLURAY_IG_BUTTON_MOVE_LEFT
	//          BLURAY_IG_BUTTON_MOVE_RIGHT
	//          BLURAY_IG_BUTTON_ACTIVATE
	public static final int BLURAY_SET_IG_OPERATION = BLURAY_BASE+11;

	// 获取当前Title的总章节数
	public static final int BLURAY_GET_CHAPTER = BLURAY_BASE+12;
	// 播放当前Title的某一章节
	public static final int BLURAY_PLAY_CHAPTER = BLURAY_BASE+13;
	// 获取当前正在播放的章节
	public static final int BLURAY_GET_CURRENT_CHAPTER = BLURAY_BASE+14;

	// 获取当前Title的Angle数
	public static final int BLURAY_GET_NUMBER_OF_ANGLE = BLURAY_BASE+15;
	// 获取当前Title的正在播放的Angle
	public static final int BLURAY_GET_CURRENT_ANGLE = BLURAY_BASE+16;
	// 播放当前Title的Angle not support
	public static final int BLURAY_PLAY_ANGLE = BLURAY_BASE +17;

	/* 
	*  蓝光的Title分为3类: FirstPlay(AutoRun) Title、Top Menu Title和普通的Title
	*  FirstPlay(AutoRun) Title 为插入蓝光光盘时自动开始播放的Title
	*  Top Menu Title 为导航界面对应的Title
	*  普通的Title 为其他的视频播放的Title
	*/
	// 获取Title总数,此处的Title总数不包含FirstPlay(AutoRun) Title和Top Menu Title
	public static final int BLURAY_GET_NUMBER_OF_TITLE = BLURAY_BASE+18;
	// 获取当前正在播放的Title
	public static final int BLURAY_GET_CURRENT_TITLE = BLURAY_BASE+19;
	// 设置播放某个Title
	public static final int BLURAY_PLAY_TITLE = BLURAY_BASE+20;
	// 设置播放Top Title
	public static final int BLURAY_PLAY_TOP_TITLE = BLURAY_BASE+21;

	// 跳过当前播放内容，播放后续内容
	public static final int BLURAY_SKIP_CURRENT_CONTEXT = BLURAY_BASE+22;
	
	public static final int BLURAY_PLAY_SEEK_TIME_PLAY = BLURAY_BASE+23;

	// 用于查询是否有解析到导航菜单
	public static final int BLUERAYP_PLAY_QUERY_NAVIGATION_MENU = BLURAY_BASE+24;


	// 字幕/导航栏显示
	public static final int BLURAY_SURFACE_SHOW = 1;
	// 字幕/导航栏隐藏
	public static final int BLURAY_SURFACE_HIDE = 0;

	// 导航按钮移动方向和激活，只处理按下消息，弹起消息不处理
	// 向上移动
	public static final int BLURAY_IG_BUTTON_MOVE_UP = 0;  
	// 向下移动
	public static final int BLURAY_IG_BUTTON_MOVE_DOWN = 1;
	// 向右移动
	public static final int BLURAY_IG_BUTTON_MOVE_LEFT = 2;
	// 向左移动
	public static final int BLURAY_IG_BUTTON_MOVE_RIGHT = 3;
	// 激活当前按钮
	public static final int BLURAY_IG_BUTTON_ACTIVATE = 4;


	// 操作成功
	public static final int BLURAY_OPERATION_SUCCESS = 0;
	// 操作失败
	public static final int BLURAY_OPERATION_FAIL = 1;
	// 禁止当前操作
	public static final int BLURAY_OPERATION_FAIL_OPEATION_DISABLE = 2;
	// 参数有问题
	public static final int BLURAY_OPERATION_FAIL_PARAMETER_ERROR = 3;
	// BDJ模式，不支持当前此操作
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

		// 防止目录路径中带有转义字符
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

	// 获取当前正在播放的视频相关信息
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

	// 获取当前正在播放的音频信息
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

	// 获取当前正在播放显示的字幕信息
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

	// 获取当前视频的所有音轨
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

	// 获取当前视频的所有字幕
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
	* 设置播放某个音轨,切换音轨，,该函数将向蓝光库的处理线程请求切换。该函数为非阻塞函数，切换结果将通过MediaPlayer的onInfo返回
	* index为BlurayAudioInfor中mIndex对应的值
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
	*  设置播放某个字幕,切换字幕,该函数将向蓝光库的处理线程请求切换。该函数为非阻塞函数，切换结果将通过MediaPlayer的onInfo返回
	*  index为BluraySubtitleInfor中mIndex对应的值
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
	*   设置字幕的显示和隐藏
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
	*   获取字幕的显示/隐藏状态
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
	*   设置导航栏的显示和隐藏
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
	*   获取导航栏的显示/隐藏状态
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
	* 蓝光导航按钮操作,只有显示导航菜单时，操作才有效
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
	* 功能:获取当前Title的章节数量
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
	* 功能:播放当前Title的某个章节,
	* param: chapter: 0~getChapterCount()-1
	* 说明: 该函数将向蓝光库发送消息请求播放当前Title某个章节，该函数为非阻塞函数
	*       返回值只表示请求发送成功或者失败，是否播放某个章节成功或者失败将通过MediaPlayer.OnInfoListener返回
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
	* 功能:获取当前Title的章节数量
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
	* 功能:获取当前Title的Angle数量
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
	* 功能:获取当前Title的正在播放的Angle
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
	* 功能:播放当前Title的某个Angle对应的视频
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
	* 功能:获取Title总数
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
	* 功能:获取当前正在播放的Title
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
	* 功能:播放当前Title的某个Angle对应的视频
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
	* 跳转到导航界面播放
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
	* 跳过当前播放内容，播放后续内容
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
	* 查询是否有解析到导航菜单
	* 0: 没有导航菜单  1: 有导航菜单
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

