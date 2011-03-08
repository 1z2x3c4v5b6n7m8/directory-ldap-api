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
package org.apache.directory.shared.ldap.model.name;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.schema.SchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class which serialize and deserialize a Rdn
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class RdnSerializer
{
    /** The LoggerFactory used by this class */
    protected static final Logger LOG = LoggerFactory.getLogger( RdnSerializer.class );


    /**
     * Private constructor.
     */
    private RdnSerializer()
    {
    }


    /**
     * Serialize a Rdn instance
     * 
     * A Rdn is composed of on to many ATAVs (AttributeType And Value).
     * We should write all those ATAVs sequencially, following the 
     * structure :
     * 
     * <li>nbAtavs : The number of ATAVs to write. Can't be 0.</li>
     * <li>upName< : The User provided Rdn</li>
     * <li>normName : The normalized Rdn. It can be empty if the normalized
     * name equals the upName.</li>
     * <li>atavs</li>
     * <p>
     * For each ATAV :<p>
     * <li>Call the ATAV write method :The ATAV itself</li>
     * <br/>
     * @param rdn The Rdn to serialize
     * @param out the stream in which the Rdn will be serialized
     * @throws IOException If we can't write in this stream
     */
    public static void serialize( Rdn rdn, ObjectOutput out ) throws IOException
    {
        rdn.writeExternal( out );
        out.flush();
    }
    
    
    /**
     * Deserialize a Rdn instance
     * 
     * We read back the data to create a new Rdn. The structure 
     * read is exposed in the {@link Rdn#writeExternal(ObjectOutput)}
     * method<p>
     * 
     * @param schemaManager The SchemaManager (can be null)
     * @param in The input stream from which the Rdn is read
     * @return a deserialized Rdn
     * @throws IOException If the stream can't be read
     */
    public static Rdn deserialize( SchemaManager schemaManager, ObjectInput in )
        throws IOException, LdapInvalidDnException
    {
        Rdn rdn = new Rdn( schemaManager );
    
        try
        {
            rdn.readExternal( in );
        }
        catch ( ClassNotFoundException cnfe )
        {
            throw new IOException( cnfe.getMessage() );
        }

        return rdn;
    }
}
