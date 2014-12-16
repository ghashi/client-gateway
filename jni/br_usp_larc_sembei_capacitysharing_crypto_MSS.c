#include <jni.h>

#include "mss.h"
#include "util.h"

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
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSS
 * Method:    keyGen
 * Signature: (Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSS_keyGen(JNIEnv *jvm, jclass jString, jbyteArray jseed) {
	const char *seed = (*jvm)->GetByteArrayElements(jvm, jseed, JNI_FALSE);
	unsigned char *keypair, skey[MSS_SKEY_SIZE], pkey[MSS_PKEY_SIZE];

	keypair = mss_keygen(seed);

	memcpy(skey, keypair, MSS_SKEY_SIZE);
	memcpy(pkey, keypair + MSS_SKEY_SIZE, MSS_PKEY_SIZE);

	jobjectArray jkeypair = (jobjectArray)(*jvm)->NewObjectArray(jvm, 2, (jclass) (*jvm)->FindClass(jvm, "java/lang/String"), (*jvm)->NewStringUTF(jvm, ""));  
  
	unsigned char buffer_skey[2 * MSS_SKEY_SIZE];
	unsigned char buffer_pkey[2 * MSS_PKEY_SIZE];
 
	base64encode(skey, MSS_SKEY_SIZE, buffer_skey, 2 * MSS_SKEY_SIZE);
	(*jvm)->SetObjectArrayElement(jvm, jkeypair, 0, (*jvm)->NewStringUTF(jvm, buffer_skey));  

	base64encode(pkey, MSS_PKEY_SIZE, buffer_pkey, 2 * MSS_PKEY_SIZE);
	(*jvm)->SetObjectArrayElement(jvm, jkeypair, 1, (*jvm)->NewStringUTF(jvm, buffer_pkey));

	return jkeypair;
}

#define MAX(x, y) (((x) > (y)) ? (x) : (y))

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSS
 * Method:    sign
 * Signature: (Ljava/lang/String;Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSS_sign(JNIEnv *jvm, jclass jString, jstring jmessage, jstring jskey) {
	const char *message = (*jvm)->GetStringUTFChars(jvm, jmessage, JNI_FALSE);
	DECODE_IN_B64(skey);

	unsigned char *signature = mss_sign(skey, message);
	signature = mss_sign(skey, message);

	jobjectArray jsignature = (jobjectArray)(*jvm)->NewObjectArray(jvm, 2, (*jvm)->FindClass(jvm, "java/lang/String"), (*jvm)->NewStringUTF(jvm, ""));

	unsigned char buffer_signature[2 * MSS_SIGNATURE_SIZE];
	unsigned int buffer_signature_len = 2 * MSS_SIGNATURE_SIZE;
 
	base64encode(signature, MSS_SIGNATURE_SIZE, buffer_signature, buffer_signature_len);
	(*jvm)->SetObjectArrayElement(jvm, jsignature, 0, (*jvm)->NewStringUTF(jvm, buffer_signature));

	unsigned char buffer_skey[2 * MSS_SKEY_SIZE];
	unsigned int buffer_skey_len = 2 * MSS_SKEY_SIZE;
 
	base64encode(skey, MSS_SKEY_SIZE, buffer_skey, buffer_skey_len);
	(*jvm)->SetObjectArrayElement(jvm, jsignature, 1, (*jvm)->NewStringUTF(jvm, buffer_skey));

	free(signature);

	(*jvm)->ReleaseStringUTFChars(jvm, jString, message);
	RELEASE(skey);

	return jsignature;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSS
 * Method:    verify
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSS_verify(JNIEnv *jvm, jclass jString, jstring jmessage, jstring jsignature, jstring jpkey) {
	const char *message = (*jvm)->GetStringUTFChars(jvm, jmessage, JNI_FALSE);
	DECODE_IN_B64(signature);
	DECODE_IN_B64(pkey);

	jboolean accepted = mss_verify(signature, pkey, message);

	(*jvm)->ReleaseStringUTFChars(jvm, jString, message);
	RELEASE(signature);
	RELEASE(pkey);

	return accepted;
}
