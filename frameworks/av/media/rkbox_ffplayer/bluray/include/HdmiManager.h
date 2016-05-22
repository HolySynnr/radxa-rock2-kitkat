#ifndef _BLURAY_HDMI_MANAGER_H_
#define _BLURAY_HDMI_MANAGER_H_



#include <utils/Vector.h>


/*
*  HDMIManager����
*  1.������Ƶ���2D��3D֮���ת��
*  2.������Ļ�Ĺ�ɨ��
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
    * socketͨ��״̬��ȡֵ��Χͬhttp code,һ��Ϊ200
    */
    int mCode;

    /*
    * һ��Ϊ0
    */
    int mStatus;

    /*
    * ������������أ���Ҫ���ݲ�ͬ������������
    */
    char* mReply;
};

#define TV_INFORMATION_MAX_LENGTH 48
/*
* ͨ��HDMI��ȡ���ĵ���֧�ֵ�ģʽ, ����1080i-24֡
*/
class TVInformation
{
public:
    /*
    * �ֱ��ʿ�
    */
    int mWidth;

    /*
    * �ֱ��ʸ�
    */
    int mHeight;

    /*
    * ˢ����
    */
    int mFrameRate;

    /*
    * �Ƿ�֧��3D
    */
    int m3DMode;

    /*
    * P/I 
    */
    char mMode;

    /*
    * ��HDMI�õ���ԭʼ���ݣ�����1920x1080p-50,321
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
    * ˵��: ���õ���3Dģʽ
    * ����ֵ: 0 ���óɹ�������ʧ��
    */
    int set3DMode();

    /*
    * ˵��: ���õ���ģʽ
    * ����: mode ��ѡֵ: HDMI_3D_NONE
                         HDMI_3D_FRAME_PACKING
                         HDMI_3D_SIDE_BY_SIDE_HALT
                         HDMI_3D_TOP_BOTTOM
    * ����ֵ: 0 ���óɹ�������ʧ��
    */
    int setMode(int mode);
    
    /*
    * ��ȡ��ǰ����֧�ֵ�3Dģʽ
    */
    int getSupport3DMode();

    /*
    * ͨ��HDMI��ȡ���ӵ�ǰ��ģʽ
    */
    int get3DMode();

    /*
    * ��ȡ���ӵ�ǰ��ģʽ
    */
    int getCurrentMode();

    /*
    * �ָ����벥��ǰ�Ĺ�ɨ��״̬
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
    * ������ӵ�ǰ��״̬
    */
    int mMode;

    /*
    * ����������ⲥ��ʱ��3D״̬���˳����ⲥ��ʱ�ָ�
    */
    int mOldMode;

    TVInformation mCurrentResolution;

    TVInformation mOldResolution;
    
    Vector<TVInformation*>  mAllResolution;
};

}

#endif
