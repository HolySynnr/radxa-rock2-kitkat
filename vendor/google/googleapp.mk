CUR_PATH := vendor/google

PRODUCT_PACKAGES += \
	 ChromeBookmarksSyncAdapter \
	 CloudPrint2 \
         Drive \
         Gmail \
         GmsCore \
         GoogleBackupTransport \
         GoogleFeedback \
         GoogleLoginService \
         GooglePartnerSetup \
         GoogleServicesFramework \
         GoogleTTS \
         Hangouts \
         Keep \
         Maps \
         MediaUploader \
         NetworkLocation \
         Phonesky \
         PlusOne \
         QuickOffice \
         Street \
         Talk \
         talkback \
         VoiceSearchStub \
	 PrebuiltGmsCore \
	 SetupWizard \
	 talkback \
	 Velvet

gapps_files := $(shell ls $(CUR_PATH)/libs )
PRODUCT_COPY_FILES += $(foreach file, $(gapps_files), \
$(CUR_PATH)/libs/$(file):system/lib/$(file))

gapps_files := $(shell ls $(CUR_PATH)/framework )
PRODUCT_COPY_FILES += $(foreach file, $(gapps_files), \
    $(CUR_PATH)/framework/$(file):system/framework/$(file))

gapps_files := $(shell ls $(CUR_PATH)/etc/permissions )
PRODUCT_COPY_FILES += $(foreach file, $(gapps_files), \
	$(CUR_PATH)/etc/permissions/$(file):system/etc/permissions/$(file))

gapps_files := $(shell ls $(CUR_PATH)/usr/srec/en-US )
PRODUCT_COPY_FILES += $(foreach file, $(gapps_files), \
	$(CUR_PATH)/usr/srec/en-US/$(file):system/usr/srec/en-US/$(file))
