ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3288)
ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), Mali-T760)

PRODUCT_COPY_FILES += \
    device/rockchip/common/gpu/libMali-T760/libGLES_mali.so:system/vendor/lib/egl/libGLES_mali.so \
	device/rockchip/common/gpu/libMali-T760/mali_kbase.ko:system/lib/modules/mali_kbase.ko.3.10.0 \
	device/rockchip/common/gpu/libMali-T760/mali_kbase.ko:system/lib/modules/mali_kbase.ko \
    device/rockchip/common/gpu/gpu_performance/performance_info.xml:system/etc/performance_info.xml \
    device/rockchip/common/gpu/gpu_performance/packages-compat.xml:system/etc/packages-compat.xml \
    device/rockchip/common/gpu/gpu_performance/performance:system/bin/performance \
    device/rockchip/common/gpu/gpu_performance/libperformance_runtime.so:system/lib/libperformance_runtime.so \
    device/rockchip/common/gpu/gpu_performance/gpu.$(TARGET_BOARD_HARDWARE).so:system/lib/hw/gpu.$(TARGET_BOARD_HARDWARE).so \
    device/rockchip/common/gpu/gpu_performance/libface_detection_native.so:system/lib/libface_detection_native.so \
    device/rockchip/common/gpu/gpu_performance/libopencv_java.so:system/lib/libopencv_java.so

endif
endif

ifeq ($(strip $(TARGET_BOARD_PLATFORM)), rk3188)
ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), Mali400MP)
PRODUCT_COPY_FILES += \
    device/rockchip/common/gpu/libMali-T760/libMali.so:system/lib/libMali.so \
    device/rockchip/common/gpu/libMali-T760/libMali.so:obj/lib/libMali.so \
    device/rockchip/common/gpu/libMali-T760/libUMP.so:system/lib/libUMP.so \
    device/rockchip/common/gpu/libMali-T760/libUMP.so:obj/lib/libUMP.so \
    device/rockchip/common/gpu/libMali-T760/libEGL_mali.so:system/lib/egl/libEGL_mali.so \
    device/rockchip/common/gpu/libMali-T760/libGLESv1_CM_mali.so:system/lib/egl/libGLESv1_CM_mali.so \
    device/rockchip/common/gpu/libMali-T760/libGLESv2_mali.so:system/lib/egl/libGLESv2_mali.so \
    device/rockchip/common/gpu/libMali-T760/mali.ko.3.10.0:system/lib/modules/mali.ko.3.10.0 \
    device/rockchip/common/gpu/libMali-T760/ump.ko.3.10.0:system/lib/modules/ump.ko.3.10.0 \
    device/rockchip/common/gpu/gpu_performance/performance_info.xml:system/etc/performance_info.xml \
    device/rockchip/common/gpu/gpu_performance/packages-compat.xml:system/etc/packages-compat.xml \
    device/rockchip/common/gpu/gpu_performance/performance:system/bin/performance \
    device/rockchip/common/gpu/gpu_performance/libperformance_runtime.so:system/lib/libperformance_runtime.so \
    device/rockchip/common/gpu/gpu_performance/gpu.$(TARGET_BOARD_HARDWARE).so:system/lib/hw/gpu.$(TARGET_BOARD_HARDWARE).so

endif
endif
