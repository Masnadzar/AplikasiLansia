package com.alya.aplikasilansia;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.UserRepository;
import com.alya.aplikasilansia.data.inputMedHistory;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class RegisterViewModel extends ViewModel {
    private UserRepository userRepository;
    public MutableLiveData<FirebaseUser> userLiveData;
    public MutableLiveData<String> errorLiveData;
    private MutableLiveData<String> updateResultLiveData;
    public RegisterViewModel() {
        userRepository = new UserRepository();
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        updateResultLiveData = new MutableLiveData<>();
    }

    public void register(String email, String password, String birthDate, String userName, String gender) {
        userRepository.register(email, password, birthDate, userName, gender, null, null, null, userLiveData, errorLiveData);
    }
    public void registerGoogleAcc(GoogleSignInAccount account, String birthDate, String userName, String gender) {
        userRepository.registerWithGoogle(
                account,         // Pass the GoogleSignInAccount object
                birthDate,
                userName,
                gender,
                null,
                userLiveData,
                errorLiveData
        );
    }


    public void registerHealth1(List<inputMedHistory> medHistory){
        userRepository.updateMedHistory(medHistory, updateResultLiveData);
    }

    public void registerHealth2(String caregiver, String maritalStatus) {
        userRepository.updateMedData(caregiver, maritalStatus, updateResultLiveData);
    }

}

