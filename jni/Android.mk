LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := ndkspeaker
LOCAL_LDLIBS 	:= -llog
LOCAL_SRC_FILES :=  lib_mfcc/abs.c \
					lib_mfcc/abs1.c \
					lib_mfcc/fft.c \
					lib_mfcc/fi_fft.c \
					lib_mfcc/fi_mfcc.c \
					lib_mfcc/fi_mfcc_initialize.c \
					lib_mfcc/fi_mfcc_rtwutil.c \
					lib_mfcc/fi_mfcc_terminate.c \
					lib_mfcc/log.c \
					lib_mfcc/mfcc_bare.c \
					lib_mfcc/mtimes1.c \
					lib_mfcc/power1.c \
					lib_mfcc/rt_nonfinite.c \
					lib_mfcc/rtGetInf.c \
					lib_mfcc/rtGetNaN.c \
					lib_mfcc/sqrt.c \
					lib_mfcc/sum.c \
					SpeakerRecognizer.c
					
include $(BUILD_SHARED_LIBRARY)