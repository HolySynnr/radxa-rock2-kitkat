CUR_PATH := device/rockchip/common/app
PRODUCT_PACKAGES += \
    ITVLauncher \
    ITVSetting

PRODUCT_COPY_FILES += \
    $(CUR_PATH)/apk/itvlauncher/libitvbox.so:system/lib/libitvbox.so
