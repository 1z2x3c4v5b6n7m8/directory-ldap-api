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
package org.apache.directory.shared.dsmlv2;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.directory.shared.dsmlv2.request.BatchRequestDsml;
import org.apache.directory.shared.dsmlv2.request.BatchRequestDsml.Processing;
import org.apache.directory.shared.dsmlv2.request.BatchRequestDsml.ResponseOrder;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.codec.api.CodecControl;
import org.apache.directory.shared.ldap.codec.api.LdapApiService;
import org.apache.directory.shared.ldap.model.entry.BinaryValue;
import org.apache.directory.shared.ldap.model.entry.StringValue;
import org.apache.directory.shared.ldap.model.ldif.LdifUtils;
import org.apache.directory.shared.ldap.model.message.Control;
import org.apache.directory.shared.util.Base64;
import org.apache.directory.shared.util.Strings;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * This class is a Helper class for the DSML Parser
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ParserUtils
{
    /** W3C XML Schema URI. */
    public static final String XML_SCHEMA_URI = "http://www.w3c.org/2001/XMLSchema";

    /** W3C XML Schema Instance URI. */
    public static final String XML_SCHEMA_INSTANCE_URI = "http://www.w3c.org/2001/XMLSchema-instance";

    /** Base-64 identifier. */
    public static final String BASE64BINARY = "base64Binary";

    /** XSI namespace prefix. */
    public static final String XSI = "xsi";

    /** XSD namespace prefix. */
    public static final String XSD = "xsd";

    /** The DSML namespace */
    public static final Namespace DSML_NAMESPACE = new Namespace( null, "urn:oasis:names:tc:DSML:2:0:core" );

    /** The XSD namespace */
    public static final Namespace XSD_NAMESPACE = new Namespace( XSD, XML_SCHEMA_URI );

    /** The XSI namespace */
    public static final Namespace XSI_NAMESPACE = new Namespace( XSI, XML_SCHEMA_INSTANCE_URI );


    /**
     * Returns the value of the attribute 'type' of the "XMLSchema-instance' namespace if it exists
     *
     * @param xpp the XPP parser to use
     * @return the value of the attribute 'type' of the "XMLSchema-instance' namespace if it exists
     */
    public static String getXsiTypeAttributeValue( XmlPullParser xpp )
    {
        String type = null;
        int nbAttributes = xpp.getAttributeCount();
        
        for ( int i = 0; i < nbAttributes; i++ )
        {
            // Checking if the attribute 'type' from XML Schema Instance namespace is used.
            if ( xpp.getAttributeName( i ).equals( "type" )
                && xpp.getNamespace( xpp.getAttributePrefix( i ) ).equals( XML_SCHEMA_INSTANCE_URI ) )
            {
                type = xpp.getAttributeValue( i );
                break;
            }
        }
        
        return type;
    }


    /**
     * Tells is the given value is a Base64 binary value
     * 
     * @param parser the XPP parser to use
     * @param attrValue the attribute value
     * @return true if the value of the current tag is Base64BinaryEncoded, false if not
     */
    public static boolean isBase64BinaryValue( XmlPullParser parser, String attrValue )
    {
        if ( attrValue == null )
        {
            return false;
        }
        
        // We are looking for something that should look like that: "aNameSpace:base64Binary"
        // We split the String. The first element should be the namespace prefix and the second "base64Binary"
        String[] splitedString = attrValue.split( ":" );
        
        return ( splitedString.length == 2 ) && ( XML_SCHEMA_URI.equals( parser.getNamespace( splitedString[0] ) ) )
            && ( BASE64BINARY.equals( splitedString[1] ) );
    }


    /**
     * Indicates if the value needs to be encoded as Base64
     *
     * @param value the value to check
     * @return true if the value needs to be encoded as Base64
     */
    public static boolean needsBase64Encoding( Object value )
    {
        if( value instanceof StringValue )
        {
            return false;
        }
        else if( value instanceof BinaryValue )
        {
            return false;
        } 
        else if ( value instanceof byte[] )
        {
            return true;
        }
        else if ( value instanceof String )
        {
            return !LdifUtils.isLDIFSafe( ( String ) value );
        }
        
        return true;
    }


    /**
     * Encodes the value as a Base64 String
     *
     * @param value the value to encode
     * @return the value encoded as a Base64 String
     */
    public static String base64Encode( Object value )
    {
        if ( value instanceof byte[] )
        {
            return new String( Base64.encode( ( byte[] ) value ) );
        }
        else if ( value instanceof String )
        {
            return new String( Base64.encode( Strings.getBytesUtf8( ( String ) value ) ) );
        }

        return "";
    }


    /**
     * Parses and verify the parsed value of the requestID
     * 
     * @param attributeValue the value of the attribute
     * @param xpp the XmlPullParser
     * @return the int value of the resquestID
     * @throws XmlPullParserException if RequestID isn't an Integer and if requestID is below 0
     */
    public static int parseAndVerifyRequestID( String attributeValue, XmlPullParser xpp ) throws XmlPullParserException
    {
        try
        {
            int requestID = Integer.parseInt( attributeValue );

            if ( requestID < 0 )
            {
                throw new XmlPullParserException( I18n.err( I18n.ERR_03038, requestID ), xpp, null );
            }

            return requestID;
        }
        catch ( NumberFormatException e )
        {
            throw new XmlPullParserException( I18n.err( I18n.ERR_03039 ), xpp, null );
        }
    }


    /**
     * Adds Controls to the given Element.
     *
     * @param element the element to add the Controls to
     * @param controls a List of Controls
     */
    public static void addControls( LdapApiService codec, Element element, Collection<Control> controls )
    {
        if ( controls != null )
        {
            for ( Control control : controls )
            {
                Element controlElement = element.addElement( "control" );

                if ( control.getOid() != null )
                {
                    controlElement.addAttribute( "type", control.getOid() );
                }

                if ( control.isCritical() )
                {
                    controlElement.addAttribute( "criticality", "true" );
                }

                byte[] value;
                
                if ( control instanceof CodecControl<?> )
                {
                    value = ( ( org.apache.directory.shared.ldap.codec.api.CodecControl<?> ) control ).getValue();
                }
                else
                {
                    value = codec.newControl( control ).getValue();
                }

                if ( value != null )
                {
                    if ( ParserUtils.needsBase64Encoding( value ) )
                    {
                        element.getDocument().getRootElement().add( XSD_NAMESPACE );
                        element.getDocument().getRootElement().add( XSI_NAMESPACE );

                        Element valueElement = controlElement.addElement( "controlValue" ).addText(
                            ParserUtils.base64Encode( value ) );
                        valueElement.addAttribute( new QName( "type", XSI_NAMESPACE ), ParserUtils.XSD + ":"
                            + ParserUtils.BASE64BINARY );
                    }
                    else
                    {
                        controlElement.addElement( "controlValue" ).setText( Arrays.toString( value ) );
                    }
                }
            }
        }
    }


    /**
     * Indicates if a request ID is needed.
     *
     * @param container the associated container
     * @return true if a request ID is needed (ie Processing=Parallel and ResponseOrder=Unordered)
     * @throws XmlPullParserException if the batch request has not been parsed yet
     */
    public static boolean isRequestIdNeeded( Dsmlv2Container container ) throws XmlPullParserException
    {
        BatchRequestDsml batchRequest = container.getBatchRequest();

        if ( batchRequest == null )
        {
            throw new XmlPullParserException( I18n.err( I18n.ERR_03040 ), container.getParser(), null );
        }

        return ( ( batchRequest.getProcessing() == Processing.PARALLEL ) && ( batchRequest.getResponseOrder() == ResponseOrder.UNORDERED ) );
    }


    /**
     * XML Pretty Printer XSLT Transformation
     * 
     * @param document the Dom4j Document
     * @return the transformed document
     */
    public static Document styleDocument( Document document )
    {
        // load the transformer using JAXP
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = null;
        
        try
        {
            transformer = factory.newTransformer( new StreamSource( ParserUtils.class
                .getResourceAsStream( "DSMLv2.xslt" ) ) );
        }
        catch ( TransformerConfigurationException e1 )
        {
            e1.printStackTrace();
            // return original document
            return document;
        }

        // now lets style the given document
        DocumentSource source = new DocumentSource( document );
        DocumentResult result = new DocumentResult();
        
        try
        {
            transformer.transform( source, result );
        }
        catch ( TransformerException e )
        {
            // return original document
            return document;
        }

        // return the transformed document
        Document transformedDoc = result.getDocument();
        return transformedDoc;
    }
    
    
    /**
     * GrammarAction that reads the SOAP header data
     */
    public static final GrammarAction readSoapHeader = new GrammarAction( "Reads SOAP header" )
    {
        public void action( Dsmlv2Container container ) throws XmlPullParserException
        {
            try
            {
                XmlPullParser xpp = container.getParser();
                StringBuilder sb = new StringBuilder();

                String startTag = xpp.getText();
                sb.append( startTag );

                // string '<' and '>'
                startTag = startTag.substring( 1, startTag.length() - 1 );

                int tagType = -1;
                String endTag = "";

                // continue parsing till we get to the end tag of SOAP header
                // and match the tag values including the namespace
                while ( !startTag.equals( endTag ) )
                {
                    tagType = xpp.next();
                    endTag = xpp.getText();
                    sb.append( endTag );

                    if ( tagType == XmlPullParser.END_TAG )
                    {
                        // strip '<', '/' and '>'
                        endTag = endTag.substring( 2, endTag.length() - 1 );
                    }
                }

                // change the state to header end
                container.setState( Dsmlv2StatesEnum.SOAP_HEADER_END_TAG );

                //System.out.println( sb );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }

        }
    };
    
}
