#include <jni.h>

#include "aes_128.h"
#include "hmac.h"
#include "util.h"
#include "certificate.h"

#include <string.h>

#define DECODE_IN_B64(var)								\
        const char *var##_b64 = (*jvm)->GetStringUTFChars(jvm, j##var, JNI_FALSE);	\
	unsigned int var##_len = (*jvm)->GetStringUTFLength(jvm, j##var);		\
	unsigned char *var = malloc(var##_len);						\
	base64decode(var##_b64, var##_len, var, &var##_len);

#define ENCODE_B64(var)							\
	unsigned int buffer_len = 2 * var##_len;			\
	unsigned char *buffer = malloc(buffer_len);			\
        base64encode(var, var##_len, buffer, buffer_len);
	

#define RELEASE(var)						\
	free(var);						\
        (*jvm)->ReleaseStringUTFChars(jvm, j##var, var##_b64);

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    get_hash
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_get_1hash(JNIEnv *jvm, jobject jobj, jstring jmessage) {
	const char *message = (*jvm)->GetStringUTFChars(jvm, jmessage, JNI_FALSE);
	unsigned int message_len = (*jvm)->GetStringUTFLength(jvm, jmessage);

        unsigned char buffer[2 * (2 * MSS_SEC_LVL)];

	sponge_hash(message, message_len, buffer, 2 * MSS_SEC_LVL);


        base64encode(buffer, 2 * MSS_SEC_LVL, buffer, 2 * (2 * MSS_SEC_LVL));
        jstring jhash = (*jvm)->NewStringUTF(jvm, buffer);

        (*jvm)->ReleaseStringUTFChars(jvm, jmessage, message);

	return jhash;
}


/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    get_hmac
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_get_1hmac(JNIEnv *jvm, jobject jobj, jstring jmessage, jstring jkey) {
	const char *message = (*jvm)->GetStringUTFChars(jvm, jmessage, JNI_FALSE);
	DECODE_IN_B64(key);

        unsigned char tag[HMAC_TAG_SIZE];
        unsigned int tag_len = HMAC_TAG_SIZE;

	get_hmac(message, key, tag);

	ENCODE_B64(tag);
        jstring jtag = (*jvm)->NewStringUTF(jvm, buffer);

	free(buffer);

        (*jvm)->ReleaseStringUTFChars(jvm, jmessage, message);
	RELEASE(key);

	return jtag;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    verify_hmac
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_verify_1hmac(JNIEnv *jvm, jobject jobj, jstring jmessage, jstring jkey, jstring jtag) {
	const char *message = (*jvm)->GetStringUTFChars(jvm, jmessage, JNI_FALSE);
	DECODE_IN_B64(key);
	DECODE_IN_B64(tag);

	jboolean accepted = verify_hmac(tag, message, key);

        (*jvm)->ReleaseStringUTFChars(jvm, jmessage, message);
	RELEASE(key);
	RELEASE(tag);

        return accepted;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    symmetric_encrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_symmetric_1encrypt(JNIEnv *jvm, jobject jobj, jstring jplaintext, jstring jiv, jstring jkey) {
	const char *plaintext = (*jvm)->GetStringUTFChars(jvm, jplaintext, JNI_FALSE);
	unsigned int plaintext_len = (*jvm)->GetStringUTFLength(jvm, jplaintext);
	DECODE_IN_B64(iv);
	DECODE_IN_B64(key);

	unsigned int ciphertext_len = plaintext_len + AES_128_BLOCK_SIZE;
	unsigned char *ciphertext = malloc(ciphertext_len);

	aes_128_cbc_encrypt(key, iv, plaintext, ciphertext, &ciphertext_len);

	ENCODE_B64(ciphertext);
        jstring jciphertext = (*jvm)->NewStringUTF(jvm, buffer);

	free(ciphertext);
	free(buffer);

        (*jvm)->ReleaseStringUTFChars(jvm, jplaintext, plaintext);
        RELEASE(iv);
        RELEASE(key);
	return jciphertext;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    symmetric_decrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_symmetric_1decrypt(JNIEnv *jvm, jobject jobj, jstring jciphertext, jstring jiv, jstring jkey) {
	DECODE_IN_B64(ciphertext);
	DECODE_IN_B64(iv);
	DECODE_IN_B64(key);

	unsigned int plaintext_len = ciphertext_len;
	unsigned char *plaintext = malloc(plaintext_len);

	aes_128_cbc_decrypt(key, iv, ciphertext, ciphertext_len, plaintext);

        jstring jplaintext = (*jvm)->NewStringUTF(jvm, plaintext);

        RELEASE(ciphertext);
        RELEASE(iv);
        RELEASE(key);
	return jplaintext;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    asymmetric_encrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_asymmetric_1encrypt(JNIEnv *jvm, jobject jobj, jstring jplaintext, jstring jpkey) {
	const char *plaintext = (*jvm)->GetStringUTFChars(jvm, jplaintext, JNI_FALSE);
	unsigned int plaintext_len = (*jvm)->GetStringUTFLength(jvm, jplaintext);
	DECODE_IN_B64(pkey);

	unsigned int ciphertext_len = ntru_ciphertext_len();
	unsigned char *ciphertext = malloc(ciphertext_len);

	ntru_encryption(pkey, plaintext, ciphertext);

	ENCODE_B64(ciphertext);
        jstring jciphertext = (*jvm)->NewStringUTF(jvm, buffer);

	free(ciphertext);

        (*jvm)->ReleaseStringUTFChars(jvm, jplaintext, plaintext);
        RELEASE(pkey);
	return jciphertext;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    asymmetric_decrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_asymmetric_1decrypt(JNIEnv *jvm, jobject jobj, jstring jciphertext, jstring jskey) {
	DECODE_IN_B64(ciphertext);
	DECODE_IN_B64(skey);

	unsigned char plaintext[100];	// maximum plaintext size < 100 bytes

	ntru_decryption(skey, ciphertext, plaintext);
        jstring jplaintext = (*jvm)->NewStringUTF(jvm, plaintext);

        RELEASE(ciphertext);
        RELEASE(skey);
	return jplaintext;
}
