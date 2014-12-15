package br.usp.larc.sembei.capacitysharing.crypto.keyagreement;

import br.usp.larc.bnpairings.*;
import br.usp.larc.bnpairings.data.BN158;
import br.usp.larc.bnpairings.data.BN190;
import br.usp.larc.bnpairings.data.BN222;
import br.usp.larc.bnpairings.data.BN254;
import java.util.Hashtable;
import br.usp.larc.pseudojava.BigInteger;


/**
 * This class works as a repo of already defined parameters.
 *
 * @author  Geovandro Carlos C. F. Pereira
 */
public class SMQVParameters {

    private static Hashtable allInstances = new Hashtable();
    //params = (k; n; \G, G; h_0; h_1; h_2; g; \k(hash); Y).
    
    /** Os seguintes parâmetros são visíveis por todas as classes, inclusive de aplicação **/

    /**
     * Security parameter. Indicates the number of bits of the private key.
     */
    public final int K;

    /**
     *  The order of the subjacent group from the security parameter K.
     *  Elements must be reduced modulus N when they became greater than N or less then zero.
     */
    public final BigInteger N;
    
    /** Vale a pena manter os seguintes atributos aqui, evita ficar calculando toda
        vez que instancia um BLINDSTORMClient ou BLINDSTORMBOOTH **/

    /**
     * The generator of the subjacent group from the security parameter K. Pre-computation can be made.
     */
    public final BNPoint G;
    /**
     *  Elliptic curve containing the elements (BNPoints) of the subjacent group.
     */
    public final BNCurve E;
    /**
     *
     */
    protected final String[] GTable;

    /**
     * Generates the public params = (k; n; \G, G; h_0; h_1; h_2; g; \k(hash); Y).
     * from a given K.
     * 
     * @param K The security parameter in bits.
     */
    private SMQVParameters(int k) {

        this.K = k;

        E = new BNCurve(new BNParams(k));

        N = E.getOrder();

        G = E.getCurveGenerator();

        //Aqui precisamos definir os bytes PPub... Não podemos calculá-lo como P.multiply(s)
        //pois não temos o s nesse escopo...
        //Os valores foram pré-calculados no Booth e fornecidos para utilização.

        // Na forma comprimida

        //byte[] gBytes;

        switch (k) {
        case 158:
            GTable = BN158.Gtab;
            break;
        case 190:
            GTable = BN190.Gtab;
            break;
        case 222:
            GTable = BN222.Gtab;
            break;
        case 254:
            GTable = BN254.Gtab;
            break;
        default: 
            throw new IllegalArgumentException("The given K = " + k + " is not supported by the protocol.");
        }

        G.loadCompiledTable(GTable);
    }

    public static int getUnderlyingSecurityLength() {
        return SMQVConfig.SECURITY_FIELD_LENGTH[SMQVConfig.getSecurityLevel()];
    }

    /**
     * Gets a instance containing given parameters to a specified k.
     * 
     * @param k             The security parameter.
     * @return A BlindSTORMParameters instance with the given parameters to a given k.
     */
    public static SMQVParameters getInstance(byte k) {
        
        if (allInstances.containsKey(new Byte(k))) {
            return (SMQVParameters) allInstances.get(new Byte(k));

        } else {
            //Tenta gerar os parâmetros
            SMQVParameters newInstance = new SMQVParameters(SMQVUtil.getSecurityLevelInt(k));
            //Sucesso! Coloca no Hashtable para futuras referências
            allInstances.put(new Byte(k), newInstance);
            return newInstance;
        }

    }

}


