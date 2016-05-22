LOCAL_PATH := $(call my-dir)

# Use BUILD_PREBUILT instead of PRODUCT_COPY_FILES to bring in the NOTICE file.
include $(CLEAR_VARS)                   
LOCAL_PREBUILT_LIBS := librk_on2.so                
LOCAL_MODULE_TAGS := optional           
include $(BUILD_MULTI_PREBUILT)

ifneq ($(strip $(TARGET_BOARD_PLATFORM)), rk2928)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libjpeghwenc.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libjpeghwdec.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

endif

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libjesancache.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := librkswscale.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := librkwmapro.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libapedec.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

#FFMPEG Lib
include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libRKBluray.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := librkboxffplayer.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)



include $(CLEAR_VARS)

LOCAL_PREBUILT_LIBS := librkffplayer.so

LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)

LOCAL_PREBUILT_LIBS := libffmpeg.so

LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)


include $(CLEAR_VARS)

LOCAL_PREBUILT_LIBS := libffmpeg_vp9dec.so

LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libhevcdec.so

LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := librk_hevcdec.so

LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)
