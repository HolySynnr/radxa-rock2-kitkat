#ifndef _BLURAY_PLAYCONTROLLER_H_
#define _BLURAY_PLAYCONTROLLER_H_

#include "BlurayRegisters.h"
#include "blurayMacro.h"
#include "UIEventCallback.h"
#include "PlayControlEventCallback.h"
#include "MovObject.h"
#include "Sound.h"
#include "IGManager.h"
#include "BlurayCallBack.h"
#include "BlurayAudioConfig.h"
#include "BlurayVideoConfig.h"
#include "BluraySubtitleConfig.h"
#include "BlurayIGConfig.h"
#include "TextFontParser.h"
#include "HdmiManager.h"

#include <utils/Vector.h>

extern "C"
{
#include "navigation.h"
}

namespace RKBluray
{

#define STN_VIDEO 0
#define STN_AUDIO 1
#define STN_SUBTITLE 2
#define STN_IG 3

/**
 * Playback Control Engine State
 */
typedef enum tagPLAYCTRL_STATE
{
    PLAYCTRL_STATE_STOP = 0,
    PLAYCTRL_STATE_PAUSE,
    PLAYCTRL_STATE_PLAY
} PLAYCTRL_STATE;

/**
 * Stream entry types
 */
typedef enum tagSTREAM_ENTRY_TYPE
{
    STREAM_ENTRY_PVIDEO = 0,    // primary video
    STREAM_ENTRY_PAUDIO,        // primary audio
    STREAM_ENTRY_PGTEXTST,      // pg/textst
    STREAM_ENTRY_IG,            // IG
    STREAM_ENTRY_SVIDEO,        // secondary video
    STREAM_ENTRY_SAUDIO         // secondary audio
} STREAM_ENTRY_TYPE;

/* streams */
typedef enum tagDR_STREAM
{
    STREAM_MAIN = 0,

    STREAM_MAX_HDDVD_STD_STREAMS,

    STREAM_SECONDARY = 1,

    STREAM_MAX_HDDVD_ADV_STREAMS,

    STREAM_BDROM_SUBPATH_INTERACTIVE_GRAPHICS = 2,
    STREAM_BDROM_SUBPATH_TEXT_SUBTITLE = 3,

    STREAM_MAX_BDROM_STREAMS,

    STREAM_NONE          /* ADDITIONAL VALUE, USED IN GETSTATUS() */
}STREAM;


class RepeatInfo
{
public:
    int mMode;
    unsigned short  mInPlayItemId;//uiPlayitemID_IN;
    unsigned short  mOutPlayItemId;
    unsigned int  mInTime;//time45k_IN;
    unsigned int  mOutTime;
    bool          mUsePIOut;

    RepeatInfo()
    {
        reset();
    }

    void reset()
    {
        mMode = VDVD_INFO_REPEAT_OFF;
        mInPlayItemId = 0;
        mOutPlayItemId = 0;
        mInTime = 0;
        mOutTime = 0;
        mUsePIOut = false;
    }
};

class BlurayPlayStremInfo
{
public:
    int mPrimaryVideoStn;
    int mSecondaryVideoStn;
    int mPrimaryAudioStn;
    int mSecondaryAudioStn;
    int mPGTextStn;
    int mIGSStn;
};

class BDInfor
{
public:
    int mWidth;
    int mHeight;
    int mType;
    int mFormat;
    int mVideoRate;
    int mVideoType;
    int mAudioType;

    BDInfor()
    {
        mWidth = 0;
        mHeight = 0;
        mType = 0;
        mFormat = 0;
        mVideoRate = 0;
        mVideoType = 0;
        mAudioType = 0;
    }
};

class BlurayPlayController
{
public:
    BlurayPlayController(char* path,BlurayRegisters* reg);
    ~BlurayPlayController();
    /*
      * 设置视频所在surface的z order,以方便设置导航栏surface的z order
      */
    void setZOrder(int order);
    void setHdmiManager(HdmiManager* manager);
    int set3DMode();
    int set2DMode(); 

    /*
    * 用于接收suspendMovieProgram导航命令，实现挂起当前播放
    */
    int  suspendPlay();
    /*
    * 用于接收resume导航命令，实现恢复播放
    */
    int resumePlay();
    /*
    * 查询是否有导航菜单
    */
    int queryNavigationMenu();
    
    void setPlayMovieObjectId(unsigned int id);
    // 设置当前的播放时间，用来更新PSR的某些状态
    void  updatePlayerTime(int64_t time);
    // 更新PlayItem状态寄存器
    void updatePlayItem(int item);
    // 更新Chapter状态寄存器
    void updateChapter(int64_t time);
    void stopPlay();
     
    int seek(int64_t time);

    // 删除2D或者3D AVC视频的配置
    void deleteMainVideoConfig();

    // 删除3D MVC视频的配置
    void deleteExtendVideoConfig();
    void deleteAudioConfig();
    void deleteSubtitleConfig();
    int64_t findVideoPosition(MPLS_PI* playItem,int playItemId,int64_t inTime);
    int64_t findExtendVideoPosition(MPLS_PI* playItem,int playItemId,int64_t inTime);
    int64_t findAudioPosition(MPLS_PI* playItem,int playItemId,int64_t inTime);
    int64_t findSubtitlePosition(MPLS_PI* playItem,int playItemId,int64_t inTime);
    TextFont* getTextSubtitleFont(int index);
    int textSubtitleStyleChange(int style);
    unsigned int getDuration();
    int audioTrackChange(int pid);
    int subtitleTrackChange(int pid);

    
    bool isPlayFileExsit(NAV_TITLE* title);
    void setTitleType(bool interactive);
    void playMainTitle();

    void setAudioTrack(int pid);
    void setSubtitle(int pid);
    void setOutPutMode(int mode);
    IGManager* getIGManager();
    void setEventCallBack(PlayControlEventCallback* playCallback);
    void setPlayCallBack(BlurayPlayCallBack* callback);
    BlurayPlayCallBack* getBlurayPlayCallBack();
    int playTilteRepeat();
    int playPlayListPlayItem(int playlist,int playItem);
    int playConfigVideoAudio(NAV_TITLE* title,int playlist,int playItem,int videoStn);
//    int playLinkMark_chapter(int chapter, bool userOpetion);
    int playPLLinkMark(int playlist,int chapter);
    int playLinkMark(int chapter,bool useOperation);
    int playLinkPlayItem(int playItem);
    int playLink(int inPlayItemId,unsigned int inTime, int outPlayItem,unsigned int outTime,bool userOpetion);
  //  int playLink(int playItemId,unsigned int inTime ,bool userOpetion);
    int playLinkPlayItem(int inPlayItemId,unsigned int inTime, int outPlayItem,unsigned int outTime,bool firstPlay);
    int playItem(NAV_TITLE* title,int playItem,bool playitem);
    int getStreamType(NAV_TITLE* title,STREAM_ENTRY_TYPE type,int playItemId,unsigned int stn);
    int setIGSSurfaceVisible(bool visible);
    int subtileDisplayOn();
    int subtitleDisplayOff();
    int subtitleStreamChange(int subtitleStn);
    int audioChange(int audioStn);
    int igChange(int igStn);
    int angleChange(int angle);
    int subtitleSurfaceEnable(int enable);
    NAV_TITLE* getTitle();
    MPLS_PLM* getMarkInfo(int markNum, int markType);
    int setRepeat(int mode);
    int getRepeatMode();
    unsigned int getTitleDuration();
    int getNumberOfChapters();
    int getNumberOfAngles();
    int getTrackCount(int type);
    MPLS_STREAM* getSTNInfor(int type);
    MPLS_PI*  getPlayListItem(int itemId);
    bool getUOMaskEnble(int tableIndex);
    int enableButton(int buttonId);
    int disableButton(int buttonId);
    int popOffMenu();
    int selectButtonAndPage(unsigned int buttonId,unsigned int pageId,int cmdFlag);
    int selectButton(int buttonNumber);
    int moveSelectButton(int direction);
    int activateButton();
    int igsCommandCompelete();
private:
    int playPlayListPlayItem(NAV_TITLE* title,int playlist,int playItem);
    int playSubPlayItem(STREAM_ENTRY_TYPE type,int playItemId,unsigned int stn);
    bool isSameClip(STREAM_ENTRY_TYPE type, int newPlayitemID,int newStn, int oldPlayitemID, int oldStn);
    int selectAudio(unsigned int auidoStn,MPLS_STN* stn,unsigned int* selected);
    
    int selectPGandST(unsigned int streamNumber, MPLS_STN *stnTable, unsigned int *selectedStn);
     bool checkTextSTCapability(unsigned int streamNumber, MPLS_STN *stnTable);
     bool checkLanguageCapability(unsigned char* lang);
     bool checkAudioCapability(unsigned int streamNumber, MPLS_STN *stnTable, bool surround);
//    int selectPGandST(unsigned int pgStn,MPLS_STN* stn);
    int selectIG(unsigned int igStn,MPLS_STN* stn,unsigned int* igStnNew);

    int getAudioCapability(unsigned int* capability);
    int configAudioCapability();

    /*
    * 用于配置2D或者3D base Video(2D AVC视频)
    */
    int getMainVideoConfig(MPLS_PI* playItem,int playItemId,Vector<BlurayVideoConfig*>* videoList);

    /*
    * 用于配置3D Extend Video(3D MVC视频)
    */
    int getExtendVideoConfig(MPLS_PI* playItem,int playItemId,Vector<BlurayVideoConfig*>* videoList);
    int getAudioConfig(MPLS_PI* playItem,int playItemId,int audioStn,Vector<BlurayAudioConfig*>* audioList);
    int getSubtitleConfig(MPLS_PI* playItem,int playItemId,int subtitleStn,Vector<BluraySubtitleConfig*>* subtitleList);
    int getIGConfig(MPLS_PI* playItem,int playItemId,int igStn,Vector<BlurayIGConfig*>* igList);

    int64_t      mPlayTime;
    int mPlayList;
    int mPlayItem;
    int mAngle;
    unsigned int mPlayMovieObject;

    int mTextSubtitleCount;

    int mPlayState;

    int  mBackUpValid;
    bool mInteractiveTitle;

    bool mSubtitleChangePending;
    bool mIGChangePending;
    
    BlurayRegisters* mRegister;
    
    RepeatInfo* mRepeatInfor;
    BlurayPlayStremInfo *mStreamInfor;

    UIEventCallback* mUICallBack;

    NAV_TITLE* mTitle;
    char* mPath;

    IGManager*      mIGManager;

    TextFont*        mTextFont[TEXT_SUBTITLE_MAX];
    BDInfor mBDInfor;

    BlurayPlayCallBack* mPlayCallBack;

    /*
    * 用于指示视频播放模式，可选的值为2D和3D
    */
    int                 mPlayTVMode;

    HdmiManager*        mHdmiManager;

    /*
    * 对应蓝光base view(AVC)数据
    */
    Vector<BlurayVideoConfig*>* mBaseVideoConfig;

    /*
    * 对应蓝光3D extend view(MVC)数据
    */
    Vector<BlurayVideoConfig*>* mExtendVedioConfig;
    Vector<BlurayAudioConfig*>* mAudioConfig;
    Vector<BluraySubtitleConfig*>* mSubtitleConfig;
};


}
#endif
