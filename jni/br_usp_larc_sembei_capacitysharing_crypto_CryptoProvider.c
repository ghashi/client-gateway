#include <jni.h>

#include "time.h"
#include "mss.h"

/*
 * Class:     br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider
 * Method:    benchKeyGen
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_br_usp_larc_sembei_capacitysharing_crypto_CryptoProvider_benchKeyGen(JNIEnv *jvm, jobject jobj) {
int mark = 1;
	clock_t elapsed;

	dm_t dm;
	mmo_t mmo;
	struct mss_node node[2];
	struct mss_state state;
	unsigned char seed[LEN_BYTES(MSS_SEC_LVL)], pkey[NODE_VALUE_SIZE];

	elapsed = -clock();
	mss_keygen_core(&dm,&mmo, seed, &node[0], &node[1], &state, pkey);
	elapsed += clock();

        return 1000*(float)elapsed/CLOCKS_PER_SEC/mark;
}
