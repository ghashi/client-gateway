LOCAL_PATH := $(call my-dir)
CRYPTO_PROJ_PATH := $(LOCAL_PATH)/hashbasedsignature-code/workspace
CRYPTO_PROJ_SRC := 				\
	$(CRYPTO_PROJ_PATH)/src/TI_aes_128.c	\
	$(CRYPTO_PROJ_PATH)/src/aes_128.c	\
	$(CRYPTO_PROJ_PATH)/src/hash.c		\
	$(CRYPTO_PROJ_PATH)/src/winternitz.c	\
	$(CRYPTO_PROJ_PATH)/src/mss.c		\
	$(CRYPTO_PROJ_PATH)/src/util.c
CRYPTO_PROJ_FLAGS := -DAES_ENC_DEC -DAES_CBC_MODE 

#build native lib
include $(CLEAR_VARS)
LOCAL_MODULE := crypto-library
LOCAL_C_INCLUDES += $(CRYPTO_PROJ_PATH)/include
LOCAL_CFLAGS += $(CRYPTO_PROJ_FLAGS)
LOCAL_SRC_FILES := br_usp_larc_sembei_capacitysharing_crypto_MSS.c br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider.c $(CRYPTO_PROJ_SRC)

include $(BUILD_SHARED_LIBRARY)
