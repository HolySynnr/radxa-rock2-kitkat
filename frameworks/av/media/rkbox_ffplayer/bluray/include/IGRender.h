#ifndef _BLURAY_RENDER_H_
#define _BLURAY_RENDER_H_

#include <stdint.h>
#include <sys/types.h>

//#include <androidfw/AssetManager.h>
#include <utils/threads.h>

#include <EGL/egl.h>
#include <GLES/gl.h>

#include <gui/ISurfaceComposer.h>
#include <gui/SurfaceComposerClient.h>
#include <gui/Surface.h>
#include <utils/threads.h>

#ifdef AVS44
#include <gui/IGraphicBufferProducer.h>
#include <gui/Surface.h>
#else
#include <gui/ISurfaceTexture.h>
#include <gui/SurfaceTextureClient.h>
#endif


extern "C"
{
#include "vcitypes.h"
#include "osapi.h"
}

#ifdef AVS44
#include <android/native_window.h>
#endif

#include <utils/Vector.h>
#include "HdmiManager.h"



using namespace android;

namespace RKBluray
{
    
#define FIXED_ONE 1
#define IG_MAX_ODS  4096
#define RENDER_RGA  0
#define RENDER_GPU  1


typedef enum RenderStatus
{
    Render_INIT = 0,
    Render_START ,
    Render_PAUSE,
    Render_STOP,
    Render_EXIT
}RenderStatus;


typedef enum IGRender_MSG_TYPE
{
    IGRender_MSG_RENDER = 0,
    IGRender_MSG_EXIT,
} IGRender_MSG_TYPE;

typedef struct tagIGRender_MESSAGE
{
    int                 tMsgType;
    ULONG               ulData0;
    ULONG               ulData1;
} IGRender_MESSAGE;


class IGDisplayObject
{
public:
    IGDisplayObject()
    {
        mId = 0;
        mXPosition = 0;
        mYPosition = 0;
        mWidth = 0;
        mHeight = 0;
        mICSWidth = 0;
        mICSHight = 0;
        mData = NULL;
    }
public:
    unsigned int   mId;
    unsigned int   mXPosition;
    unsigned int   mYPosition;
    unsigned int   mWidth;
    unsigned int   mHeight;

    int            mICSWidth;
    int            mICSHight;
    unsigned char* mData;
};

class IGRender:public IBinder::DeathRecipient
{
public:
	  IGRender();
	  ~IGRender();

      /*
      * ������Ƶ����surface��z order
      */
      void setZOrder(int order);

      /*
      * ���������˵���surface��z order
      */
      void adjustZOrder();

      /*
      * ����Hdmi Manager,���ڵ����˵���Hdmi��ȡ��ǰ����ʾģʽ(2D,3D left/right, 3D top/bottom, 3D frame packing)
      */
      void setHdmiManager(HdmiManager* manager);

      /*
      * ��ȡ�����˵���surface�Ƿ�ɼ�
      */
      bool getSurfaceVisibility();

      /*
      * ���õ����˵���surface�Ƿ�ɼ�
      */
      void setSurfaceVisibility(bool visible);

      /*
      * ���ӵ����˵�����ʾ�б�
      */
      void addToRender(IGDisplayObject* object);

      /*
      * ���surface�ϵĵ����˵�ͼƬ
      */
      void clearSurface();

      /*
      * ���������˵�����Ⱦ
      */
      void startRender();

      /*
      * ֹͣ�����˵�����Ⱦ
      */
      void stopRender();
      void setStatus(int status);

      /*
      * �����Ⱦ�б�
      */
      void clearRenderList();

      /*
      * gpu��������
      */
      int initTexture(void* data, int len,int width,int height,GLuint& texture);

      /*
      * gpu��Ⱦǰ
      */
      void initScene(void);

      /*
      * ����Surface
      */
      void createSurface();


      void createEGLSurface();

      /*
      * �ͷ�Surface
      */
      void destorySurface();

      /*
      * ��Ⱦ�����˵�
      */
      void render();

      /*
      * �����˵���Ⱦ�߳�
      */
      static void* renderThread(void *thiz);
private:
    int         mWidth;
    int         mHeight;
    EGLDisplay  mDisplay;
    EGLContext  mContext;
    EGLSurface  mSurface;
    int         mRenderType;
    int         mRGAFd;
    
    pthread_mutex_t  mLock;
    
    sp<SurfaceComposerClient> mClient;
    sp<Surface> mFlingerSurface;
    sp<SurfaceControl> mControl;

    int         mRenderThreadStatus;
    pthread_t	mRenderThread;
    int         mRenderStatus;
    int         mCleanSurface;
    bool        mSurfaceVisible;
    bool        mIsWriteSurface;
    
    HdmiManager* mHdmiManager;
    int          mNavigationMenuSurfaceZOrder;

    Vector<IGDisplayObject*>* mIgDisplayList;

    virtual void        onFirstRef();
    virtual void        binderDied(const wp<IBinder>& who);
    bool bindSurfaceToThread();
    
    void clearEGLSurface();
    void surfaceHide();
    void surfaceShow();
    void gpuRender(void* data, unsigned int len,int x,int y,int width,int height,int icsWidth,int icsHeight);
    void rgaRender(void* nativeWindow,unsigned int x,unsigned int y,unsigned int width,unsigned int height,
        int icsWidth,int icsHeight,unsigned char* data,int offset);

};

}

#endif
