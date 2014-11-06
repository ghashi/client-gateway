javah -classpath ../bin/classes -jni -d jni br.usp.larc.sembei.capacitysharing.crypto.CryptoProvider
mv jni/* .
rmdir jni
