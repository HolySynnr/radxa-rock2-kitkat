#ifndef _RK_BLURAY_CONFIG_H_
#define _RK_BLURAY_CONFIG_H_

#include <stdlib.h>

namespace RKBluray {


class BlurayConfig
{
public:
    int mPid;
    int mPlayItem;
    int mCode;
    int mInMainPath;
    unsigned int mDuration;
    unsigned int mMovieObejctId;
    char*  mPath;
    int64_t mStartOffset;
    int64_t mEndOffset;

    BlurayConfig()
    {
        mPid = 0;
        mPlayItem = 0;
        mCode = 0;
        mDuration = 0;
        mInMainPath = 1;
        mMovieObejctId = 0;
        mPath = NULL;
        mStartOffset = 0;
        mEndOffset = 0;
    }

    ~BlurayConfig()
    {
        if(mPath != NULL)
        {
            free(mPath);
            mPath = NULL;
        }
    }
};


}
#endif
