#include <jni.h>

#include "time.h"
#include "certificate.h"
#include "mss.h"

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
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider
 * Method:    keyGen
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider_keyGen(JNIEnv *jvm, jobject jobj, jint mark) {
        clock_t elapsed;
        unsigned int i;

        dm_t dm;
        mmo_t mmo;
        struct mss_node node[2];
        struct mss_state state;
        unsigned char seed[LEN_BYTES(MSS_SEC_LVL)], pkey[NODE_VALUE_SIZE];

        elapsed = -clock();
        for(i = 0; i < mark; i++)
                mss_keygen_core(&dm,&mmo, seed, &node[0], &node[1], &state, pkey);
        elapsed += clock();

        return 1000*(float)elapsed/CLOCKS_PER_SEC;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider
 * Method:    sign
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider_sign(JNIEnv *jvm, jclass jobj, jint mark) {
        clock_t elapsed;
        unsigned int i;

        /* Auxiliary varibles */
        struct mss_node node[3];
        unsigned char hash[LEN_BYTES(WINTERNITZ_N)];
        unsigned char ots[MSS_OTS_SIZE];
        unsigned char aux[LEN_BYTES(WINTERNITZ_SEC_LVL)];

        mmo_t hash_mmo;
        dm_t hash_dm;

        /* Merkle-tree variables */
        struct mss_state state;
        struct mss_node authpath[MSS_HEIGHT];

        unsigned char pkey[NODE_VALUE_SIZE];
        unsigned char skey[LEN_BYTES(MSS_SEC_LVL)] = {0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3A,0x3B,0x3C,0x3D,0x3E,0x3F};

        mss_keygen_core(&hash_dm, &hash_mmo, skey, &node[0], &node[1], &state, pkey);

        const char message[] = "Long Johnson, Don Piano";

        elapsed = -clock();
        for(i = 0; i < mark; i++)
                mss_sign_core(&state, skey, &node[0], (const char*)message, strlen(message) + 1, &hash_mmo, &hash_dm, hash, i, &node[1], &node[2], ots, authpath);
        elapsed += clock();

        return 1000*(float)elapsed/CLOCKS_PER_SEC;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider
 * Method:    verify
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider_verify(JNIEnv *jvm, jclass jobj, jint mark) {
        clock_t elapsed;
        unsigned int i;

        /* Auxiliary varibles */
        struct mss_node node[3];
        unsigned char hash[LEN_BYTES(WINTERNITZ_N)];
        unsigned char ots[MSS_OTS_SIZE];
        unsigned char aux[LEN_BYTES(WINTERNITZ_SEC_LVL)];

        mmo_t hash_mmo;
        dm_t hash_dm;

        /* Merkle-tree variables */
        struct mss_state state;
        struct mss_node authpath[MSS_HEIGHT];

        unsigned char pkey[NODE_VALUE_SIZE];
        unsigned char skey[LEN_BYTES(MSS_SEC_LVL)] = {0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3A,0x3B,0x3C,0x3D,0x3E,0x3F};

        mss_keygen_core(&hash_dm, &hash_mmo, skey, &node[0], &node[1], &state, pkey);

        const char message[] = "Long Johnson, Don Piano";

        mss_sign_core(&state, skey, &node[0], (const char*)message, strlen(message) + 1, &hash_mmo, &hash_dm, hash, i, &node[1], &node[2], ots, authpath);

        elapsed = -clock();
        for(i = 0; i < mark; i++)
                mss_verify_core(authpath, node[0].value, message, strlen(message) + 1, &hash_mmo, &hash_dm, hash, node[0].index, ots, aux, &node[0], pkey);
        elapsed += clock();

        return 1000*(float)elapsed/CLOCKS_PER_SEC;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    generateCSR
 * Signature: (ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider_generateCSR(JNIEnv *jvm, jobject jobj, jint jid, jstring jcname, jstring jauth_key, jstring jtoken_key, jstring jmss_skey) {
	unsigned int id = jid;
	char *cname = (*jvm)->GetStringUTFChars(jvm, jcname, JNI_FALSE);
	DECODE_IN_B64(auth_key);
	DECODE_IN_B64(token_key);
	DECODE_IN_B64(mss_skey);

	char *csr = malloc(CSR_MAX_SIZE);

	generate_csr(id, cname, auth_key, token_key, mss_skey, csr);

	jstring jcsr = (*jvm)->NewStringUTF(jvm, csr);

        (*jvm)->ReleaseStringUTFChars(jvm, jcname, cname);
	RELEASE(auth_key);
	RELEASE(token_key);
	RELEASE(mss_skey);

	return jcsr;
}

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    readCSR
 * Signature: (Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT jboolean JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider_readCSR(JNIEnv *jvm, jobject jobj, jstring jcsr, jint jid, jstring jcname, jstring jtime, jstring jauth_key, jstring jtoken_key, jstring jmss_skey) {
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
JNIEXPORT jboolean JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider_readCert(JNIEnv *jvm, jobject jobj, jstring jcertificate, jint jid, jstring jcname, jstring jtime, jstring jvalid, jstring jauth_key, jstring jtoken_key, jstring jcert_signature, jstring jca_pkey) {
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
