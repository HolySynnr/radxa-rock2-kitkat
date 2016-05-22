ifeq ($(strip $(TARGET_BOARD_PLATFORM_GPU)), PVR540)

PRODUCT_COPY_FILES += \
    device/rockchip/common/gpu/libPVR540/pvrsrvctl:/system/vendor/bin/pvrsrvctl\
    device/rockchip/common/gpu/libPVR540/gralloc.rk30xxb.so:system/vendor/lib/hw/gralloc.rk30xxb.so\
    device/rockchip/common/gpu/libPVR540/libEGL_POWERVR_SGX540_130.so:system/vendor/lib/egl/libEGL_POWERVR_SGX540_130.so\
    device/rockchip/common/gpu/libPVR540/libGLESv1_CM_POWERVR_SGX540_130.so:system/vendor/lib/egl/libGLESv1_CM_POWERVR_SGX540_130.so\
    device/rockchip/common/gpu/libPVR540/libGLESv2_POWERVR_SGX540_130.so:system/vendor/lib/egl/libGLESv2_POWERVR_SGX540_130.so\
    device/rockchip/common/gpu/libPVR540/libIMGegl.so:system/vendor/lib/libIMGegl.so\
    device/rockchip/common/gpu/libPVR540/libPVRScopeServices.so:system/vendor/lib/libPVRScopeServices.so\
    device/rockchip/common/gpu/libPVR540/libglslcompiler.so:system/vendor/lib/libglslcompiler.so\
    device/rockchip/common/gpu/libPVR540/libpvr2d.so:system/vendor/lib/libpvr2d.so\
    device/rockchip/common/gpu/libPVR540/libpvrANDROID_WSEGL.so:system/vendor/lib/libpvrANDROID_WSEGL.so\
    device/rockchip/common/gpu/libPVR540/libsrv_init.so:system/vendor/lib/libsrv_init.so\
    device/rockchip/common/gpu/libPVR540/libsrv_um.so:system/vendor/lib/libsrv_um.so\
    device/rockchip/common/gpu/libPVR540/libusc.so:system/vendor/lib/libusc.so\
    device/rockchip/common/gpu/libPVR540/rklfb.ko:system/lib/modules/rklfb.ko\
    device/rockchip/common/gpu/libPVR540/pvrsrvkm.ko:system/lib/modules/pvrsrvkm.ko\
    device/rockchip/common/gpu/libPVR540/libperformance_runtime.so:system/lib/libperformance_runtime.so\
    device/rockchip/common/gpu/libPVR540/gpu.$(TARGET_BOARD_HARDWARE).so:system/lib/hw/gpu.$(TARGET_BOARD_HARDWARE).so\
    device/rockchip/common/gpu/libPVR540/performance_info.xml:system/etc/performance_info.xml

endif
