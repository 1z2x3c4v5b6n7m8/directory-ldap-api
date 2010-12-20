/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.shared.ldap.util;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.naming.InvalidNameException;

import org.apache.directory.shared.asn1.Hex;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.entry.BinaryValue;
import org.apache.directory.shared.ldap.entry.StringValue;
import org.apache.directory.shared.ldap.schema.syntaxCheckers.UuidSyntaxChecker;
import org.apache.directory.shared.util.CharConstants;
import org.apache.directory.shared.util.Strings;


/**
 * Various string manipulation methods that are more efficient then chaining
 * string operations: all is done in the same buffer without creating a bunch of
 * string objects.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class StringTools
{
    /**
     * Private constructor
     */
    private StringTools()
    {
    }

    /** The default charset, because it's not provided by JDK 1.5 */
    static String defaultCharset = null;

    // The following methods are taken from org.apache.commons.lang.StringUtils

    /**
     * The empty String <code>""</code>.
     * 
     * @since 2.0
     */
    public static final String EMPTY = "";

    /**
     * The empty String[]
     */
    public static final String[] EMPTY_STRINGS = new String[] {};

    /**
     * Trims several consecutive characters into one.
     * 
     * @param str
     *            the string to trim consecutive characters of
     * @param ch
     *            the character to trim down
     * @return the newly trimmed down string
     */
    public static String trimConsecutiveToOne( String str, char ch )
    {
        if ( ( null == str ) || ( str.length() == 0 ) )
        {
            return "";
        }

        char[] buffer = str.toCharArray();
        char[] newbuf = new char[buffer.length];
        int pos = 0;
        boolean same = false;

        for ( int i = 0; i < buffer.length; i++ )
        {
            char car = buffer[i];

            if ( car == ch )
            {
                if ( same )
                {
                    continue;
                }
                else
                {
                    same = true;
                    newbuf[pos++] = car;
                }
            }
            else
            {
                same = false;
                newbuf[pos++] = car;
            }
        }

        return new String( newbuf, 0, pos );
    }


    /**
     * A deep trim of a string remove whitespace from the ends as well as
     * excessive whitespace within the inside of the string between
     * non-whitespace characters. A deep trim reduces internal whitespace down
     * to a single space to perserve the whitespace separated tokenization order
     * of the String.
     * 
     * @param string the string to deep trim.
     * @return the trimmed string.
     */
    public static String deepTrim( String string )
    {
        return deepTrim( string, false );
    }


    /**
     * This does the same thing as a trim but we also lowercase the string while
     * performing the deep trim within the same buffer. This saves us from
     * having to create multiple String and StringBuffer objects and is much
     * more efficient.
     * 
     * @see StringTools#deepTrim( String )
     */
    public static String deepTrimToLower( String string )
    {
        return deepTrim( string, true );
    }


    /**
     * Put common code to deepTrim(String) and deepTrimToLower here.
     * 
     * @param str the string to deep trim
     * @param toLowerCase how to normalize for case: upper or lower
     * @return the deep trimmed string
     * @see StringTools#deepTrim( String )
     * 
     * TODO Replace the toCharArray() by substring manipulations
     */
    public static String deepTrim( String str, boolean toLowerCase )
    {
        if ( ( null == str ) || ( str.length() == 0 ) )
        {
            return "";
        }

        char ch;
        char[] buf = str.toCharArray();
        char[] newbuf = new char[buf.length];
        boolean wsSeen = false;
        boolean isStart = true;
        int pos = 0;

        for ( int i = 0; i < str.length(); i++ )
        {
            ch = buf[i];

            // filter out all uppercase characters
            if ( toLowerCase && Character.isUpperCase( ch ) )
            {
                ch = Character.toLowerCase( ch );
            }

            // Check to see if we should add space
            if ( Character.isWhitespace( ch ) )
            {
                // If the buffer has had characters added already check last
                // added character. Only append a spc if last character was
                // not whitespace.
                if ( wsSeen )
                {
                    continue;
                }
                else
                {
                    wsSeen = true;

                    if ( isStart )
                    {
                        isStart = false;
                    }
                    else
                    {
                        newbuf[pos++] = ch;
                    }
                }
            }
            else
            {
                // Add all non-whitespace
                wsSeen = false;
                isStart = false;
                newbuf[pos++] = ch;
            }
        }

        return ( pos == 0 ? "" : new String( newbuf, 0, ( wsSeen ? pos - 1 : pos ) ) );
    }

    /**
     * Truncates large Strings showing a portion of the String's head and tail
     * with the center cut out and replaced with '...'. Also displays the total
     * length of the truncated string so size of '...' can be interpreted.
     * Useful for large strings in UIs or hex dumps to log files.
     * 
     * @param str the string to truncate
     * @param head the amount of the head to display
     * @param tail the amount of the tail to display
     * @return the center truncated string
     */
    public static String centerTrunc( String str, int head, int tail )
    {
        StringBuffer buf = null;

        // Return as-is if String is smaller than or equal to the head plus the
        // tail plus the number of characters added to the trunc representation
        // plus the number of digits in the string length.
        if ( str.length() <= ( head + tail + 7 + str.length() / 10 ) )
        {
            return str;
        }

        buf = new StringBuffer();
        buf.append( '[' ).append( str.length() ).append( "][" );
        buf.append( str.substring( 0, head ) ).append( "..." );
        buf.append( str.substring( str.length() - tail ) );
        buf.append( ']' );
        return buf.toString();
    }


    /**
     * Gets a hex string from byte array.
     * 
     * @param res
     *            the byte array
     * @return the hex string representing the binary values in the array
     */
    public static String toHexString( byte[] res )
    {
        StringBuffer buf = new StringBuffer( res.length << 1 );
        
        for ( int ii = 0; ii < res.length; ii++ )
        {
            String digit = Integer.toHexString( 0xFF & res[ii] );
            
            if ( digit.length() == 1 )
            {
                digit = '0' + digit;
            }
            
            buf.append( digit );
        }
        return buf.toString().toUpperCase();
    }


    /**
     * Rewrote the toLowercase method to improve performances.
     * In Ldap, attributesType are supposed to use ASCII chars :
     * 'a'-'z', 'A'-'Z', '0'-'9', '.' and '-' only.
     * 
     * @param value The String to lowercase
     * @return The lowercase string
     */
    public static String toLowerCase( String value )
    {
        if ( ( null == value ) || ( value.length() == 0 ) )
        {
            return "";
        }
        
        char[] chars = value.toCharArray();
        
        for ( int i = 0; i < chars.length; i++ )
        {
            chars[i] = CharConstants.TO_LOWER_CASE[ chars[i] ];
        }
        
        return new String( chars );
    }

    
    /**
     * Rewrote the toLowercase method to improve performances.
     * In Ldap, attributesType are supposed to use ASCII chars :
     * 'a'-'z', 'A'-'Z', '0'-'9', '.' and '-' only.
     * 
     * @param value The String to uppercase
     * @return The uppercase string
     */
    public static String toUpperCase( String value )
    {
        if ( ( null == value ) || ( value.length() == 0 ) )
        {
            return "";
        }
        
        char[] chars = value.toCharArray();
        
        for ( int i = 0; i < chars.length; i++ )
        {
            chars[i] = CharConstants.UPPER_CASE[ chars[i] ];
        }
        
        return new String( chars );
    }

    
    /**
     * Get byte array from hex string
     * 
     * @param hexString
     *            the hex string to convert to a byte array
     * @return the byte form of the hex string.
     */
    public static byte[] toByteArray( String hexString )
    {
        int arrLength = hexString.length() >> 1;
        byte [] buf = new byte[arrLength];
        
        for ( int ii = 0; ii < arrLength; ii++ )
        {
            int index = ii << 1;
            
            String digit = hexString.substring( index, index + 2 );
            buf[ii] = ( byte ) Integer.parseInt( digit, 16 );
        }
        
        return buf;
    }


    /**
     * This method is used to insert HTML block dynamically
     * 
     * @param source the HTML code to be processes
     * @param replaceNl if true '\n' will be replaced by &lt;br>
     * @param replaceTag if true '<' will be replaced by &lt; and '>' will be replaced
     *            by &gt;
     * @param replaceQuote if true '\"' will be replaced by &quot;
     * @return the formated html block
     */
    public static String formatHtml( String source, boolean replaceNl, boolean replaceTag,
        boolean replaceQuote )
    {
        StringBuffer buf = new StringBuffer();
        int len = source.length();

        for ( int ii = 0; ii < len; ii++ )
        {
            char ch = source.charAt( ii );
            
            switch ( ch )
            {
                case '\"':
                    if ( replaceQuote )
                    {
                        buf.append( "&quot;" );
                    }
                    else
                    {
                        buf.append( ch );
                    }
                    break;

                case '<':
                    if ( replaceTag )
                    {
                        buf.append( "&lt;" );
                    }
                    else
                    {
                        buf.append( ch );
                    }
                    break;

                case '>':
                    if ( replaceTag )
                    {
                        buf.append( "&gt;" );
                    }
                    else
                    {
                        buf.append( ch );
                    }
                    break;

                case '\n':
                    if ( replaceNl )
                    {
                        if ( replaceTag )
                        {
                            buf.append( "&lt;br&gt;" );
                        }
                        else
                        {
                            buf.append( "<br>" );
                        }
                    }
                    else
                    {
                        buf.append( ch );
                    }
                    break;

                case '\r':
                    break;

                case '&':
                    buf.append( "&amp;" );
                    break;

                default:
                    buf.append( ch );
                    break;
            }
        }

        return buf.toString();
    }


    /**
     * Creates a regular expression from an LDAP substring assertion filter
     * specification.
     * 
     * @param initialPattern
     *            the initial fragment before wildcards
     * @param anyPattern
     *            fragments surrounded by wildcards if any
     * @param finalPattern
     *            the final fragment after last wildcard if any
     * @return the regular expression for the substring match filter
     * @throws PatternSyntaxException
     *             if a syntactically correct regular expression cannot be
     *             compiled
     */
    public static Pattern getRegex( String initialPattern, String[] anyPattern, String finalPattern )
        throws PatternSyntaxException
    {
        StringBuffer buf = new StringBuffer();

        if ( initialPattern != null )
        {
            buf.append( '^' ).append( Pattern.quote( initialPattern ) );
        }

        if ( anyPattern != null )
        {
            for ( int i = 0; i < anyPattern.length; i++ )
            {
                buf.append( ".*" ).append( Pattern.quote( anyPattern[i] ) );
            }
        }

        if ( finalPattern != null )
        {
            buf.append( ".*" ).append( Pattern.quote( finalPattern ) );
        }
        else
        {
            buf.append( ".*" );
        }

        return Pattern.compile( buf.toString() );
    }


    /**
     * Generates a regular expression from an LDAP substring match expression by
     * parsing out the supplied string argument.
     * 
     * @param ldapRegex
     *            the substring match expression
     * @return the regular expression for the substring match filter
     * @throws PatternSyntaxException
     *             if a syntactically correct regular expression cannot be
     *             compiled
     */
    public static Pattern getRegex( String ldapRegex ) throws PatternSyntaxException
    {
        if ( ldapRegex == null )
        {
            throw new PatternSyntaxException( I18n.err( I18n.ERR_04429 ), "null", -1 );
        }

        List<String> any = new ArrayList<String>();
        String remaining = ldapRegex;
        int index = remaining.indexOf( '*' );

        if ( index == -1 )
        {
            throw new PatternSyntaxException( I18n.err( I18n.ERR_04430 ), remaining, -1 );
        }

        String initialPattern = null;

        if ( remaining.charAt( 0 ) != '*' )
        {
            initialPattern = remaining.substring( 0, index );
        }

        remaining = remaining.substring( index + 1, remaining.length() );

        while ( ( index = remaining.indexOf( '*' ) ) != -1 )
        {
            any.add( remaining.substring( 0, index ) );
            remaining = remaining.substring( index + 1, remaining.length() );
        }

        String finalPattern = null;
        if ( !remaining.endsWith( "*" ) && remaining.length() > 0 )
        {
            finalPattern = remaining;
        }

        if ( any.size() > 0 )
        {
            String[] anyStrs = new String[any.size()];

            for ( int i = 0; i < anyStrs.length; i++ )
            {
                anyStrs[i] = any.get( i );
            }

            return getRegex( initialPattern, anyStrs, finalPattern );
        }

        return getRegex( initialPattern, null, finalPattern );
    }


    /**
     * Splits apart a OS separator delimited set of paths in a string into
     * multiple Strings. File component path strings are returned within a List
     * in the order they are found in the composite path string. Optionally, a
     * file filter can be used to filter out path strings to control the
     * components returned. If the filter is null all path components are
     * returned.
     * 
     * @param paths
     *            a set of paths delimited using the OS path separator
     * @param filter
     *            a FileFilter used to filter the return set
     * @return the filter accepted path component Strings in the order
     *         encountered
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements") // Used because of comments
    public static List<String> getPaths( String paths, FileFilter filter )
    {
        int start = 0;
        int stop = -1;
        String path = null;
        List<String> list = new ArrayList<String>();

        // Abandon with no values if paths string is null
        if ( paths == null || paths.trim().equals( "" ) )
        {
            return list;
        }

        final int max = paths.length() - 1;

        // Loop spliting string using OS path separator: terminate
        // when the start index is at the end of the paths string.
        while ( start < max )
        {
            stop = paths.indexOf( File.pathSeparatorChar, start );

            // The is no file sep between the start and the end of the string
            if ( stop == -1 )
            {
                // If we have a trailing path remaining without ending separator
                if ( start < max )
                {
                    // Last path is everything from start to the string's end
                    path = paths.substring( start );

                    // Protect against consecutive separators side by side
                    if ( !path.trim().equals( "" ) )
                    {
                        // If filter is null add path, if it is not null add the
                        // path only if the filter accepts the path component.
                        if ( filter == null || filter.accept( new File( path ) ) )
                        {
                            list.add( path );
                        }
                    }
                }

                break; // Exit loop no more path components left!
            }

            // There is a separator between start and the end if we got here!
            // start index is now at 0 or the index of last separator + 1
            // stop index is now at next separator in front of start index
            path = paths.substring( start, stop );

            // Protect against consecutive separators side by side
            if ( !path.trim().equals( "" ) )
            {
                // If filter is null add path, if it is not null add the path
                // only if the filter accepts the path component.
                if ( filter == null || filter.accept( new File( path ) ) )
                {
                    list.add( path );
                }
            }

            // Advance start index past separator to start of next path comp
            start = stop + 1;
        }

        return list;
    }


    // ~ Methods
    // ------------------------------------------------------------------------------------

    /**
     * Helper function that dump a byte in hex form
     * 
     * @param octet The byte to dump
     * @return A string representation of the byte
     */
    public static String dumpByte( byte octet )
    {
        return new String( new byte[]
            { '0', 'x', CharConstants.HEX_CHAR[( octet & 0x00F0 ) >> 4], CharConstants.HEX_CHAR[octet & 0x000F] } );
    }


    /**
     * Helper function that returns a char from an hex
     * 
     * @param hex The hex to dump
     * @return A char representation of the hex
     */
    public static char dumpHex( byte hex )
    {
        return ( char ) CharConstants.HEX_CHAR[hex & 0x000F];
    }


    /**
     * 
     * Helper method to render an object which can be a String or a byte[]
     *
     * @return A string representing the object
     */
    public static String dumpObject( Object object )
    {
        if ( object != null )
        {
            if ( object instanceof String )
            {
                return (String) object;
            }
            else if ( object instanceof byte[] )
            {
                return Strings.dumpBytes((byte[]) object);
            }
            else if ( object instanceof StringValue )
            {
                return ( ( StringValue ) object ).get();
            }
            else if ( object instanceof BinaryValue )
            {
                return Strings.dumpBytes(((BinaryValue) object).get());
            }
            else
            {
                return "<unknown type>";
            }
        }
        else
        {
            return "";
        }
    }

    /**
     * Helper function that dump an array of bytes in hex pair form, 
     * without '0x' and space chars
     * 
     * @param buffer The bytes array to dump
     * @return A string representation of the array of bytes
     */
    public static String dumpHexPairs( byte[] buffer )
    {
        if ( buffer == null )
        {
            return "";
        }

        char[] str = new char[buffer.length << 1];

        for ( int i = 0, pos = 0; i < buffer.length; i++ )
        {
            str[pos++] = ( char ) ( CharConstants.HEX_CHAR[( buffer[i] & 0x00F0 ) >> 4] );
            str[pos++] = ( char ) ( CharConstants.HEX_CHAR[buffer[i] & 0x000F] );
        }

        return new String( str );
    }

    /**
     * Return the Unicode char which is coded in the bytes at position 0.
     * 
     * @param bytes The byte[] represntation of an Unicode string.
     * @return The first char found.
     */
    public static char bytesToChar( byte[] bytes )
    {
        return bytesToChar( bytes, 0 );
    }


    /**
     * Count the number of bytes needed to return an Unicode char. This can be
     * from 1 to 6.
     * 
     * @param bytes The bytes to read
     * @param pos Position to start counting. It must be a valid start of a
     *            encoded char !
     * @return The number of bytes to create a char, or -1 if the encoding is
     *         wrong. TODO : Should stop after the third byte, as a char is only
     *         2 bytes long.
     */
    public static int countBytesPerChar( byte[] bytes, int pos )
    {
        if ( bytes == null )
        {
            return -1;
        }

        if ( ( bytes[pos] & CharConstants.UTF8_MULTI_BYTES_MASK ) == 0 )
        {
            return 1;
        }
        else if ( ( bytes[pos] & CharConstants.UTF8_TWO_BYTES_MASK ) == CharConstants.UTF8_TWO_BYTES )
        {
            return 2;
        }
        else if ( ( bytes[pos] & CharConstants.UTF8_THREE_BYTES_MASK ) == CharConstants.UTF8_THREE_BYTES )
        {
            return 3;
        }
        else if ( ( bytes[pos] & CharConstants.UTF8_FOUR_BYTES_MASK ) == CharConstants.UTF8_FOUR_BYTES )
        {
            return 4;
        }
        else if ( ( bytes[pos] & CharConstants.UTF8_FIVE_BYTES_MASK ) == CharConstants.UTF8_FIVE_BYTES )
        {
            return 5;
        }
        else if ( ( bytes[pos] & CharConstants.UTF8_SIX_BYTES_MASK ) == CharConstants.UTF8_SIX_BYTES )
        {
            return 6;
        }
        else
        {
            return -1;
        }
    }


    /**
     * Return the number of bytes that hold an Unicode char.
     * 
     * @param car The character to be decoded
     * @return The number of bytes to hold the char. TODO : Should stop after
     *         the third byte, as a char is only 2 bytes long.
     */
    public static int countNbBytesPerChar( char car )
    {
        if ( ( car & CharConstants.CHAR_ONE_BYTE_MASK ) == 0 )
        {
            return 1;
        }
        else if ( ( car & CharConstants.CHAR_TWO_BYTES_MASK ) == 0 )
        {
            return 2;
        }
        else if ( ( car & CharConstants.CHAR_THREE_BYTES_MASK ) == 0 )
        {
            return 3;
        }
        else if ( ( car & CharConstants.CHAR_FOUR_BYTES_MASK ) == 0 )
        {
            return 4;
        }
        else if ( ( car & CharConstants.CHAR_FIVE_BYTES_MASK ) == 0 )
        {
            return 5;
        }
        else if ( ( car & CharConstants.CHAR_SIX_BYTES_MASK ) == 0 )
        {
            return 6;
        }
        else
        {
            return -1;
        }
    }


    /**
     * Count the number of bytes included in the given char[].
     * 
     * @param chars The char array to decode
     * @return The number of bytes in the char array
     */
    public static int countBytes( char[] chars )
    {
        if ( chars == null )
        {
            return 0;
        }

        int nbBytes = 0;
        int currentPos = 0;

        while ( currentPos < chars.length )
        {
            int nbb = countNbBytesPerChar( chars[currentPos] );

            // If the number of bytes necessary to encode a character is
            // above 3, we will need two UTF-16 chars
            currentPos += ( nbb < 4 ? 1 : 2 );
            nbBytes += nbb;
        }

        return nbBytes;
    }


    /**
     * Return the Unicode char which is coded in the bytes at the given
     * position.
     * 
     * @param bytes The byte[] represntation of an Unicode string.
     * @param pos The current position to start decoding the char
     * @return The decoded char, or -1 if no char can be decoded TODO : Should
     *         stop after the third byte, as a char is only 2 bytes long.
     */
    public static char bytesToChar( byte[] bytes, int pos )
    {
        if ( bytes == null )
        {
            return ( char ) -1;
        }

        if ( ( bytes[pos] & CharConstants.UTF8_MULTI_BYTES_MASK ) == 0 )
        {
            return ( char ) bytes[pos];
        }
        else
        {
            if ( ( bytes[pos] & CharConstants.UTF8_TWO_BYTES_MASK ) == CharConstants.UTF8_TWO_BYTES )
            {
                // Two bytes char
                return ( char ) ( ( ( bytes[pos] & 0x1C ) << 6 ) + // 110x-xxyy
                                                                    // 10zz-zzzz
                                                                    // ->
                                                                    // 0000-0xxx
                                                                    // 0000-0000
                    ( ( bytes[pos] & 0x03 ) << 6 ) + // 110x-xxyy 10zz-zzzz
                                                        // -> 0000-0000
                                                        // yy00-0000
                ( bytes[pos + 1] & 0x3F ) // 110x-xxyy 10zz-zzzz -> 0000-0000
                                            // 00zz-zzzz
                ); // -> 0000-0xxx yyzz-zzzz (07FF)
            }
            else if ( ( bytes[pos] & CharConstants.UTF8_THREE_BYTES_MASK ) == CharConstants.UTF8_THREE_BYTES )
            {
                // Three bytes char
                return ( char ) (
                // 1110-tttt 10xx-xxyy 10zz-zzzz -> tttt-0000-0000-0000
                ( ( bytes[pos] & 0x0F ) << 12 )
                    // 1110-tttt 10xx-xxyy 10zz-zzzz -> 0000-xxxx-0000-0000
                    + ( ( bytes[pos + 1] & 0x3C ) << 6 )
                    // 1110-tttt 10xx-xxyy 10zz-zzzz -> 0000-0000-yy00-0000
                    + ( ( bytes[pos + 1] & 0x03 ) << 6 )
                // 1110-tttt 10xx-xxyy 10zz-zzzz -> 0000-0000-00zz-zzzz
                + ( bytes[pos + 2] & 0x3F )
                // -> tttt-xxxx yyzz-zzzz (FF FF)
                );
            }
            else if ( ( bytes[pos] & CharConstants.UTF8_FOUR_BYTES_MASK ) == CharConstants.UTF8_FOUR_BYTES )
            {
                // Four bytes char
                return ( char ) (
                // 1111-0ttt 10uu-vvvv 10xx-xxyy 10zz-zzzz -> 000t-tt00
                // 0000-0000 0000-0000
                ( ( bytes[pos] & 0x07 ) << 18 )
                    // 1111-0ttt 10uu-vvvv 10xx-xxyy 10zz-zzzz -> 0000-00uu
                    // 0000-0000 0000-0000
                    + ( ( bytes[pos + 1] & 0x30 ) << 16 )
                    // 1111-0ttt 10uu-vvvv 10xx-xxyy 10zz-zzzz -> 0000-0000
                    // vvvv-0000 0000-0000
                    + ( ( bytes[pos + 1] & 0x0F ) << 12 )
                    // 1111-0ttt 10uu-vvvv 10xx-xxyy 10zz-zzzz -> 0000-0000
                    // 0000-xxxx 0000-0000
                    + ( ( bytes[pos + 2] & 0x3C ) << 6 )
                    // 1111-0ttt 10uu-vvvv 10xx-xxyy 10zz-zzzz -> 0000-0000
                    // 0000-0000 yy00-0000
                    + ( ( bytes[pos + 2] & 0x03 ) << 6 )
                // 1111-0ttt 10uu-vvvv 10xx-xxyy 10zz-zzzz -> 0000-0000
                // 0000-0000 00zz-zzzz
                + ( bytes[pos + 3] & 0x3F )
                // -> 000t-ttuu vvvv-xxxx yyzz-zzzz (1FFFFF)
                );
            }
            else if ( ( bytes[pos] & CharConstants.UTF8_FIVE_BYTES_MASK ) == CharConstants.UTF8_FIVE_BYTES )
            {
                // Five bytes char
                return ( char ) (
                // 1111-10tt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz ->
                // 0000-00tt 0000-0000 0000-0000 0000-0000
                ( ( bytes[pos] & 0x03 ) << 24 )
                    // 1111-10tt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz ->
                    // 0000-0000 uuuu-uu00 0000-0000 0000-0000
                    + ( ( bytes[pos + 1] & 0x3F ) << 18 )
                    // 1111-10tt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz ->
                    // 0000-0000 0000-00vv 0000-0000 0000-0000
                    + ( ( bytes[pos + 2] & 0x30 ) << 12 )
                    // 1111-10tt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz ->
                    // 0000-0000 0000-0000 wwww-0000 0000-0000
                    + ( ( bytes[pos + 2] & 0x0F ) << 12 )
                    // 1111-10tt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz ->
                    // 0000-0000 0000-0000 0000-xxxx 0000-0000
                    + ( ( bytes[pos + 3] & 0x3C ) << 6 )
                    // 1111-10tt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz ->
                    // 0000-0000 0000-0000 0000-0000 yy00-0000
                    + ( ( bytes[pos + 3] & 0x03 ) << 6 )
                // 1111-10tt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz ->
                // 0000-0000 0000-0000 0000-0000 00zz-zzzz
                + ( bytes[pos + 4] & 0x3F )
                // -> 0000-00tt uuuu-uuvv wwww-xxxx yyzz-zzzz (03 FF FF FF)
                );
            }
            else if ( ( bytes[pos] & CharConstants.UTF8_FIVE_BYTES_MASK ) == CharConstants.UTF8_FIVE_BYTES )
            {
                // Six bytes char
                return ( char ) (
                // 1111-110s 10tt-tttt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz
                // ->
                // 0s00-0000 0000-0000 0000-0000 0000-0000
                ( ( bytes[pos] & 0x01 ) << 30 )
                    // 1111-110s 10tt-tttt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz
                    // ->
                    // 00tt-tttt 0000-0000 0000-0000 0000-0000
                    + ( ( bytes[pos + 1] & 0x3F ) << 24 )
                    // 1111-110s 10tt-tttt 10uu-uuuu 10vv-wwww 10xx-xxyy
                    // 10zz-zzzz ->
                    // 0000-0000 uuuu-uu00 0000-0000 0000-0000
                    + ( ( bytes[pos + 2] & 0x3F ) << 18 )
                    // 1111-110s 10tt-tttt 10uu-uuuu 10vv-wwww 10xx-xxyy
                    // 10zz-zzzz ->
                    // 0000-0000 0000-00vv 0000-0000 0000-0000
                    + ( ( bytes[pos + 3] & 0x30 ) << 12 )
                    // 1111-110s 10tt-tttt 10uu-uuuu 10vv-wwww 10xx-xxyy
                    // 10zz-zzzz ->
                    // 0000-0000 0000-0000 wwww-0000 0000-0000
                    + ( ( bytes[pos + 3] & 0x0F ) << 12 )
                    // 1111-110s 10tt-tttt 10uu-uuuu 10vv-wwww 10xx-xxyy
                    // 10zz-zzzz ->
                    // 0000-0000 0000-0000 0000-xxxx 0000-0000
                    + ( ( bytes[pos + 4] & 0x3C ) << 6 )
                    // 1111-110s 10tt-tttt 10uu-uuuu 10vv-wwww 10xx-xxyy
                    // 10zz-zzzz ->
                    // 0000-0000 0000-0000 0000-0000 yy00-0000
                    + ( ( bytes[pos + 4] & 0x03 ) << 6 )
                // 1111-110s 10tt-tttt 10uu-uuuu 10vv-wwww 10xx-xxyy 10zz-zzzz
                // ->
                // 0000-0000 0000-0000 0000-0000 00zz-zzzz
                + ( bytes[pos + 5] & 0x3F )
                // -> 0stt-tttt uuuu-uuvv wwww-xxxx yyzz-zzzz (7F FF FF FF)
                );
            }
            else
            {
                return ( char ) -1;
            }
        }
    }


    /**
     * Return the Unicode char which is coded in the bytes at the given
     * position.
     * 
     * @param car The character to be transformed to an array of bytes
     * 
     * @return The byte array representing the char 
     * 
     * TODO : Should stop after the third byte, as a char is only 2 bytes long.
     */
    public static byte[] charToBytes( char car )
    {
        byte[] bytes = new byte[countNbBytesPerChar( car )];

        if ( car <= 0x7F )
        {
            // Single byte char
            bytes[0] = ( byte ) car;
            return bytes;
        }
        else if ( car <= 0x7FF )
        {
            // two bytes char
            bytes[0] = ( byte ) ( 0x00C0 + ( ( car & 0x07C0 ) >> 6 ) );
            bytes[1] = ( byte ) ( 0x0080 + ( car & 0x3F ) );
        }
        else
        {
            // Three bytes char
            bytes[0] = ( byte ) ( 0x00E0 + ( ( car & 0xF000 ) >> 12 ) );
            bytes[1] = ( byte ) ( 0x0080 + ( ( car & 0x0FC0 ) >> 6 ) );
            bytes[2] = ( byte ) ( 0x0080 + ( car & 0x3F ) );
        }

        return bytes;
    }


    /**
     * Count the number of chars included in the given byte[].
     * 
     * @param bytes The byte array to decode
     * @return The number of char in the byte array
     */
    public static int countChars( byte[] bytes )
    {
        if ( bytes == null )
        {
            return 0;
        }

        int nbChars = 0;
        int currentPos = 0;

        while ( currentPos < bytes.length )
        {
            currentPos += countBytesPerChar( bytes, currentPos );
            nbChars++;
        }

        return nbChars;
    }


    /**
     * Check if a text is present at the current position in a buffer.
     * 
     * @param bytes The buffer which contains the data
     * @param index Current position in the buffer
     * @param text The text we want to check
     * @return <code>true</code> if the buffer contains the text.
     */
    public static int areEquals( byte[] bytes, int index, String text )
    {
        if ( ( bytes == null ) || ( bytes.length == 0 ) || ( bytes.length <= index ) || ( index < 0 )
            || ( text == null ) )
        {
            return CharConstants.NOT_EQUAL;
        }
        else
        {
            try
            {
                byte[] data = text.getBytes( "UTF-8" );

                return areEquals( bytes, index, data );
            }
            catch ( UnsupportedEncodingException uee )
            {
                // if this happens something is really strange
                throw new RuntimeException( uee );
            }
        }
    }


    /**
     * Check if a text is present at the current position in a buffer.
     * 
     * @param chars The buffer which contains the data
     * @param index Current position in the buffer
     * @param text The text we want to check
     * @return <code>true</code> if the buffer contains the text.
     */
    public static int areEquals( char[] chars, int index, String text )
    {
        if ( ( chars == null ) || ( chars.length == 0 ) || ( chars.length <= index ) || ( index < 0 )
            || ( text == null ) )
        {
            return CharConstants.NOT_EQUAL;
        }
        else
        {
            char[] data = text.toCharArray();

            return areEquals( chars, index, data );
        }
    }


    /**
     * Check if a text is present at the current position in a buffer.
     * 
     * @param chars The buffer which contains the data
     * @param index Current position in the buffer
     * @param chars2 The text we want to check
     * @return <code>true</code> if the buffer contains the text.
     */
    public static int areEquals( char[] chars, int index, char[] chars2 )
    {
        if ( ( chars == null ) || ( chars.length == 0 ) || ( chars.length <= index ) || ( index < 0 )
            || ( chars2 == null ) || ( chars2.length == 0 )
            || ( chars2.length > ( chars.length + index ) ) )
        {
            return CharConstants.NOT_EQUAL;
        }
        else
        {
            for ( int i = 0; i < chars2.length; i++ )
            {
                if ( chars[index++] != chars2[i] )
                {
                    return CharConstants.NOT_EQUAL;
                }
            }

            return index;
        }
    }

    /**
     * Check if a text is present at the current position in another string.
     * 
     * @param string The string which contains the data
     * @param index Current position in the string
     * @param text The text we want to check
     * @return <code>true</code> if the string contains the text.
     */
    public static boolean areEquals( String string, int index, String text )
    {
        if ( ( string == null ) || ( text == null ) ) 
        {
            return false;
        }
        
        int length1 = string.length();
        int length2 = text.length();

        if ( ( length1 == 0 ) || ( length1 <= index ) || ( index < 0 )
            || ( length2 == 0 ) || ( length2 > ( length1 + index ) ) )
        {
            return false;
        }
        else
        {
            return string.substring( index ).startsWith( text );
        }
    }
    

    /**
     * Check if a text is present at the current position in a buffer.
     * 
     * @param bytes The buffer which contains the data
     * @param index Current position in the buffer
     * @param bytes2 The text we want to check
     * @return <code>true</code> if the buffer contains the text.
     */
    public static int areEquals( byte[] bytes, int index, byte[] bytes2 )
    {

        if ( ( bytes == null ) || ( bytes.length == 0 ) || ( bytes.length <= index ) || ( index < 0 )
            || ( bytes2 == null ) || ( bytes2.length == 0 )
            || ( bytes2.length > ( bytes.length + index ) ) )
        {
            return CharConstants.NOT_EQUAL;
        }
        else
        {
            for ( int i = 0; i < bytes2.length; i++ )
            {
                if ( bytes[index++] != bytes2[i] )
                {
                    return CharConstants.NOT_EQUAL;
                }
            }

            return index;
        }
    }


    /**
     * Test if the current character is equal to a specific character. This
     * function works only for character between 0 and 127, as it does compare a
     * byte and a char (which is 16 bits wide)
     * 
     * @param byteArray
     *            The buffer which contains the data
     * @param index
     *            Current position in the buffer
     * @param car
     *            The character we want to compare with the current buffer
     *            position
     * @return <code>true</code> if the current character equals the given
     *         character.
     */
    public static boolean isCharASCII( byte[] byteArray, int index, char car )
    {
        if ( ( byteArray == null ) || ( byteArray.length == 0 ) || ( index < 0 ) || ( index >= byteArray.length ) )
        {
            return false;
        }
        else
        {
            return ( ( byteArray[index] == car ) ? true : false );
        }
    }


    /**
     * Test if the current character is equal to a specific character.
     * 
     * @param chars
     *            The buffer which contains the data
     * @param index
     *            Current position in the buffer
     * @param car
     *            The character we want to compare with the current buffer
     *            position
     * @return <code>true</code> if the current character equals the given
     *         character.
     */
    public static boolean isCharASCII( char[] chars, int index, char car )
    {
        if ( ( chars == null ) || ( chars.length == 0 ) || ( index < 0 ) || ( index >= chars.length ) )
        {
            return false;
        }
        else
        {
            return ( ( chars[index] == car ) ? true : false );
        }
    }

    /**
     * Test if the current character is equal to a specific character.
     * 
     * @param string The String which contains the data
     * @param index Current position in the string
     * @param car The character we want to compare with the current string
     *            position
     * @return <code>true</code> if the current character equals the given
     *         character.
     */
    public static boolean isCharASCII( String string, int index, char car )
    {
        if ( string == null )
        {
            return false;
        }
        
        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            return string.charAt( index ) == car;
        }
    }


    /**
     * Test if the current character is equal to a specific character.
     * 
     * @param string The String which contains the data
     * @param index Current position in the string
     * @param car The character we want to compare with the current string
     *            position
     * @return <code>true</code> if the current character equals the given
     *         character.
     */
    public static boolean isICharASCII( String string, int index, char car )
    {
        if ( string == null )
        {
            return false;
        }
        
        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            return ( ( string.charAt( index ) | 0x20 ) & car ) == car;
        }
    }


    /**
     * Test if the current character is equal to a specific character.
     * 
     * @param bytes The String which contains the data
     * @param index Current position in the string
     * @param car The character we want to compare with the current string
     *            position
     * @return <code>true</code> if the current character equals the given
     *         character.
     */
    public static boolean isICharASCII( byte[] bytes, int index, char car )
    {
        if ( bytes == null )
        {
            return false;
        }
        
        int length = bytes.length;
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            return ( ( bytes[ index ] | 0x20 ) & car ) == car;
        }
    }


    /**
     * Test if the current character is a bit, ie 0 or 1.
     * 
     * @param string
     *            The String which contains the data
     * @param index
     *            Current position in the string
     * @return <code>true</code> if the current character is a bit (0 or 1)
     */
    public static boolean isBit( String string, int index )
    {
        if ( string == null )
        {
            return false;
        }
        
        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            char c =  string.charAt(  index );
            return ( ( c == '0' ) || ( c == '1' ) );
        }
    }


    /**
     * Get the character at a given position in a string, checking fo limits
     * 
     * @param string The string which contains the data
     * @param index Current position in the string
     * @return The character ar the given position, or '\0' if something went wrong 
     */
    public static char charAt( String string, int index )
    {
        if ( string == null )
        {
            return '\0';
        }
        
        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return '\0';
        }
        else
        {
            return string.charAt( index ) ;
        }
    }

    
    /**
     * Translate two chars to an hex value. The chars must be 
     * in [a-fA-F0-9]
     *
     * @param high The high value 
     * @param low The low value
     * @return A byte representation of the two chars
     */
    public static byte getHexValue( char high, char low )
    {
        if ( ( high > 127 ) || ( low > 127 ) || ( high < 0 ) | ( low < 0 ) )
        {
            return -1;
        }
        
        return (byte)( ( CharConstants.HEX_VALUE[high] << 4 ) | CharConstants.HEX_VALUE[low] );
    }


    /**
     * Translate two bytes to an hex value. The bytes must be 
     * in [0-9a-fA-F]
     *
     * @param high The high value 
     * @param low The low value
     * @return A byte representation of the two bytes
     */
    public static byte getHexValue( byte high, byte low )
    {
        if ( ( high > 127 ) || ( low > 127 ) || ( high < 0 ) | ( low < 0 ) )
        {
            return -1;
        }
        
        return (byte)( ( CharConstants.HEX_VALUE[high] << 4 ) | CharConstants.HEX_VALUE[low] );
    }

    
    /**
     * Return an hex value from a sinle char
     * The char must be in [0-9a-fA-F]
     *
     * @param c The char we want to convert
     * @return A byte between 0 and 15
     */
    public static byte getHexValue( char c )
    {
        if ( ( c > 127 ) || ( c < 0 ) )
        {
            return -1;
        }
        
        return CharConstants.HEX_VALUE[c];
    }


    /**
     * Check if the current byte is an Hex Char 
     * &lt;hex> ::= [0x30-0x39] | [0x41-0x46] | [0x61-0x66]
     * 
     * @param b The byte we want to check
     * @return <code>true</code> if the current byte is a Hex byte
     */
    public static boolean isHex( byte b )
    {
        return ( ( b | 0x7F ) == 0x7F ) || CharConstants.HEX[b];
    }


    /**
     * Check if the current character is an Hex Char &lt;hex> ::= [0x30-0x39] |
     * [0x41-0x46] | [0x61-0x66]
     * 
     * @param bytes The buffer which contains the data
     * @param index Current position in the buffer
     * @return <code>true</code> if the current character is a Hex Char
     */
    public static boolean isHex( byte[] bytes, int index )
    {
        if ( ( bytes == null ) || ( bytes.length == 0 ) || ( index < 0 ) || ( index >= bytes.length ) )
        {
            return false;
        }
        else
        {
            byte c = bytes[index];

            if ( ( ( c | 0x7F ) != 0x7F ) || ( CharConstants.HEX[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }


    /**
     * Check if the current character is an Hex Char &lt;hex> ::= [0x30-0x39] |
     * [0x41-0x46] | [0x61-0x66]
     * 
     * @param chars The buffer which contains the data
     * @param index Current position in the buffer
     * @return <code>true</code> if the current character is a Hex Char
     */
    public static boolean isHex( char[] chars, int index )
    {
        if ( ( chars == null ) || ( chars.length == 0 ) || ( index < 0 ) || ( index >= chars.length ) )
        {
            return false;
        }
        else
        {
            char c = chars[index];

            if ( ( c > 127 ) || ( CharConstants.HEX[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    /**
     * Check if the current character is an Hex Char &lt;hex> ::= [0x30-0x39] |
     * [0x41-0x46] | [0x61-0x66]
     * 
     * @param string The string which contains the data
     * @param index Current position in the string
     * @return <code>true</code> if the current character is a Hex Char
     */
    public static boolean isHex( String string, int index )
    {
        if ( string == null )
        {
            return false;
        }
        
        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            char c = string.charAt( index );

            if ( ( c > 127 ) || ( CharConstants.HEX[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }
    

    /**
     * Test if the current character is a digit &lt;digit> ::= '0' | '1' | '2' |
     * '3' | '4' | '5' | '6' | '7' | '8' | '9'
     * 
     * @param bytes The buffer which contains the data
     * @return <code>true</code> if the current character is a Digit
     */
    public static boolean isDigit( byte[] bytes )
    {
        if ( ( bytes == null ) || ( bytes.length == 0 ) )
        {
            return false;
        }
        else
        {
            return ( ( ( ( bytes[0] | 0x7F ) != 0x7F ) || !CharConstants.DIGIT[bytes[0]] ) ? false : true );
        }
    }


    /**
     * Test if the current character is an Alpha character : &lt;alpha> ::=
     * [0x41-0x5A] | [0x61-0x7A]
     * 
     * @param bytes The buffer which contains the data
     * @param index Current position in the buffer
     * @return <code>true</code> if the current character is an Alpha
     *         character
     */
    public static boolean isAlphaASCII( byte[] bytes, int index )
    {
        if ( ( bytes == null ) || ( bytes.length == 0 ) || ( index < 0 ) || ( index >= bytes.length ) )
        {
            return false;
        }
        else
        {
            byte c = bytes[index];

            if ( ( ( c | 0x7F ) != 0x7F ) || ( CharConstants.ALPHA[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }


    /**
     * Test if the current character is an Alpha character : &lt;alpha> ::=
     * [0x41-0x5A] | [0x61-0x7A]
     * 
     * @param chars The buffer which contains the data
     * @param index Current position in the buffer
     * @return <code>true</code> if the current character is an Alpha
     *         character
     */
    public static boolean isAlphaASCII( char[] chars, int index )
    {
        if ( ( chars == null ) || ( chars.length == 0 ) || ( index < 0 ) || ( index >= chars.length ) )
        {
            return false;
        }
        else
        {
            char c = chars[index];

            if ( ( c > 127 ) || ( CharConstants.ALPHA[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    
    /**
     * Test if the current character is an Alpha character : &lt;alpha> ::=
     * [0x41-0x5A] | [0x61-0x7A]
     * 
     * @param string The string which contains the data
     * @param index Current position in the string
     * @return <code>true</code> if the current character is an Alpha
     *         character
     */
    public static boolean isAlphaASCII( String string, int index )
    {
        if ( string == null )
        {
            return false;
        }
        
        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            char c = string.charAt( index );

            if ( ( c > 127 ) || ( CharConstants.ALPHA[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    
    /**
     * Test if the current character is a lowercased Alpha character : <br/>
     * &lt;alpha> ::= [0x61-0x7A]
     * 
     * @param string The string which contains the data
     * @param index Current position in the string
     * @return <code>true</code> if the current character is a lower Alpha
     *         character
     */
    public static boolean isAlphaLowercaseASCII( String string, int index )
    {
        if ( string == null )
        {
            return false;
        }

        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            char c = string.charAt( index );

            if ( ( c > 127 ) || ( CharConstants.ALPHA_LOWER_CASE[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    
    /**
     * Test if the current character is a uppercased Alpha character : <br/>
     * &lt;alpha> ::= [0x61-0x7A]
     * 
     * @param string The string which contains the data
     * @param index Current position in the string
     * @return <code>true</code> if the current character is a lower Alpha
     *         character
     */
    public static boolean isAlphaUppercaseASCII( String string, int index )
    {
        if ( string == null )
        {
            return false;
        }

        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            char c = string.charAt( index );

            if ( ( c > 127 ) || ( CharConstants.ALPHA_UPPER_CASE[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }


    /**
     * Test if the current character is a digit &lt;digit> ::= '0' | '1' | '2' |
     * '3' | '4' | '5' | '6' | '7' | '8' | '9'
     * 
     * @param bytes The buffer which contains the data
     * @param index Current position in the buffer
     * @return <code>true</code> if the current character is a Digit
     */
    public static boolean isDigit( byte[] bytes, int index )
    {
        if ( ( bytes == null ) || ( bytes.length == 0 ) || ( index < 0 ) || ( index >= bytes.length ) )
        {
            return false;
        }
        else
        {
            return ( ( ( ( bytes[index] | 0x7F ) !=  0x7F ) || !CharConstants.DIGIT[bytes[index]] ) ? false : true );
        }
    }


    /**
     * Test if the current character is a digit &lt;digit> ::= '0' | '1' | '2' |
     * '3' | '4' | '5' | '6' | '7' | '8' | '9'
     * 
     * @param chars The buffer which contains the data
     * @param index Current position in the buffer
     * @return <code>true</code> if the current character is a Digit
     */
    public static boolean isDigit( char[] chars, int index )
    {
        if ( ( chars == null ) || ( chars.length == 0 ) || ( index < 0 ) || ( index >= chars.length ) )
        {
            return false;
        }
        else
        {
            return ( ( ( chars[index] > 127 ) || !CharConstants.DIGIT[chars[index]] ) ? false : true );
        }
    }

    
    /**
     * Test if the current character is a digit &lt;digit> ::= '0' | '1' | '2' |
     * '3' | '4' | '5' | '6' | '7' | '8' | '9'
     * 
     * @param string The string which contains the data
     * @param index Current position in the string
     * @return <code>true</code> if the current character is a Digit
     */
    public static boolean isDigit( String string, int index )
    {
        if ( string == null )
        {
            return false;
        }

        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            char c = string.charAt(  index  );
            return ( ( ( c > 127 ) || !CharConstants.DIGIT[c] ) ? false : true );
        }
    }

    
    /**
     * Test if the current character is a digit &lt;digit> ::= '0' | '1' | '2' |
     * '3' | '4' | '5' | '6' | '7' | '8' | '9'
     * 
     * @param chars The buffer which contains the data
     * @return <code>true</code> if the current character is a Digit
     */
    public static boolean isDigit( char[] chars )
    {
        if ( ( chars == null ) || ( chars.length == 0 ) )
        {
            return false;
        }
        else
        {
            return ( ( ( chars[0] > 127 ) || !CharConstants.DIGIT[chars[0]] ) ? false : true );
        }
    }

    
    /**
     * Check if the current character is an 7 bits ASCII CHAR (between 0 and
     * 127). 
     * &lt;char> ::= &lt;alpha> | &lt;digit>
     * 
     * @param string The string which contains the data
     * @param index Current position in the string
     * @return The position of the next character, if the current one is a CHAR.
     */
    public static boolean isAlphaDigit( String string, int index )
    {
        if ( string == null )
        {
            return false;
        }

        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            char c = string.charAt( index );

            if ( ( c > 127 ) || ( CharConstants.ALPHA_DIGIT[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }


    /**
     * Check if the current character is an 7 bits ASCII CHAR (between 0 and
     * 127). &lt;char> ::= &lt;alpha> | &lt;digit> | '-'
     * 
     * @param bytes The buffer which contains the data
     * @param index Current position in the buffer
     * @return The position of the next character, if the current one is a CHAR.
     */
    public static boolean isAlphaDigitMinus( byte[] bytes, int index )
    {
        if ( ( bytes == null ) || ( bytes.length == 0 ) || ( index < 0 ) || ( index >= bytes.length ) )
        {
            return false;
        }
        else
        {
            byte c = bytes[index];

            if ( ( ( c | 0x7F ) != 0x7F ) || ( CharConstants.CHAR[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }


    /**
     * Check if the current character is an 7 bits ASCII CHAR (between 0 and
     * 127). &lt;char> ::= &lt;alpha> | &lt;digit> | '-'
     * 
     * @param chars The buffer which contains the data
     * @param index Current position in the buffer
     * @return The position of the next character, if the current one is a CHAR.
     */
    public static boolean isAlphaDigitMinus( char[] chars, int index )
    {
        if ( ( chars == null ) || ( chars.length == 0 ) || ( index < 0 ) || ( index >= chars.length ) )
        {
            return false;
        }
        else
        {
            char c = chars[index];

            if ( ( c > 127 ) || ( CharConstants.CHAR[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    
    /**
     * Check if the current character is an 7 bits ASCII CHAR (between 0 and
     * 127). &lt;char> ::= &lt;alpha> | &lt;digit> | '-'
     * 
     * @param string The string which contains the data
     * @param index Current position in the string
     * @return The position of the next character, if the current one is a CHAR.
     */
    public static boolean isAlphaDigitMinus( String string, int index )
    {
        if ( string == null )
        {
            return false;
        }

        int length = string.length();
        
        if ( ( length == 0 ) || ( index < 0 ) || ( index >= length ) )
        {
            return false;
        }
        else
        {
            char c = string.charAt( index );

            if ( ( c > 127 ) || ( CharConstants.CHAR[c] == false ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    
    // Empty checks
    // -----------------------------------------------------------------------


    /**
     * <p>
     * Checks if a String is not empty ("") and not null.
     * </p>
     * 
     * <pre>
     *  StringUtils.isNotEmpty(null)      = false
     *  StringUtils.isNotEmpty(&quot;&quot;)        = false
     *  StringUtils.isNotEmpty(&quot; &quot;)       = true
     *  StringUtils.isNotEmpty(&quot;bob&quot;)     = true
     *  StringUtils.isNotEmpty(&quot;  bob  &quot;) = true
     * </pre>
     * 
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty( String str )
    {
        return str != null && str.length() > 0;
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from both start and ends of this String,
     * handling <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start and end characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trim(null)          = null
     *  StringUtils.trim(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trim(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trim(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trim(&quot;    abc    &quot;) = &quot;abc&quot;
     * </pre>
     * 
     * @param str the String to be trimmed, may be null
     * @return the trimmed string, <code>null</code> if null String input
     */
    public static String trim( String str )
    {
        return ( Strings.isEmpty(str) ? "" : str.trim() );
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from both start and ends of this bytes
     * array, handling <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start and end characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trim(null)          = null
     *  StringUtils.trim(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trim(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trim(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trim(&quot;    abc    &quot;) = &quot;abc&quot;
     * </pre>
     * 
     * @param bytes the byte array to be trimmed, may be null
     * 
     * @return the trimmed byte array
     */
    public static byte[] trim( byte[] bytes )
    {
        if ( Strings.isEmpty(bytes) )
        {
            return CharConstants.EMPTY_BYTES;
        }

        int start = trimLeft( bytes, 0 );
        int end = trimRight( bytes, bytes.length - 1 );

        int length = end - start + 1;

        if ( length != 0 )
        {
            byte[] newBytes = new byte[end - start + 1];

            System.arraycopy( bytes, start, newBytes, 0, length );

            return newBytes;
        }
        else
        {
            return CharConstants.EMPTY_BYTES;
        }
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from start of this String, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimLeft(null)          = null
     *  StringUtils.trimLeft(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trimLeft(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trimLeft(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trimLeft(&quot;    abc    &quot;) = &quot;abc    &quot;
     * </pre>
     * 
     * @param str the String to be trimmed, may be null
     * @return the trimmed string, <code>null</code> if null String input
     */
    public static String trimLeft( String str )
    {
        if ( Strings.isEmpty(str) )
        {
            return "";
        }

        int start = 0;
        int end = str.length();
        
        while ( ( start < end ) && ( str.charAt( start ) == ' ' ) )
        {
            start++;
        }

        return ( start == 0 ? str : str.substring( start ) );
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from start of this array, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimLeft(null)          = null
     *  StringUtils.trimLeft(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trimLeft(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trimLeft(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trimLeft(&quot;    abc    &quot;) = &quot;abc    &quot;
     * </pre>
     * 
     * @param chars the chars array to be trimmed, may be null
     * @return the position of the first char which is not a space, or the last
     *         position of the array.
     */
    public static int trimLeft( char[] chars, int pos )
    {
        if ( chars == null )
        {
            return pos;
        }

        while ( ( pos < chars.length ) && ( chars[pos] == ' ' ) )
        {
            pos++;
        }

        return pos;
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from a position in this array, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimLeft(null)          = null
     *  StringUtils.trimLeft(&quot;&quot;,...)            = &quot;&quot;
     *  StringUtils.trimLeft(&quot;     &quot;,...)       = &quot;&quot;
     *  StringUtils.trimLeft(&quot;abc&quot;,...)         = &quot;abc&quot;
     *  StringUtils.trimLeft(&quot;    abc    &quot;,...) = &quot;abc    &quot;
     * </pre>
     * 
     * @param string the string to be trimmed, may be null
     * @param pos The starting position
     */
    public static void trimLeft( String string, Position pos )
    {
        if ( string == null )
        {
            return;
        }

        int length = string.length();
        
        while ( ( pos.start < length ) && ( string.charAt( pos.start ) == ' ' ) )
        {
            pos.start++;
        }
        
        pos.end = pos.start;
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from a position in this array, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimLeft(null)          = null
     *  StringUtils.trimLeft(&quot;&quot;,...)            = &quot;&quot;
     *  StringUtils.trimLeft(&quot;     &quot;,...)       = &quot;&quot;
     *  StringUtils.trimLeft(&quot;abc&quot;,...)         = &quot;abc&quot;
     *  StringUtils.trimLeft(&quot;    abc    &quot;,...) = &quot;abc    &quot;
     * </pre>
     * 
     * @param bytes the byte array to be trimmed, may be null
     * @param pos The starting position
     */
    public static void trimLeft( byte[] bytes, Position pos )
    {
        if ( bytes == null )
        {
            return;
        }

        int length = bytes.length;
        
        while ( ( pos.start < length ) && ( bytes[ pos.start ] == ' ' ) )
        {
            pos.start++;
        }
        
        pos.end = pos.start;
    }

    
    /**
     * <p>
     * Removes spaces (char &lt;= 32) from start of this array, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimLeft(null)          = null
     *  StringUtils.trimLeft(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trimLeft(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trimLeft(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trimLeft(&quot;    abc    &quot;) = &quot;abc    &quot;
     * </pre>
     * 
     * @param bytes the byte array to be trimmed, may be null
     * @return the position of the first byte which is not a space, or the last
     *         position of the array.
     */
    public static int trimLeft( byte[] bytes, int pos )
    {
        if ( bytes == null )
        {
            return pos;
        }

        while ( ( pos < bytes.length ) && ( bytes[pos] == ' ' ) )
        {
            pos++;
        }

        return pos;
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from end of this String, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimRight(null)          = null
     *  StringUtils.trimRight(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trimRight(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trimRight(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trimRight(&quot;    abc    &quot;) = &quot;    abc&quot;
     * </pre>
     * 
     * @param str the String to be trimmed, may be null
     * @return the trimmed string, <code>null</code> if null String input
     */
    public static String trimRight( String str )
    {
        if ( Strings.isEmpty(str) )
        {
            return "";
        }

        int length = str.length();
        int end = length;
        
        while ( ( end > 0 ) && ( str.charAt( end - 1 ) == ' ' ) )
        {
            if ( ( end > 1 ) && ( str.charAt(  end - 2 ) == '\\' ) )
            {
                break;
            }
            
            end--;
        }

        return ( end == length ? str : str.substring( 0, end ) );
    }

    /**
     * <p>
     * Removes spaces (char &lt;= 32) from end of this String, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimRight(null)          = null
     *  StringUtils.trimRight(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trimRight(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trimRight(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trimRight(&quot;    abc    &quot;) = &quot;    abc&quot;
     * </pre>
     * 
     * @param str the String to be trimmed, may be null
     * @param escapedSpace The last escaped space, if any
     * @return the trimmed string, <code>null</code> if null String input
     */
    public static String trimRight( String str, int escapedSpace )
    {
        if ( Strings.isEmpty(str) )
        {
            return "";
        }

        int length = str.length();
        int end = length;
        
        while ( ( end > 0 ) && ( str.charAt( end - 1 ) == ' ' ) && ( end > escapedSpace ) )
        {
            if ( ( end > 1 ) && ( str.charAt(  end - 2 ) == '\\' ) )
            {
                break;
            }
            
            end--;
        }

        return ( end == length ? str : str.substring( 0, end ) );
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from end of this array, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimRight(null)          = null
     *  StringUtils.trimRight(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trimRight(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trimRight(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trimRight(&quot;    abc    &quot;) = &quot;    abc&quot;
     * </pre>
     * 
     * @param chars the chars array to be trimmed, may be null
     * @return the position of the first char which is not a space, or the last
     *         position of the array.
     */
    public static int trimRight( char[] chars, int pos )
    {
        if ( chars == null )
        {
            return pos;
        }

        while ( ( pos >= 0 ) && ( chars[pos - 1] == ' ' ) )
        {
            pos--;
        }

        return pos;
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from end of this string, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimRight(null)          = null
     *  StringUtils.trimRight(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trimRight(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trimRight(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trimRight(&quot;    abc    &quot;) = &quot;    abc&quot;
     * </pre>
     * 
     * @param string the string to be trimmed, may be null
     * @return the position of the first char which is not a space, or the last
     *         position of the string.
     */
    public static String trimRight( String string, Position pos )
    {
        if ( string == null )
        {
            return "";
        }

        while ( ( pos.end >= 0 ) && ( string.charAt( pos.end - 1 ) == ' ' ) )
        {
            if ( ( pos.end > 1 ) && ( string.charAt(  pos.end - 2 ) == '\\' ) )
            {
                break;
            }
            
            pos.end--;
        }

        return ( pos.end == string.length() ? string : string.substring( 0, pos.end ) );
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from end of this string, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimRight(null)          = null
     *  StringUtils.trimRight(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trimRight(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trimRight(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trimRight(&quot;    abc    &quot;) = &quot;    abc&quot;
     * </pre>
     * 
     * @param bytes the byte array to be trimmed, may be null
     * @return the position of the first char which is not a space, or the last
     *         position of the byte array.
     */
    public static String trimRight( byte[] bytes, Position pos )
    {
        if ( bytes == null )
        {
            return "";
        }

        while ( ( pos.end >= 0 ) && ( bytes[pos.end - 1] == ' ' ) )
        {
            if ( ( pos.end > 1 ) && ( bytes[pos.end - 2] == '\\' ) )
            {
                break;
            }
            
            pos.end--;
        }

        if ( pos.end == bytes.length )
        {
            return Strings.utf8ToString(bytes);
        }
        else
        {
            return Strings.utf8ToString(bytes, pos.end);
        }
    }


    /**
     * <p>
     * Removes spaces (char &lt;= 32) from end of this array, handling
     * <code>null</code> by returning <code>null</code>.
     * </p>
     * Trim removes start characters &lt;= 32.
     * 
     * <pre>
     *  StringUtils.trimRight(null)          = null
     *  StringUtils.trimRight(&quot;&quot;)            = &quot;&quot;
     *  StringUtils.trimRight(&quot;     &quot;)       = &quot;&quot;
     *  StringUtils.trimRight(&quot;abc&quot;)         = &quot;abc&quot;
     *  StringUtils.trimRight(&quot;    abc    &quot;) = &quot;    abc&quot;
     * </pre>
     * 
     * @param bytes the byte array to be trimmed, may be null
     * @return the position of the first char which is not a space, or the last
     *         position of the array.
     */
    public static int trimRight( byte[] bytes, int pos )
    {
        if ( bytes == null )
        {
            return pos;
        }

        while ( ( pos >= 0 ) && ( bytes[pos] == ' ' ) )
        {
            pos--;
        }

        return pos;
    }


    // Case conversion
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Converts a String to upper case as per {@link String#toUpperCase()}.
     * </p>
     * <p>
     * A <code>null</code> input String returns <code>null</code>.
     * </p>
     * 
     * <pre>
     *  StringUtils.upperCase(null)  = null
     *  StringUtils.upperCase(&quot;&quot;)    = &quot;&quot;
     *  StringUtils.upperCase(&quot;aBc&quot;) = &quot;ABC&quot;
     * </pre>
     * 
     * @param str the String to upper case, may be null
     * @return the upper cased String, <code>null</code> if null String input
     */
    public static String upperCase( String str )
    {
        if ( str == null )
        {
            return null;
        }
        
        return str.toUpperCase();
    }


    /**
     * <p>
     * Converts a String to lower case as per {@link String#toLowerCase()}.
     * </p>
     * <p>
     * A <code>null</code> input String returns <code>null</code>.
     * </p>
     * 
     * <pre>
     *  StringUtils.lowerCase(null)  = null
     *  StringUtils.lowerCase(&quot;&quot;)    = &quot;&quot;
     *  StringUtils.lowerCase(&quot;aBc&quot;) = &quot;abc&quot;
     * </pre>
     * 
     * @param str the String to lower case, may be null
     * @return the lower cased String, <code>null</code> if null String input
     */
    public static String lowerCase( String str )
    {
        if ( str == null )
        {
            return null;
        }
        
        return str.toLowerCase();
    }

    
    /**
     * Rewrote the toLowercase method to improve performances.
     * In Ldap, attributesType are supposed to use ASCII chars :
     * 'a'-'z', 'A'-'Z', '0'-'9', '.' and '-' only. We will take
     * care of any other chars either.
     * 
     * @param str The String to lowercase
     * @return The lowercase string
     */
    public static String lowerCaseAscii( String str )
    {
        if ( str == null )
        {
            return null;
        }
     
        char[] chars = str.toCharArray();
        int pos = 0;
        
        for ( char c:chars )
        {
            chars[pos++] = CharConstants.TO_LOWER_CASE[c];
        }
        
        return new String( chars );
    }

    
    // Equals
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Compares two Strings, returning <code>true</code> if they are equal.
     * </p>
     * <p>
     * <code>null</code>s are handled without exceptions. Two
     * <code>null</code> references are considered to be equal. The comparison
     * is case sensitive.
     * </p>
     * 
     * <pre>
     *  StringUtils.equals(null, null)   = true
     *  StringUtils.equals(null, &quot;abc&quot;)  = false
     *  StringUtils.equals(&quot;abc&quot;, null)  = false
     *  StringUtils.equals(&quot;abc&quot;, &quot;abc&quot;) = true
     *  StringUtils.equals(&quot;abc&quot;, &quot;ABC&quot;) = false
     * </pre>
     * 
     * @see java.lang.String#equals(Object)
     * @param str1 the first String, may be null
     * @param str2 the second String, may be null
     * @return <code>true</code> if the Strings are equal, case sensitive, or
     *         both <code>null</code>
     */
    public static boolean equals( String str1, String str2 )
    {
        return str1 == null ? str2 == null : str1.equals( str2 );
    }


    /**
     * Utility method that return a String representation of a list
     * 
     * @param list The list to transform to a string
     * @return A csv string
     */
    public static String listToString( List<?> list )
    {
        if ( ( list == null ) || ( list.size() == 0 ) )
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for ( Object elem : list )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ", " );
            }

            sb.append( elem );
        }

        return sb.toString();
    }




    /**
     * Utility method that return a String representation of a set
     * 
     * @param set The set to transform to a string
     * @return A csv string
     */
    public static String setToString( Set<?> set )
    {
        if ( ( set == null ) || ( set.size() == 0 ) )
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for ( Object elem : set )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ", " );
            }

            sb.append( elem );
        }

        return sb.toString();
    }


    /**
     * Utility method that return a String representation of a list
     * 
     * @param list The list to transform to a string
     * @param tabs The tabs to add in ffront of the elements
     * @return A csv string
     */
    public static String listToString( List<?> list, String tabs )
    {
        if ( ( list == null ) || ( list.size() == 0 ) )
        {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        for ( Object elem : list )
        {
            sb.append( tabs );
            sb.append( elem );
            sb.append( '\n' );
        }

        return sb.toString();
    }


    /**
     * Utility method that return a String representation of a map. The elements
     * will be represented as "key = value"
     * 
     * @param map The map to transform to a string
     * @return A csv string
     */
    public static String mapToString( Map<?,?> map )
    {
        if ( ( map == null ) || ( map.size() == 0 ) )
        {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;

        for ( Map.Entry<?, ?> entry:map.entrySet() )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ", " );
            }

            sb.append( entry.getKey() );
            sb.append( " = '" ).append( entry.getValue() ).append( "'" );
        }

        return sb.toString();
    }


    /**
     * Utility method that return a String representation of a map. The elements
     * will be represented as "key = value"
     * 
     * @param map The map to transform to a string
     * @param tabs The tabs to add in ffront of the elements
     * @return A csv string
     */
    public static String mapToString( Map<?,?> map, String tabs )
    {
        if ( ( map == null ) || ( map.size() == 0 ) )
        {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        for ( Map.Entry<?, ?> entry:map.entrySet() )
        {
            sb.append( tabs );
            sb.append( entry.getKey() );

            sb.append( " = '" ).append( entry.getValue().toString() ).append( "'\n" );
        }

        return sb.toString();
    }

    
    /**
     * Get the default charset
     * 
     * @return The default charset
     */
    public static String getDefaultCharsetName()
    {
        if ( null == defaultCharset ) 
        {
            try 
            {
                // Try with jdk 1.5 method, if we are using a 1.5 jdk :)
                Method method = Charset.class.getMethod( "defaultCharset", new Class[0] );
                defaultCharset = ((Charset) method.invoke( null, new Object[0]) ).name();
            } 
            catch (NoSuchMethodException e) 
            {
                // fall back to old method
                defaultCharset = new OutputStreamWriter( new ByteArrayOutputStream() ).getEncoding();
            }
            catch (InvocationTargetException e) 
            {
                // fall back to old method
                defaultCharset = new OutputStreamWriter( new ByteArrayOutputStream() ).getEncoding();
            }
            catch (IllegalAccessException e) 
            {
                // fall back to old method
                defaultCharset = new OutputStreamWriter( new ByteArrayOutputStream() ).getEncoding();
            }
        }

        return defaultCharset;
    }
    
    
    /**
     * Decodes values of attributes in the DN encoded in hex into a UTF-8 
     * String.  RFC2253 allows a DN's attribute to be encoded in hex.
     * The encoded value starts with a # then is followed by an even 
     * number of hex characters.  
     * 
     * @param str the string to decode
     * @return the decoded string
     */
    public static String decodeHexString( String str ) throws InvalidNameException
    {
        if ( str == null || str.length() == 0 )
        {
            throw new InvalidNameException( I18n.err( I18n.ERR_04431 ) );
        }
        
        char[] chars = str.toCharArray();
        
        if ( chars[0] != '#' )
        {
            throw new InvalidNameException( I18n.err( I18n.ERR_04432, str ) );
        }
        
        // the bytes representing the encoded string of hex
        // this should be ( length - 1 )/2 in size
        byte[] decoded = new byte[ ( chars.length - 1 ) >> 1 ];

        for ( int ii = 1, jj = 0 ; ii < chars.length; ii+=2, jj++ )
        {
            int ch = ( CharConstants.HEX_VALUE[chars[ii]] << 4 )
                 + CharConstants.HEX_VALUE[chars[ii + 1]];
            decoded[jj] = ( byte ) ch;
        }
        
        return Strings.utf8ToString(decoded);
    }


    /**
     * Convert an escaoed list of bytes to a byte[]
     * 
     * @param str the string containing hex escapes
     * @return the converted byte[]
     */
    public static byte[] convertEscapedHex( String str ) throws InvalidNameException
    {
        if ( str == null )
        {
            throw new InvalidNameException( I18n.err( I18n.ERR_04433 ) );
        }
        
        int length = str.length();
        
        if ( length == 0 )
        {
            throw new InvalidNameException( I18n.err( I18n.ERR_04434 ) );
        }
        
        // create buffer and add everything before start of scan
        byte[] buf = new byte[ str.length()/3];
        int pos = 0;
        
        // start scaning until we find an escaped series of bytes
        for ( int i = 0; i < length; i++ )
        {
            char c = str.charAt( i );
            
            if ( c == '\\' )
            {
                // we have the start of a hex escape sequence
                if ( isHex( str, i+1 ) && isHex ( str, i+2 ) )
                {
                    byte value = ( byte ) ( ( CharConstants.HEX_VALUE[str.charAt( i + 1 )] << 4 )
                        + CharConstants.HEX_VALUE[str.charAt( i + 2 )] );
                    
                    i+=2;
                    buf[pos++] = value;
                }
            }
            else
            {
                throw new InvalidNameException( I18n.err( I18n.ERR_04435 ) );
            }
        }

        return buf;
    }


    /**
     * Thansform an array of ASCII bytes to a string. the byte array should contains
     * only values in [0, 127].
     * 
     * @param bytes The byte array to transform
     * @return The resulting string
     */
    public static String asciiBytesToString( byte[] bytes )
    {
        if ( (bytes == null) || (bytes.length == 0 ) )
        {
            return "";
        }
        
        char[] result = new char[bytes.length];
        
        for ( int i = 0; i < bytes.length; i++ )
        {
            result[i] = (char)bytes[i];
        }
        
        return new String( result );
    }
    
    
    /**
     * Build an AttributeType froma byte array. An AttributeType contains
     * only chars within [0-9][a-z][A-Z][-.].
     *  
     * @param bytes The bytes containing the AttributeType
     * @return The AttributeType as a String
     */
    public static String getType( byte[] bytes)
    {
        if ( bytes == null )
        {
            return null;
        }
        
        char[] chars = new char[bytes.length];
        int pos = 0;
        
        for ( byte b:bytes )
        {
            chars[pos++] = (char)b;
        }
        
        return new String( chars );
    }
    
    
    /**
     * 
     * Check that a String is a valid IA5String. An IA5String contains only
     * char which values is between [0, 7F]
     *
     * @param str The String to check
     * @return <code>true</code> if the string is an IA5String or is empty, 
     * <code>false</code> otherwise
     */
    public static boolean isIA5String( String str )
    {
        if ( ( str == null ) || ( str.length() == 0 ) )
        {
            return true;
        }
        
        // All the chars must be in [0x00, 0x7F]
        for ( char c:str.toCharArray() )
        {
            if ( ( c < 0 ) || ( c > 0x7F ) )
            {
                return false;
            }
        }

        return true;
    }
    
    
    /**
     * 
     * Check that a String is a valid PrintableString. A PrintableString contains only
     * the following set of chars :
     * { ' ', ''', '(', ')', '+', '-', '.', '/', [0-9], ':', '=', '?', [A-Z], [a-z]}
     *
     * @param str The String to check
     * @return <code>true</code> if the string is a PrintableString or is empty, 
     * <code>false</code> otherwise
     */
    public static boolean isPrintableString( String str )
    {
        if ( ( str == null ) || ( str.length() == 0 ) )
        {
            return true;
        }
        
        for ( char c:str.toCharArray() )
        {
            if ( ( c > 127 ) || !CharConstants.IS_PRINTABLE_CHAR[ c ] )
            {
                return false;
            }
        }
    
        return true;
    }

    
    /**
     * Check if the current char is in the unicodeSubset : all chars but
     * '\0', '(', ')', '*' and '\'
     *
     * @param str The string to check
     * @param pos Position of the current char
     * @return True if the current char is in the unicode subset
     */
    public static boolean isUnicodeSubset( String str, int pos )
    {
        if ( ( str == null ) || ( str.length() <= pos ) || ( pos < 0 ) ) 
        {
            return false;
        }
        
        char c = str.charAt( pos );
        
        return ( ( c > 127 ) || CharConstants.UNICODE_SUBSET[c] );
    }


    /**
     * converts the bytes of a UUID to string
     *  
     * @param bytes bytes of a UUID
     * @return UUID in string format
     */
    public static String uuidToString( byte[] bytes )
    {
        if ( bytes == null || bytes.length != 16 )
        {
            return "Invalid UUID";
        }

        char[] hex = Hex.encodeHex(bytes);
        StringBuffer sb = new StringBuffer();
        sb.append( hex, 0, 8 );
        sb.append( '-' );
        sb.append( hex, 8, 4 );
        sb.append( '-' );
        sb.append( hex, 12, 4 );
        sb.append( '-' );
        sb.append( hex, 16, 4 );
        sb.append( '-' );
        sb.append( hex, 20, 12 );

        return sb.toString().toLowerCase();
    }


    /**
     * converts the string representation of an UUID to bytes
     *  
     * @param string the string representation of an UUID
     * @return the bytes, null if the the syntax is not valid
     */
    public static byte[] uuidToBytes( String string )
    {
        if ( !new UuidSyntaxChecker().isValidSyntax( string ) )
        {
            return null;
        }

        char[] chars = string.toCharArray();
        byte[] bytes = new byte[16];
        bytes[0] = getHexValue( chars[0], chars[1] );
        bytes[1] = getHexValue( chars[2], chars[3] );
        bytes[2] = getHexValue( chars[4], chars[5] );
        bytes[3] = getHexValue( chars[6], chars[7] );

        bytes[4] = getHexValue( chars[9], chars[10] );
        bytes[5] = getHexValue( chars[11], chars[12] );

        bytes[6] = getHexValue( chars[14], chars[15] );
        bytes[7] = getHexValue( chars[16], chars[17] );

        bytes[8] = getHexValue( chars[19], chars[20] );
        bytes[9] = getHexValue( chars[21], chars[22] );

        bytes[10] = getHexValue( chars[24], chars[25] );
        bytes[11] = getHexValue( chars[26], chars[27] );
        bytes[12] = getHexValue( chars[28], chars[29] );
        bytes[13] = getHexValue( chars[30], chars[31] );
        bytes[14] = getHexValue( chars[32], chars[33] );
        bytes[15] = getHexValue( chars[34], chars[35] );

        return bytes;
    }

}
