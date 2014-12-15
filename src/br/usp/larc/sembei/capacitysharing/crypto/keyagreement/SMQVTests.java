package br.usp.larc.sembei.capacitysharing.crypto.keyagreement;

/**
 *
 * @author Geovandro C. C. F. Pereira
 */
public class SMQVTests {
	
	public static boolean run() {
		boolean accepted = false;
		byte secLevel = SMQVConfig.SECURITY_LEVEL_128;
        SMQVConfig.setSecurityLevel(secLevel);
        
        SMQVClient Alice = new SMQVClient(secLevel);
        SMQVClient Bob = new SMQVClient(secLevel);
        
        byte[] aliceKey = SMQVUtil.randomBigInteger(SMQVConfig.getSecurityLevelInt()).toByteArray();
        Alice.setPrivateKey(aliceKey);
        Alice.makePublicKey();
        
        byte[] bobKey = SMQVUtil.randomBigInteger(SMQVConfig.getSecurityLevelInt()).toByteArray();
        Bob.setPrivateKey(bobKey);
        Bob.makePublicKey();
        
        /*******************************************
         * Alice initializes a key agreement request
         */
        byte[][] quadrupleABXx = Alice.request(SMQVConfig.getSecurityLevelInt(), Bob.A);
        byte[][] tripleABX = new byte[][]{quadrupleABXx[0],quadrupleABXx[1],quadrupleABXx[2]};

        /********************************************
         * Bob proccess the key agreement request
         */
        byte[][] quadrupleBAYK = Bob.processRequest(SMQVConfig.getSecurityLevelInt(), tripleABX, 128);
        byte[][] tripleBAY = new byte[][]{quadrupleBAYK[0],quadrupleBAYK[1],quadrupleBAYK[2]};

        
        /********************************************
         * Alice proccess Bob's response
         */        
        byte[] K = Alice.processResponse(quadrupleABXx[3], quadrupleABXx[2], tripleBAY, 128);
        
        /********************************************
         * Check if computed keys K are the same
         */        
        if (SMQVUtil.equals(quadrupleBAYK[3], K)) {
        	accepted = true;
            System.out.println("SMQV is consistent!");
        } else {
            System.out.println("SMQV is not consistent!");
        }
        return accepted;
	}
    
//    public static void main(String[] args) {
//    	run();
//    }
    
}
