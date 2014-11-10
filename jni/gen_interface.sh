echo "Generating C headers..."
while read class
do
	echo -e -n "\t$class... "
	javah -classpath ../bin/classes -jni -d jni br.usp.larc.sembei.capacitysharing.crypto.$class
	echo "Done!"
done < java_classes.txt
mv jni/* .
rmdir jni
