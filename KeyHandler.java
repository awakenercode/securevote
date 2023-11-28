/*
Αριθμός Μητρώου:3212017194
Ονοματεπώνυμο: Τριανταφύλλου Χρήστος
*/
package securevote;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Δημιουργεί το δημόσιο, ιδιοτικό RSA και το συμμετρικό AES κλειδια και
 * και τα αποθυκεύει σε αρχεία.
 * Υλοποιεί συναρτήσεις για κωδικοποίηση ή αποκωδικοποίηση με τη χρήση 
 * των κλειδιών. 
 */
public class KeyHandler {
    
    /**
     * Κατασκευαστής της κλάσης
     * Η κλάση δεν διατηρεί στη μνήμη τα κλειδιά που δημιουργεί οπότε δεν 
     * έχει αντίστοιχα ιδιωτικά στοιχεία. Η δημιουργία του ζεύγους κλειδιών RSA 
     * και αποθύκευση τους σε αρχείο γίνεται με την δημιουργία ενός τέτοιου αντικειμένου
     * Όχι όμως και η δημιουργία και αποθύκευση του συμμετρικού κλειδιού
     */
    
    KeyHandler() throws NoSuchAlgorithmException{
        SecretKey AESkey;
        KeyPairGenerator keyGen;
        String publickey;  //Base64 encoding of the key to store the key
        String privatekey;  //Base64 encoding of the key to store the key
        byte[] pub; //the actual public key bytes
        byte[] pri; //the actual private key bytes
        
        keyGen= KeyPairGenerator.getInstance("RSA");  // H KeyPairGenerator.getInstance μπορεί να δημιουργήσει μια εξαίρεση NoSuchAlgorithmException 
        keyGen.initialize(2048);
        KeyPair keys = keyGen.genKeyPair();
        pub = keys.getPublic().getEncoded();
        pri = keys.getPrivate().getEncoded();
        publickey = Base64.getEncoder().encodeToString(pub);
        privatekey = Base64.getEncoder().encodeToString(pri);
        savePublicKey(publickey);
        savePrivateKey(privatekey);
       // System.out.println("Public Key: "+publickey);
       // readPublicKey();
       // System.out.println("Private Key: "+privatekey);
       // readPrivateKey();
    }
    /**
     * Αποθηκεύει το αλφαριθμητικό που θα λάβει στο αρχείου του σημόσιου κλειδιου
     * Το αλφαριθμητικό πρέπει να έχει λάβει μορφή ώστε να μην περιέχει χαρακτήρες
     * οι οποίοι δεν μπορούν να αποθηκευτούν.
     * @param publickey  Συμβολοσειρά που δημιουργείται από το δημόσιο κλειδί. Πρέπει να είναι κωδικοποιημένη χρησιμοποιόντας το Base64.
     */
    void savePublicKey(String publickey){
        try {
            File pkFile = new File("./election/key.public");
            pkFile.createNewFile();
            FileWriter writer = new FileWriter(pkFile);
            writer.write(publickey);
            writer.close();
        } catch (IOException ex) {

        }
        
    }
    /**
     * Αποθηκεύει το αλφαριθμητικό που θα λάβει στο αρχείου του ιδιωτικου κλειδιού
     * Το αλφαριθμητικό πρέπει να έχει λάβει μορφή ώστε να μην περιέχει χαρακτήρες
     * οι οποίοι δεν μπορούν να αποθηκευτούν.
     * @param publickey  Συμβολοσειρά που δημιουργείται από το ιδιωτικό κλειδί. Πρέπει να είναι κωδικοποιημένη χρησιμοποιόντας το Base64.
     */
    void savePrivateKey(String privatekey){
        try {
            File pkFile = new File("./election/key.private");
            pkFile.createNewFile();
            FileWriter writer = new FileWriter(pkFile);
            writer.write(privatekey);
            writer.close();
        } catch (IOException ex) {

        }
    }
    /**
     * Διαβάζει και επιστρέφει συμβολοσειρά του δημόσιου κλειδιού από αρχείο
     * @return τη συμβολοσειρά δημόσιου κλειδιού που αποθηκεύτηκε
     */
    String readPublicKey(){
        File pkFile = new File("./election/key.public");
        try {
            Scanner reader  = new Scanner(pkFile);
            String pubkey = reader.next();
            //System.out.println("Public Key: "+pubkey);
            return pubkey;
        } catch (FileNotFoundException ex) {
            return null;
        }
        
    }
    /**
     * Διαβάζει και επιστρέφει συμβολοσειρά του ιδιωτικού κλειδιού από αρχείο
     * @return τη συμβολοσειρά ιδιωτικού κλειδιού που αποθηκεύτηκε
     */
    String readPrivateKey(){
         File pkFile = new File("./election/key.private");
        try {
            Scanner reader  = new Scanner(pkFile);
            String prikey = reader.next();
            //System.out.println("Private Key: "+prikey);
            return prikey;
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(KeyHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }  
        
    }
    /**
     * Δημιουργεί και αποθυκεύει συμμετρικό κλειδί για το μέλος της εφορευτικής
     */
    void generateAndStoreAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey key = keyGenerator.generateKey();
            
            String AESstr = encode(Base64.getEncoder().encodeToString(key.getEncoded()));
            File afile = new File("./election/key.AES");
            FileWriter writer = new FileWriter(afile);
            writer.write(AESstr);
            writer.close();
        }
        catch(Exception e){
            System.err.println("AES fail");
            System.err.println(e.getMessage());
        }
    }
    /**
     * Διαβάζει και ξαναδημιουργεί το συμμετρικό κλειδί του μέλους από το
     * αρχείο
     * @return το συμμετρικό κλειδί 
     */
    SecretKey readAESKey(){
        try {
            File afile = new File("./election/key.AES");
            Scanner reader = new Scanner(afile);
            String AESstr = reader.next();
            reader.close();
            AESstr = decode(AESstr);
            byte[] AESbytes = Base64.getDecoder().decode(AESstr);

            SecretKey aesKey = new SecretKeySpec( AESbytes, "AES");
            return aesKey;
        }catch(Exception e){
            return null;
        }
    }
    /**
     * Κρυπτογραφεί ένα αλφαριθμητικό χρησιμοποιόντας το δημόσιο κλειδί RSA
     * @param text Το αλφαριθμητικό που θέλουμε να κρυπτογρφίσουμε
     * @return Το κρυπτογραφημένο αλφαριθμηυικό
     */
    String encode(String text){
        try{
            PublicKey pk;
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            String publickey = readPublicKey();
            byte[] byteKey = Base64.getDecoder().decode(publickey);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            pk = kf.generatePublic(X509publicKey);
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            byte[] res = cipher.doFinal(text.getBytes());
            return Base64.getEncoder().encodeToString(res);
        } catch( Exception e){
            return null;
        }
    }
    /**
     * Αποκρυπτογραφεί ένα αλφαριθμητικό χρησιμοποιώντας το ιδιοτικό κλειδί RSA
     * @param text Κρυπτογραφημένο κείμενο
     * @return το αποκρυπτογραφημένο κείμενο
     */
    String decode(String text){
        try{
             
            byte[] btext  = Base64.getDecoder().decode(text);
            String privatekey = readPrivateKey();
            byte[] byteKey = Base64.getDecoder().decode(privatekey);
            PKCS8EncodedKeySpec PKSprivateKey = new PKCS8EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey prk = kf.generatePrivate(PKSprivateKey);
            
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, prk);
            String res =new String( cipher.doFinal(btext));
            return res;
        }catch(Exception e){
            return null;
        }
    }  
    
     /**
     * Κρυπτογραφεί ένα αλφαριθμητικό χρησιμοποιόντας το συμμετρικό κλειδί AES
     * @param text Το αλφαριθμητικό που θέλουμε να κρυπτογρφίσουμε
     * @return Το κρυπτογραφημένο αλφαριθμηυικό
     */
    String AESencode(String text){
        try{
            byte[] crypted = null;
            SecretKey skey = readAESKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            crypted = cipher.doFinal(text.getBytes()); 
            String encr = Base64.getEncoder().encodeToString(crypted);
            return encr;
        }catch(Exception e){
            return null;
        }
    }
     /**
     * Αποκρυπτογραφεί ένα αλφαριθμητικό χρησιμοποιώντας συμμετρικό κλειδί AES
     * @param text Κρυπτογραφημένο κείμενο
     * @return το αποκρυπτογραφημένο κείμενο
     */
    String AESdecode(String text){
        try{
            byte[] bytes= text.getBytes();
            SecretKey skey = readAESKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skey);
            byte[] h =Base64.getDecoder().decode(bytes);
            String de = new String(cipher.doFinal(h));  
            return de; 
        }catch(Exception e){
            return null;
        }
    }    

}
