/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.shared.ldap.entry;

import java.io.Externalizable;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.InvalidAttributeValueException;


/**
 * A generic interface mocking the Attribute JNDI interface. This interface
 * will be the base interface for the ServerAttribute and ClientAttribute.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public interface EntryAttribute<T extends Value<?>> extends Iterable<T>,  Externalizable, Cloneable
{
    /**
     * Adds some values to this attribute. If the new values are already present in
     * the attribute values, the method has no effect.
     * <p>
     * The new values are added at the end of list of values.
     * </p>
     * <p>
     * This method returns the number of values that were added.
     * </p>
     *
     * @param val some new values to be added which may be null
     * @return the number of added values, or 0 if none has been added
     */
    int add( String... vals ) throws InvalidAttributeValueException, NamingException;


    /**
     * Puts some values to this attribute.
     * <p>
     * The new values will replace the previous values.
     * </p>
     * <p>
     * This method returns the number of values that were put.
     * </p>
     *
     * @param val some values to be put which may be null
     * @return the number of added values, or 0 if none has been added
     * @throws InvalidAttributeValueException If we try to add some values
     * which conflicts with the AttributeType for this attribute
     * @throws NamingException If the attributeType does not have a syntax
     */
    int put( String... vals ) throws InvalidAttributeValueException, NamingException;


    /**
     * Puts some values to this attribute.
     * <p>
     * The new values will replace the previous values.
     * </p>
     * <p>
     * This method returns the number of values that were put.
     * </p>
     *
     * @param vals some values to be put which may be null
     * @return the number of added values, or 0 if none has been added
     * @throws InvalidAttributeValueException If we try to add some values
     * which conflicts with the AttributeType for this attribute
     * @throws NamingException If the attributeType does not have a syntax
     */
    int put( List<?> vals ) throws InvalidAttributeValueException, NamingException;


    /**
     * Adds some values to this attribute. If the new values are already present in
     * the attribute values, the method has no effect.
     * <p>
     * The new values are added at the end of list of values.
     * </p>
     * <p>
     * This method returns the number of values that were added.
     * </p>
     *
     * @param val some new values to be added which may be null
     * @return the number of added values, or 0 if none has been added
     */
    int add( byte[]... vals ) throws InvalidAttributeValueException, NamingException;


    /**
     * Puts some values to this attribute.
     * <p>
     * The new values will replace the previous values.
     * </p>
     * <p>
     * This method returns the number of values that were put.
     * </p>
     *
     * @param val some values to be put which may be null
     * @return the number of added values, or 0 if none has been added
     */
    int put( byte[]... vals ) throws InvalidAttributeValueException, NamingException;


    /**
     * Removes all values of this attribute.
     */
    void clear();


    /**
     * Indicates whether the specified values are some of the attribute's values.
     *
     * @param vals the values
     * @return true if this attribute contains all the values, otherwise false
     */
    boolean contains( String... vals ) throws NamingException;


    /**
     * Indicates whether the specified values are some of the attribute's values.
     *
     * @param vals the values
     * @return true if this attribute contains all the values, otherwise false
     */
    boolean contains( byte[]... vals ) throws NamingException;


    /**
     * Indicates whether the specified values are some of the attribute's values.
     *
     * @param vals the values
     * @return true if this attribute contains all the values, otherwise false
     */
    boolean contains( Object... vals ) throws NamingException;


   /**
      * Retrieves the number of values in this attribute.
      *
      * @return the number of values in this attribute, including any values
      * wrapping a null value if there is one
      */
    int size();


    /**
     * Removes all the  values that are equal to the given values.
     * <p>
     * Returns true if a value is removed. If there is no value equal to <code>
     * val</code> this method simply returns false.
     * </p>
     *
     * @param vals the values to be removed
     * @return true if all the values are removed, otherwise false
     */
    boolean remove( byte[]... val );


    /**
     * Removes all the  values that are equal to the given values.
     * <p>
     * Returns true if a value is removed. If there is no value equal to <code>
     * val</code> this method simply returns false.
     * </p>
     *
     * @param vals the values to be removed
     * @return true if all the values are removed, otherwise false
     */
    boolean remove( String... vals );
    
    
    /**
     * Gets the first value of this attribute. <code>null</code> is a valid value.
     *
     * <p>
     * If the attribute has no values this method throws
     * <code>NoSuchElementException</code>.
     * </p>
     *
     * @return a value of this attribute
     */
    T get();


    /**
     * Get's the attribute identifier for this entry.  This is the value
     * that will be used as the identifier for the attribute within the
     * entry.  
     *
     * @return the identifier for this attribute
     */
    String getId();

    
    /**
     * Get's the user provided identifier for this entry.  This is the value
     * that will be used as the identifier for the attribute within the
     * entry.  If this is a commonName attribute for example and the user
     * provides "COMMONname" instead when adding the entry then this is
     * the format the user will have that entry returned by the directory
     * server.  To do so we store this value as it was given and track it
     * in the attribute using this property.
     *
     * @return the user provided identifier for this attribute
     */
    String getUpId();

    
    /**
     * Returns an iterator over all the attribute's values.
     * <p>
     * The effect on the returned enumeration of adding or removing values of
     * the attribute is not specified.
     * </p>
     * <p>
     * This method will throw any <code>NamingException</code> that occurs.
     * </p>
     *
     * @return an enumeration of all values of the attribute
     */
    Iterator<T> getAll();


    /**
     * Removes all the  values that are equal to the given values.
     * <p>
     * Returns true if a value is removed. If there is no value equal to <code>
     * val</code> this method simply returns false.
     * </p>
     *
     * @param vals the values to be removed
     * @return true if all the values are removed, otherwise false
     */
    boolean remove( T... vals );

    
    /**
     * Indicates whether the specified values are some of the attribute's values.
     *
     * @param vals the values
     * @return true if this attribute contains all the values, otherwise false
     */
    boolean contains( T... vals ) throws NamingException;

    
    /**
     * Adds some values to this attribute. If the new values are already present in
     * the attribute values, the method has no effect.
     * <p>
     * The new values are added at the end of list of values.
     * </p>
     * <p>
     * This method returns the number of values that were added.
     * </p>
     *
     * @param val some new values to be added which may be null
     * @return the number of added values, or 0 if none has been added
     */
    int add( T... val ) throws InvalidAttributeValueException, NamingException;
    
    
    /**
     * Puts some values to this attribute.
     * <p>
     * The new values are replace the previous values.
     * </p>
     * <p>
     * This method returns the number of values that were put.
     * </p>
     *
     * @param val some values to be put which may be null
     * @return the number of added values, or 0 if none has been added
     */
    int put( T... vals ) throws InvalidAttributeValueException, NamingException;


    /**
     * Returns a cloned version of the current attribute.
     *
     * @return A copy of the current attribute
     */
    EntryAttribute<T> clone();
}
