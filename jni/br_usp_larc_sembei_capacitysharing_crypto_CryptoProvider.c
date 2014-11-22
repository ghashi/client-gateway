#include <jni.h>

#include "aes_128.h"
#include "hmac.h"
#include "util.h"

#include <string.h>

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    get_hmac
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_get_1hmac(JNIEnv *jvm, jobject jobj, jstring jmessage, jstring jkey) {
	const char *message = (*jvm)->GetStringUTFChars(jvm, jmessage, JNI_FALSE);
        const unsigned char *key = (*jvm)->GetStringUTFChars(jvm, jkey, JNI_FALSE);

        unsigned char tag[HMAC_TAG_SIZE];

	get_hmac(message, key, tag);
        char buffer[2 * HMAC_TAG_SIZE];

        base64encode(tag, HMAC_TAG_SIZE, buffer, 2 * HMAC_TAG_SIZE);
        jstring jtag = (*jvm)->NewStringUTF(jvm, buffer);

        (*jvm)->ReleaseStringUTFChars(jvm, jmessage, message);
        (*jvm)->ReleaseStringUTFChars(jvm, jkey, key);

	return jtag;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    verify_hmac
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_verify_1hmac(JNIEnv *jvm, jobject jobj, jstring jmessage, jstring jkey, jstring jtag) {
	const char *message = (*jvm)->GetStringUTFChars(jvm, jmessage, JNI_FALSE);
        const unsigned char *key = (*jvm)->GetStringUTFChars(jvm, jkey, JNI_FALSE);
        const char *tag = (*jvm)->GetStringUTFChars(jvm, jtag, JNI_FALSE);

	unsigned int buffer_len = (*jvm)->GetStringUTFLength(jvm, jtag);
	unsigned char *buffer = malloc(buffer_len);
	base64decode(tag, buffer_len, buffer, &buffer_len);

	jboolean accepted = verify_hmac(buffer, message, key);

        (*jvm)->ReleaseStringUTFChars(jvm, jmessage, message);
        (*jvm)->ReleaseStringUTFChars(jvm, jkey, key);
        (*jvm)->ReleaseStringUTFChars(jvm, jtag, tag);

	free(buffer);

        return accepted;

}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    symmetric_encrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_symmetric_1encrypt(JNIEnv *jvm, jobject jobj, jstring jplaintext, jstring jkey) {
        jstring jtag = (*jvm)->NewStringUTF(jvm, "teste");
	return jtag;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    symmetric_decrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_symmetric_1decrypt(JNIEnv *jvm, jobject jobj, jstring jciphertext, jstring jkey) {
        jstring jtag = (*jvm)->NewStringUTF(jvm, "teste");
	return jtag;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    asymmetric_encrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_asymmetric_1encrypt(JNIEnv *jvm, jobject jobj, jstring plaintext, jstring pkey) {
        jstring jtag = (*jvm)->NewStringUTF(jvm, "teste");
	return jtag;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    asymmetric_decrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_asymmetric_1decrypt(JNIEnv *jvm, jobject jobj, jstring ciphertext, jstring skey) {
        jstring jtag = (*jvm)->NewStringUTF(jvm, "teste");
	return jtag;
}
