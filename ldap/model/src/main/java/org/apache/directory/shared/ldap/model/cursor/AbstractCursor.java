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
package org.apache.directory.shared.ldap.model.cursor;


import java.util.Iterator;

import org.apache.directory.shared.i18n.I18n;


/**
 * Simple class that contains often used Cursor code.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @param <E> The type of element on which this cursor will iterate
 */
public abstract class AbstractCursor<E> implements Cursor<E>
{
    /** The default associated monitor */
    private ClosureMonitor monitor = new DefaultClosureMonitor();


    /**
     * {@inheritDoc}
     */
    public void setClosureMonitor( ClosureMonitor monitor )
    {
        if ( monitor == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_02001_MONITOR ) );
        }

        this.monitor = monitor;
    }


    /**
     * Check that the cursor is not closed before executing an operation.
     * 
     * @param operation The operation we try to execute
     * @throws Exception If there is a problem during the check
     */
    public final void checkNotClosed( String operation ) throws Exception
    {
        monitor.checkNotClosed();
    }


    /**
     * {@inheritDoc}
     */
    public boolean isClosed() throws Exception
    {
        return monitor.isClosed();
    }


    /**
     * {@inheritDoc}
     */
    public void close( Exception cause ) throws Exception
    {
        monitor.close( cause );
    }


    /**
     * {@inheritDoc}
     */
    public void close() throws Exception
    {
        monitor.close();
    }


    /**
     * {@inheritDoc}
     */
    public Iterator<E> iterator()
    {
        return new CursorIterator<E>( this );
    }


    /**
     * {@inheritDoc}
     */
    public boolean isAfterLast() throws Exception
    {
        throw new UnsupportedOperationException( I18n.err( I18n.ERR_02014_UNSUPPORTED_OPERATION, getClass().getName()
            .concat( "." ).concat( "isAfterLast()" ) ) );
    }


    /**
     * {@inheritDoc}
     */
    public boolean isBeforeFirst() throws Exception
    {
        throw new UnsupportedOperationException( I18n.err( I18n.ERR_02014_UNSUPPORTED_OPERATION, getClass().getName()
            .concat( "." ).concat( "isBeforeFirst()" ) ) );
    }


    /**
     * {@inheritDoc}
     */
    public boolean isFirst() throws Exception
    {
        throw new UnsupportedOperationException( I18n.err( I18n.ERR_02014_UNSUPPORTED_OPERATION, getClass().getName()
            .concat( "." ).concat( "isFirst()" ) ) );
    }


    /**
     * {@inheritDoc}
     */
    public boolean isLast() throws Exception
    {
        throw new UnsupportedOperationException( I18n.err( I18n.ERR_02014_UNSUPPORTED_OPERATION, getClass().getName()
            .concat( "." ).concat( "isLast()" ) ) );
    }


    /**
     * {@inheritDoc}
     */
    public String toString( String tabs )
    {
        return tabs;
    }
}
