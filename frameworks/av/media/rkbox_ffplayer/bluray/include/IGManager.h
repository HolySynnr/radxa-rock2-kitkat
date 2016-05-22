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
      * ������Ƶ����surface��z order,�Է������õ�����surface��z order
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
    * describe: ��������Ļص�����������������ʾ�µ�Page��Button
    * param: buttonId: �µĽ��㰴ť��Id
             pageId: ����ʾ��ҳ���Id
             flag: ��Ч��־λ����flag��1bit����Ϊ1ʱ��pageId������Ч; 
                               ��flag��2bit����Ϊ1ʱ��buttonId������Ч;
                               ��flag��31bit����Ϊ1ʱ��������Ҫ��ʾ������Ч
    */
    int selectButtonAndPage(unsigned int buttonId,unsigned int pageId,int flag);

    /*
    * describe: ��������Ļص�������������ʾ�����ص����˵�
                popOnMenu()  ��ʾ�����˵�
                popOffMenu()  ���ص����˵�
    */
    int popOnMenu();
    int popOffMenu();

    /*
    * describe: ��������Ļص��������������ò˵���ť��״̬
                enableButton()   ���ð�ť״̬ΪBUTTON_ENABLED״̬
                disableButton()  ���ð�ť״̬ΪBUTTON_DISABLED״̬
      param:   buttonId: ��ťid
               �����˵���ť��4��״̬��BUTTON_DISABLED,BUTTON_ENABLED,BUTTON_SELECTED��BUTTON_ACTIVE״̬
               BUTTON_DISABLED: ��ǰ�˵���ť���ɼ�
               BUTTON_ENABLED:  ��ǰ�˵���ť�ɼ�
               BUTTON_SELECTED: ��ǰ�˵��ɼ��һ�ȡ����
               BUTTON_ACTIVE:   ��ǰ�˵��ɼ�����ȡ�������Ѽ���
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
    // �û�ѡ�񵼺��˵���ʱ���ú������������ص����˵�
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
