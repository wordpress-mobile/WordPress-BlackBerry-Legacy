/*-
 * Copyright (c) 2009, Derek Konigsberg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.wordpress.utils;

import net.rim.device.api.collection.util.LongHashtableCollection;


/**
 * Unicode normalizer translating strings with characters in precomposed
 * forms into decomposed characters.  This operation is necessary for
 * correct rendering on the BlackBerry of characters for which device
 * fonts lack glyphs for the precomposed form.
 *
 * <p>
 * Normalization tables have been compiled and incorporated for the
 * following languages:
 * <ul>
 * <li><b>Vietnamese</b> - Provided by: Louis Tang &lt;louistang.rmit@gmail.com&gt;
 * </ul>
 * </p>
 */
public class UnicodeNormalizer {
    private static UnicodeNormalizer instance;
    
    private LongHashtableCollection normalizerTable;
    
    /**
     * Gets the single instance of DecomposeUnicode.
     * 
     * @return single instance of DecomposeUnicode
     */
    public static synchronized UnicodeNormalizer getInstance() {
        if(instance == null) {
            instance = new UnicodeNormalizer();
        }
        return instance;
    }
    
    /**
     * Instantiates a new Unicode normalizer instance.
     */
    private UnicodeNormalizer() {    
        normalizerTable = new  LongHashtableCollection();
        
        normalizerTable.put((long)'\u1ea3', "\u0061\u0309"); // a?
        normalizerTable.put((long)'\u1ea1', "\u0061\u0323"); // a.

        normalizerTable.put((long)'\u1eaf', "\u0103\u0301"); // a('
        normalizerTable.put((long)'\u1eb1', "\u0103\u0300"); // a(`
        normalizerTable.put((long)'\u1eb3', "\u0103\u0309"); // a(?
        normalizerTable.put((long)'\u1eb5', "\u0103\u0303"); // a(~
        normalizerTable.put((long)'\u1eb7', "\u0103\u0323"); // a(. 

        normalizerTable.put((long)'\u1ea5', "\u00e2\u0301"); // a^'
        normalizerTable.put((long)'\u1ea7', "\u00e2\u0300"); // a^`
        normalizerTable.put((long)'\u1ea9', "\u00e2\u0309"); // a^?
        normalizerTable.put((long)'\u1eab', "\u00e2\u0303"); // a^~
        normalizerTable.put((long)'\u1ead', "\u00e2\u0323"); // a^.
 
        normalizerTable.put((long)'\u1ebb', "\u0065\u0309"); // e?
        normalizerTable.put((long)'\u1ebd', "\u0065\u0303"); // e~
        normalizerTable.put((long)'\u1eb9', "\u0065\u0323"); // e. 

        normalizerTable.put((long)'\u1ebf', "\u00ea\u0301"); // e^'
        normalizerTable.put((long)'\u1ec1', "\u00ea\u0300"); // e^`
        normalizerTable.put((long)'\u1ec3', "\u00ea\u0309"); // e^?
        normalizerTable.put((long)'\u1ec5', "\u00ea\u0303"); // e^~ 
        normalizerTable.put((long)'\u1ec7', "\u00ea\u0323"); // e^.
        
        normalizerTable.put((long)'\u1ec9', "\u0069\u0309"); // i? 
        normalizerTable.put((long)'\u1ecb', "\u0069\u0323"); // i. 
        
        normalizerTable.put((long)'\u1ecf', "\u006f\u0309"); // o? 
        normalizerTable.put((long)'\u1ecd', "\u006f\u0323"); // o.
        
        normalizerTable.put((long)'\u1ed1', "\u00f4\u0301"); // o^'
        normalizerTable.put((long)'\u1ed3', "\u00f4\u0300"); // o^`
        normalizerTable.put((long)'\u1ed5', "\u00f4\u0309"); // o^?  
        normalizerTable.put((long)'\u1ed7', "\u00f4\u0303"); // o^~
        normalizerTable.put((long)'\u1ed9', "\u00f4\u0323"); // o^.
        
        normalizerTable.put((long)'\u1edb', "\u01a1\u0301"); // o+' 
        normalizerTable.put((long)'\u1edd', "\u01a1\u0300"); // o+` 
        normalizerTable.put((long)'\u1edf', "\u01a1\u0309"); // o+? 
        normalizerTable.put((long)'\u1ee1', "\u01a1\u0303"); // o+~ 
        normalizerTable.put((long)'\u1ee3', "\u01a1\u0323"); // o+.
        
        normalizerTable.put((long)'\u1ee7', "\u0075\u0309"); // u?  
        normalizerTable.put((long)'\u1ee5', "\u0075\u0323"); // u.
        
        normalizerTable.put((long)'\u1ee9', "\u01b0\u0301"); // u+' 
        normalizerTable.put((long)'\u1eeb', "\u01b0\u0300"); // u+` 
        normalizerTable.put((long)'\u1eed', "\u01b0\u0309"); // u+? 
        normalizerTable.put((long)'\u1eef', "\u01b0\u0303"); // u+~ 
        normalizerTable.put((long)'\u1ef1', "\u01b0\u0323"); // u+.
        
        normalizerTable.put((long)'\u1ef7', "\u0079\u0309"); // y? 
        normalizerTable.put((long)'\u1ef9', "\u0079\u0303"); // y~ 
        normalizerTable.put((long)'\u1ef5', "\u0079\u0323"); // y.
        
        // Capital
        
        normalizerTable.put((long)'\u1ea2', "\u0041\u0309");
        normalizerTable.put((long)'\u1ea0', "\u0041\u0323");
        
        normalizerTable.put((long)'\u1eae', "\u0102\u0301");
        normalizerTable.put((long)'\u1eb0', "\u0102\u0300");
        normalizerTable.put((long)'\u1eb2', "\u0102\u0309");
        normalizerTable.put((long)'\u1eb4', "\u0102\u0303");
        normalizerTable.put((long)'\u1eb6', "\u0102\u0323");
        
        normalizerTable.put((long)'\u1ea4', "\u00c2\u0301");
        normalizerTable.put((long)'\u1ea6', "\u00c2\u0300");
        normalizerTable.put((long)'\u1ea8', "\u00c2\u0309");
        normalizerTable.put((long)'\u1eaa', "\u00c2\u0303");
        normalizerTable.put((long)'\u1eac', "\u00c2\u0323");
        
        normalizerTable.put((long)'\u1eba', "\u0045\u0309");
        normalizerTable.put((long)'\u1ebc', "\u0045\u0303");
        normalizerTable.put((long)'\u1eb8', "\u0045\u0323");
        
        normalizerTable.put((long)'\u1ebe', "\u00ca\u0301");
        normalizerTable.put((long)'\u1ec0', "\u00ca\u0300");
        normalizerTable.put((long)'\u1ec2', "\u00ca\u0309");
        normalizerTable.put((long)'\u1ec4', "\u00ca\u0303");
        normalizerTable.put((long)'\u1ec6', "\u00ca\u0323");
        
        normalizerTable.put((long)'\u1ec8', "\u0049\u0309");
        normalizerTable.put((long)'\u1eca', "\u0049\u0323");
        
        normalizerTable.put((long)'\u1ece', "\u004f\u0309");
        normalizerTable.put((long)'\u1ecc', "\u004f\u0323");
        
        normalizerTable.put((long)'\u1ed0', "\u00d4\u0301");
        normalizerTable.put((long)'\u1ed2', "\u00d4\u0300");
        normalizerTable.put((long)'\u1ed4', "\u00d4\u0309");
        normalizerTable.put((long)'\u1ed6', "\u00d4\u0303");
        normalizerTable.put((long)'\u1ed8', "\u00d4\u0323");
        
        normalizerTable.put((long)'\u1eda', "\u01a0\u0301");
        normalizerTable.put((long)'\u1edc', "\u01a0\u0300");
        normalizerTable.put((long)'\u1ede', "\u01a0\u0309");
        normalizerTable.put((long)'\u1ee0', "\u01a0\u0303");
        normalizerTable.put((long)'\u1ee2', "\u01a0\u0323");
        
        normalizerTable.put((long)'\u1ee6', "\u0055\u0309");
        normalizerTable.put((long)'\u1ee4', "\u0055\u0323");
        
        normalizerTable.put((long)'\u1ee8', "\u01af\u0301");
        normalizerTable.put((long)'\u1eea', "\u01af\u0300");
        normalizerTable.put((long)'\u1eec', "\u01af\u0309");
        normalizerTable.put((long)'\u1eee', "\u01af\u0303");
        normalizerTable.put((long)'\u1ef0', "\u01af\u0323");
        
        normalizerTable.put((long)'\u1ef6', "\u0059\u0309");
        normalizerTable.put((long)'\u1ef8', "\u0059\u0303");
        normalizerTable.put((long)'\u1ef4', "\u0059\u0323");
        
    }
    
    /**
     * Quickly scan the provided Unicode string to determine whether
     * it has any characters that would be translated by a call
     * to {@link UnicodeNormalizer#normalize(String)}.
     * 
     * @param input The unicode string.
     * @return True if the string has normalizable characters.
     */
    public boolean hasNormalizableCharacters(String input) {
        int size = input.length();
        for(int i = 0; i < size; i++) {
            if(normalizerTable.contains((long)input.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Normalize the provided Unicode string by decomposing characters.
     * 
     * @param composedString The composed unicode string.
     * @return The decomposed unicode string.
     */
    public String normalize(String composedString){
        StringBuffer buf = new StringBuffer();
        int size = composedString.length();

        for(int i = 0; i < size; i++) {
            char currentChar = composedString.charAt(i);
            String decomposedChar = (String)normalizerTable.get((long)currentChar);
            if(decomposedChar == null) {
                buf.append(currentChar);
            }
            else {
                buf.append(decomposedChar);
            }
        }
        return buf.toString();
    }
} 
