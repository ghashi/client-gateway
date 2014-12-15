package br.usp.larc.sembei.capacitysharing.crypto.keyagreement;

import br.usp.larc.keccak.*;
import br.usp.larc.pseudojava.BigInteger;
import br.usp.larc.pseudojava.SecureRandom;
import java.util.Calendar;


/**
 * Auxiliary functions to SMQV protocol
 * 
 * @author Geovandro C.C.F. Pereira
 */
public class SMQVUtil {

    private static final Keccak keccak;
    private static final SecureRandom rnd;
    private static final String hex = "0123456789abcdef";

    static {

        //Pseudo Random Number Generator
        byte[] randSeed = new byte[32];
        (new SecureRandom()).nextBytes(randSeed);
        rnd = new SecureRandom(randSeed);

        //Keccak hash function
        keccak = new Keccak();
    }
    
    /**
     * This method is the hash function H used by SMQV protocol.
     *
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     * @param arg5
     * @param outLen      The output hash size in bytes. Should be the length of the symmetric key to be agreed.
     * @return H(arg1,arg2,arg3,arg4,arg5)
     */
    public static final byte[] H(byte[] arg1, byte[] arg2, byte[] arg3, byte[] arg4, byte[] arg5, int outLen) {

        keccak.init(0);

        keccak.update(arg1, arg1.length*8);
        keccak.update(arg2, arg2.length*8);
        keccak.update(arg3, arg3.length*8);
        keccak.update(arg4, arg4.length*8);
        keccak.update(arg5, arg5.length*8);
        
        byte[] hashHbar = new byte[outLen];

        keccak.getHash(null);
        keccak.squeeze(hashHbar, 8*outLen);

        return hashHbar;
    }    


    /**
     * This method is the hash function \bar{H} used by SMQV protocol.
     *
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     * @param n     The order of the group \G.
     * @return Hbar(arg1,arg2,arg3,arg4)
     */
    public static final BigInteger Hbar(byte[] arg1, byte[] arg2, byte[] arg3, byte[] arg4, BigInteger n) {

        keccak.init(0);

        keccak.update(arg1, arg1.length*8);
        keccak.update(arg2, arg2.length*8);
        keccak.update(arg3, arg3.length*8);
        keccak.update(arg4, arg4.length*8);
        
        byte[] hashHbar = new byte[(n.bitLength()+2 + 7)/8];

        keccak.getHash(null);
        keccak.squeeze(hashHbar, n.bitLength()+2);

        return new BigInteger(hashHbar).mod(n);
    }

    /**
     * This method generates a random BigInteger number with the specified number of bits k.
     *
     * @param k         The number of bits for the random number to be generated.
     * @return
     */
    public static BigInteger randomBigInteger(int k) {
        
        return new BigInteger(k, rnd);
    }


    /**
     * This method computes a hexadecimal representation of the byte array input.
     *
     * @param array         The byte array to  be converted to hex representation.
     * @return A string in hex.
     */
    public static String printByteArray(byte[] array) {
       //int i;
       //for(i = 0; i < array.length; i++){
               //"%02x", array[i]);
       //}
       //System.out.println(new String(array));
               
        //*
        String ret = new String() + "[ ";
        for (int i = 0; i < array.length; i++) {
            ret += hex.charAt((array[i] & 0xff) >>> 4) + hex.charAt(array[i] & 15) + " ";
        }
        return ret + " ]";
        //*/
    }

    /**
     * This method converts a byte array to a friendly string to be printed.
     * 
     * @param ba            The byte array to be converted.
     * @return  A string representing the byte array.
     */
    public static String byteArrayToDebugableString(byte[] ba) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ba.length; i++) {
            int byteAsInt = (int) ba[i];
            if (byteAsInt < 0) {
                byteAsInt = 256 + byteAsInt;
            }
            String strByte = Integer.toHexString(byteAsInt);
            if (strByte.length() == 1) {
                strByte = "0" + strByte;
            }
            sb.append(strByte);
        }
        return sb.toString();
    }

    /**
     * This method converts a byte array to a friendly string public key.
     *
     * @param ba            The byte array to be converted.
     * @return  A string representing the friendly byte array.
     */
    public static String byteArrayToPublicKey(byte[] ba) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ba.length; i++) {
            int byteAsInt = (int) ba[i];
            if (byteAsInt < 0) {
                byteAsInt = 256 + byteAsInt;
            }
            String strByte = Integer.toHexString(byteAsInt);
            if (strByte.length() == 1) {
                strByte = "0" + strByte;
            }
            sb.append(", (byte)0x"+strByte);
        }
        sb.deleteCharAt(0);
        return sb.toString();
    }

    /**
     * compares the two given byte arrays for equality
     *
     * @param b1 first byte array
     * @param b2 second byte array
     * @returns true if the arrays have the same contents, false otherwise.
     */
    public static boolean equals(byte[] b1, byte[] b2){
      if (b1 == null && b2 == null){
        return true;
      }
      if (b1 == null || b2 == null){
        return false;
      }
      if (b1.length != b2.length){
        return false;
      }
      for (int i=0; i<b1.length; i++){
        if (b1[i] != b2[i]){
          return false;
        }
      }
      return true;
    }
    
    public static byte[] getDateTime(){
        return getCurrentTime();
    }
    
    /**
     * Converts a long value to a byte array having a total of 
     * <code>range</code> bytes. The bits extracted from the long are
     * placed in the array's highest indexes.
     * 
     * @param val The value to be converted
     * @param range The length of the array to be created
     * @return The corresponding byte array
     */
    public static byte[] longToByteArray(long val, int range){
        byte[] res = new byte[range];
        for (int i = range-1; i >= 0; i--) {
            res[i] = (byte) val;
            val = val >>> 8;        //"erases" the bytes already read
        }
        return res;
    }
    
    /**
     * Converts a byte array to the corresponding long value. 
     * If the array is longer than 8 bytes, only the 8 first 
     * bytes are taken into account
     * 
     * @param val The value to be converted
     * @return The long value corresponding to the byte array
     * 
     * @see longToByteArray(long val, int range)
     */
    public static long byteArrayToLong(byte[] val){
        //Gets the number of bytes to be converted
        int max = 8;
        if(max > val.length){
            max = val.length;
        }
        
        //Inserts each byte into long value
        long res = 0;
        for (int i = 0; i < max; i++) {
            res = (res << 8) | (val[i] & 0xff);
        }
        
        return res;
    }
    
        /**
     * 
     * @return The current time as a 6-byte array
     * @see parseTime(byte[])
     */
    public static byte[] getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        long res = calendar.get(Calendar.YEAR);                 //in years
        res = res*12 + calendar.get(Calendar.MONTH);            //in months
        res = (res*31) + calendar.get(Calendar.DAY_OF_MONTH);   //in days
        res = (res*24) + calendar.get(Calendar.HOUR_OF_DAY);    //in hours
        res = (res*60) + calendar.get(Calendar.MINUTE);         //in minutes
        res = (res*60) + calendar.get(Calendar.SECOND);         //in seconds
        res = (res*1000) + calendar.get(Calendar.MILLISECOND);  //in milliseconds
        
        return longToByteArray(res, 6);
    }
    
    /**
     * Converts a byte array as the current time
     * @param value The value to be parsed
     * @return The parsed value in the format YYYY/MM/DD HH:MM:SS:mm
     * @see getCurrentTime()
     */
    public static String parseTime(byte[] value){
        long timeVal = byteArrayToLong(value);
        
        //Extracting miliseconds
        int milisecond = (int) timeVal % 1000;
        timeVal /= 1000;
        
        //Extracting seconds
        int second = (int) timeVal % 60;
        timeVal /= 60;
        
        //Extracting minutes
        int minute = (int) timeVal % 60;
        timeVal /= 60;
        
        //Extracting hours
        int hour = (int) timeVal % 24;
        timeVal /= 24;
        
        //Extracting days
        int day = (int) timeVal % 31;
        timeVal /= 31;
        
        //Extracting months. Obs.: adds 1 because January = 0 
        int month = (int) (timeVal % 12) + 1;
        timeVal /= 12;
        
        //Extracting years
        int year = (int) timeVal;        
        
        return  year + "/" + month + "/" + day + " " + hour + ":"
                            + minute + ":" + second + ":" +  milisecond;
    }

    public static void fillByteArray(byte[] arg, byte value) {
        for (int i = 0; i < arg.length; i++) 
            arg[i] = value;        
    }

    public static byte[] padBigInteger(byte[] arg, int bits) {

        byte secLength = (byte) ((bits + 7)/8);

        return fixParameter(arg, secLength);
    }

    public static byte[] fixCompressedBNPoint(byte[] arg, int bits) {

        byte secLength = (byte) (((bits + 7)/8) + 1);

        return fixParameter(arg, secLength);
    }

    public static byte[] fixParameter(byte[] arg, byte secLength) {

        if (arg.length < secLength) {

            byte[] fixedArg = new byte[secLength];
            SMQVUtil.fillByteArray(fixedArg, (byte)0);
            System.arraycopy(arg, 0, fixedArg, 1, arg.length);

            System.out.println("argLen: " + arg.length + ", arg :" + SMQVUtil.byteArrayToDebugableString(arg));
            System.out.println("correctLen: " + secLength);
            //System.out.println("fargLen: " + fixedArg.length + "farg:" + BlindSTORMUtil.byteArrayToDebugableString(fixedArg));
            //System.out.println(this.getClass().getName() + ": Biginteger : " + arg.length);

            return fixedArg;
        } else {
            return arg;
        }
    }
    
    public static int getSecurityLevelInt(byte securityLevel) {

        switch (securityLevel) {
            case SMQVConfig.SECURITY_LEVEL_80:
                return 158;
            case SMQVConfig.SECURITY_LEVEL_96:
                return 190;
            case SMQVConfig.SECURITY_LEVEL_112:
                return 222;
            case SMQVConfig.SECURITY_LEVEL_128:
                return 254;
            default:
                // levantar excecao
                return SMQVConfig.SECURITY_LEVEL_80;
        }
    }

}
