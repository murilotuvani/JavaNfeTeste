package br.com.autogeral.javanfe.javanfeteste;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Murilo
 */
public class NfceQRCodeTeste {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String chaveAcesso = "41180678393592000146558900000006041028190697";
            String idCsrt = "1";
            String ambiente = "2";
            
            String concat = chaveAcesso + "|1|" + ambiente + "|" + idCsrt + "|";
            
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(concat.getBytes());
            byte[] digest = md.digest();
            String hash = getHexa(digest);
            System.out.println(hash);
            
            concat = "G8063VRTNDMO886SFNK5LDUDEI24XJ22YIPO41180678393592000146558900000006041028190697";
            md.update(concat.getBytes());
            digest = md.digest();
            hash = getHexa(digest);
            System.out.println(hash);
            
            chaveAcesso = "28170800156225000131650110000151341562040824";
            String csc = "SEU-CODIGO-CSC-CONTRIBUINTE-36-CARACTERES";
            String versao = "2";
            ambiente = "1";
            concat = chaveAcesso + "|" + versao + "|" + ambiente + "|" + idCsrt;
            System.out.println("Resultado 01 : " + concat);
            concat += csc;
            System.out.println("Resultado 02 : " + concat);
            //28170800156225000131650110000151341562040824|2|1|1SEU-CODIGO-CSC-CONTRIBUINTE-36-CARACTERES
            md.update(concat.getBytes());
            digest = md.digest();
            hash = getHexa(digest);
            System.out.println(hash);
            
            
            // Resultado esperado na classe EnvioNfceTeste
            chaveAcesso = "35230405437537000137650010000000011000000016";
            csc = "536fd53c-9091-4b0f-8f5b-bddf416b3bea";
            ambiente = "2";
            concat = chaveAcesso + "|" + versao + "|" + ambiente + "|" + idCsrt;
            System.out.println("Resultado 01 : " + concat);
            concat += csc;
            System.out.println("Resultado 02 : " + concat);
            md.update(concat.getBytes());
            digest = md.digest();
            hash = getHexa(digest).toUpperCase();
            System.out.println(hash);
            
            String resultadoFinal = "http://www.sefazexemplo.gov.br/nfce/qrcode?p="
                    + chaveAcesso + "|" + versao + "|" + ambiente + "|" + idCsrt
                    + "|"+ hash;
            System.out.println(resultadoFinal);
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(NfceQRCodeTeste.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    private static String getHexa(byte[] bytes) {  
        StringBuilder s = new StringBuilder();
        for (byte aByte : bytes) {
            int parteAlta = ((aByte >> 4) & 0xf) << 4;
            int parteBaixa = aByte & 0xf;
            if (parteAlta == 0) {
                s.append('0');
            }
            s.append(Integer.toHexString(parteAlta | parteBaixa));
        }  
        return s.toString();  
    } 

}
