CUR_PATH := vendor/google

PRODUCT_PACKAGES += \
         GmsCore \
	 PrebuiltGmsCore \
         GoogleBackupTransport \
         GoogleFeedback \
         GoogleLoginService \
         GooglePartnerSetup \
         GoogleServicesFramework \
         GoogleTTS \
         MediaUploader \
         NetworkLocation \
         Phonesky \
         VoiceSearchStub \
	 talkback \
	 Velvet \
	 Maps \
	 QuickOffice \
	 Gmail \
	 chrome

gapps_files := $(shell ls $(CUR_PATH)/libs/mini)
PRODUCT_COPY_FILES += $(foreach file, $(gapps_files), \
$(CUR_PATH)/libs/mini/$(file):system/lib/$(file))

gapps_files := $(shell ls $(CUR_PATH)/framework )
PRODUCT_COPY_FILES += $(foreach file, $(gapps_files), \
    $(CUR_PATH)/framework/$(file):system/framework/$(file))

gapps_files := $(shell ls $(CUR_PATH)/etc/permissions )
PRODUCT_COPY_FILES += $(foreach file, $(gapps_files), \
	$(CUR_PATH)/etc/permissions/$(file):system/etc/permissions/$(file))

gapps_files := $(shell ls $(CUR_PATH)/usr/srec/en-US )
PRODUCT_COPY_FILES += $(foreach file, $(gapps_files), \
	$(CUR_PATH)/usr/srec/en-US/$(file):system/usr/srec/en-US/$(file))
