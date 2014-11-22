LOCAL_PATH := $(call my-dir)
CRYPTO_PROJ_PATH := $(LOCAL_PATH)/hashbasedsignature-code/workspace
CRYPTO_PROJ_SRC := 				\
	$(CRYPTO_PROJ_PATH)/src/TI_aes_128.c	\
	$(CRYPTO_PROJ_PATH)/src/aes_128.c	\
	$(CRYPTO_PROJ_PATH)/src/hash.c		\
	$(CRYPTO_PROJ_PATH)/src/winternitz.c	\
	$(CRYPTO_PROJ_PATH)/src/mss.c		\
	$(CRYPTO_PROJ_PATH)/src/hmac.c		\
	$(CRYPTO_PROJ_PATH)/src/util.c
CRYPTO_PROJ_FLAGS := -DAES_ENC_DEC -DAES_CBC_MODE 

SPONGE_PROJ_PATH := $(CRYPTO_PROJ_PATH)/modules/Hash-Functions-8-16-bits
SPONGE_PROJ_INCLUDE := $(SPONGE_PROJ_PATH) $(SPONGE_PROJ_PATH)/sponge_functions
SPONGE_PROJ_SRC :=						\
	$(SPONGE_PROJ_PATH)/sponge_functions/keccak8bits.c	\
	$(SPONGE_PROJ_PATH)/sponge8bits.c	

CERTIFICATE_PROJ_PATH := $(LOCAL_PATH)/certificate
CERTIFICATE_PROJ_SRC :=				\
	$(CERTIFICATE_PROJ_PATH)/certificate.c	\
	$(CERTIFICATE_PROJ_PATH)/cert_time.c	\
	$(CERTIFICATE_PROJ_PATH)/ecdsa.c	\
	$(CERTIFICATE_PROJ_PATH)/ntru.c

NTRU_PROJ_PATH := $(CERTIFICATE_PROJ_PATH)/libntru
NTRU_PROJ_SRC :=				\
	$(NTRU_PROJ_PATH)/src/ntru.c		\
	$(NTRU_PROJ_PATH)/src/bitstring.c	\
	$(NTRU_PROJ_PATH)/src/encparams.c	\
	$(NTRU_PROJ_PATH)/src/hash.c		\
	$(NTRU_PROJ_PATH)/src/idxgen.c		\
	$(NTRU_PROJ_PATH)/src/key.c		\
	$(NTRU_PROJ_PATH)/src/mgf.c		\
	$(NTRU_PROJ_PATH)/src/poly.c		\
	$(NTRU_PROJ_PATH)/src/rand.c		\
	$(NTRU_PROJ_PATH)/src/sha1.c		\
	$(NTRU_PROJ_PATH)/src/sha2.c

uECC_PROJ_PATH := $(CERTIFICATE_PROJ_PATH)/micro-ecc
uECC_PROJ_SRC := $(uECC_PROJ_PATH)/uECC.c

#build native lib
include $(CLEAR_VARS)
LOCAL_MODULE := crypto-library
LOCAL_C_INCLUDES += $(CRYPTO_PROJ_PATH)/include $(SPONGE_PROJ_INCLUDE) $(CERTIFICATE_PROJ_PATH) $(NTRU_PROJ_PATH)/src $(uECC_PROJ_PATH) 
LOCAL_CFLAGS += $(CRYPTO_PROJ_FLAGS)
LOCAL_SRC_FILES :=									\
			br_usp_larc_sembei_capacitysharing_crypto_MSS.c			\
			br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider.c	\
			br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider.c	\
			$(CRYPTO_PROJ_SRC)						\
			$(SPONGE_PROJ_SRC)						\
			$(CERTIFICATE_PROJ_SRC)						\
			$(NTRU_PROJ_SRC)						\
			$(uECC_PROJ_SRC)

include $(BUILD_SHARED_LIBRARY)
