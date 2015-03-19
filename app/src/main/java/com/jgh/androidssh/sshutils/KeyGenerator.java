package com.jgh.androidssh.sshutils;


import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jon on 15/02/24.
 */
public class KeyGenerator {

    public KeyGenerator(){

    }

    public KeyPair generateKeyPair(){
        KeyPair kp = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator
                    .getInstance("RSA");
            keyGen.initialize(2048);
            kp = keyGen.generateKeyPair();
            return kp;
        }catch(NoSuchAlgorithmException e) {
            Log.e("KeyGenerator", "Exception : " + e.toString());
        }
        return null;
    }
}
