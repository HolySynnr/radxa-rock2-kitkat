CUR_PATH := vendor/google

# copy all model files
define all-models-files-under
$(patsubst ./%,%, \
	$(shell cd $(CUR_PATH)/$(1) ; \
    find ./ -type f -and -not -name "*.apk" -and -not -name "*.so" -and -not -name "*.mk") \
)
endef

PRODUCT_PACKAGES += \
    FaceLock

PRODUCT_COPY_FILES += \
	vendor/google/libs/libfacelock_jni.so:system/lib/libfacelock_jni.so

PRODUCT_PROPERTY_OVERRIDES += \
    ro.config.facelock = enable_facelock \
    persist.facelock.detect_cutoff=5000 \
    persist.facelock.recog_cutoff=5000
