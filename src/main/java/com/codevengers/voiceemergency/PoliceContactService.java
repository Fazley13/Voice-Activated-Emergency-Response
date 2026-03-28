package com.codevengers.voiceemergency;

import java.util.*;

public class PoliceContactService {
    private static final Map<String, List<PoliceContact>> policeContacts = new HashMap<>();
    
    static {
        initializePoliceContacts();
    }
    
    private static void initializePoliceContacts() {
        // Dhaka Division
        List<PoliceContact> dhakaContacts = new ArrayList<>();
        dhakaContacts.add(new PoliceContact("Dhaka", "Dhaka Metropolitan Police", "999", "General Emergency", "Main emergency hotline"));
        dhakaContacts.add(new PoliceContact("Dhaka", "Ramna Police Station", "02-9556390", "General", "Central Dhaka area"));
        dhakaContacts.add(new PoliceContact("Dhaka", "Dhanmondi Police Station", "02-9661351", "General", "Dhanmondi area"));
        dhakaContacts.add(new PoliceContact("Dhaka", "Gulshan Police Station", "02-9882248", "General", "Gulshan area"));
        dhakaContacts.add(new PoliceContact("Dhaka", "Uttara Police Station", "02-8958789", "General", "Uttara area"));
        dhakaContacts.add(new PoliceContact("Dhaka", "Wari Police Station", "02-7317234", "General", "Old Dhaka area"));
        dhakaContacts.add(new PoliceContact("Dhaka", "Tejgaon Police Station", "02-8124402", "General", "Tejgaon industrial area"));
        dhakaContacts.add(new PoliceContact("Dhaka", "Mirpur Police Station", "02-9003451", "General", "Mirpur area"));
        
        // Chittagong Division
        List<PoliceContact> chittagongContacts = new ArrayList<>();
        chittagongContacts.add(new PoliceContact("Chittagong", "Chittagong Metropolitan Police", "999", "General Emergency", "Main emergency hotline"));
        chittagongContacts.add(new PoliceContact("Chittagong", "Kotwali Police Station", "031-610244", "General", "Central Chittagong"));
        chittagongContacts.add(new PoliceContact("Chittagong", "Panchlaish Police Station", "031-656844", "General", "Panchlaish area"));
        chittagongContacts.add(new PoliceContact("Chittagong", "Double Mooring Police Station", "031-711122", "General", "Port area"));
        chittagongContacts.add(new PoliceContact("Chittagong", "Chandgaon Police Station", "031-672233", "General", "Chandgaon area"));
        chittagongContacts.add(new PoliceContact("Chittagong", "Halishahar Police Station", "031-741155", "General", "Halishahar area"));
        
        // Sylhet Division
        List<PoliceContact> sylhetContacts = new ArrayList<>();
        sylhetContacts.add(new PoliceContact("Sylhet", "Sylhet Metropolitan Police", "999", "General Emergency", "Main emergency hotline"));
        sylhetContacts.add(new PoliceContact("Sylhet", "Kotwali Police Station", "0821-710400", "General", "Central Sylhet"));
        sylhetContacts.add(new PoliceContact("Sylhet", "South Surma Police Station", "0821-717766", "General", "South Surma area"));
        sylhetContacts.add(new PoliceContact("Sylhet", "Airport Police Station", "0821-711900", "General", "Airport area"));
        
        // Rajshahi Division
        List<PoliceContact> rajshahiContacts = new ArrayList<>();
        rajshahiContacts.add(new PoliceContact("Rajshahi", "Rajshahi Metropolitan Police", "999", "General Emergency", "Main emergency hotline"));
        rajshahiContacts.add(new PoliceContact("Rajshahi", "Boalia Police Station", "0721-772277", "General", "Boalia area"));
        rajshahiContacts.add(new PoliceContact("Rajshahi", "Motihar Police Station", "0721-750399", "General", "Motihar area"));
        rajshahiContacts.add(new PoliceContact("Rajshahi", "Rajpara Police Station", "0721-775566", "General", "Rajpara area"));
        
        // Khulna Division
        List<PoliceContact> khulnaContacts = new ArrayList<>();
        khulnaContacts.add(new PoliceContact("Khulna", "Khulna Metropolitan Police", "999", "General Emergency", "Main emergency hotline"));
        khulnaContacts.add(new PoliceContact("Khulna", "Kotwali Police Station", "041-761823", "General", "Central Khulna"));
        khulnaContacts.add(new PoliceContact("Khulna", "Sonadanga Police Station", "041-769955", "General", "Sonadanga area"));
        khulnaContacts.add(new PoliceContact("Khulna", "Daulatpur Police Station", "041-761144", "General", "Daulatpur area"));
        
        // Barisal Division
        List<PoliceContact> barisalContacts = new ArrayList<>();
        barisalContacts.add(new PoliceContact("Barisal", "Barisal Metropolitan Police", "999", "General Emergency", "Main emergency hotline"));
        barisalContacts.add(new PoliceContact("Barisal", "Kotwali Police Station", "0431-64202", "General", "Central Barisal"));
        barisalContacts.add(new PoliceContact("Barisal", "Airport Police Station", "0431-64455", "General", "Airport area"));
        
        // Rangpur Division
        List<PoliceContact> rangpurContacts = new ArrayList<>();
        rangpurContacts.add(new PoliceContact("Rangpur", "Rangpur Metropolitan Police", "999", "General Emergency", "Main emergency hotline"));
        rangpurContacts.add(new PoliceContact("Rangpur", "Kotwali Police Station", "0521-63235", "General", "Central Rangpur"));
        rangpurContacts.add(new PoliceContact("Rangpur", "Tajhat Police Station", "0521-56677", "General", "Tajhat area"));
        
        // Mymensingh Division
        List<PoliceContact> mymensinghContacts = new ArrayList<>();
        mymensinghContacts.add(new PoliceContact("Mymensingh", "Mymensingh Metropolitan Police", "999", "General Emergency", "Main emergency hotline"));
        mymensinghContacts.add(new PoliceContact("Mymensingh", "Kotwali Police Station", "091-66101", "General", "Central Mymensingh"));
        mymensinghContacts.add(new PoliceContact("Mymensingh", "Sadar Police Station", "091-55234", "General", "Sadar area"));
        
        // Store in map
        policeContacts.put("Dhaka", dhakaContacts);
        policeContacts.put("Chittagong", chittagongContacts);
        policeContacts.put("Sylhet", sylhetContacts);
        policeContacts.put("Rajshahi", rajshahiContacts);
        policeContacts.put("Khulna", khulnaContacts);
        policeContacts.put("Barisal", barisalContacts);
        policeContacts.put("Rangpur", rangpurContacts);
        policeContacts.put("Mymensingh", mymensinghContacts);
    }
    
    public static List<String> getAllDivisions() {
        return new ArrayList<>(policeContacts.keySet());
    }
    
    public static List<PoliceContact> getContactsByDivision(String division) {
        return policeContacts.getOrDefault(division, new ArrayList<>());
    }
    
    public static Map<String, List<PoliceContact>> getAllContacts() {
        return new HashMap<>(policeContacts);
    }
}
