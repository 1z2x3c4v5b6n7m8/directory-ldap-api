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
package org.apache.directory.shared.ldap.name;


import org.apache.directory.junit.tools.Concurrent;
import org.apache.directory.junit.tools.ConcurrentJunitRunner;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;


/**
 * Test the class Dn
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrent()
public class DnParserTest
{
    // ~ Methods
    // ------------------------------------------------------------------------------------

    // CONSTRUCTOR functions --------------------------------------------------

    /**
     * test an empty Dn
     */
    @Test
    public void testLdapDNEmpty() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();

        assertEquals( "", ( (Dn) dnParser.parse( "" ) ).getName() );
    }


    /**
     * test a simple Dn : a = b
     */
    @Test
    public void testLdapDNSimple() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();

        assertEquals( "a = b", ( (Dn) dnParser.parse( "a = b" ) ).getName() );
        assertEquals( "a=b", ( (Dn) dnParser.parse( "a = b" ) ).getNormName() );
    }


    /**
     * test a composite Dn : a = b, d = e
     */
    @Test
    public void testLdapDNComposite() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "a = b, c = d" );
        assertEquals( "a=b,c=d", dn.getNormName() );
        assertEquals( "a = b, c = d", dn.getName() );
    }


    /**
     * test a composite Dn with or without spaces: a=b, a =b, a= b, a = b, a = b
     */
    @Test
    public void testLdapDNCompositeWithSpace() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "a=b, a =b, a= b, a = b, a  =  b" );
        assertEquals( "a=b,a=b,a=b,a=b,a=b", dn.getNormName() );
        assertEquals( "a=b, a =b, a= b, a = b, a  =  b", dn.getName() );
    }


    /**
     * test a composite Dn with differents separators : a=b;c=d,e=f It should
     * return a=b,c=d,e=f (the ';' is replaced by a ',')
     */
    @Test
    public void testLdapDNCompositeSepators() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "a=b;c=d,e=f" );
        assertEquals( "a=b,c=d,e=f", dn.getNormName() );
        assertEquals( "a=b;c=d,e=f", dn.getName() );
    }


    /**
     * test a simple Dn with multiple NameComponents : a = b + c = d
     */
    @Test
    public void testLdapDNSimpleMultivaluedAttribute() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "a = b + c = d" );
        assertEquals( "a=b+c=d", dn.getNormName() );
        assertEquals( "a = b + c = d", dn.getName() );
    }


    /**
     * test a composite Dn with multiple NC and separators : a=b+c=d, e=f + g=h +
     * i=j
     */
    @Test
    public void testLdapDNCompositeMultivaluedAttribute() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "a=b+c=d, e=f + g=h + i=j" );
        assertEquals( "a=b+c=d,e=f+g=h+i=j", dn.getNormName() );
        assertEquals( "a=b+c=d, e=f + g=h + i=j", dn.getName() );
    }


    /**
     * test a simple Dn with an oid prefix (uppercase) : OID.12.34.56 = azerty
     */
    @Test
    public void testLdapDNOidUpper() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "OID.12.34.56 = azerty" );
        assertEquals( "oid.12.34.56=azerty", dn.getNormName() );
        assertEquals( "OID.12.34.56 = azerty", dn.getName() );
    }


    /**
     * test a simple Dn with an oid prefix (lowercase) : oid.12.34.56 = azerty
     */
    @Test
    public void testLdapDNOidLower() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "oid.12.34.56 = azerty" );
        assertEquals( "oid.12.34.56=azerty", dn.getNormName() );
        assertEquals( "oid.12.34.56 = azerty", dn.getName() );
    }


    /**
     * test a simple Dn with an oid attribut without oid prefix : 12.34.56 =
     * azerty
     */
    @Test
    public void testLdapDNOidWithoutPrefix() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "12.34.56 = azerty" );
        assertEquals( "12.34.56=azerty", dn.getNormName() );
        assertEquals( "12.34.56 = azerty", dn.getName() );
    }


    /**
     * test a composite Dn with an oid attribut wiithout oid prefix : 12.34.56 =
     * azerty; 7.8 = test
     */
    @Test
    public void testLdapDNCompositeOidWithoutPrefix() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "12.34.56 = azerty; 7.8 = test" );
        assertEquals( "12.34.56=azerty,7.8=test", dn.getNormName() );
        assertEquals( "12.34.56 = azerty; 7.8 = test", dn.getName() );
    }


    /**
     * test a simple Dn with pair char attribute value : a = \,\=\+\<\>\#\;\\\"\C3\A9"
     */
    @Test
    public void testLdapDNPairCharAttributeValue() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "a = \\,\\=\\+\\<\\>\\#\\;\\\\\\\"\\C3\\A9" );
        assertEquals( "a=\\,=\\+\\<\\>#\\;\\\\\\\"\u00e9", dn.getNormName() );
        assertEquals( "a = \\,\\=\\+\\<\\>\\#\\;\\\\\\\"\\C3\\A9", dn.getName() );

        dn = (Dn) dnParser.parse( "a = \\,\\=\\+\\<\\>\\#\\;\\\\\\\"\u00e9" );
        assertEquals( "a=\\,=\\+\\<\\>#\\;\\\\\\\"\u00e9", dn.getNormName() );
        assertEquals( "a = \\,\\=\\+\\<\\>\\#\\;\\\\\\\"\u00e9", dn.getName() );
    }


    /**
     * test a simple Dn with hexString attribute value : a = #0010A0AAFF
     */
    @Test
    public void testLdapDNHexStringAttributeValue() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "a = #0010A0AAFF" );
        assertEquals( "a=#0010A0AAFF", dn.getNormName() );
        assertEquals( "a = #0010A0AAFF", dn.getName() );
    }


    /**
     * test exception from illegal hexString attribute value : a=#zz.
     */
    @Test
    public void testBadLdapDNHexStringAttributeValue() throws LdapException
    {
        try
        {
            DnParser dnParser = DnParser.getNameParser();
            dnParser.parse( "a=#zz" );
            fail();
        }
        catch ( LdapInvalidDnException ine )
        {
            assertTrue( true );
        }
    }
    
    
    /**
     * test a simple Dn with quoted attribute value : a = "quoted \"value"
     */
    @Test
    public void testLdapDNQuotedAttributeValue() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "a = quoted \\\"value" );
        assertEquals( "a=quoted \\\"value", dn.getNormName() );
        assertEquals( "a = quoted \\\"value", dn.getName() );
        
        dn = (Dn) dnParser.parse( "cn=Mackie \\\"The Knife\\\" Messer" );
        assertEquals( "cn=Mackie \\\"The Knife\\\" Messer", dn.getNormName() );
        assertEquals( "cn=Mackie \\\"The Knife\\\" Messer", dn.getName() );
    }


    /**
     * Tests a corner case of the parser because the sequence "\DC" is also a valid hex pair
     */
    @Test
    public void testLdapDNBackslashInAttributeValue() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "a = AC\\\\DC" );
        assertEquals( "a=AC\\\\DC", dn.getNormName() );
        assertEquals( "a = AC\\\\DC", dn.getName() );
    }


    /**
     * Test the encoding of a LdanDN
     */
    @Test
    public void testNameToBytes() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn dn = (Dn) dnParser.parse( "cn = John, ou = People, OU = Marketing" );

        byte[] bytes = Dn.getBytes(dn);

        assertEquals( 30, bytes.length );
        assertEquals( "cn=John,ou=People,ou=Marketing", Strings.utf8ToString(bytes) );
    }


    @Test
    public void testStringParser() throws LdapException
    {
        String dn = Strings.utf8ToString(new byte[]
                {'C', 'N', ' ', '=', ' ', 'E', 'm', 'm', 'a', 'n', 'u', 'e', 'l', ' ', ' ', 'L', (byte) 0xc3,
                        (byte) 0xa9, 'c', 'h', 'a', 'r', 'n', 'y'});

        DnParser dnParser = DnParser.getNameParser();
        Dn name = (Dn) dnParser.parse( dn );

        assertEquals( dn, name.getName() );
        assertEquals( "cn=Emmanuel  L\u00e9charny", name.getNormName() );
    }


    @Test
    public void testStringParserShort() throws LdapException
    {
        String dn = Strings.utf8ToString(new byte[]
                {'C', '=', ' ', 'E', (byte) 0xc3, (byte) 0xa9, 'c'});

        DnParser dnParser = DnParser.getNameParser();
        Dn name = (Dn) dnParser.parse( dn );

        assertEquals( dn, name.getName() );
        assertEquals( "c=E\u00e9c", name.getNormName() );
    }


    @Test
    public void testVsldapExtras() throws LdapException
    {
        DnParser dnParser = DnParser.getNameParser();
        Dn name = (Dn) dnParser
            .parse( "cn=Billy Bakers, OID.2.5.4.11=Corporate Tax, ou=Fin-Accounting, ou=Americas, ou=Search, o=IMC, c=US" );

        assertEquals(
            "cn=Billy Bakers, OID.2.5.4.11=Corporate Tax, ou=Fin-Accounting, ou=Americas, ou=Search, o=IMC, c=US", name
                .getName() );
        assertEquals(
            "cn=Billy Bakers,oid.2.5.4.11=Corporate Tax,ou=Fin-Accounting,ou=Americas,ou=Search,o=IMC,c=US", name
                .getNormName() );
    }


    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * Class under test for void DnParser()
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testDnParser()
    {
        DnParser parser = DnParser.getNameParser();

        assertNotNull( parser );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseStringEmpty() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();

        Dn nameEmpty = parser.parse( "" );

        assertNotNull( nameEmpty );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseStringNull() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();

        Dn nameNull = parser.parse( null );

        assertEquals( "Null Dn are legal : ", "", nameNull.toString() );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseStringRFC1779_1() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();

        Dn nameRFC1779_1 = parser
            .parse( "CN=Marshall T. Rose, O=Dover Beach Consulting, L=Santa Clara, ST=California, C=US" );

        assertEquals( "RFC1779_1 : ",
            "CN=Marshall T. Rose, O=Dover Beach Consulting, L=Santa Clara, ST=California, C=US",
            ( (Dn) nameRFC1779_1 ).getName() );
        assertEquals( "RFC1779_1 : ", "cn=Marshall T. Rose,o=Dover Beach Consulting,l=Santa Clara,st=California,c=US",
            nameRFC1779_1.toString() );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseStringRFC2253_1() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();

        Dn nameRFC2253_1 = parser.parse( "CN=Steve Kille,O=Isode limited,C=GB" );

        assertEquals( "RFC2253_1 : ", "CN=Steve Kille,O=Isode limited,C=GB", ( (Dn) nameRFC2253_1 ).getName() );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseStringRFC2253_2() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();

        Dn nameRFC2253_2 = parser.parse( "CN = Sales + CN =   J. Smith , O = Widget Inc. , C = US" );

        assertEquals( "RFC2253_2 : ", "CN = Sales + CN =   J. Smith , O = Widget Inc. , C = US",
            ( (Dn) nameRFC2253_2 ).getName() );
        assertEquals( "RFC2253_2 : ", "cn=J. Smith+cn=Sales,o=Widget Inc.,c=US", nameRFC2253_2.toString() );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseStringRFC2253_3() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();

        Dn nameRFC2253_3 = parser.parse( "CN=L. Eagle,   O=Sue\\, Grabbit and Runn, C=GB" );

        assertEquals( "RFC2253_3 : ", "CN=L. Eagle,   O=Sue\\, Grabbit and Runn, C=GB", ( (Dn) nameRFC2253_3 )
            .getName() );
        assertEquals( "RFC2253_3 : ", "cn=L. Eagle,o=Sue\\, Grabbit and Runn,c=GB", nameRFC2253_3.toString() );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseStringRFC2253_4() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();

        Dn nameRFC2253_4 = parser.parse( "CN=Before\\0DAfter,O=Test,C=GB" );
        assertEquals( "RFC2253_4 : ", "CN=Before\\0DAfter,O=Test,C=GB", ( (Dn) nameRFC2253_4 ).getName() );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseStringRFC2253_5() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();

        Dn nameRFC2253_5 = parser.parse( "1.3.6.1.4.1.1466.0=#04024869,O=Test,C=GB" );

        assertEquals( "RFC2253_5 : ", "1.3.6.1.4.1.1466.0=#04024869,O=Test,C=GB", ( (Dn) nameRFC2253_5 )
            .getName() );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseStringRFC2253_6() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();

        Dn nameRFC2253_6 = parser.parse( "SN=Lu\\C4\\8Di\\C4\\87" );

        assertEquals( "RFC2253_6 : ", "SN=Lu\\C4\\8Di\\C4\\87", ( (Dn) nameRFC2253_6 ).getName() );
    }


    /**
     * Class under test for Name parse(String)
     *
     * @throws LdapException
     *             if anything goes wrong
     */
    public final void testParseInvalidString()
    {
        DnParser parser = DnParser.getNameParser();

        try
        {
            parser.parse( "&#347;=&#347;rasulu,dc=example,dc=com" );
            fail( "the invalid name should never succeed in a parse" );
        }
        catch ( Exception e )
        {
            assertNotNull( e );
        }
    }


    /**
     * Tests to see if inner whitespace is preserved after an escaped ',' in a
     * value of a name component. This test was added to try to reproduce the
     * bug encountered in DIREVE-179 <a
     * href="http://issues.apache.org/jira/browse/DIREVE-179"> here</a>.
     *
     * @throws LdapException
     *             if anything goes wrong on parse()
     */
    public final void testPreserveSpaceAfterEscape() throws LdapException
    {
        DnParser parser = DnParser.getNameParser();
        String input = "ou=some test\\,  something else";
        String result = parser.parse( input ).toString();
        assertEquals( "ou=some test\\,  something else", result );
    }


    @Test
    public void testWindowsFilePath() throws Exception
    {
        // '\' should be escaped as stated in RFC 2253
        String path = "windowsFilePath=C:\\\\cygwin";
        DnParser parser = DnParser.getNameParser();
        Dn result = parser.parse( path );
        assertEquals( path, ( (Dn) result ).getName() );
        assertEquals( "windowsfilepath=C:\\\\cygwin", ((Dn)result).getNormName() );
    }


    @Test
    public void testNameFrenchChars() throws Exception
    {
        String cn = new String( new byte[]
            { 'c', 'n', '=', 0x4A, ( byte ) 0xC3, ( byte ) 0xA9, 0x72, ( byte ) 0xC3, ( byte ) 0xB4, 0x6D, 0x65 },
            "UTF-8" );

        DnParser parser = DnParser.getNameParser();
        String result = parser.parse( cn ).toString();

        assertEquals( "cn=J\u00e9r\u00f4me", result.toString() );
    }


    @Test
    public void testNameGermanChars() throws Exception
    {
        String cn = new String( new byte[]
            { 'c', 'n', '=', ( byte ) 0xC3, ( byte ) 0x84, ( byte ) 0xC3, ( byte ) 0x96, ( byte ) 0xC3, ( byte ) 0x9C,
                ( byte ) 0xC3, ( byte ) 0x9F, ( byte ) 0xC3, ( byte ) 0xA4, ( byte ) 0xC3, ( byte ) 0xB6,
                ( byte ) 0xC3, ( byte ) 0xBC }, "UTF-8" );

        DnParser parser = DnParser.getNameParser();
        String result = parser.parse( cn ).toString();

        assertEquals( "cn=\u00C4\u00D6\u00DC\u00DF\u00E4\u00F6\u00FC", result.toString() );
    }


    @Test
    public void testNameTurkishChars() throws Exception
    {
        String cn = new String( new byte[]
            { 'c', 'n', '=', ( byte ) 0xC4, ( byte ) 0xB0, ( byte ) 0xC4, ( byte ) 0xB1, ( byte ) 0xC5, ( byte ) 0x9E,
                ( byte ) 0xC5, ( byte ) 0x9F, ( byte ) 0xC3, ( byte ) 0x96, ( byte ) 0xC3, ( byte ) 0xB6,
                ( byte ) 0xC3, ( byte ) 0x9C, ( byte ) 0xC3, ( byte ) 0xBC, ( byte ) 0xC4, ( byte ) 0x9E,
                ( byte ) 0xC4, ( byte ) 0x9F }, "UTF-8" );

        DnParser parser = DnParser.getNameParser();
        String result = parser.parse( cn ).toString();

        assertEquals( "cn=\u0130\u0131\u015E\u015F\u00D6\u00F6\u00DC\u00FC\u011E\u011F", result.toString() );
    }
    
   
    @Test
    public void testAUmlautPlusBytes() throws Exception
    {
        String cn = new String( new byte[] { 'c', 'n', '=', (byte)0xC3, (byte)0x84, 0x5C, 0x32, 0x42 }, "UTF-8" );


        DnParser parser = DnParser.getNameParser();
        String result = ((Dn)parser.parse( cn )).getNormName();
        
        assertEquals( "cn=\u00c4\\+", result );
    }


    @Test
    public void testAUmlautPlusChar() throws Exception
    {
        String cn = new String( new byte[] { 'c', 'n', '=', (byte)0xC3, (byte)0x84, '\\', '+' }, "UTF-8" );
        
        DnParser parser = DnParser.getNameParser();
        String result = parser.parse( cn ).toString();
        
        assertEquals( "cn=\u00c4\\+", result );
    }


    /**
     * Test to check that even with a non escaped char, the Dn is parsed ok
     * or at least an error is generated.
     *
     * @throws LdapException
     *             if anything goes wrong on parse()
     */
    public final void testNonEscapedChars()
    {
        DnParser parser = DnParser.getNameParser();
        String input = "ou=ou+test";

        try
        {
            parser.parse( input ).toString();
            fail( "Should never rech this point" );
        }
        catch ( LdapException ne )
        {
            assertTrue( true );
            return;
        }
    }
}
