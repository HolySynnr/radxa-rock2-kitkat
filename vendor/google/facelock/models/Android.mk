#limitations under the License.

LOCAL_PATH := $(call my-dir)

# ==== FaceLock models ========================
include $(CLEAR_VARS)
LOCAL_MODULE := detection/yaw_roll_face_detectors.6/head-y0-yi45-p0-pi45-rn30-ri30.5-v24/full_model.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

#============================================================
include $(CLEAR_VARS)
LOCAL_MODULE := detection/yaw_roll_face_detectors.6/head-y0-yi45-p0-pi45-r0-ri30.4a-v24/full_model.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC 

# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

#============================================================
include $(CLEAR_VARS)
LOCAL_MODULE := detection/yaw_roll_face_detectors.6/head-y0-yi45-p0-pi45-rp30-ri30.5-v24/full_model.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC 

# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

#============================================================
include $(CLEAR_VARS)
LOCAL_MODULE := detection/multi_pose_face_landmark_detectors.7/left_eye-y0-yi45-p0-pi45-r0-ri20.lg_32/full_model.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC

# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)
#============================================================

include $(CLEAR_VARS)
LOCAL_MODULE := detection/multi_pose_face_landmark_detectors.7/nose_base-y0-yi45-p0-pi45-r0-ri20.lg_32/full_model.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC

# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)
#============================================================

include $(CLEAR_VARS)
LOCAL_MODULE := detection/multi_pose_face_landmark_detectors.7/right_eye-y0-yi45-p0-pi45-r0-ri20.lg_32-2/full_model.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC

# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)
#============================================================

include $(CLEAR_VARS)
LOCAL_MODULE := recognition/face.face.y0-y0-22-b-N/full_model.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC

# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := detection/multi_pose_face_landmark_detectors.7/left_eye-y0-yi45-p0-pi45-r0-ri20.lg_32.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC

# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := detection/multi_pose_face_landmark_detectors.7/nose_base-y0-yi45-p0-pi45-r0-ri20.lg_32.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := detection/multi_pose_face_landmark_detectors.7/right_eye-y0-yi45-p0-pi45-r0-ri20.lg_32-2.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := detection/yaw_roll_face_detectors.6/head-y0-yi45-p0-pi45-r0-ri30.4a-v24.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := detection/yaw_roll_face_detectors.6/head-y0-yi45-p0-pi45-rn30-ri30.5-v24.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)


include $(CLEAR_VARS)
LOCAL_MODULE := detection/yaw_roll_face_detectors.6/head-y0-yi45-p0-pi45-rp30-ri30.5-v24.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)


include $(CLEAR_VARS)
LOCAL_MODULE := recognition/face.face.y0-y0-22-b-N.bin
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
# This will install the file in /system/vendor/pittpatt/models
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/pittpatt/models
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)
