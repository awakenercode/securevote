/*
Αριθμός Μητρώου:3212017194
Ονοματεπώνυμο: Τριανταφύλλου Χρήστος
*/
package securevote;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;


/**
 * Βοηθητική κλάση για τη δημιουργία των κωδικών πρόσβασης
 */
public class PasswordHandler {
    ArrayList<String> pwds = new  ArrayList();
    PasswordHandler(){
    }
    /**
     * Δημιουργεί password με 12 στοιχεία. Τα στοιχεία μπορεί να είναι 
     * κεφαλαία λατινικά γράμματα και αριθμοί
     * @return τον κωδικό πρόσβασης
     */
    String generatePassword(){
        String validpasschars = "ABCDEFGHIJKLMNOPQRSTUVXYZ0123456789";
        String password="";
        Random rnd = new SecureRandom();
        for (int i=0;i<12;i++){
            password += validpasschars.charAt(rnd.nextInt(validpasschars.length()));
        }
        //System.out.println(password);
        return password;
    }
    
     /**
     * Δημιουργεί τυχαίο salt. 
     * @return τον salt που δημιούργησε
     */
    String generateSalt(){
        String salt="";
        byte[] sltbytes = new byte[6];
        byte[] sltbytes2 = new byte[6];
        Random rnd = new SecureRandom();
        rnd.nextBytes(sltbytes);
        salt = Base64.getEncoder().encodeToString(sltbytes);
        // SALT is not bound to the password character set  
        return salt;
    }
    
   
}
