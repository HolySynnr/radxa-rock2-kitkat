#ifndef _BLURAY_HDMI_MANAGER_H_
#define _BLURAY_HDMI_MANAGER_H_



#include <utils/Vector.h>


/*
*  HDMIManager用于
*  1.蓝光控制电视2D和3D之间的转换
*  2.控制屏幕的过扫描
*/

#define HDMI_3D_NONE  -1
#define HDMI_3D_FRAME_PACKING 0
#define HDMI_3D_TOP_BOTTOM 6
#define HDMI_3D_SIDE_BY_SIDE_HALT 8


using namespace android;

namespace RKBluray 
{


class SocketReply
{
public:
    SocketReply();
    ~SocketReply();
    int parseReply(char* buffer);
    
public:
    /*
    * socket通信状态，取值范围同http code,一般为200
    */
    int mCode;

    /*
    * 一般为0
    */
    int mStatus;

    /*
    * 内容与命令相关，需要根据不同的命令来解析
    */
    char* mReply;
};

#define TV_INFORMATION_MAX_LENGTH 48
/*
* 通过HDMI获取到的电视支持的模式, 比如1080i-24帧
*/
class TVInformation
{
public:
    /*
    * 分辨率宽
    */
    int mWidth;

    /*
    * 分辨率高
    */
    int mHeight;

    /*
    * 刷新率
    */
    int mFrameRate;

    /*
    * 是否支持3D
    */
    int m3DMode;

    /*
    * P/I 
    */
    char mMode;

    /*
    * 从HDMI拿到的原始数据，比如1920x1080p-50,321
    */
    char* mInfor;

public:
    TVInformation()
    {
        mWidth = 0;
        mHeight = 0;
        mFrameRate = 0;
        mMode = 0;
        m3DMode = 0;
        mInfor = new char[TV_INFORMATION_MAX_LENGTH];
        if(mInfor != NULL)
        {
            memset(mInfor,0,TV_INFORMATION_MAX_LENGTH);
        }
    }
    
    ~TVInformation()
    {
        if(mInfor != NULL)
        {
            delete[] mInfor;
            mInfor = NULL;
        }
    }
    
    void parse();
};

class HdmiManager
{
public:
    HdmiManager();
    ~HdmiManager();

    /*
    * 说明: 设置电视3D模式
    * 返回值: 0 设置成功，其他失败
    */
    int set3DMode();

    /*
    * 说明: 设置电视模式
    * 参数: mode 可选值: HDMI_3D_NONE
                         HDMI_3D_FRAME_PACKING
                         HDMI_3D_SIDE_BY_SIDE_HALT
                         HDMI_3D_TOP_BOTTOM
    * 返回值: 0 设置成功，其他失败
    */
    int setMode(int mode);
    
    /*
    * 获取当前电视支持的3D模式
    */
    int getSupport3DMode();

    /*
    * 通过HDMI获取电视当前的模式
    */
    int get3DMode();

    /*
    * 获取电视当前的模式
    */
    int getCurrentMode();

    /*
    * 恢复进入播放前的过扫描状态
    */
    void resumeOverScan();


    
    int getAllResolution();
    
    int setResolution(int format,int frameRate,int support3D);
    
private:
    int send(int sock,char *cmd,SocketReply& reply);
    int getReply(int sock,SocketReply& reply,int stop_after_cmd); 

    int getResolution(TVInformation& infor);
    
    int adjustResolution(TVInformation* infor);
    
    void find3DModeList(Vector<TVInformation*>& list,Vector<TVInformation*>& list3D);

    int  findBetter(TVInformation* infor,int width,int height,char foramt,int rate);
    /*
    * 保存电视当前的状态
    */
    int mMode;

    /*
    * 保存进入蓝光播放时的3D状态，退出蓝光播放时恢复
    */
    int mOldMode;

    TVInformation mCurrentResolution;

    TVInformation mOldResolution;
    
    Vector<TVInformation*>  mAllResolution;
};

}

#endif
