#include <jni.h>

#include "mss.h"
#include "util.h"

#include <string.h>

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
	memcpy(pkey, keypair + MSS_SKEY_SIZE, MSS_SKEY_SIZE);

	jobjectArray jkeypair = (jobjectArray)(*jvm)->NewObjectArray(jvm, 2, (jclass) (*jvm)->FindClass(jvm, "java/lang/String"), (*jvm)->NewStringUTF(jvm, ""));  
  
	unsigned char buffer[2 * MSS_SKEY_SIZE];
 
	base64encode(skey, MSS_SKEY_SIZE, buffer, 2 * MSS_SKEY_SIZE);
	(*jvm)->SetObjectArrayElement(jvm, jkeypair, 0, (*jvm)->NewStringUTF(jvm, buffer));  

	base64encode(pkey, MSS_PKEY_SIZE, buffer, 2 * MSS_SKEY_SIZE);
	(*jvm)->SetObjectArrayElement(jvm, jkeypair, 1, (*jvm)->NewStringUTF(jvm, buffer));

	return jkeypair;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSS
 * Method:    sign
 * Signature: (Ljava/lang/String;Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSS_sign(JNIEnv *jvm, jclass jString, jstring jmessage, jstring jskey) {
	/*const char *seed = (*jvm)->GetStringUTFChars(jvm, jseed, 0);

	unsigned char *keypair, skey[MSS_SKEY_SIZE], pkey[MSS_PKEY_SIZE];

	keypair = mss_keygen(seed);

	memcpy(skey, keypair, MSS_SKEY_SIZE);
	memcpy(pkey, keypair + MSS_SKEY_SIZE, MSS_SKEY_SIZE);

	(*jvm)->ReleaseStringUTFChars(jvm, jString, seed);
	*/jobjectArray jsignature = (jobjectArray)(*jvm)->NewObjectArray(jvm, 2, (*jvm)->FindClass(jvm, "java/lang/String"), (*jvm)->NewStringUTF(jvm, ""));
	return jsignature;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSS
 * Method:    verify
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSS_verify(JNIEnv *jvm, jclass jString, jstring jmessage, jstring jsignature, jstring jkey) {
	return JNI_TRUE;
}
