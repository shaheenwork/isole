package com.ezenit.isoleborromee.fragments;

import android.util.Log;

import junit.framework.TestCase;

import java.util.Scanner;



/**
 * Created by bibin.b on 5/17/2017.
 */
public class FragmentAudioDetailTest extends TestCase {

    private static final String PATTERN = "\\w+";

    public void testScannerTest(){
        String codeNo="001p";
        Scanner scanner = new Scanner(codeNo);
        scanner.useDelimiter(PATTERN);
        if (scanner.hasNext()) {
            String room = scanner.next();
            System.out.println(room);
        }
        scanner.close();
    }

}