/*
 * Carrot2 Project
 * Copyright (C) 2002-2003, Dawid Weiss
 * All rights reserved.
 *
 * Refer to full text of the licence "carrot2.LICENCE" in the root folder
 * of CVS checkout or at:
 * http://www.cs.put.poznan.pl/dweiss/carrot2.LICENCE
 */

package com.dawidweiss.carrot.util;

import java.util.HashMap;
import java.util.Map;

import gnu.regexp.RE;
import gnu.regexp.REException;


/**
 * Utility class for stripping HTML tags and decoding some of HTML
 * entities.
 * 
 * Instances of this class are not guaranteed to be thread-safe. 
 */
public class HTMLTextStripper
{
    /**
     * Returns an instance of the serializer. The instance is not thread-safe,
     * but can be reused many times (and should be).
     */
    public static HTMLTextStripper getInstance() {
        return new HTMLTextStripper();
    }

    /**
     * Use static <code>getInstance</code> method.
     */
    private HTMLTextStripper()
    {
    }

    /**
     * Strips all HTML tags from a string. Inserts a blank space for all tags it removes.
     */
    private static final String pattern = "(<.*?>)|(<script.*?/script>)";
    private static final RE     patternMatch;
    
    private static final Map namedEntities;

    static {
        try { 
            patternMatch = 
                new RE( pattern, RE.REG_DOT_NEWLINE | RE.REG_ICASE ); }
        catch (REException e) {
            throw new java.lang.Error( "RegExp pattern does not compile!" );
        }
        
        namedEntities = new HashMap();
        namedEntities.put("amp", "&");
        namedEntities.put("lt", "<");
        namedEntities.put("gt", ">");
        namedEntities.put("quot", "\"");
        namedEntities.put("apos", "'");
    }

    /**
     * Returns a textual representation of a block of HTML code.
     * SLOOOOOW implementation right now.
     */
    public String htmlToText(String html)
    {
        if (html != null) {
           String plain = patternMatch.substituteAll(html, " ");
           
           // now substitute character entities and
           // named entities
           StringBuffer buf = new StringBuffer( plain.length() );
           int max = plain.length();
           for (int i=0;i<max;i++) {
               if (plain.charAt(i) == '&') {
                   int j;
                   int maxlookahead = Math.min( max, i+20);
                   for (j = i+1; j<maxlookahead;j++) {
                       if (plain.charAt(j) == ';') {
                           break;
                       }
                   }
                   if (j==maxlookahead) {
                       // no end-of-entity semicolon?
                       // just place the ampersand then.
                       buf.append('&');
                   } else {
                       if (plain.charAt(i+1)=='#') {
                           try {
                               if (plain.charAt(i+2)=='x' || plain.charAt(i+2) == 'X') {
                                   // hex
                                   int value = Integer.parseInt(
                                        plain.substring(i+3, j), 16);
                                   buf.append((char) value);
                               } else {
                                   // dec
                                   int value = Integer.parseInt(
                                        plain.substring(i+2, j), 10);    
                                   buf.append((char) value);
                               }
                           }
                           catch (NumberFormatException f) {
                               // ignore wrong entities.
                           }
                       } else {
                           // named entity?
                           Object named;
                           if ((named = namedEntities.get( plain.substring(i+1, j)))!=null) {
                               buf.append(named);
                           } else {
                               // unrecognized named entity.
                           }
                       }
                       // go to the end of entity declaration
                       i = j;
                   }
               } else {
                    buf.append(plain.charAt(i));
               }
           }
    
            return buf.toString();
        }
        else
            return html;
    }

}


 