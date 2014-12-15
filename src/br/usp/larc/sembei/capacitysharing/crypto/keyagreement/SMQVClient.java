/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.usp.larc.sembei.capacitysharing.crypto.keyagreement;

import br.usp.larc.bnpairings.BNCurve;
import br.usp.larc.bnpairings.BNPoint;
import br.usp.larc.pseudojava.BigInteger;

/**
 *
 * @author Geovandro C. C. F. Pereira
 */
public class SMQVClient {

    protected byte k;  //seclevel  

    protected BNCurve E;
    protected BigInteger N;    
    protected BNPoint G;

    private byte[] privKey; // Entity's long term private key
    protected byte[] A; // Entity's long term public key
        
 
    private void initParams() {

        SMQVParameters bssParams = SMQVParameters.getInstance(k);

        E = bssParams.E;
        N = bssParams.N;
        G = bssParams.G;
        //G.loadCompiledTable(bssParams.GTable);

        privKey = new byte[(E.getOrder().bitLength()+7)/8]; // |a| = |#E| = |#G|
    }
    
    
    /**
     * This method sets entity's private key a
     * Must be followed by a call to makePublicKey method.
     *
     * @param secretKey     A byte representation of the entity's private key
     */
    public void setPrivateKey(byte[] secretKey) {

        this.privKey = new BigInteger(secretKey).mod(N).toByteArray();
    }
    
    /**
     * This method computes the entity's public key A from the private key a.
     */
    public void makePublicKey() {
        if (privKey == null) throw new RuntimeException ("SMQV: Private key not set!");

        this.A = G.multiply(new BigInteger(privKey)).toByteArray(BNPoint.COMPRESSED);
    }
    
    public byte[] getPublicKey() {
    	return A;
    }
    
    public SMQVClient(byte secLevel){
        this.k = secLevel;
        initParams();
    }
    
    /**
     * The initiator A computes X = xG and sends (A,B,X) to the peer B
     * @param fieldBits 2*(security level) in bits
     * @param B  Is the peer's public key 
     * @return triple (A,B,X)
     */   
    byte[][] request(int fieldBits, byte[] B) {
        BigInteger x = SMQVUtil.randomBigInteger(fieldBits).mod(N);
        BNPoint X = G.multiply(x);
        return new byte[][]{A,B,X.toByteArray(BNPoint.COMPRESSED),x.toByteArray()};
    }
    
    /**
     * @return quadruple (B,A,Y,K)
     */
    byte[][] processRequest(int fieldBits, byte[][] tripleABX, int keySize) {
        
        BNPoint X = new BNPoint(E, tripleABX[2]);
        if (!E.contains(X)) throw new RuntimeException ("SMQV: invalid X point!");
        
        BigInteger y = SMQVUtil.randomBigInteger(fieldBits).mod(N);
        BNPoint Y = G.multiply(y);
        byte[] Ybytes = Y.toByteArray(BNPoint.COMPRESSED);
        
        //send (B,A,Y) back to A
        
        BigInteger d = SMQVUtil.Hbar(tripleABX[2], Ybytes, tripleABX[0], tripleABX[1], N); //d = H(X,Y,A,B)
        BigInteger e = SMQVUtil.Hbar(Ybytes, tripleABX[2], tripleABX[0], tripleABX[1], N); //e = H(Y,X,A,B)
        
        BigInteger sB = y.multiply(e).add(new BigInteger(this.privKey)).mod(N);
        BNPoint sigma = X.multiply(d).add(new BNPoint(E, tripleABX[0])).multiply(sB);
        
        byte[] K = SMQVUtil.H(sigma.toByteArray(BNPoint.COMPRESSED), tripleABX[0], tripleABX[1], tripleABX[2], Ybytes, keySize);
        
        return new byte[][]{tripleABX[1],tripleABX[0],Ybytes,K};
    }
    
    byte[] processResponse(byte[] x, byte[] Xbytes, byte[][] tripleBAY, int keySize) {
        
        BNPoint Y = new BNPoint(E, tripleBAY[2]);
        if (!E.contains(Y)) throw new RuntimeException ("SMQV: invalid Y point!");
        
        BigInteger d = SMQVUtil.Hbar(Xbytes, tripleBAY[2], tripleBAY[1], tripleBAY[0], N); //d = H(X,Y,A,B)
        BigInteger e = SMQVUtil.Hbar(tripleBAY[2], Xbytes, tripleBAY[1], tripleBAY[0], N); //e = H(Y,X,A,B)
        
        BigInteger sA = new BigInteger(x).multiply(d).add(new BigInteger(this.privKey)).mod(N);
        BNPoint sigma = Y.multiply(e).add(new BNPoint(E, tripleBAY[0])).multiply(sA);
        
        byte[] K = SMQVUtil.H(sigma.toByteArray(BNPoint.COMPRESSED), tripleBAY[1], tripleBAY[0], Xbytes, tripleBAY[2], keySize);
        
        return K;
    }
}
