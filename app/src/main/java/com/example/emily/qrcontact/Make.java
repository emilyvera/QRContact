package com.example.emily.qrcontact;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.emily.qrcontact.TinyDB;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.google.android.gms.common.api.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.EmailType;
import ezvcard.property.StructuredName;

public class Make extends AppCompatActivity {
    final String[] VCardTags = {"VERSION", "PRODID", "N:", "EMAIL", "TEL", "END"};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make);
        Picasso.get().setLoggingEnabled(true);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final Button refresh = (Button) findViewById(R.id.button);
        final EditText firstName = (EditText) findViewById(R.id.firstName);
        final EditText lastName = (EditText) findViewById(R.id.lastName);
        final EditText phone = (EditText) findViewById(R.id.phone);
        final EditText email = (EditText) findViewById(R.id.email);
        final EditText profileName = findViewById(R.id.profileName);
        final TinyDB tinyDB = new TinyDB(this);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VCard vcard = makeVCard(firstName, lastName, email, phone);

                String fullContact = Ezvcard.write(vcard).version(VCardVersion.V3_0).go();

                fullContact = getUrlVCard(vcard);

                Log.d("myTag", "Registered Click");
                Picasso.get().load("https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=" + fullContact).into(imageView);


            }
        });
        loadImage(imageView);

        final ListView profileList = findViewById(R.id.profileList);
        final Button saveProfile = findViewById(R.id.saveProfile);
        final Button chooseProfile = findViewById(R.id.chooseProfile);
        ArrayList<ProfileSerialized> passer = new ArrayList<>();
        try {
            passer = getArrayList("Profiles");
        } catch (Exception e) {
            Log.d("Persistence", e.toString());
        }
        final ArrayList<Profile> profileArrayList = SerializeableToProfile(passer);

        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_scan:
                        Intent intent1 = new Intent(Make.this, MainActivity.class);
//                        if (profileArrayList != null) {
//                            intent1.putExtra("Profiles", profileArrayList);
//                        }
                        startActivity(intent1);
                        break;
                    case R.id.navigation_make:
                        break;
                    case R.id.navigation_help:
                        Intent intent3 = new Intent(Make.this, Help.class);
//                        if (profileArrayList != null) {
//
//                            intent3.putExtra("Profiles", profileArrayList);
//                        }
                        startActivity(intent3);
                        break;
                }

                return false;
            }
        });


        final ConstraintLayout profileLayout = findViewById(R.id.profileLayout);


        ArrayAdapter<Profile> arrayAdapter = new ArrayAdapter<>(
                this.getApplicationContext(),
                android.R.layout.simple_list_item_1,
                profileArrayList
        );
        profileList.setAdapter(arrayAdapter);

        chooseProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileArrayList.size() == 0) {
                    Log.d("Choose Profile", "No profiles");
                    return;
                }
                profileLayout.setVisibility(View.GONE);
                bottomNavigationView.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                profileList.setVisibility(View.VISIBLE);

            }
        });

        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Profile temp = profileArrayList.get(position);
                firstName.setText(temp.getVCard().getStructuredName().getGiven());
                lastName.setText(temp.getVCard().getStructuredName().getFamily());
                email.setText(temp.getVCard().getEmails().get(0).getValue());
                phone.setText(temp.getVCard().getTelephoneNumbers().get(0).getText());
                profileName.setText(temp.getProfileName());
                refresh(firstName, lastName, email, phone, imageView);


                profileLayout.setVisibility(View.VISIBLE);
                bottomNavigationView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                profileList.setVisibility(View.GONE);



            }
        });

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isThereACopy = false;
                VCard toAddToProfile = makeVCard(firstName, lastName, email, phone);
                Profile toAddToList = new Profile(toAddToProfile, profileName.getText().toString());

                if (toAddToList == null || toAddToList.getProfileName().equals("")) {
                    Log.d("Save Profile", "No Profile Name");
                    return;
                }

                for (Profile profile : profileArrayList) {
                    if (profile.equals(toAddToList) || toAddToList.getProfileName().equals(profile.getProfileName())) {
                        isThereACopy = true;
                        break;
                    }
                    isThereACopy = false;
                }

                if (isThereACopy) {
                    Log.d("Save Profile", "Profile already exists or Profile Name is Already Taken");
                    return;
                }


                profileArrayList.add(toAddToList);
                ArrayList<ProfileSerialized> thirdHand = ProfileToSerializeable(profileArrayList);

                saveArrayList(thirdHand, "Profiles");

                //Log.d("Persistence", "Saved Data");
            }
        });


    }

    private void saveArrayList(ArrayList<ProfileSerialized> profileArrayList, String key) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(profileArrayList);
        editor.putString(key, json);
        editor.apply();
        editor.commit();
    }

    private ArrayList<ProfileSerialized> getArrayList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<ProfileSerialized>>() {
        }.getType();
        Log.d("Persistence", type.toString());
        return gson.fromJson(json, type);
    }

    private String getUrlVCard(VCard vCard) {
        String fullContact = Ezvcard.write(vCard).version(VCardVersion.V3_0).go();
        String newFullContact = fullContact;

        for (int x = 0; x < VCardTags.length; x++) {
            if (newFullContact.contains(VCardTags[x])) {
                int parseStop = newFullContact.indexOf(VCardTags[x]);
                if (VCardTags[x].equals("N:")) {
                    parseStop = newFullContact.indexOf(VCardTags[x], newFullContact.indexOf(VCardTags[x - 1]));
                }
                try {
                    newFullContact = newFullContact.substring(0, parseStop) + "%0A"
                            + newFullContact.substring(parseStop);
                } catch (Exception e) {
                    Log.d("out of bounds", e.toString() + " " + parseStop);

                }
            }
        }


        Log.d("test", newFullContact);
        return newFullContact;


    }


    private void loadImage(ImageView toSet) {
        Log.d("fucntion", "Default code");

//        Picasso.get().load("https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + Ezvcard.write(Vcard).version(VCardVersion.V3_0).go()").into(toSet);
    }

    private VCard makeVCard(EditText firstName, EditText lastName, EditText email, EditText phone) {
        VCard vcard = new VCard();
        StructuredName fullName = new StructuredName();
        fullName.setFamily(String.valueOf(lastName.getText()));
        fullName.setGiven(String.valueOf(firstName.getText()));
        vcard.setStructuredName(fullName);
        vcard.addEmail(String.valueOf(email.getText()), EmailType.WORK);
        vcard.addTelephoneNumber(String.valueOf(phone.getText()));
        return vcard;
    }

    private VCard makeVCard(String firstName, String lastName, String email, String phone) {
        VCard vcard = new VCard();
        StructuredName fullName = new StructuredName();
        fullName.setFamily(lastName);
        fullName.setGiven(firstName);
        vcard.setStructuredName(fullName);
        vcard.addEmail(email, EmailType.WORK);
        vcard.addTelephoneNumber(phone);
        return vcard;
    }

    public class Profile {

        private VCard vCard;
        private String profileName;

        public VCard getVCard() {
            return vCard;
        }

        public void setVCard(VCard toSet) {
            if (toSet != null) {
                vCard = toSet;
            }
        }

        public String getProfileName() {
            return profileName;
        }

        public void setProfileName(String toSet) {
            if (toSet != null) {
                profileName = toSet;
            }
        }

        public Profile(ProfileSerialized toPass) {
            vCard = makeVCard(toPass.getFirstName(), toPass.getLastName(), toPass.getEmail()
                    , toPass.getPhone());
            profileName = toPass.getProfileName();
        }

        public Profile(VCard toVCard, String toProfileName) {
            vCard = toVCard;

            profileName = toProfileName;
        }


        private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
            aOutputStream.writeUTF(vCard.getStructuredName().getGiven());
            aOutputStream.writeUTF(vCard.getStructuredName().getFamily());
            aOutputStream.writeUTF(vCard.getEmails().get(0).toString());
            aOutputStream.writeUTF(vCard.getTelephoneNumbers().get(0).toString());
            aOutputStream.writeUTF(profileName);
        }

        public boolean equals(Object toCompare) {
            if (!(toCompare instanceof Profile)) {
                return false;
            }

            Profile newCompare = (Profile) toCompare;
            String firstName = nullToEmpty(vCard.getStructuredName().getGiven());
            String toCompareFirstName = nullToEmpty(newCompare.getVCard().getStructuredName().getGiven());
            String lastName = nullToEmpty(vCard.getStructuredName().getFamily());
            String toCompareLastName = nullToEmpty(newCompare.getVCard().getStructuredName().getGiven());
            String email = nullToEmpty(vCard.getEmails().get(0).getValue());
            String toCompareEmail = nullToEmpty(newCompare.getVCard().getEmails().get(0).getValue());
            String phone = nullToEmpty(vCard.getTelephoneNumbers().get(0).getText());
            String toComparePhone = nullToEmpty(newCompare.getVCard().getTelephoneNumbers().get(0).getText());


            return (firstName.equals(toCompareFirstName) && lastName.equals(toCompareLastName) && email.equals(toCompareEmail)
                    && phone.equals(toComparePhone) && profileName.equals(newCompare.getProfileName()));

        }

        private String nullToEmpty(String maybeNull) {
            if (maybeNull == null) {
                return "";
            }

            return maybeNull;
        }


        public String toString() {
            return profileName + " - " + vCard.getStructuredName().getGiven() + " " + vCard.getStructuredName().getFamily();

        }


    }

    public class ProfileSerialized {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String profileName;

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getProfileName() {
            return profileName;
        }

        public ProfileSerialized(Profile toPass) {
            firstName = toPass.getVCard().getStructuredName().getGiven();
            lastName = toPass.getVCard().getStructuredName().getFamily();
            email = toPass.getVCard().getEmails().get(0).getValue();
            phone = toPass.getVCard().getTelephoneNumbers().get(0).getText();
            profileName = toPass.getProfileName();
        }

        public Profile makeProfile() {
            return new Profile(makeVCard(firstName, lastName, email, phone), profileName);
        }
    }

    public Profile ProfileFromSerializable(ProfileSerialized value) {
        return new Profile(makeVCard(value.getFirstName(), value.getLastName(), value.getEmail(), value.getPhone())
                , value.getProfileName());
    }

    public ArrayList<ProfileSerialized> ProfileToSerializeable(ArrayList<Profile> toPass) {
        ArrayList<ProfileSerialized> toReturn = new ArrayList<>();
        for (Profile value : toPass) {
            toReturn.add(new ProfileSerialized(value));
        }
        return toReturn;
    }

    public ArrayList<Profile> SerializeableToProfile(ArrayList<ProfileSerialized> toPass) {
        ArrayList<Profile> regularProfiles = new ArrayList<>();

        for (int x = 0; x < toPass.size(); x++) {

            Log.d("Persistence", regularProfiles.getClass().toString());
            regularProfiles.add(new Profile(toPass.get(x)));

        }

        return regularProfiles;

    }


    public void refresh(EditText firstName, EditText lastName, EditText email, EditText phone, ImageView imageView) {
        VCard vcard = makeVCard(firstName, lastName, email, phone);

        String fullContact = Ezvcard.write(vcard).version(VCardVersion.V3_0).go();

        fullContact = getUrlVCard(vcard);

        Log.d("myTag", "Registered Click");
        Picasso.get().load("https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=" + fullContact).into(imageView);
    }

}
