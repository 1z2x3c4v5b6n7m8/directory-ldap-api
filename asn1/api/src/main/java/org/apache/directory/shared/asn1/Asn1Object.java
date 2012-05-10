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
package org.apache.directory.shared.asn1;


import java.nio.ByteBuffer;


/**
 * An abstract class which implements basic TLV operations.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface Asn1Object
{
    /**
     * Add a length to the object
     * 
     * @param length The length to add.
     * @throws DecoderException Thrown if the current length exceed the expected length
     */
    void addLength( int length ) throws DecoderException;


    /**
     * Compute the object length, which is the sum of all inner length.
     * 
     * @return The object's computed length
     */
    int computeLength();


    /**
     * Encode the object to a PDU.
     * 
     * @param buffer The buffer where to put the PDU
     * @return The encoded PDU.
     * @throws EncoderException if the buffer can't be encoded
     */
    ByteBuffer encode( ByteBuffer buffer ) throws EncoderException;


    /**
     * Get the current object length, which is the sum of all inner length
     * already decoded.
     * 
     * @return The current object's length
     */
    int getCurrentLength();


    /**
     * Get the expected object length.
     * 
     * @return The expected object's length
     */
    int getExpectedLength();


    /**
     * Get the parent
     * 
     * @return Returns the parent.
     */
    Asn1Object getParent();


    /**
     * Set the current length
     * 
     * @param currentLength The currentLength to set.
     */
    void setCurrentLength( int currentLength );


    /**
     * Set the expected length
     * 
     * @param expectedLength The expectedLength to set.
     */
    void setExpectedLength( int expectedLength );
}
