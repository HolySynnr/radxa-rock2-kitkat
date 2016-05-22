#ifndef _RK_BLURAY_IG_
#define _RK_BLURAY_IG_

#include <stdio.h>
#include "vcitypes.h"
#include "BlurayRegisters.h"
#include "NavigationCommand.h"
#include <pthread.h> 
#include "LOGD.h"
#include "BlurayIGConfig.h"
#include "PlayControlEventCallback.h"

extern "C"
{
#include "vcitypes.h"
#include "osapi.h"
#include "colorspace.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavcodec/ig.h"
#include "libavformat/mpegts.h"
}
#include <utils/Vector.h>

#include "IGRender.h"

using namespace android;




namespace RKBluray 
{

#define MAX_BUTTONID_VALUE   0x1FDF
#define MAX_BUTTON_NUMBER_VALUE         0x270F /* '9999' */


#define NUMBER 256 
#define PICTURENUMBER (4096)



/* macros */
#define SWITCH_1(byte)                  (0x80 & byte)
#define SWITCH_2(byte)                  (0x40 & byte)
#define RLZERO(byte)                    (0x3f & byte)
#define RL3TO63(byte)                   (0x3f & byte)
#define RL64TO16K_TOP(byte)             (0x3f & byte)


/**
 * Message
 */
typedef struct tagIGManagerMessage
{
    int                 tMsgType;
    ULONG               ulData0;
    ULONG               ulData1;
} IGManagerMessage;//IGSPARSE_MESSAGE;

typedef enum IGS_MANAGER_MSG_TYPE
{
    IGS_MANAGER_MSG_PARSER = 0,
    IGS_MANAGER_MSG_EXIT,
} IGS_MANAGER_MSG_TYPE;



typedef enum IGS_SELECT_BUTTON_DIRECTION
{
    SelectButton_MoveUp = 0,
    SelectButton_MoveDown ,
    SelectButton_MoveLeft ,
    SelectButton_MoveRight ,
    SelectButton_Activate,
    IGS_ON_OFF,
}IGS_SELECT_BUTTON_DIRECTION;

class IGManager
{
public:
    IGManager(BlurayRegisters* reg);
    ~IGManager();

    /*
      * 设置视频所在surface的z order,以方便设置导航栏surface的z order
      */
    void setZOrder(int order);
    void setHdmiManager(HdmiManager* manager);
    int queryNavigationMenu();
    void setPlayEventCallBack(PlayControlEventCallback* playCallback);
    
    bool getSurfaceVisibility();
    void setSurfaceVisibility(bool visible);
    
    void stopRender();
    void startRender();
    
    /*
    * describe: 导航命令的回调函数，用于设置显示新的Page和Button
    * param: buttonId: 新的焦点按钮的Id
             pageId: 新显示的页面的Id
             flag: 有效标志位。当flag第1bit设置为1时，pageId参数有效; 
                               当flag第2bit设置为1时，buttonId参数有效;
                               当flag第31bit设置为1时，表明需要显示进入特效
    */
    int selectButtonAndPage(unsigned int buttonId,unsigned int pageId,int flag);

    /*
    * describe: 导航命令的回调函数，用于显示和隐藏导航菜单
                popOnMenu()  显示导航菜单
                popOffMenu()  隐藏导航菜单
    */
    int popOnMenu();
    int popOffMenu();

    /*
    * describe: 导航命令的回调函数，用于设置菜单按钮的状态
                enableButton()   设置按钮状态为BUTTON_ENABLED状态
                disableButton()  设置按钮状态为BUTTON_DISABLED状态
      param:   buttonId: 按钮id
               导航菜单按钮有4个状态，BUTTON_DISABLED,BUTTON_ENABLED,BUTTON_SELECTED和BUTTON_ACTIVE状态
               BUTTON_DISABLED: 当前菜单按钮不可见
               BUTTON_ENABLED:  当前菜单按钮可见
               BUTTON_SELECTED: 当前菜单可见且获取焦点
               BUTTON_ACTIVE:   当前菜单可见、获取焦点且已激活
    */
    int enableButton(int buttonId);
    int disableButton(int buttonId);

    
    int activateButton();
    int needParseIGS();
    
    void deleteIGList();
    void resetButtonStatus();
    void deleteIG();
    void initIG(IG* ig);
    void deleteIG(IG* ig);
    int  readPacket();
    static void*  readIGPacketThread(void * data);
    int load(Vector<BlurayIGConfig*>* list);
    void closeIGContext();
    int initIGContext();
    int initIGTrack(AVFormatContext *ic);
    int initIGDecoder(AVFormatContext *ic);
    
    int sendMsgParseIG(Vector<BlurayIGConfig*>* list);
    IG* getIG();
    int decodeRle(IGSPicture* pic,unsigned char* outBuf);
    int processPicture(IG* ig);
//    void freeIG(IG* ig);
    int parse();
    bool isOutMux();
    bool isPopUpMenu();
    bool isDisplayEnable();
    void setPopup(bool enable);
    int findPageAndSelectedButton();
    int findFirstSelectedButton();
    int findSelectedButton(IGSPage* page,int selectedId,IGSBOG** buttonGroup,int* bogIndex,IGSButton** button);
    int disableBogButton(IGSBOG* bog,int buttonId);
    int getBogIndex(IGSPage*page, int id);
    int disableBog(IGSBOG* bog,int id);
    int buttonActivateComplete();
    int findBOGDefaultButton(IGSPage* page,IGSBOG** buttonGroup,int* bogIndex,IGSButton** button);
    int findButton(IGSPage* page,int buttonId,IGSButton** button);
    int findButton(IGSPage* page,IGSBOG** bog,int* bogIndex,IGSButton** button);
    int findButton(IGSBOG** buttonGroup,IGSButton** button);
    int findAnyValidButton(IGSPage* page,IGSButton** button);
    void setBOGDefaultButton(IGSPage* page,int buttonId);
    int moveSelectedButton(int direct);
    int setPage(int id);
    int setNextPage(int id);
    int getPage();
    int getButtonId(IGSPage* page,int index);
    int selectButton(int buttonId);
    int selectButtonById(int buttonId);
    int selectNextButton(int buttonId);
    int activeSelectButton(bool active);
    
 //   int display(int a);
    static ULONG IGSParseThread(void* data);

    
    void startUserSeletedTimer();
    void resetUserTimer();
    // 用户选择导航菜单超时调用函数，用来隐藏导航菜单
    static ULONG userSelectedTimeOut(void* param);
    static ULONG animation(void* param);
    void stopAnimation();
    void startAnimation(ICS* ics,IGSPage* page);
    IGSPicture* findButtonPicture(unsigned int buttonId);
    IGSButton* findSelectedButton(IGSPage* page, unsigned int buttonId);
    void render();
    void renderDefaultButton(IGSPage* page);
    void renderSelectedButton();
    void renderPage(int pageId);
public:
    IG*   mIg;
    AVFormatContext *mIGContext;
    IGSButton*   mSelectedButton;
    IGSButton*   mNextSelectedButton;
    IGSButton*   mHintButton;
    
    int          mPage;  // current show page
    int          mNextPage;

    int                      mIgTrack;
    Vector<BlurayIGConfig*>* mIgList;


    BlurayRegisters* mRegister;

    bool         mCommandExcuteCompelete;
    bool         mIsPopup;
    bool         mTimerStart;
    bool         mExit;

    pthread_t                   mIGSParseThread;

    int                         mIGParseThreadStatus;

    int             mState;
    IGRender*       mRender;

    PlayControlEventCallback*  mControlCallBack;

    OS_TIMER_ID    mAnimationTimer;
    OS_TIMER_ID    mUserSelectedTimer;

    ULONG          mUserSelectedRemain;

    pthread_mutex_t mParseMutex;
};

}
#endif
