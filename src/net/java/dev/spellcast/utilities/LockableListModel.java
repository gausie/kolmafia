/**
 * Copyright (c) 2003, Spellcast development team
 * http://spellcast.dev.java.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "Spellcast development team" nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.java.dev.spellcast.utilities;

// list-related imports
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Collection;
import java.util.Iterator;

// update components
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;

/**
 * <p>Lockable aspects of this class have been removed due to incompatibilities with Swing;
 * synchronization between two threads when one is the Swing thread turns out to have a lot
 * of problems.  It retains its original name for convenience purposes only.  The original
 * methods only retain their lock properties if you synchronize upon the object lock, which
 * may be more trouble than they're worth.</p>
 *
 * <p>In addition to this assertion, the <code>LockableListModel</code> also provides the
 * ability to create a <i>mirror image</i>: namely, another <code>LockableListModel</code>
 * that changes whenever the data in <tt>this</tt> <code>LockableListModel</code> changes
 * but does not respond to object-selection changes.  This makes it so that multiple copies
 * of data can be maintained - synchronization of data, in a sense.</p>
 */

public class LockableListModel extends javax.swing.AbstractListModel
	implements Cloneable, java.util.List, javax.swing.ListModel, javax.swing.ComboBoxModel, LockableObject
{
	private ObjectLock lock;
	private List elements;
	private int selectedIndex;

	/**
	 * Constructs a new <code>LockableListModel</code>.
	 */

	public LockableListModel()
	{
		elements = new ArrayList();
		selectedIndex = -1;
	}

	private class FireListEventRunnable implements Runnable
	{
		private int changeType;
		private Object source;
		private int index0, index1;

		public FireListEventRunnable( int changeType, Object source, int index0, int index1 )
		{
			this.changeType = changeType;
			this.source = source;
			this.index0 = index0;
			this.index1 = index1;
		}

		public void run()
		{
			// If you are not in the Swing thread, then wait
			// until you are in the Swing thread before making
			// the object to avoid deadlocks.

			if ( !SwingUtilities.isEventDispatchThread() )
			{
				SwingUtilities.invokeLater( this );
				return;
			}

			switch ( changeType )
			{
				case ListDataEvent.CONTENTS_CHANGED:
					LockableListModel.super.fireContentsChanged( source, index0, index1 );
					break;

				case ListDataEvent.INTERVAL_ADDED:
					LockableListModel.super.fireIntervalAdded( source, index0, index1 );
					break;

				case ListDataEvent.INTERVAL_REMOVED:
					LockableListModel.super.fireIntervalRemoved( source, index0, index1 );
					break;
			}
		}
	}

	protected void fireContentsChanged( Object source, int index0, int index1 )
	{	SwingUtilities.invokeLater( new FireListEventRunnable( ListDataEvent.CONTENTS_CHANGED, source, index0, index1 ) );
	}

	protected void fireIntervalAdded( Object source, int index0, int index1 )
	{	SwingUtilities.invokeLater( new FireListEventRunnable( ListDataEvent.INTERVAL_ADDED, source, index0, index1 ) );
	}

	protected void fireIntervalRemoved( Object source, int index0, int index1 )
	{	SwingUtilities.invokeLater( new FireListEventRunnable( ListDataEvent.INTERVAL_REMOVED, source, index0, index1 ) );
	}

	/**
	 * Please refer to {@link java.util.List#add(int,Object)} for more
	 * information regarding this function.
	 */

	public void add( int index, Object element )
	{
		if ( element == null )
			throw new IllegalArgumentException( "cannot add a null object to this list" );

		elements.add( index, element );
		fireIntervalAdded( this, index, index );
	}

	/**
	 * Please refer to {@link java.util.List#add(Object)} for more
	 * information regarding this function.
	 */

	public boolean add( Object o )
	{
		if ( o == null )
			return false;

		int index = size();
		elements.add( index, o );
		fireIntervalAdded( this, index, index );
		return true;
	}

	/**
	 * Please refer to {@link java.util.List#addAll(Collection)} for more
	 * information regarding this function.
	 */

	public boolean addAll( Collection c )
	{
		try
		{
			Iterator myIterator = c.iterator();
			while ( myIterator.hasNext() )
				if ( !add( myIterator.next() ) )
					return false;
			return true;
		}
		catch( IllegalArgumentException e )
		{
			return false;
		}
	}

	/**
	 * Please refer to {@link java.util.List#addAll(int,Collection)} for more
	 * information regarding this function.
	 */

	public boolean addAll( int index, Collection c )
	{
		try
		{
			Iterator myIterator = c.iterator();
			for ( int i = index; myIterator.hasNext(); ++i )
				add( i, myIterator.next() );
			return true;
		}
		catch( IllegalArgumentException e )
		{
			return false;
		}
	}

	/**
	 * Please refer to {@link java.util.List#clear()} for more
	 * information regarding this function.
	 */

	public void clear()
	{
		int lastIndex = size() - 1;
		elements.clear();
		if ( lastIndex >= 0 )
			fireIntervalRemoved( this, 0, lastIndex );
	}

	/**
	 * Please refer to {@link java.util.List#contains(Object)} for more
	 * information regarding this function.
	 */

	public boolean contains( Object o )
	{	return elements.contains( o );
	}

	/**
	 * Please refer to {@link java.util.List#containsAll(Collection)} for more
	 * information regarding this function.
	 */

	public boolean containsAll( Collection c )
	{	return elements.containsAll( c );
	}

	/**
	 * Please refer to {@link java.util.List#equals(Object)} for more
	 * information regarding this function.
	 */

	public boolean equals( Object o )
	{	return elements.equals( o );
	}

	/**
	 * Please refer to {@link java.util.List#get(int)} for more
	 * information regarding this function.
	 */

	public Object get( int index )
	{
		if ( index < 0 || index >= size() )
			return null;
		return elements.get( index );
	}

	/**
	 * Please refer to {@link java.util.List#hashCode()} for more
	 * information regarding this function.
	 */

	public int hashCode()
	{	return elements.hashCode();
	}

	/**
	 * Please refer to {@link java.util.List#indexOf(Object)} for more
	 * information regarding this function.
	 */

	public int indexOf( Object o )
	{	return elements.indexOf( o );
	}

	/**
	 * Please refer to {@link java.util.List#isEmpty()} for more
	 * information regarding this function.
	 */

	public boolean isEmpty()
	{	return elements.isEmpty();
	}

	/**
	 * Please refer to {@link java.util.List#iterator()} for more
	 * information regarding this function.
	 */

	public Iterator iterator()
	{	return elements.iterator();
	}

	/**
	 * Please refer to {@link java.util.List#lastIndexOf(Object)} for more
	 * information regarding this function.
	 */

	public int lastIndexOf( Object o )
	{	return elements.lastIndexOf( o );
	}

	/**
	 * Please refer to {@link java.util.List#listIterator()} for more
	 * information regarding this function.
	 */

	public ListIterator listIterator()
	{	return elements.listIterator();
	}

	/**
	 * Please refer to {@link java.util.List#listIterator(int)} for more
	 * information regarding this function.
	 */

	public ListIterator listIterator( int index )
	{	return elements.listIterator( index );
	}


	/**
	 * Please refer to {@link java.util.List#remove(int)} for more
	 * information regarding this function.
	 */

	public Object remove( int index )
	{
		Object removedElement = elements.remove( index );
		if ( removedElement == null )
			return null;
		fireIntervalRemoved( this, index, index );
		return removedElement;
	}

	/**
	 * Please refer to {@link java.util.List#remove(Object)} for more
	 * information regarding this function.
	 */

	public boolean remove( Object o )
	{
		int index = indexOf( o );
		if ( index == -1 )
			return false;

		elements.remove( index );
		fireIntervalRemoved( this, index, index );
		return true;
	}

	/**
	 * Please refer to {@link java.util.List#removeAll(Collection)} for more
	 * information regarding this function.
	 */

	public boolean removeAll( Collection c )
	{
		Iterator myIterator = c.iterator();
		while ( myIterator.hasNext() )
			if ( !remove( myIterator.next() ) )
				return false;
		return true;
	}

	/**
	 * Please refer to {@link java.util.List#retainAll(Collection)} for more
	 * information regarding this function.
	 */

	public boolean retainAll( Collection c )
	{
		boolean hasChanged = false;
		Object [] elements = toArray();

		for ( int i = 0; i < elements.length; ++i )
		{
			if ( !c.contains( elements[i] ) )
			{
				remove( elements[i] );
				hasChanged = true;
			}
		}
		return hasChanged;
	}

	/**
	 * Please refer to {@link java.util.List#set(int,Object)} for more
	 * information regarding this function.
	 */

	public Object set( int index, Object element )
	{
		if ( element == null )
			throw new IllegalArgumentException( "cannot add a null object to this list" );

		Object originalElement = get( index );
		elements.set( index, element );
		fireContentsChanged( this, index, index );
		return originalElement;
	}

	/**
	 * Please refer to {@link java.util.List#size()} for more
	 * information regarding this function.
	 */

	public int size()
	{	return elements.size();
	}

	/**
	 * Please refer to {@link java.util.List#subList(int,int)} for more
	 * information regarding this function.
	 */

	public List subList( int fromIndex, int toIndex )
	{	return elements.subList( fromIndex, toIndex );
	}

	/**
	 * Please refer to {@link java.util.List#toArray()} for more
	 * information regarding this function.
	 */

	public Object [] toArray()
	{	return elements.toArray();
	}

	/**
	 * Please refer to {@link java.util.List#toArray(Object[])} for more
	 * information regarding this function.
	 */

	public Object [] toArray( Object[] a )
	{	return elements.toArray(a);
	}

	/**
	 * Please refer to {@link javax.swing.ListModel#getElementAt(int)} for more
	 * information regarding this function.
	 */

	public Object getElementAt( int index )
	{	return get( index );
	}

	/**
	 * Please refer to {@link javax.swing.ListModel#getSize()} for more
	 * information regarding this function.
	 */

	public int getSize()
	{	return size();
	}

    /**
     * Please refer to {@link javax.swing.ComboBoxModel#getSelectedItem()} for more
     * information regarding this function.
     */

    public Object getSelectedItem()
    {	return get( getSelectedIndex() );
	}

	/**
	 * Returns the index of the currently selected item in this <code>LockableListModel</code>.
	 * This is used primarily in cloning, to ensure that the same indices are being selected;
	 * however it may also be used to report the index of the currently selected item in testing
	 * a new object which uses a list model.
	 *
	 * @return	the index of the currently selected item
	 */

	public int getSelectedIndex()
	{	return selectedIndex;
	}

    /**
     * Please refer to {@link javax.swing.ComboBoxModel#setSelectedItem(Object)} for more
     * information regarding this function.
     */

	public void setSelectedItem( Object o )
	{	setSelectedIndex( indexOf( o ) );
	}

	/**
	 * Sets the given index in this <code>LockableListModel</code> as the currently
	 * selected item.  This is meant to be a complement to setSelectedItem(), and also
	 * functions to help in the cloning process.
	 */

	public void setSelectedIndex( int index )
	{
		selectedIndex = index;
		fireContentsChanged( this, -1, -1 );
	}

	/**
	 * Returns a deep copy of the data associated with this <code>LockableListModel</code>.
	 * Note that any subclasses must override this method in order to ensure that the object can
	 * be cast appropriately; note also that the listeners are not inherited by the clone copy,
	 * since this violates the principle of independence.  If they are required, then the
	 * class using this model should add them using the functions provided in the <code>ListModel</code>
	 * interface.  Note also that if an element added to the list does not implement the
	 * <code>Cloneable</code> interface, or implements it by causing it to fail by default, this
	 * method will not fail; it will add a reference to the object, in effect creating a shallow
	 * copy of it.  Thus, retrieving an object using get() and modifying a field will result
	 * in both <code>LockableListModel</code> objects changing, in the same way retrieving
	 * an element from a cloned <code>Vector</code> will.
	 *
	 * @return	a deep copy (exempting listeners) of this <code>LockableListModel</code>.
	 */

	public Object clone()
	{
		try
		{
			LockableListModel cloneCopy = (LockableListModel) super.clone();
			cloneCopy.listenerList = new javax.swing.event.EventListenerList();
			cloneCopy.elements = cloneList();
			cloneCopy.setSelectedIndex( getSelectedIndex() );
			return cloneCopy;
		}
		catch ( CloneNotSupportedException e )
		{
			// Because none of the super classes support clone(), this means
			// that this method is overriding the one found in Object.  Thus,
			// this exception should never be thrown, unless one of the super
			// classes is re-written to throw the exception by default.
			throw new RuntimeException( "AbstractListModel or one of its superclasses was rewritten to throw CloneNotSupportedException by default, call to clone() was unsuccessful" );
		}
	}

	/**
	 * Because <code>ArrayList</code> only creates a shallow copy of the objects,
	 * the one used as a data structure here must be cloned manually in order
	 * to satifsy the contract established by <code>clone()</code>.  However,
	 * the individual elements are known to be of class <code>Object</code>,
	 * and objects only force the clone() method to be protected.  Thus, in
	 * order to invoke clone() on each individual element, it must be done
	 * reflectively, which is the purpose of this private method.
	 *
	 * @return	as deep a copy of the object as can be obtained
	 */

	private List cloneList()
	{
		List clonedList = new ArrayList();
		java.lang.reflect.Method cloneMethod;  Object toClone;

		for ( int i = 0; i < size(); ++i )
			clonedList.add( attemptClone( get(i) ) );

		return clonedList;
	}

	/**
	 * A private function which attempts to clone the object, if
	 * and only if the object implements the <code>Cloneable</code>
	 * interface.  If the object provided does not implement the
	 * <code>Cloneable</code> interface, or the clone() method is
	 * protected (as it is in class <code>Object</code>), then the
	 * original object is returned.
	 *
	 * @param	o	the object to be cloned
	 * @return	a copy of the object, either shallow or deep, pending
	 *			whether the original object was intended to be able to
	 *			be deep-copied
	 */

	private static Object attemptClone( Object o )
	{
		if ( !( o instanceof Cloneable ) )
			return o;

		java.lang.reflect.Method cloneMethod;

		try
		{
			// The function clone() has no parameters; this implementation
			// is implementation and specification dependent, and is used
			// with the traditional rule about null for a parameter list in
			// Class and Method indicating a zero-length parameter list.
			cloneMethod = o.getClass().getDeclaredMethod( "clone", null );
		}
		catch ( SecurityException e )
		{
			// If the methods of this function cannot be accessed
			// because it is protected, then it cannot be called
			// from this context - the original object should be
			// returned in this case.
			return o;
		}
		catch ( NoSuchMethodException e )
		{
			// This exception should never be thrown because all
			// objects have the clone() function.  If it is thrown,
			// then the clone() method was somehow deleted from
			// class Object (lack of backwards compatibility).
			// In this case, the original object should be returned.
			return o;
		}

		try
		{
			// The function clone() has no parameters; this implementation
			// is implementation and specification dependent, and is used
			// with the traditional rule about null for a parameter list in
			// Class and Method indicating a zero-length parameter list.
			return cloneMethod.invoke( o, null );
		}
		catch ( IllegalAccessException e )
		{
			// This exception should not occur, since the SecurityException
			// would have been thrown in the cases where this would have occurred.
			// But, if it does happen to occur *after* the SecurityException
			// caught all the instances, then something is wrong.
			throw new InternalError("accessible clone() method exists, but IllegalAccessException thrown");
		}
		catch ( IllegalArgumentException e )
		{
			// This exception should not occur, since the NoSuchMethodException
			// would have been thrown in the cases where this would have occurred.
			// But, if it does happen to occur *after* the NoSuchMethodException
			// caught all the instances, then something is wrong.
			throw new InternalError("accessible clone() method exists, but IllegalArgumentException thrown when no arguments are provided");
		}
		catch ( java.lang.reflect.InvocationTargetException e )
		{
			// The only exception normally thrown by the clone() operation is
			// the CloneNotSupportedException.  If this is thrown by the element,
			// then it is known that even if it implements the Cloneable interface,
			// it throws the exception by default - return the original object.
			return o;
		}
	}

	/**
	 * Tests to see if the object is currently locked.  Note that a return
	 * of <tt>true</tt> indicates that there are <i>no</i> locks in place
	 * for this object.
	 *
	 * @return	<tt>true</tt> if this object is currently locked
	 */

	public boolean isLocked()
	{	return lock != null;
	}

	/**
	 * Tests the lock of the given list model.  If the lock is in place, and this
	 * is not the event dispatch thread, the call to this function will cause the
	 * thread to block until the lock is released by another thread.  Note that
	 * the return value indicates the list's state in the <i>past</i>, prior to
	 * the call to this function; if there is a need to know if the object is
	 * <i>currently</i> locked, one should use <code>isLocked()</code>
	 *
	 * @return	<tt>true</tt> if the list was locked previous to the method call
	 */

	private boolean checkLock()
	{
		if ( javax.swing.SwingUtilities.isEventDispatchThread() )
			return isLocked();

		if ( !isLocked() )
			return false;

		synchronized ( this )
		{	return true;
		}
	}

	/**
	 * Locks this object with the given key.  Note that in order to unlock the object,
	 * an equivalent key must be provided to the <code>unlock()</code> function.
	 * If the lock attempt is successful, this function returns true.  Note that a
	 * given key can only be used to lock this object once; multiple lock attempts
	 * with the same key will fail if a lock with the given key is already in place.
	 *
	 * @param	key	the key to be used to lock the object
	 * @return	<tt>true</tt> if the object was successfully locked with the given key
	 */

	public boolean lockWith( Object key )
	{
		if ( isLocked() )
			return false;
		lock = new ObjectLock( key, this );
		return true;
	}

	/**
	 * Attempts to unlock this object using the given key.  If the unlock attempt is
	 * successful or if the object is already unlocked, this function returns true.
	 *
	 * @param	testKey	the key to be used to attempt an unlock
	 * @return	<tt>true</tt> if the unlock attempt is successful
	 */

	public boolean unlockWith( Object testKey )
	{
		if ( !isLocked() )
			return true;

		synchronized ( lock )
		{
			if ( !lock.unlockWith( testKey ) )
				return false;
			lock = null;
			return true;
		}
	}


	/**
	 * Returns a mirror image of this <code>LockableListModel</code>.  In essence,
	 * the object returned will be a clone of the original object.  However, it has
	 * the additional feature of listening for changes to the <em>underlying data</em> of this
	 * <code>LockableListModel</code>.  Note that this means any changes in selected
	 * indices will not be mirrored in the mirror image.  Note that because this function
	 * modifies the listeners for this class, an asynchronous version is not available.
	 *
	 * @return	a mirror image of this <code>LockableListModel</code>
	 */

	public LockableListModel getMirrorImage()
	{
		LockableListModel mirrorImage = (LockableListModel) clone();
		addListDataListener( new MirrorImageListener( mirrorImage ) );
		return mirrorImage;
	}

	/**
	 * An internal listener class which is added whenever a mirror image is created.
	 * The <code>MirrorImageListener</code> will respond to any changes in the
	 * <code>LockableListModel</code> by changing the underlying data of the
	 * mirror image(s) of the <code>LockableListModel</code>.
	 */

	private class MirrorImageListener implements javax.swing.event.ListDataListener
	{
		private LockableListModel mirrorImage;

		/**
		 * Constructs a new <code>MirrorImageListener</code> which will respond
		 * to changes in the class it's listening on by making changes to the
		 * given mirror image.  Note that it does not check to ensure that the
		 * given object is truly a copy of the original; thus, before creating a
		 * listener, a mirror image must be created.
		 *
		 * @param	mirrorImage	the mirror image of this <code>LockableListModel</code>
		 */

		public MirrorImageListener( LockableListModel mirrorImage )
		{	this.mirrorImage = mirrorImage;
		}

		/**
		 * Called whenever contents have been added to the original list; a
		 * function required by every <code>ListDataListener</code>.
		 *
		 * @param	e	the <code>ListDataEvent</code> that triggered this function call
		 */

		public void intervalAdded( ListDataEvent e )
		{
			if ( e.getType() == ListDataEvent.INTERVAL_ADDED && e.getSource() instanceof LockableListModel )
				intervalAdded( (LockableListModel) e.getSource(), e.getIndex0(), e.getIndex1() );
		}

		/**
		 * Indicates that the given list has added elements.  This function then
		 * proceeds to add the elements within the given index range to the mirror
		 * image currently being stored.
		 *
		 * @param	source	the list that has changed
		 * @param	index0	the lower index in the range
		 * @param	index1	the upper index in the range
		 */

		private void intervalAdded( LockableListModel source, int index0, int index1 )
		{
			if ( mirrorImage == null || source == null || index0 < 0 || index1 < 0 || index1 >= source.size() )
				return;

			for ( int i = index0; i <= index1; ++i )
				mirrorImage.add( i, source.get(i) );
		}

		/**
		 * Called whenever contents have been removed from the original list;
		 * a function required by every <code>ListDataListener</code>.
		 *
		 * @param	e	the <code>ListDataEvent</code> that triggered this function call
		 */

		public void intervalRemoved( ListDataEvent e )
		{
			if ( e.getType() == ListDataEvent.INTERVAL_REMOVED && e.getSource() instanceof LockableListModel )
				intervalRemoved( (LockableListModel) e.getSource(), e.getIndex0(), e.getIndex1() );
		}

		/**
		 * Indicates that the given list has removed elements.  This function then
		 * proceeds to remove the elements within the given index range from the mirror
		 * image currently being stored.
		 *
		 * @param	source	the list that has changed
		 * @param	index0	the lower index in the range
		 * @param	index1	the upper index in the range
		 */

		private void intervalRemoved( LockableListModel source, int index0, int index1 )
		{
			if ( mirrorImage == null || source == null || index0 < 0 || index1 < 0 || index1 >= mirrorImage.size() )
				return;

			for ( int i = index1; i >= index0; --i )
				mirrorImage.remove( i );
		}

		/**
		 * Called whenever contents in the original list have changed; a
		 * function required by every <code>ListDataListener</code>.
		 *
		 * @param	e	the <code>ListDataEvent</code> that triggered this function call
		 */

		public void contentsChanged( ListDataEvent e )
		{
			if ( e.getType() == ListDataEvent.CONTENTS_CHANGED && e.getSource() instanceof LockableListModel )
				contentsChanged( (LockableListModel) e.getSource(), e.getIndex0(), e.getIndex1() );
		}

		/**
		 * Indicates that the given list has changed its contents.  This function then
		 * proceeds to change the elements within the given index range in the mirror
		 * image currently being stored to match the ones in the original list.
		 *
		 * @param	source	the list that has changed
		 * @param	index0	the lower index in the range
		 * @param	index1	the upper index in the range
		 */

		private void contentsChanged( LockableListModel source, int index0, int index1 )
		{
			if ( mirrorImage == null || source == null || index1 < 0 || index1 >= mirrorImage.size() )
				return;

			for ( int i = index1; i >= index0; --i )
				mirrorImage.set( i, source.get(i) );
		}
	}
}