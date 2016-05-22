LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := user
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_CERTIFICATE := platform

LOCAL_PACKAGE_NAME := DigtalTVPlayer

include $(BUILD_PACKAGE)

#copy libs and other file to system

#@echo "#############################################"
#@echo "copy DVB libs and etc files to system"
#@echo "#############################################"
#native
$(shell cp $(LOCAL_PATH)/libs/lib/libdvbstream.so $(TARGET_OUT)/lib/)
$(shell cp $(LOCAL_PATH)/libs/lib/librockchip_dvb_jni.so $(TARGET_OUT)/lib/)
$(shell cp $(LOCAL_PATH)/libs/bin/bindvbplayservice $(TARGET_OUT)/bin/)
	 		
#etc files	
$(shell mkdir $(TARGET_OUT)/etc/dtv)	
$(shell cp $(LOCAL_PATH)/libs/etc/dtv/ROCKCHIP.TV $(TARGET_OUT)/etc/dtv/)	 		
# Use the folloing include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))
