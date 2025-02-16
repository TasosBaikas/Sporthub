# Sporthub

Not publicly released Android App. Solves the problem of not having teammates to play a sport.

## Εγκατάσταση (Δεν κυκλοφορεί δημόσια)

Εγκαταστήστε το **Sporthub.apk** από [εδώ](https://drive.google.com/drive/folders/1YKdiTVbeTkGKAJNNfVmK4En4JofCmeB4?usp=drive_link).

## Τεχνολογίες

**Client:** Android, Android Studio, Java, MVVM Architecture  
**Server:** Firebase (Firestore, Cloud Functions, Authentication, Storage)


## Screenshots

### 1. Εγγραφή στην Εφαρμογή

Μπείτε στην εφαρμογή και κάντε εγγραφή. Η εγγραφή γίνεται με δύο τρόπους: είτε με τον κλασικό τρόπο (email ή τηλέφωνο και κωδικός), είτε μέσω OAuth2 Google Authentication. Θα χρησιμοποιήσουμε τον πρώτο τρόπο.

<img src="https://github.com/user-attachments/assets/7097b255-f804-4b28-a915-984d8f08e800" alt="1" width="260">
<img src="https://github.com/user-attachments/assets/f787a965-9b0e-4df8-af21-96035463597b" alt="2" width="260"><br><br>

**Πρώτο βήμα:**<br>
Μόλις ο χρήστης εισάγει τα στοιχεία που θα χρησιμοποιεί για είσοδο, πρέπει να δηλώσει κάποια απαραίτητα δεδομένα για τη λειτουργία της εφαρμογής. Επιπλέον, πραγματοποιείται επαλήθευση του κινητού τηλεφώνου (App not released: Γίνεται απευθείας ταυτοποίηση), ώστε να αποφεύγονται διπλότυποι λογαριασμοί.

<img src="https://github.com/user-attachments/assets/9936470b-4142-4902-8c44-ca79171735d0" alt="4" width="260">
<img src="https://github.com/user-attachments/assets/e1864b98-2452-4528-8b61-9b92226ca0e2" alt="5" width="260">
<img src="https://github.com/user-attachments/assets/742c1675-b4d6-427b-951a-186d890303b0" alt="6" width="260"><br><br>

**Δεύτερο βήμα:**<br>
Ο χρήστης δηλώνει την τοποθεσία του, η οποία ανιχνεύεται αυτόματα από τη συσκευή, εφόσον παραχωρηθεί η κατάλληλη άδεια.

<img src="https://github.com/user-attachments/assets/ec17075a-ae16-4cc9-a0c7-ec511084c86d" alt="7" width="260">
<img src="https://github.com/user-attachments/assets/b4399b02-97fd-4d3e-85fe-0d30b16bbab4" alt="8" width="260"><br><br>

**Τρίτο βήμα:**<br>
Ο χρήστης διαλέγει τα αθλήματα που τον ενδιαφέρουν και το επίπεδο που τον αντιπροσωπεύει.  
 

<img src="https://github.com/user-attachments/assets/08bd7c2b-642f-4099-9927-de9b1e02157b" alt="9" width="260">

### 2. Κεντρική Σελίδα

**2.1 Κεντρική:**<br>
Στην κεντρική σελίδα πραγματοποιείται η αναζήτηση των ομάδων που αναζητούν παίκτη για συμμετοχή. Ο χρήστης μπορεί να φιλτράρει ανά άθλημα και ανά ημέρα (έως 15 ημέρες μπροστά), βασιζόμενος στη δηλωμένη του τοποθεσία.  


<img src="https://github.com/user-attachments/assets/067297ba-d6a2-43e6-aba3-c526b68f62e5" alt="10" width="260"><br><br>

**2.2 Αλλαγή Ακτίνας Αναζήτησης:**<br>
Εάν ο χρήστης επιθυμεί να ψάξει σε μικρότερη ή μεγαλύτερη ακτίνα για να βρει συμπαίκτες, μπορεί να πατήσει στο εικονίδιο του Google Maps. Από εκεί, μπορεί εύκολα να ρυθμίσει την ακτίνα αναζήτησης.  

<img src="https://github.com/user-attachments/assets/442fdb4b-dc1f-40c4-ade4-ce5d2637f7da" alt="11" width="260">
<img src="https://github.com/user-attachments/assets/8e79771d-fbdd-4a5f-8d3b-378b66d0ba80" alt="12" width="260"><br><br>

**2.3 Προφίλ Χρήστη:**<br>
Στην κεντρική σελίδα, πατώντας το εικονίδιο του χρήστη (πάνω δεξιά ή με swipe left), εμφανίζεται μια σύντομη επισκόπηση του προφίλ του. Πατώντας το κουμπί «Το προφίλ μου», ο χρήστης μπορεί να δει ολόκληρο το προφίλ του.  
  
<img src="https://github.com/user-attachments/assets/ee4d310e-74b4-434d-a4f7-b72978db41e3" alt="13" width="260">
<img src="https://github.com/user-attachments/assets/32ca0370-e112-4159-b2af-150eb321315e" alt="14" width="260"><br><br>

**2.4 Δημιουργία Ομάδας:**<br>
Πατώντας το εικονίδιο της ομάδας, ο χρήστης εισέρχεται στη «Διαχείριση Ομαδών». Από εκεί, πατώντας το πράσινο σταυρό, μπορεί να δημιουργήσει μια νέα ομάδα.  
  
<img src="https://github.com/user-attachments/assets/e4b190c0-3bfc-4829-9142-4f64db7baffc" alt="15" width="260">
<img src="https://github.com/user-attachments/assets/356e6028-9dec-450f-abf5-e87db3add746" alt="16" width="260"><br><br>

**Ακολουθούν 3 βήματα για τη δημιουργία ομάδας:**<br>
  1. Επιλέξτε το άθλημα  
  2. Επιλογή ημέρας  
  3. Επιλογή ώρας – Επιπλέον στοιχεία (επίπεδο, διάρκεια αγώνα, ύπαρξη γηπέδου, μήνυμα προς τους υποψήφιους)  


<img src="https://github.com/user-attachments/assets/2dd836e4-c2eb-4dbc-8cd7-19d9e99423f1" alt="17" width="260">
<img src="https://github.com/user-attachments/assets/081d6071-284d-4d18-a7e3-1f533f28f452" alt="19" width="260">
<img src="https://github.com/user-attachments/assets/4db78c14-5b04-4303-b61f-d6c75357aff5" alt="20" width="260"><br><br>

**2.5 Κεντρική Σελίδα με Ομάδα:**<br>
Μετά τη δημιουργία της πρώτης ομάδας, αυτή εμφανίζεται στην αναζήτηση τόσο για εμάς όσο και για όσους αναζητούν ομάδα με τα συγκεκριμένα κριτήρια (π.χ., επίπεδο).  


<img src="https://github.com/user-attachments/assets/9050f695-d81f-4245-b42f-8e6b47dd01f8" alt="21" width="260"><br><br>

Αν ο χρήστης πατήσει στο εικονίδιο, θα δει το προφίλ του (όπως στο 2.3). Επίσης, επιλέγοντας την ομάδα, εμφανίζονται συνοπτικές πληροφορίες της ομάδας.  


<img src="https://github.com/user-attachments/assets/42ad2634-1d1c-4417-aaf8-ddaa0a6db46c" alt="22" width="260">
<img src="https://github.com/user-attachments/assets/a4defb0e-17c4-44d2-8149-e2670eb237a1" alt="23" width="260">

### 3. Επικοινωνία με Μέλη Ομάδας

Η εφαρμογή επιτρέπει την επικοινωνία μεταξύ των μελών της ομάδας. Ο χρήστης πρέπει να πατήσει την καρτέλα «Συνομιλίες» στο κάτω δεξί μέρος της κεντρικής σελίδας, ώστε να εισέλθει στη συνομιλία της ομάδας.

<img src="https://github.com/user-attachments/assets/7d3ff996-4edb-4f76-9d6e-4eb2235af9fc" alt="24_1_ψηατ" width="260">
<img src="https://github.com/user-attachments/assets/07598dc3-3ad4-4a8b-b0e8-f7fa6b8e9dab" alt="24_2" width="260">
<img src="https://github.com/user-attachments/assets/475ef247-bf41-4a66-90be-2d0707c6c2eb" alt="25" width="260">

### 4. Αλληλεπίδραση με Άλλο Άτομο

**4.1 Άλλος:**  
Δημιουργούμε έναν νέο λογαριασμό και ορίζουμε ως τοποθεσία ένα σημείο στην Καλλιθέα.  


<img src="https://github.com/user-attachments/assets/9e7e5ef9-1a1e-4847-a46b-eff1184b0cdd" alt="26" width="260">
<img src="https://github.com/user-attachments/assets/c8670045-bcb7-4df0-877c-3a4abdfe9d64" alt="27" width="260">
<img src="https://github.com/user-attachments/assets/441f6087-6f07-4108-9545-dd3f2968047a" alt="28" width="260"><br><br>

Ο νέος χρήστης μπορεί να δει την ομάδα για τένις που έχουμε δημιουργήσει και να υποβάλει αίτηση συμμετοχής.<br>
Ο χρήστης που υπέβαλε αίτηση περιμένει την απάντηση από τον διαχειριστή. Ο διαχειριστής λαμβάνει αυτόματα ένα push notification στο κινητό του.

**4.2 Αποδοχή Αίτησης:**  
Από την πλευρά του διαχειριστή, μετά την παραλαβή της ειδοποίησης (στο κάτω δεξί μέρος), μεταβαίνει στη «Διαχείριση Ομαδών». Εκεί, επιλέγοντας την επιλογή έγκρισης αιτήσεων, εγκρίνει την αίτηση του ενδιαφερόμενου.  

<img src="https://github.com/user-attachments/assets/93aad043-d40d-484a-86e2-aae5747df4e5" alt="29" width="260">
<img src="https://github.com/user-attachments/assets/cc78663c-b397-4ad3-b829-a9da65af5dfd" alt="30" width="260">
<img src="https://github.com/user-attachments/assets/f7278f80-1a52-4a58-86f9-6ad25e8fa04f" alt="31" width="260"><br><br>

**4.3 Επικοινωνία μεταξύ Μελών:**  
Μόλις τα δύο άτομα ανήκουν στην ίδια ομάδα, μπορούν να επικοινωνήσουν μέσω του live-sync chat για να κανονίσουν λεπτομέρειες της συνάντησης.

<img src="https://github.com/user-attachments/assets/5123f20b-0b3d-4540-90e6-aca760bcb714" alt="32" width="260">
<img src="https://github.com/user-attachments/assets/aefce0ea-d2e1-4455-bea6-a091bab77fc1" alt="33" width="260">


