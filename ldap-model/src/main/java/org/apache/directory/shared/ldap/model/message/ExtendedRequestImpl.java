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
package org.apache.directory.shared.ldap.model.message;


import java.util.Arrays;

import javax.naming.NamingException;
import javax.naming.ldap.ExtendedResponse;

import org.apache.directory.shared.util.Strings;


/**
 * ExtendedRequest implementation.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExtendedRequestImpl extends AbstractRequest implements ExtendedRequest
{
    static final long serialVersionUID = 7916990159044177480L;

    /** Extended request's Object Identifier or <b>requestName</b> */
    private String oid;

    /** Extended request's value */
    protected byte[] requestValue;

    /** The associated response */
    protected ResultResponse response;


    /**
     * Creates an ExtendedRequest implementing object used to perform
     * extended protocol operation on the server.
     */
    public ExtendedRequestImpl()
    {
        super( -1, TYPE, true );
    }


    /**
     * Creates an ExtendedRequest implementing object used to perform
     * extended protocol operation on the server.
     * 
     * @param id the sequential message identifier
     */
    public ExtendedRequestImpl( final int id )
    {
        super( id, TYPE, true );
    }


    // -----------------------------------------------------------------------
    // ExtendedRequest Interface Method Implementations
    // -----------------------------------------------------------------------

    /**
     * Sets the Object Identifier corresponding to the extended request type.
     * 
     * @param newOid the dotted-decimal representation as a String of the OID
     */
    public void setRequestName( String newOid )
    {
        this.oid = newOid;
    }


    /**
     * Gets the extended request's <b>requestValue</b> portion of the PDU. The
     * form of the data is request specific and is determined by the extended
     * request OID.
     * 
     * @return byte array of data
     */
    public byte[] getRequestValue()
    {
        if ( requestValue == null )
        {
            return null;
        }

        final byte[] copy = new byte[requestValue.length];
        System.arraycopy( requestValue, 0, copy, 0, requestValue.length );
        return copy;
    }


    /**
     * Sets the extended request's <b>requestValue</b> portion of the PDU.
     * 
     * @param payload byte array of data encapsulating ext. req. parameters
     */
    public void setRequestValue( byte[] payload )
    {
        if ( payload != null )
        {
            this.requestValue = new byte[payload.length];
            System.arraycopy( payload, 0, this.requestValue, 0, payload.length );
        }
        else
        {
            this.requestValue = null;
        }
    }


    // ------------------------------------------------------------------------
    // SingleReplyRequest Interface Method Implementations
    // ------------------------------------------------------------------------

    /**
     * Gets the protocol response message type for this request which produces
     * at least one response.
     * 
     * @return the message type of the response.
     */
    public MessageTypeEnum getResponseType()
    {
        return RESP_TYPE;
    }


    /**
     * The result containing response for this request.
     * 
     * @return the result containing response for this request
     */
    public ResultResponse getResultResponse()
    {
        if ( response == null )
        {
            response = new ExtendedResponseImpl( getMessageId() );
        }

        return response;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int hash = 37;
        if ( oid != null )
        {
            hash = hash * 17 + oid.hashCode();
        }
        if ( requestValue != null )
        {
            hash = hash * 17 + Arrays.hashCode( requestValue );
        }
        hash = hash * 17 + super.hashCode();

        return hash;
    }


    /**
     * Checks to see if an object equals this ExtendedRequest.
     * 
     * @param obj the object to be checked for equality
     * @return true if the obj equals this ExtendedRequest, false otherwise
     */
    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }

        if ( !super.equals( obj ) )
        {
            return false;
        }

        if ( !( obj instanceof ExtendedRequest ) )
        {
            return false;
        }

        ExtendedRequest req = ( ExtendedRequest ) obj;

        if ( ( oid != null ) && ( req.getRequestName() == null ) )
        {
            return false;
        }

        if ( ( oid == null ) && ( req.getRequestName() != null ) )
        {
            return false;
        }

        if ( ( oid != null ) && ( req.getRequestName() != null ) && !oid.equals( req.getRequestName() ) )
        {
            return false;
        }

        if ( ( requestValue != null ) && ( req.getRequestValue() == null ) )
        {
            return false;
        }

        if ( ( requestValue == null ) && ( req.getRequestValue() != null ) )
        {
            return false;
        }

        return ( ( requestValue == null ) || ( req.getRequestValue() == null )
            || Arrays.equals( requestValue, req.getRequestValue() ) );
    }


    /**
     * Gets the Object Idendifier corresponding to the extended request type.
     * This is the <b>requestName</b> portion of the ext. req. PDU.
     * 
     * @return the dotted-decimal representation as a String of the OID
     */
    public String getRequestName()
    {
        return oid;
    }


    /**
     * Creates the extended response.
     * 
     * This implement always returns null.
     *
     * @param id the OID
     * @param berValue the value
     * @param offset the offset
     * @param length the length
     * @return the extended response
     * @throws NamingException the naming exception
     */
    public ExtendedResponse createExtendedResponse( String id, byte[] berValue, int offset, int length )
        throws NamingException
    {
        return null;
    }


    /**
     * Get a String representation of an Extended Request
     * 
     * @return an Extended Request String
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append( "    Extended request\n" );
        sb.append( "        Request name : '" ).append( oid ).append( "'\n" );

        if ( oid != null )
        {
            sb.append( "        Request value : '" ).append( Strings.utf8ToString(requestValue) ).append( '/' )
                .append( Strings.dumpBytes(requestValue) ).append( "'\n" );
        }

        // The controls
        sb.append( super.toString() );

        return super.toString( sb.toString() );
    }
}
