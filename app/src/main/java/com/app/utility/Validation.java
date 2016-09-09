package com.app.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* Copyright (C) Justpharma - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
public class Validation {

    public static boolean isEmailValid(String email) {
        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }


    public static boolean isNullOrEmpty(String str) {
        return !(str != null && !str.isEmpty() && !str.equalsIgnoreCase("null"));
    }


//	private boolean isValidMobile(String phone) 
//	{
//	    boolean check=false;
//	    if(!Pattern.matches("[a-zA-Z]+", phone))
//	    {
//	        if(phone.length() < 6 || phone.length() > 13)
//	        {
//	            check = false;
//	            //txtPhone.setError("Not Valid Number");
//	        }
//	        else
//	        {
//	            check = true;
//	        }
//	    }
//	    else
//	    {
//	        check=false;
//	    }
//	    return check;
//	}

    public static boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

}
