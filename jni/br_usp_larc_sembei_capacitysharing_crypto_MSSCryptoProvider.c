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
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider
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
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider
 * Method:    readCert
 * Signature: (Ljava/lang/String;)[Ljava/lang/Object;
 */
JNIEXPORT jobjectArray JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_MSSCryptoProvider_readCert(JNIEnv *jvm, jobject job, jstring jcertificate, jstring jca_pkey) {
	unsigned char *certificate = (*jvm)->GetStringUTFChars(jvm, jcertificate, JNI_FALSE);
	DECODE_IN_B64(ca_pkey);
	
	unsigned int id;
	char cname[100];
	char time[TIME_BUFFER_SIZE];
	char valid[TIME_BUFFER_SIZE];
	unsigned char auth_key[SMQV_PKEY_SIZE];
	unsigned char token_key[MSS_PKEY_SIZE];
	unsigned char signature[ECDSA_SIGNATURE_SIZE];

	unsigned char accept = read_certificate(&id, cname, time, valid, auth_key, token_key, signature, ca_pkey, certificate);

	unsigned char buffer_auth_key[2 * SMQV_PKEY_SIZE];
	unsigned char buffer_token_key[2 * MSS_PKEY_SIZE];
	unsigned char buffer_signature[2 * ECDSA_SIGNATURE_SIZE];

	base64encode(auth_key, SMQV_PKEY_SIZE, buffer_auth_key, 2 * SMQV_PKEY_SIZE);
	base64encode(token_key, MSS_PKEY_SIZE, buffer_token_key, 2 * MSS_PKEY_SIZE);
	base64encode(signature, ECDSA_SIGNATURE_SIZE, buffer_signature, 2 * ECDSA_SIGNATURE_SIZE);

	jobjectArray jcertificate_fields = (jobjectArray)(*jvm)->NewObjectArray(jvm, 7, (*jvm)->FindClass(jvm, "java/lang/Object"), NULL);

	(*jvm)->SetObjectArrayElement(jvm, jcertificate_fields, 0, (*jvm)->NewObject(jvm, (*jvm)->FindClass(jvm, "java/lang/Integer"), (*jvm)->GetMethodID(jvm, (*jvm)->FindClass(jvm, "java/lang/Integer"), "<init>", "(I)V"), id)); // http://stackoverflow.com/questions/13877543/how-to-instantiate-a-class-in-jni
	(*jvm)->SetObjectArrayElement(jvm, jcertificate_fields, 1, (*jvm)->NewStringUTF(jvm, cname));
	(*jvm)->SetObjectArrayElement(jvm, jcertificate_fields, 2, (*jvm)->NewStringUTF(jvm, time));
	(*jvm)->SetObjectArrayElement(jvm, jcertificate_fields, 3, (*jvm)->NewStringUTF(jvm, valid));
	(*jvm)->SetObjectArrayElement(jvm, jcertificate_fields, 4, (*jvm)->NewStringUTF(jvm, buffer_auth_key));
	(*jvm)->SetObjectArrayElement(jvm, jcertificate_fields, 5, (*jvm)->NewStringUTF(jvm, buffer_token_key));
	(*jvm)->SetObjectArrayElement(jvm, jcertificate_fields, 6, (*jvm)->NewStringUTF(jvm, buffer_signature));

        (*jvm)->ReleaseStringUTFChars(jvm, jcertificate, certificate);
	RELEASE(ca_pkey);

	return jcertificate_fields;
}
