package br.usp.larc.sembei.capacitysharing.crypto.keyagreement;

/**
 * Configuration of the BlindSTORM for the SMS framework
 * @author Geovandro Pereira
 */
public class SMQVConfig {

    public static final byte FIELD_DATETIME = 0;
    public static final byte FIELD_TYPE = 1;
    public static final byte FIELD_ID = 2;
    public static final byte FIELD_AMOUNT = 3;
    public static final byte[] FIELD_LENGTH = {6, 1, 6, 1};

    public static final byte SECURITY_LEVEL_80 = 0;
    public static final byte SECURITY_LEVEL_96 = 1;
    public static final byte SECURITY_LEVEL_112 = 2;
    public static final byte SECURITY_LEVEL_128 = 3;
    public static final byte[] SECURITY_FIELD_LENGTH = {20, 24, 28, 32};

    private static byte securityLevel;

    public SMQVConfig() {
    }


    public static byte getValueLength(byte field){
	return FIELD_LENGTH[field];
    }

    public static int getSecurityLevelInt() {

        switch (securityLevel) {
            case SECURITY_LEVEL_80:
                return 158;
            case SECURITY_LEVEL_96:
                return 190;
            case SECURITY_LEVEL_112:
                return 222;
            case SECURITY_LEVEL_128:
                return 254;
            default:
                return SECURITY_LEVEL_80;
        }
    }

    public static byte getSecurityLevel() {
        return SMQVConfig.securityLevel;
    }

    public static void setSecurityLevel(byte securityLevel) {
	SMQVConfig.securityLevel = securityLevel;
    }

    public static byte getSecurityFieldLength(byte securityLevel){
	return SECURITY_FIELD_LENGTH[securityLevel];
    }
}
