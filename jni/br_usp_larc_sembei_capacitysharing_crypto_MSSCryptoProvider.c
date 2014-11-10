#include <jni.h>

#include "time.h"
#include "mss.h"

#include <string.h>

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
