CUR_PATH := device/rockchip/common/app
PRODUCT_PACKAGES += \
		RkApkinstaller \
		RkVideoPlayer \
		StressTest \
		RkMusic \
		eHomeMediaCenter_box \
		WifiDisplay \
		RKGameControlSettingV1.0.1\
		RKSettings \
		RKBasicSettings \
		HDMINotification

PRODUCT_COPY_FILES += \
        $(CUR_PATH)/apk/ESFileExplorer.apk:system/app/ESFileExplorer.apk \
        $(CUR_PATH)/apk/flashplayer:system/app/flashplayer \
        $(CUR_PATH)/apk/SuperSU.apk:system/app/SuperSU.apk \
