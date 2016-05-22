LOCAL_PATH := $(call my-dir)
    include $(CLEAR_VARS)
    LOCAL_PREBUILT_LIBS := lib/libion_rk.so
    LOCAL_MODULE_TAGS := optional
    LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/lib
    include $(BUILD_MULTI_PREBUILT)
