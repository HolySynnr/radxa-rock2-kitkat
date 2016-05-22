/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

#include <linux/kdev_t.h>

#define LOG_TAG "DirectVolume"

#include <cutils/log.h>
#include <sysutils/NetlinkEvent.h>

#include "DirectVolume.h"
#include "VolumeManager.h"
#include "ResponseCode.h"
#include "cryptfs.h"

#define PARTITION_DEBUG

DirectVolume::DirectVolume(VolumeManager *vm, const fstab_rec* rec, int flags) :
        Volume(vm, rec, flags) {
    mPaths = new PathCollection();
    for (int i = 0; i < MAX_PARTITIONS; i++)
        mPartMinors[i] = -1;
    mPendingPartMap = 0;
    mDiskMajor = -1;
    mDiskMinor = -1;
    mDiskNumParts = 0;
    mIsDecrypted = 0;

    if (strcmp(rec->mount_point, "auto") != 0) {
        ALOGE("Vold managed volumes must have auto mount point; ignoring %s",
              rec->mount_point);
    }

    char mount[PATH_MAX];

    snprintf(mount, PATH_MAX, "%s/%s", Volume::MEDIA_DIR, rec->label);
    //mMountpoint = strdup(rec->label);
    mMountpoint = strdup(rec->mount_point);
    snprintf(mount, PATH_MAX, "%s/%s", Volume::FUSE_DIR, rec->label);
    mFuseMountpoint = strdup(mount);
    setState(Volume::State_NoMedia);
}

DirectVolume::~DirectVolume() {
    PathCollection::iterator it;

    for (it = mPaths->begin(); it != mPaths->end(); ++it)
        free(*it);
    delete mPaths;
}

int DirectVolume::addPath(const char *path) {
    mPaths->push_back(strdup(path));
    return 0;
}

dev_t DirectVolume::getDiskDevice() {
    return MKDEV(mDiskMajor, mDiskMinor);
}

dev_t DirectVolume::getShareDevice() {
    if (mPartIdx != -1) {
        return MKDEV(mDiskMajor, mPartIdx);
    } else {
        return MKDEV(mDiskMajor, mDiskMinor);
    }
}

void DirectVolume::handleVolumeShared() {
    setState(Volume::State_Shared);
}

void DirectVolume::handleVolumeUnshared() {
    setState(Volume::State_Idle);
}
void DirectVolume::handleDiskForDuoPartitionRemoved(const char *devpath, NetlinkEvent *evt) {
    int major = atoi(evt->findParam("MAJOR"));
    int minor = atoi(evt->findParam("MINOR"));
    char msg[255];
    char devicePath[255];

    sprintf(devicePath, "/dev/block/vold/%d:%d", major,
            minor+mDiskNumParts);
    
    SLOGD("handleDiskForDuoPartitionRemoved Volume %s %s disk %d:%d removed\n", getLabel(), getMountpoint(), major, minor);
	//to remove disk mount file.
	//maybe it's a ntfs udisk, in other words, the whole disk is the unique partition,not any logical partitions.
	//just remove any partitions in the disk.
	handleAllUdiskPartitionRemoved();
    CHANGE_ANDROIDFILESYSTEM_TO_READWRITE;
	if(rmdir(mDiskMountFilePathName)){
		SLOGE("RemoveUdiskMountFile %s Failed! errno = %s", mDiskMountFilePathName, strerror(errno));
	}
	CHANGE_ANDROIDFILESYSTEM_TO_READONLY;
    sprintf(devicePath, "/dev/block/vold/%d:%d", major,
            minor);
    SLOGE("handleDiskRemoved,ready to unlink: %s",devicePath);
    if ( 0 != unlink(devicePath) ) {
        SLOGE("Failed to unlink %s or it has been unlinked!",devicePath);
    }
    setState(Volume::State_NoMedia);
    setDevPath(NULL);
}
void DirectVolume::handleUdiskPartitionRemoved(const char *devpath, NetlinkEvent *evt)
{
    int major = atoi(evt->findParam("MAJOR"));
    int minor = atoi(evt->findParam("MINOR"));
	char devicePath[255];
    const char *pMountpoint =NULL;
    int istate;
    UDISK_PARTITION_CONFIG *pUdiskPartiton;

    pUdiskPartiton =getPartitionState(major,minor);
    if(pUdiskPartiton ==NULL)
        return  ;
    pMountpoint =pUdiskPartiton->ucMountPoint;
    SLOGD("Volume  %s partition %d:%d removed", pUdiskPartiton->ucMountPoint, major, minor);

    /*
    * Finally, unmount the actual block device from the staging dir
    */
    if (0 !=doUnmount(pUdiskPartiton->ucFilePathName, true)) {
        SLOGE("handleUdiskPartitionRemoved Failed to unmount %s (%s)", pUdiskPartiton->ucFilePathName, strerror(errno));
    }
    else
        SLOGI("handleUdiskPartitionRemoved %s unmounted sucessfully ", pUdiskPartiton->ucFilePathName);
    
	CHANGE_ANDROIDFILESYSTEM_TO_READWRITE;
    if(pUdiskPartiton->ucFilePathName)
        rmdir(pUdiskPartiton->ucFilePathName);
	CHANGE_ANDROIDFILESYSTEM_TO_READONLY;
    RemoveUdiskPartition(pMountpoint);
    sprintf(devicePath, "/dev/block/vold/%d:%d", major,
            minor);
    SLOGE("handleUdiskPartitionRemoved handlePartitionRemoved,ready to unlink: %s",devicePath);
    if ( 0 != unlink(devicePath) ) {
        SLOGE("handleUdiskPartitionRemoved Failed to unlink %s",devicePath);
    }
}
void DirectVolume::handleAllUdiskPartitionRemoved() {
	if(!mUdiskPartition || mUdiskPartition->empty())
		return;

	char devicePath[255];
	const char *pMountpoint =NULL;
	UDISK_PARTITION_CONFIG *pUdiskPartiton;
	UDisk_Partition_Collection::iterator it;
	int major;
	int minor;
	while(!mUdiskPartition->empty()) {
		SLOGD("there are %d partitions in the disk still", mUdiskPartition->size());
		it = mUdiskPartition->begin();
		pUdiskPartiton = *it;
		major = pUdiskPartiton->imajor;
		minor = pUdiskPartiton->iminor;
		pMountpoint = pUdiskPartiton->ucMountPoint;
		SLOGD("Volume  %s partition %d:%d removed", pUdiskPartiton->ucMountPoint, major, minor);

		/*
		* Finally, unmount the actual block device from the staging dir
		*/
		if (0 !=doUnmount(pUdiskPartiton->ucFilePathName, true)) {
			SLOGE("handleUdiskPartitionRemoved Failed to unmount %s (%s)", pUdiskPartiton->ucFilePathName, strerror(errno));
		}
		else
			SLOGI("handleUdiskPartitionRemoved %s unmounted sucessfully ", pUdiskPartiton->ucFilePathName);
		
		CHANGE_ANDROIDFILESYSTEM_TO_READWRITE;
		if(pUdiskPartiton->ucFilePathName)
			rmdir(pUdiskPartiton->ucFilePathName);
		CHANGE_ANDROIDFILESYSTEM_TO_READONLY;
		//RemoveUdiskPartition(pMountpoint);
		sprintf(devicePath, "/dev/block/vold/%d:%d", major,
				minor);
		SLOGE("handleUdiskPartitionRemoved handlePartitionRemoved,ready to unlink: %s",devicePath);
		if ( 0 != unlink(devicePath) ) {
			SLOGE("handleUdiskPartitionRemoved Failed to unlink %s",devicePath);
		}
		mUdiskPartition->erase(it);
		
	}
}


void DirectVolume::handleUdiskDiskAdded(const char *devpath, NetlinkEvent *evt) {
    mDiskMajor = atoi(evt->findParam("MAJOR"));
    mDiskMinor = atoi(evt->findParam("MINOR"));

    const char *tmp = evt->findParam("NPARTS");
    if (tmp) {
        mDiskNumParts = atoi(tmp);
    } else {
        SLOGW("Kernel block uevent missing 'NPARTS'");
        mDiskNumParts = 1;
    }
/*ifdef DISABLE_INTERNAL_DISK	  
		if (!strcmp(getLabel(),"udiskint"))
			handleToWriteLunfile();
endif DISABLE_INTERNAL_DISK*/
    char msg[255];
    int iPartNum;
    int partmask = 0;
    int i;
    for (i = 1; i <= mDiskNumParts; i++) {
        partmask |= (1 << i);
    }
    mPendingPartMap = partmask;
    
    if (mDiskNumParts == 0) {
        mPartMinors[0] = mDiskMinor;
#ifdef PARTITION_DEBUG
        SLOGD("Dv::handleUdiskDiskAdded - No partitions - good to go son!");
#endif
        setState(Volume::State_Idle);

        snprintf(msg, sizeof(msg), "Volume %s %s disk inserted (%d:%d) state %d",
                 getLabel(), getMountpoint(), mDiskMajor, mDiskMinor, Volume::State_Idle);
        mVm->getBroadcaster()->sendBroadcast(ResponseCode::VolumeDiskInserted,
                                                 msg, false);
         iPartNum =addUdiskPartition(mDiskMajor,mDiskMinor);
        if( -1 ==iPartNum)
            return ;
    } else {
#ifdef PARTITION_DEBUG
        SLOGD("Dv::diskIns - waiting for %d partitions (mask 0x%x)",
             mDiskNumParts, mPendingPartMap);
#endif
        setState(Volume::State_Pending);
	mIndexPartition =0;
	mDiskVolumelMinors[0] =mDiskMinor;
	
        snprintf(msg, sizeof(msg), "Volume %s %s disk inserted (%d:%d) state %d",
                 getLabel(), getMountpoint(), mDiskMajor, mDiskMinor, Volume::State_Pending);
        mVm->getBroadcaster()->sendBroadcast(ResponseCode::VolumeDiskInserted,
                                                 msg, false);
    }
}

void DirectVolume::handleUdiskPartitionAdded(const char *devpath, NetlinkEvent *evt) {   //add to process usb partition add message by cc
    int major = atoi(evt->findParam("MAJOR"));
    int minor = atoi(evt->findParam("MINOR"));

    int part_num;
    char msg[255];
    int iPartNum,ii =0;

    const char *tmp = evt->findParam("PARTN");
    
    if (tmp) {
        part_num = atoi(tmp);
    } else {
        SLOGW("Kernel block uevent missing 'PARTN'");
        part_num = 1;
    }
    mIndexPartition++;
 #ifdef PARTITION_DEBUG   
    SLOGW("##################handleUdiskPartitionAdded part_num =%d    mDiskNumParts =%d   mIndexPartition =%d",part_num,mDiskNumParts,mIndexPartition);
#endif
    /*part_num = 1;//only mount partition 1 add by cc
    if (part_num > mDiskNumParts) {
        mDiskNumParts = part_num;
    }*/

    if (major != mDiskMajor) {
        SLOGE("Partition '%s' has a different major than its disk!", devpath);
        return;
    }
    mPartMinors[part_num -1] = minor;
    
    //mPendingPartMap &= ~(1 << part_num);    
    mPendingPartMap &= ~(1 << mIndexPartition);    
 #ifdef PARTITION_DEBUG   
     SLOGD("Dv:partAdd: part_num = %d, minor = %d,mPendingPartMap =%x,mDiskNumParts=%d", part_num, minor,mPendingPartMap,mDiskNumParts);
#endif
    iPartNum =addUdiskPartition(major,minor);
    if( -1 ==iPartNum)
    return ;

    if (!mPendingPartMap) {        //do not care about the mask ,just send PatritionAdd message to mountservice add by cc
#ifdef PARTITION_DEBUG
        SLOGD("Dv:partAdd: Got all partitions - ready to rock!  curstate =%d",getState());
#endif

     if (getState() != Volume::State_Formatting)
        {
            setState(Volume::State_Idle);
            if (mRetryMount == true) {
                SLOGD("!!!! mount failed,and need to retry mountVol()!!!!");
                mRetryMount = false;
                mountVol();
            }else{
                snprintf(msg, sizeof(msg), "Volume %s %s partition added (%d:%d)",
                       getLabel(), getMountpoint(), major, minor);
                SLOGD(" ---------!!!! no retry ,send msg(%s) --------!!!!",msg);
                mVm->getBroadcaster()->sendBroadcast(ResponseCode::VolumePartitionAdded,
                                                msg, false);
            }
        }
    } else {
#ifdef PARTITION_DEBUG
        SLOGD("Dv:partAdd: pending mask now = 0x%x", mPendingPartMap);
#endif
    }
}
int DirectVolume::handleBlockEvent(NetlinkEvent *evt) {
    const char *dp = evt->findParam("DEVPATH");

    PathCollection::iterator  it;
    for (it = mPaths->begin(); it != mPaths->end(); ++it) {
        if (!strncmp(dp, *it, strlen(*it))) {
            /* We can handle this disk */
            int action = evt->getAction();
            const char *devtype = evt->findParam("DEVTYPE");

            if (action == NetlinkEvent::NlActionAdd) {
                int major = atoi(evt->findParam("MAJOR"));
                int minor = atoi(evt->findParam("MINOR"));
                char nodepath[255];

                snprintf(nodepath,
                         sizeof(nodepath), "/dev/block/vold/%d:%d",
                         major, minor);
                if (createDeviceNode(nodepath, major, minor)) {
                    SLOGE("Error making device node '%s' (%s)", nodepath,
                                                               strerror(errno));
                }
                if (strncmp(getLabel(),"usb_storage",strlen("usb_storage"))==0){
                    if (!strcmp(devtype, "disk")) 
                    {
                        if(getDevPath()==NULL)
                        {
                            setDevPath(dp);
                            handleUdiskDiskAdded(dp, evt);
                        }
                        else
                        {
                            return -1;
                        }
                    } 
                    else 
                    {
                        char *pDevPah =(char *)getDevPath();
                        if (!strncmp(dp, pDevPah, strlen(pDevPah)))
                        {
                            handleUdiskPartitionAdded(dp,evt);
                        }
                        else
                        {
                            return -1;
                        }
                    }
                }
                else if (!strcmp(devtype, "disk"))
                {
                    handleDiskAdded(dp, evt);
                } else {
                    handlePartitionAdded(dp, evt);
                }
                /* Send notification iff disk is ready (ie all partitions found) */
                if (getState() == Volume::State_Idle) {
                    char msg[255];

                    snprintf(msg, sizeof(msg),
                             "Volume %s %s disk inserted (%d:%d)", getLabel(),
                             getFuseMountpoint(), mDiskMajor, mDiskMinor);
                    mVm->getBroadcaster()->sendBroadcast(ResponseCode::VolumeDiskInserted,
                                                         msg, false);
                }
            } else if (action == NetlinkEvent::NlActionRemove) {
                if (strncmp(getLabel(),"usb_storage",strlen("usb_storage"))==0)
                {
                    char *pDevPah =(char *)getDevPath();
                    if(pDevPah ==NULL)
                        return -1;
                    else if(strncmp(dp, pDevPah, strlen(pDevPah)))
                        return -1;
        	           SLOGE("NlActionRemove pDevPah=%s,devtype=%s",pDevPah,devtype);
                    if (!strcmp(devtype, "disk")) {
                        handleDiskForDuoPartitionRemoved(dp, evt);
                    } else {
                        handleUdiskPartitionRemoved(dp, evt);
                    }            	    
            	}
                if (!strcmp(devtype, "disk")) {
#ifdef SUPPORTED_MULTI_USB_PARTITIONS
                    if (!strcmp(getLabel(),USB_DISK_LABEL))
                        handlePartitionRemoved(dp, evt);
#endif
                    handleDiskRemoved(dp, evt);
                } else {
                    handlePartitionRemoved(dp, evt);
                }
            } else if (action == NetlinkEvent::NlActionChange) {
                if (!strcmp(devtype, "disk")) {
                    handleDiskChanged(dp, evt);
                } else {
                    handlePartitionChanged(dp, evt);
                }
            } else {
                    SLOGW("Ignoring non add/remove/change event");
            }

            return 0;
        }
    }
    errno = ENODEV;
    return -1;
}

void DirectVolume::handleDiskAdded(const char *devpath, NetlinkEvent *evt) {
    mDiskMajor = atoi(evt->findParam("MAJOR"));
    mDiskMinor = atoi(evt->findParam("MINOR"));

    const char *tmp = evt->findParam("NPARTS");
    if (tmp) {
        mDiskNumParts = atoi(tmp);
    } else {
        SLOGW("Kernel block uevent missing 'NPARTS'");
        mDiskNumParts = 1;
    }

#ifdef PARTITION_DEBUG
	SLOGD("----handleDiskAdded,mDiskNumParts =%d,mDiskMajor=%d,mDiskMinor=%d",mDiskNumParts,mDiskMajor,mDiskMinor);
#endif

    char msg[255];

    int partmask = 0;
    int i;
    for (i = 1; i <= mDiskNumParts; i++) {
        partmask |= (1 << i);
    }
    mPendingPartMap = partmask;

    if (mDiskNumParts == 0) {
#ifdef PARTITION_DEBUG
        SLOGD("Dv::diskIns - No partitions - good to go son!");
#endif
        setState(Volume::State_Idle);
	    snprintf(msg, sizeof(msg), "Volume %s %s disk inserted (%d:%d)",
	             getLabel(), getMountpoint(), mDiskMajor, mDiskMinor);
	    mVm->getBroadcaster()->sendBroadcast(ResponseCode::VolumeDiskInserted,msg, false);

    } else {
#ifdef PARTITION_DEBUG
        SLOGD("Dv::diskIns - waiting for %d partitions (mask 0x%x)",
             mDiskNumParts, mPendingPartMap);
#endif
        setState(Volume::State_Pending);
    }
}

void DirectVolume::handlePartitionAdded(const char *devpath, NetlinkEvent *evt) {
    int major = atoi(evt->findParam("MAJOR"));
    int minor = atoi(evt->findParam("MINOR"));

    int part_num;

    const char *tmp = evt->findParam("PARTN");

    if (tmp) {
        part_num = atoi(tmp);
    } else {
        SLOGW("Kernel block uevent missing 'PARTN'");
        part_num = 1;
    }

    if (part_num > MAX_PARTITIONS || part_num < 1) {
        SLOGE("Invalid 'PARTN' value");
        return;
    }

    if (part_num > mDiskNumParts) {
        if (mDiskNumParts == 0)
            mDiskNumParts = part_num;
        else
            part_num = mDiskNumParts;
    }

#ifdef PARTITION_DEBUG
	SLOGD("---handlePartitionAdded,part_num=%d,major=%d,minor=%d",part_num,major,minor);
#endif
    if (major != mDiskMajor) {
        SLOGE("Partition '%s' has a different major than its disk!", devpath);
        return;
    }
#ifdef PARTITION_DEBUG
    SLOGD("Dv:partAdd: part_num = %d, minor = %d\n", part_num, minor);
#endif
    if (part_num >= MAX_PARTITIONS) {
        SLOGE("Dv:partAdd: ignoring part_num = %d (max: %d)\n", part_num, MAX_PARTITIONS-1);
    } else {
        mPartMinors[part_num -1] = minor;
    }
    mPendingPartMap &= ~(1 << part_num);

    if (!mPendingPartMap) {
#ifdef PARTITION_DEBUG
        SLOGD("Dv:partAdd: Got all partitions - ready to rock!");
#endif
        if (getState() != Volume::State_Formatting) {
            setState(Volume::State_Idle);
            if (mRetryMount == true) {
                mRetryMount = false;
                mountVol();
            }
        }
    } else {
#ifdef PARTITION_DEBUG
        SLOGD("Dv:partAdd: pending mask now = 0x%x", mPendingPartMap);
#endif
    }
}

void DirectVolume::handleDiskChanged(const char *devpath, NetlinkEvent *evt) {
    int major = atoi(evt->findParam("MAJOR"));
    int minor = atoi(evt->findParam("MINOR"));

    if ((major != mDiskMajor) || (minor != mDiskMinor)) {
        return;
    }

    SLOGI("Volume %s disk has changed", getLabel());
    const char *tmp = evt->findParam("NPARTS");
    if (tmp) {
        mDiskNumParts = atoi(tmp);
    } else {
        SLOGW("Kernel block uevent missing 'NPARTS'");
        mDiskNumParts = 1;
    }

    int partmask = 0;
    int i;
    for (i = 1; i <= mDiskNumParts; i++) {
        partmask |= (1 << i);
    }
    mPendingPartMap = partmask;

    if (getState() != Volume::State_Formatting) {
        if (mDiskNumParts == 0) {
            setState(Volume::State_Idle);
        } else {
            setState(Volume::State_Pending);
        }
    }
}

void DirectVolume::handlePartitionChanged(const char *devpath, NetlinkEvent *evt) {
    int major = atoi(evt->findParam("MAJOR"));
    int minor = atoi(evt->findParam("MINOR"));
    SLOGD("Volume %s %s partition %d:%d changed\n", getLabel(), getMountpoint(), major, minor);
}

void DirectVolume::handleDiskRemoved(const char *devpath, NetlinkEvent *evt) {
    int major = atoi(evt->findParam("MAJOR"));
    int minor = atoi(evt->findParam("MINOR"));
    char msg[255];
	char devicePath[255];
    bool enabled;

    if (mVm->shareEnabled(getLabel(), "ums", &enabled) == 0 && enabled) {
        mVm->unshareVolume(getLabel(), "ums");
    }

    sprintf(devicePath, "/dev/block/vold/%d:%d", major,minor);
#ifdef SUPPORTED_MULTI_USB_PARTITIONS
    /*
    * TODO :: check every partition was unmounted
    */
    if (!strcmp(getLabel(),USB_DISK_LABEL)){
       if (!isPartitionEmpty()){
           setState(Volume::State_Mounted);
           return;
       }
       SLOGD("Udisk has no partitions, now unmounting /mnt/usb_storage !");
    }
#endif

    if (access(devicePath, R_OK) == 0) {
        SLOGD("current mounted dev exist,access devicePath: %s ;partitionNum: %d", devicePath, mDiskNumParts);

        /*
         * Confirm partition removed.
         */
         if (Volume::unmountVol(true, false)) {
            SLOGE("Failed to unmount volume on bad removal (%s)",
                 strerror(errno));
             // XXX: At this point we're screwed for now
         } else {
             SLOGD("Crisis averted");
         }

         SLOGE("handlePartitionRemoved,ready to unlink: %s",devicePath);
         if ( 0 != unlink(devicePath) ) {
             SLOGE("Failed to unlink %s",devicePath);
         }
    }
    SLOGD("Volume %s %s disk %d:%d removed\n", getLabel(), getMountpoint(), major, minor);
    snprintf(msg, sizeof(msg), "Volume %s %s disk removed (%d:%d)",
             getLabel(), getFuseMountpoint(), major, minor);
    mVm->getBroadcaster()->sendBroadcast(ResponseCode::VolumeDiskRemoved,
                                             msg, false);
    setState(Volume::State_NoMedia);
}

void DirectVolume::handlePartitionRemoved(const char *devpath, NetlinkEvent *evt) {
    int major = atoi(evt->findParam("MAJOR"));
    int minor = atoi(evt->findParam("MINOR"));
    char msg[255];
    int state;

    SLOGD("Volume %s %s partition %d:%d removed\n", getLabel(), getMountpoint(), major, minor);

#ifdef SUPPORTED_MULTI_USB_PARTITIONS
    if(!strcmp(getLabel(),USB_DISK_LABEL)){
       if (Volume::unmountPartition(major,minor)){
           SLOGE("Failed to unmount volume on bad removal (%s)", strerror(errno));
       } else {
           SLOGD("Crisis averted %d:%d",major,minor);
       }

       char devicePath[255];
       sprintf(devicePath, "/dev/block/vold/%d:%d", major,minor);
       SLOGD("handlePartitionRemoved,ready to unlink: %s (%s)",devicePath,devpath);
       if ( 0 != unlink(devicePath) ) {
           SLOGE("Failed to unlink %s",devicePath);
       } 

       snprintf(msg, sizeof(msg), "Volume %s %s bad removal (%d:%d)",
                   getLabel(), getMountpoint(), major, minor);
       mVm->getBroadcaster()->sendBroadcast(ResponseCode::VolumeBadRemoval,
                           msg, false);
       return;
    }
#endif

    /*
     * The framework doesn't need to get notified of
     * partition removal unless it's mounted. Otherwise
     * the removal notification will be sent on the Disk
     * itself
     */
    state = getState();
    if (state != Volume::State_Mounted && state != Volume::State_Shared) {
        return;
    }
        
    if ((dev_t) MKDEV(major, minor) == mCurrentlyMountedKdev) {
        /*
         * Yikes, our mounted partition is going away!
         */

        bool providesAsec = (getFlags() & VOL_PROVIDES_ASEC) != 0;
        if (providesAsec && mVm->cleanupAsec(this, true)) {
            SLOGE("Failed to cleanup ASEC - unmount will probably fail!");
        }

        snprintf(msg, sizeof(msg), "Volume %s %s bad removal (%d:%d)",
                 getLabel(), getFuseMountpoint(), major, minor);
        mVm->getBroadcaster()->sendBroadcast(ResponseCode::VolumeBadRemoval,
                                             msg, false);

        if (Volume::unmountVol(true, false)) {
            SLOGE("Failed to unmount volume on bad removal (%s)", 
                 strerror(errno));
            // XXX: At this point we're screwed for now
        } else {
            SLOGD("Crisis averted");
        }
    } else if (state == Volume::State_Shared) {
        /* removed during mass storage */
	 snprintf(msg, sizeof(msg), "Volume %s %s bad removal (%d:%d)",
                 getLabel(), getMountpoint(), major, minor);
        mVm->getBroadcaster()->sendBroadcast(ResponseCode::VolumeBadRemoval,
                                             msg, false);

        if (mVm->unshareVolume(getLabel(), "ums")) {
            SLOGE("Failed to unshare volume on bad removal (%s)",
                strerror(errno));
        } else {
            SLOGD("Crisis averted");
        }
    }
}

/*
 * Called from base to get a list of devicenodes for mounting
 */
int DirectVolume::getDeviceNodes(dev_t *devs, int max) {

    if (mPartIdx == -1) {
        // If the disk has no partitions, try the disk itself
        if (!mDiskNumParts) {
            devs[0] = MKDEV(mDiskMajor, mDiskMinor);
            return 1;
        }

        int i;
        for (i = 0; i < mDiskNumParts; i++) {
            if (i == max)
                break;
            devs[i] = MKDEV(mDiskMajor, mPartMinors[i]);
        }
        return mDiskNumParts;
    }
    devs[0] = MKDEV(mDiskMajor, mPartMinors[mPartIdx -1]);
    return 1;
}

/*
 * Called from base to update device info,
 * e.g. When setting up an dm-crypt mapping for the sd card.
 */
int DirectVolume::updateDeviceInfo(char *new_path, int new_major, int new_minor)
{
    PathCollection::iterator it;

    if (mPartIdx == -1) {
        SLOGE("Can only change device info on a partition\n");
        return -1;
    }

    /*
     * This is to change the sysfs path associated with a partition, in particular,
     * for an internal SD card partition that is encrypted.  Thus, the list is
     * expected to be only 1 entry long.  Check that and bail if not.
     */
    if (mPaths->size() != 1) {
        SLOGE("Cannot change path if there are more than one for a volume\n");
        return -1;
    }

    it = mPaths->begin();
    free(*it); /* Free the string storage */
    mPaths->erase(it); /* Remove it from the list */
    addPath(new_path); /* Put the new path on the list */

    /* Save away original info so we can restore it when doing factory reset.
     * Then, when doing the format, it will format the original device in the
     * clear, otherwise it just formats the encrypted device which is not
     * readable when the device boots unencrypted after the reset.
     */
    mOrigDiskMajor = mDiskMajor;
    mOrigDiskMinor = mDiskMinor;
    mOrigPartIdx = mPartIdx;
    memcpy(mOrigPartMinors, mPartMinors, sizeof(mPartMinors));

    mDiskMajor = new_major;
    mDiskMinor = new_minor;
    /* Ugh, virual block devices don't use minor 0 for whole disk and minor > 0 for
     * partition number.  They don't have partitions, they are just virtual block
     * devices, and minor number 0 is the first dm-crypt device.  Luckily the first
     * dm-crypt device is for the userdata partition, which gets minor number 0, and
     * it is not managed by vold.  So the next device is minor number one, which we
     * will call partition one.
     */
    mPartIdx = new_minor;
    mPartMinors[new_minor-1] = new_minor;

    mIsDecrypted = 1;

    return 0;
}

/*
 * Called from base to revert device info to the way it was before a
 * crypto mapping was created for it.
 */
void DirectVolume::revertDeviceInfo(void)
{
    if (mIsDecrypted) {
        mDiskMajor = mOrigDiskMajor;
        mDiskMinor = mOrigDiskMinor;
        mPartIdx = mOrigPartIdx;
        memcpy(mPartMinors, mOrigPartMinors, sizeof(mPartMinors));

        mIsDecrypted = 0;
    }

    return;
}

/*
 * Called from base to give cryptfs all the info it needs to encrypt eligible volumes
 */
int DirectVolume::getVolInfo(struct volume_info *v)
{
    strcpy(v->label, mLabel);
    strcpy(v->mnt_point, mMountpoint);
    v->flags = getFlags();
    /* Other fields of struct volume_info are filled in by the caller or cryptfs.c */

    return 0;
}


#ifdef SUPPORTED_MULTI_USB_PARTITIONS
const char* DirectVolume::getUdiskMountpoint(char* devicepath,int major,int minor,char letter)
{
    SLOGD("GET MOUNTPOINT:%s DEVICEPATH:%s",mMountpoint,devicepath);
    if (!strcmp(getLabel(),USB_DISK_LABEL))
    {
        Partitions::iterator ir;
        for (ir = mPartitions.begin(); ir != mPartitions.end(); ++ir)
        {
            if (((VolumePartition*)*ir)->major == major && ((VolumePartition*)*ir)->minor == minor)
            {
                return ((VolumePartition*)*ir)->mountpoint;
            }
        }

        /* If no letter*/
        if (!letter)
        {
            return NULL;
        }

        /* If Unmounted*/
        char mount_point[255]={0};
        char vLabel[255]={0};
        getVolumeLabel(devicepath,vLabel,letter);
        sprintf(mount_point,"%s/%s",mMountpoint,vLabel);
        return mount_point;
    }
    /* Dont try to call this function but udisk.
     * If you try, we return value same as getMountpoint();
     */
    return mMountpoint;
}
#endif
