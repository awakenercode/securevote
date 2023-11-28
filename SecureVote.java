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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
/**
 * Κύριο κομμάτι της εφαρμογής προσομοίωσης ασφαλούς ηλεκτρονικής ψηφορφορίας.
 */
public class SecureVote {
    static ArrayList<String> voter = new ArrayList();
    static ArrayList<String> email = new ArrayList();
    static ArrayList<String> pass = new ArrayList();
    static ArrayList<String> salt = new ArrayList();
    
    static String adminPass;
    static String adminSalt;
        
    static ArrayList<String> candidate = new ArrayList();
    static ArrayList<Integer> votes = new ArrayList();
    
    static ArrayList<String> hasVoted = new ArrayList();
    static ArrayList<String> ballotbox = new ArrayList();
    static String electionTitle;
    static int maxVotes = 0;
    
    static PasswordHandler p = new PasswordHandler();
    static KeyHandler k;
    
    static JFrame frame = new JFrame("Ηλεκτρονικό Σύστημα Εκλογών");  //The main window
    static JPanel activePanel = new Vote();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NoSuchAlgorithmException {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640,480);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                //saveExit();
            }
        });
        
        
        // Δημιουργεί τα RSA κλειδιά
        k = new KeyHandler();
        //Διαβάζει το αρχείο των ψηφοφώρων
        readVoterFile();
        //Διαβάζει το ψηφοδέλτιο
        readBallotFile();
        
        //Δημιουργεί κωδικόυς και salt για κάθε ψηφοφόρο και τέλος για το μέλος της εφορευτικής
        for(int i=0; i<voter.size(); i++){
            //String dataToStore
            pass.add(p.generatePassword());
            salt.add(p.generateSalt());
        }
        adminPass = p.generatePassword();
        adminSalt = p.generateSalt();
        // Στέλνει τους κωδικού ... Τους αποθηκεύει με τα ονόματα στο αρχείο,
        // password ώστε να μπορούμε να δοκιμάσουμε την εφαρμογή.
        savePasses();  
        
        // Αποθηκεύει το αρχείο με τα στοιχεία των ψηφοφόρων και τα hashes των κωδικών τους
        saveHashes();
        
        //Δημιουργεί το συμμετρικό κλειδί AES .
        k.generateAndStoreAESKey();
        
        frame.add(activePanel);
        frame.repaint();
        frame.setVisible(true);
                
        
    }
    
    /**
     * Διαβάζει τα στοιχεία από το αρχείο ψηφοφόρων. Το
     * αρχείο έχει γραμμές της μορφής "ονομαψηφοφόρου;e-mail"
     */
    static void readVoterFile(){
        //Read Voters file
        File vFile = new File("./election/voters");
        try{
            Scanner vscan = new Scanner(vFile);
            while (vscan.hasNextLine()){
                String sep="[;]";
                String line = vscan.nextLine();
                String[] data = line.split(sep);
                if (data.length==2){  // Αν κάποια γραμμή του αρχείου δεν εχει σωστά στοιχεία την αγνοεί.
                    voter.add(data[0]);
                    email.add(data[1]);
                  //  System.out.println(data[0]+"  -  "+data[1]);
                }
            }
            vscan.close();
        }catch (FileNotFoundException x){
            
        }
    }
    /**
     * Διαβάζει τα στοιχεία από το αρχείο ψηφοδελτίου. Το
     * αρχείο έχει στην 1η γραμμή τον τίτλο των εκλογώ
     * Στην τελευταία γραμμή το μέγιστο πλήθος σταυρών
     * και στις ενδιάμεσες τα ονόματα των υποψηφίων ένα ανά γραμμή
     */
    static void readBallotFile(){
        //Read Ballot file
        File bFile = new File("./election/ballot");
        try{
            Scanner bscan = new Scanner(bFile);
            electionTitle = bscan.nextLine();
            while (bscan.hasNextLine()){
                String line = bscan.nextLine();
                candidate.add(line);

            }
            maxVotes = Integer.parseInt(candidate.remove(candidate.size()-1));
            bscan.close();
           // System.out.println(electionTitle +"   "+ candidate.get(2) +"  "+ Integer.toString(maxVotes));
        }catch (FileNotFoundException x){
            
        }
    }
    
    /**
     * Αποθηκεύει ονοματα και κωδικούς στο αρχείο passwords ώστε να μπορούμε να
     * διαβάσουμε και να χρησιμοποιήσουμε τους κωδικούς για δοκιμή της εφαρμογής
     */
    static void savePasses(){
        File pFile = new File("./election/passwords");
        try{
            pFile.createNewFile();
            FileWriter write = new FileWriter(pFile);
            String line = "admin: "+adminPass +"\n";
            write.write(line);
             for(int i=0; i<voter.size(); i++){
                line = voter.get(i)+": "+pass.get(i)+"\n";
                write.write(line);
            }
            write.close();
        }catch (IOException ex){
            
        }
    }
    
    /**
     * Αποθηκεύει τα στοιχεία των ψηφοφόρων καθώς και το κρυπτογραφημένο κωδικό
     */
    static void saveHashes(){
        File pFile = new File("./election/hashfile");
        try{
            pFile.createNewFile();
            FileWriter write = new FileWriter(pFile);
            String line = "admin:admin@myvote.org:"+adminSalt +":"+k.encode(adminPass+adminSalt) +"\n";
            write.write(line);
             for(int i=0; i<voter.size(); i++){
                line = voter.get(i)+":"+email.get(i)+":"+salt.get(i)+":"+k.encode(pass.get(i)+salt.get(i))+"\n";
                write.write(line);
            }
            write.close();
        }catch (IOException ex){
            
        }
    }
    
    String getElectionTitle(){
        return electionTitle;
    }
    
    /**
     * Ελέγχει αν είναι σωστός συνδυασμός χρήστη και κωδικού
     * @param username όνομα χρήστη 
     * @param password κωδικός
     * @return  True εάν είνα σωστό, False διαφορετικά.
     */
    static boolean authenticate(String username, String password){
        File pFile = new File("./election/hashfile");
        try{
            Scanner reader = new Scanner(pFile);
            while (reader.hasNextLine()){
                String line = reader.nextLine();
                String sep = "[:]";
                String[] data = line.split(sep);
                if(data[0].equals(username)){
                    String saltypass= password + data[2];
                    //System.out.println(password+" "+data[2]);
                    String passtoconfirg = k.decode(data[3]);
                    //System.out.println(hashedSaltyPass);
                    //System.out.println(data[3]);
                    if (passtoconfirg.equals(saltypass)){
                        reader.close();
                        return true;
                    }else{
                        reader.close();
                        return false;
                    }
                }
            }
            reader.close();
        } catch(Exception e){
            
        }
        
        return false;
    }
    
    /**
     * Επιτρέφει εάν κάποιος ψηφοφόρος έχει δικαίωμα να ψηφίσει.
     * @param voter όνομα ψηφοφόρου
     * @return True έχει δικαίωμα, False έχει ήδη ψηφήσει
     */
    static boolean canVote(String voter){
        if (hasVoted.contains(voter) ){
            return false;
        }
        return true;
    }
    
    /**
     * Παραλαμβάνει μία νέα ψήφο την καταχωρεί και ανακατεύει τα ψηφοδέλτια.
     * @param votername όνομα ψηφοφόρου
     * @param votes λίστα ακεαίων με τις θέσεις των σταυρών στο ψηφοδέλτιο
     */
    static void recieveVote(String votername, int[]votes){
        String vote="";
        for (var i : votes){
            vote+=candidate.get(i)+":";
        }
        //vote = k.encode(vote);
        vote = k.AESencode(vote);
        ballotbox.add(vote);//
        Collections.shuffle(ballotbox);
        hasVoted.add(votername);
        if (hasVoted.size() == voter.size()){
            endElections();
        }
    }
    
    /**
     * Τερματίζει την εκλογική διαδικασία και ξεκινάει την καταμέτρηση
     */
    static void endElections(){
        ((Vote)activePanel).endElections();
        results();
    }
    
    /**
     * Κάνει την καταμέτρηση των ψήφων και ελέγχει ότι τα ψηφοδέλτια είναι 
     * ίδια σε πλήθος με τους ψηφίσαντες. Δείχνει τα αποτελέσματα και τα αποθηκεύει
     * σε αρχείο
     */
    static void results(){
        String sep="[:]";
        // Αρχικοποιηση λίστας αποτελεσμάτων 0 σε όλους τους υποψηφίους
        for (var cand : candidate){
            votes.add(0);
        }
        for (var v: ballotbox){
            //String vt = k.decode(v);
            String vt = k.AESdecode(v);
            String[] data = vt.split(sep);
            for (var cand: data) addAVoteTo(cand);
        }
        if (ballotbox.size() != voter.size()-hasVoted.size()){
            JOptionPane.showMessageDialog(activePanel, "Υπάρχει διαφορά μεταξύ των καταχωρημένων ψηφοδελτίων και των ψηφησάντων!!! ");
        }
        // Save to result File... 
        String result ="";
        for (int i =0 ; i< candidate.size(); i++){
            result += candidate.get(i)  + ":" +  votes.get(i) +"\n";
        }
        JOptionPane.showMessageDialog(activePanel, result);
        File rFile = new File("./election/results.txt");
        try{
            rFile.createNewFile();
            FileWriter write = new FileWriter(rFile);
            write.write(result);
            write.close();
        }catch (IOException ex){   
        }
    }
    /**
     * προθέτει ψήφο σε κάποιο υποψήφιο
     * @param name 
     */
    static void addAVoteTo(String name){
        for (int i=0; i<candidate.size(); i++){
            if (candidate.get(i).equals(name)){
                votes.set(i, votes.get(i)+1);
            }
        }
    }
    
}

    