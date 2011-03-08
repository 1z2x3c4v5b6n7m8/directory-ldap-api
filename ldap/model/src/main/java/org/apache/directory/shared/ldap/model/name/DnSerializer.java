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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.schema.SchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class which serialize and deserialize a Dn
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class DnSerializer
{
    /** The LoggerFactory used by this class */
    protected static final Logger LOG = LoggerFactory.getLogger( DnSerializer.class );


    /**
     * Private constructor.
     */
    private DnSerializer()
    {
    }


    /**
     * Serialize a Dn
     *
     * We have to store a Dn data efficiently. Here is the structure :
     *
     * <li>upName</li> The User provided Dn<p>
     * <li>normName</li> May be null if the normName is equivalent to
     * the upName<p>
     * <li>rdns</li> The rdn's List.<p>
     *
     * for each rdn :
     * <li>call the Rdn write method</li>
     *
     * @param dn The Dn to serialize
     * @return a byte[] containing the serialized DN
     * @throws IOException If we can't write in this stream
     */
    public static byte[] serialize( Dn dn ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( baos );
        
        serialize( dn, out );
        
        return baos.toByteArray();
    }


    /**
     * Serialize a Dn
     *
     * We have to store a Dn data efficiently. Here is the structure :
     *
     * <li>upName</li> The User provided Dn<p>
     * <li>normName</li> May be null if the normName is equivalent to
     * the upName<p>
     * <li>rdns</li> The rdn's List.<p>
     *
     * for each rdn :
     * <li>call the Rdn write method</li>
     *
     * @param dn The Dn to serialize
     * @param out the stream in which the Dn will be serialized
     * @throws IOException If we can't write in this stream
     */
    public static void serialize( Dn dn, ObjectOutput out ) throws IOException
    {
        dn.writeExternal( out );
        out.flush();
    }


    /**
     * Deserialize a Dn
     *
     * We read back the data to create a new Dn. The structure
     * read is exposed in the {@link DnSerializer#serialize(Dn, ObjectOutput)}
     * method<p>
     *
     * @param in The input bytes from which the Dn is read
     * @return a deserialized Dn
     * @throws IOException If the stream can't be read
     */
    public static Dn deserialize( SchemaManager schemaManager, byte[] bytes ) 
        throws IOException, LdapInvalidDnException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
        ObjectInputStream in = new ObjectInputStream( bais );
    
        return deserialize( schemaManager, in );
    }
    

    /**
     * Deserialize a Dn
     *
     * We read back the data to create a new Dn. The structure
     * read is exposed in the {@link DnSerializer#serialize(Dn, ObjectOutput)}
     * method<p>
     *
     * @param schemaManager The SchemaManager (can be null)
     * @param in The input stream from which the Dn is read
     * @return a deserialized Dn
     * @throws IOException If the stream can't be read
     */
    public static Dn deserialize( SchemaManager schemaManager, ObjectInput in ) 
        throws IOException, LdapInvalidDnException
    {
        Dn dn = new Dn( schemaManager );
        
        try
        {
            dn.readExternal( in );
        }
        catch ( ClassNotFoundException cnfe )
        {
            throw new IOException( cnfe.getMessage() );
        }
        
        return dn;
    }
}
