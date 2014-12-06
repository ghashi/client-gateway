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

	free(buffer);

        (*jvm)->ReleaseStringUTFChars(jvm, jmessage, message);
        (*jvm)->ReleaseStringUTFChars(jvm, jkey, key);
        (*jvm)->ReleaseStringUTFChars(jvm, jtag, tag);

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

	unsigned int ciphertext_len = 2 * plaintext_len;
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
	DECODE_IN_B64(plaintext);
	DECODE_IN_B64(pkey);

	unsigned int ciphertext_len = ntru_ciphertext_len();
	unsigned char *ciphertext = malloc(ciphertext_len);

	ntru_encryption(pkey, plaintext, ciphertext);

	ENCODE_B64(ciphertext);
        jstring jciphertext = (*jvm)->NewStringUTF(jvm, buffer);

	free(ciphertext);

        RELEASE(plaintext);
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

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    generateCSR
 * Signature: (ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_generateCSR(JNIEnv *jvm, jobject jobj, jint jid, jstring jcname, jstring jauth_key, jstring jtoken_key, jstring jmss_skey) {
	unsigned int id = jid;
	char *cname = (*jvm)->GetStringUTFChars(jvm, jcname, JNI_FALSE);
	unsigned char *auth_key = (*jvm)->GetStringUTFChars(jvm, jauth_key, JNI_FALSE);
	unsigned char *token_key = (*jvm)->GetStringUTFChars(jvm, jtoken_key, JNI_FALSE);
	unsigned char *mss_skey = (*jvm)->GetStringUTFChars(jvm, jmss_skey, JNI_FALSE);

	char csr[CSR_MAX_SIZE];

	generate_csr(id, cname, auth_key, token_key, mss_skey, csr);

        (*jvm)->ReleaseStringUTFChars(jvm, jcname, cname);
        (*jvm)->ReleaseStringUTFChars(jvm, jauth_key, auth_key);
        (*jvm)->ReleaseStringUTFChars(jvm, jtoken_key, token_key);
        (*jvm)->ReleaseStringUTFChars(jvm, jmss_skey, mss_skey);

	return csr;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    readCSR
 * Signature: (Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT jboolean JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_readCSR(JNIEnv *jvm, jobject jobj, jstring jcsr, jint jid, jstring jcname, jstring jtime, jstring jauth_key, jstring jtoken_key, jstring jmss_skey) {
	unsigned int id;
	char *cname = (*jvm)->GetStringUTFChars(jvm, jcname, JNI_TRUE);
	char *time = (*jvm)->GetStringUTFChars(jvm, jtime, JNI_TRUE);
	unsigned char *auth_key = (*jvm)->GetStringUTFChars(jvm, jauth_key, JNI_TRUE);
	unsigned char *token_key = (*jvm)->GetStringUTFChars(jvm, jtoken_key, JNI_TRUE);
	unsigned char *mss_skey = (*jvm)->GetStringUTFChars(jvm, jmss_skey, JNI_TRUE);
	unsigned char *csr = (*jvm)->GetStringUTFChars(jvm, jcsr, JNI_FALSE);

	unsigned char accept = read_csr(&id, cname, time, auth_key, token_key, mss_skey, csr);

	jid = id;

        (*jvm)->ReleaseStringUTFChars(jvm, jcname, cname);
        (*jvm)->ReleaseStringUTFChars(jvm, jtime, cname);
        (*jvm)->ReleaseStringUTFChars(jvm, jauth_key, auth_key);
        (*jvm)->ReleaseStringUTFChars(jvm, jtoken_key, token_key);
        (*jvm)->ReleaseStringUTFChars(jvm, jmss_skey, mss_skey);
        (*jvm)->ReleaseStringUTFChars(jvm, jcsr, csr);

	return accept;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    readCert
 * Signature: (Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT jboolean JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_readCert(JNIEnv *jvm, jobject jobj, jstring jcertificate, jint jid, jstring jcname, jstring jtime, jstring jvalid, jstring jauth_key, jstring jtoken_key, jstring jcert_signature, jstring jca_pkey) {
	unsigned int id;
	char *cname = (*jvm)->GetStringUTFChars(jvm, jcname, JNI_TRUE);
	char *time = (*jvm)->GetStringUTFChars(jvm, jtime, JNI_TRUE);
	char *valid = (*jvm)->GetStringUTFChars(jvm, jvalid, JNI_TRUE);
	unsigned char *auth_key = (*jvm)->GetStringUTFChars(jvm, jauth_key, JNI_TRUE);
	unsigned char *token_key = (*jvm)->GetStringUTFChars(jvm, jtoken_key, JNI_TRUE);
	unsigned char *cert_signature = (*jvm)->GetStringUTFChars(jvm, jcert_signature, JNI_TRUE);
	unsigned char *ca_pkey = (*jvm)->GetStringUTFChars(jvm, jca_pkey, JNI_FALSE);
	unsigned char *certificate = (*jvm)->GetStringUTFChars(jvm, jcertificate, JNI_FALSE);

	unsigned char accept = read_certificate(&id, cname, time, valid, auth_key, token_key, cert_signature, ca_pkey, certificate);

	jid = id;

        (*jvm)->ReleaseStringUTFChars(jvm, jcname, cname);
        (*jvm)->ReleaseStringUTFChars(jvm, jtime, cname);
        (*jvm)->ReleaseStringUTFChars(jvm, jvalid, cname);
        (*jvm)->ReleaseStringUTFChars(jvm, jauth_key, auth_key);
        (*jvm)->ReleaseStringUTFChars(jvm, jtoken_key, token_key);
        (*jvm)->ReleaseStringUTFChars(jvm, jcert_signature, cert_signature);
        (*jvm)->ReleaseStringUTFChars(jvm, jca_pkey, ca_pkey);
        (*jvm)->ReleaseStringUTFChars(jvm, jcertificate, certificate);


	return accept;
}
