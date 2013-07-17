package geopod.utils.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

/**
 * A ListModel that maintains the elements in sorted order.
 * 
 * @param <E>
 */
public class SortedListModel<E>
		extends AbstractListModel
{
	private static final long serialVersionUID = -784318824516443677L;

	/**
	 * A {@link SortedSet} that maintains the order of the elements.
	 */
	private SortedSet<E> m_sortedModel;

	public SortedListModel ()
	{
		m_sortedModel = new TreeSet<E> ();
	}

	/**
	 * construct a sorted list model.
	 * 
	 * @param comp
	 *            - a comparator to order the elements by.
	 */
	public SortedListModel (Comparator<? super E> comp)
	{
		m_sortedModel = new TreeSet<E> (comp);
	}

	/**
	 * Create by copying another SortedListModel.
	 * 
	 * @param other
	 *            - the SortedListModel to copy.
	 */
	public SortedListModel (SortedListModel<E> other)
	{
		m_sortedModel = new TreeSet<E> (other.m_sortedModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSize ()
	{
		return (m_sortedModel.size ());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E getElementAt (int index)
	{
		Iterator<E> iter = this.iterator ();
		E element = null;
		int elementsToSkip = index;
		while (iter.hasNext () && elementsToSkip >= 0)
		{
			element = iter.next ();
			--elementsToSkip;
		}

		return (element);
	}

	/**
	 * Removes an element at a specific index.
	 * 
	 * @param index
	 *            - the index to remove an element at.
	 * @return - the element that was removed.
	 */
	public E removeElementAt (int index)
	{
		E elementToRemove = getElementAt (index);
		this.removeElement (elementToRemove);

		return (elementToRemove);
	}

	/**
	 * Add an element to the list model.
	 * 
	 * @param element
	 *            - the element to add.
	 */
	public void add (E element)
	{
		boolean wasAdded = m_sortedModel.add (element);
		if (wasAdded)
		{
			fireContentsChanged (this, 0, getSize ());
		}
	}

	/**
	 * Adds several elements to the list model.
	 * 
	 * @param elements
	 *            - the array of elements to add
	 */
	public void addAll (E[] elements)
	{
		Collection<E> c = Arrays.asList (elements);
		this.addAll (c);
	}

	/**
	 * Adds a collection of elements to the list model.
	 * 
	 * @param elements
	 *            - the collection to add.
	 */
	public void addAll (Collection<E> elements)
	{
		boolean modelChanged = m_sortedModel.addAll (elements);
		if (modelChanged)
		{
			fireContentsChanged (this, 0, getSize ());
		}
	}

	/**
	 * Removes all elements from the list model.
	 */
	public void clear ()
	{
		m_sortedModel.clear ();
		fireContentsChanged (this, 0, getSize ());
	}

	/**
	 * Checks if the list model contains the given element.
	 * 
	 * @param element
	 *            - the element to search for.
	 * @return - <tt>true</tt> if this set contains the specified element.
	 */
	public boolean contains (E element)
	{
		return (m_sortedModel.contains (element));
	}

	/**
	 * Returns the first element in this ListModel.
	 * 
	 * @return the first (lowest sorted) element currently in this set
	 * @throws NoSuchElementException
	 *             if this set is empty
	 */
	public E firstElement ()
	{
		return (m_sortedModel.first ());
	}

	/**
	 * Returns an iterator over the elements in this ListModel. The elements are
	 * returned in sorted order.
	 * 
	 * @return an iterator over the elements in this set
	 */
	public Iterator<E> iterator ()
	{
		return (m_sortedModel.iterator ());
	}

	/**
	 * Returns the last element in this {@link ListModel}.
	 * 
	 * @return the last (highest sorted) element currently in this set
	 * @throws NoSuchElementException
	 *             if this set is empty
	 */
	public E lastElement ()
	{
		return (m_sortedModel.last ());
	}

	/**
	 * Removes the specified element from this ListModel if it is present. More
	 * formally, removes an element <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if this
	 * set contains such an element. Returns <tt>true</tt> if this set contained
	 * the element (or equivalently, if this set changed as a result of the
	 * call). (This set will not contain the element once the call returns.)
	 * 
	 * @param element
	 *            - element to be removed from this set, if present.
	 * @return <tt>true</tt> if this set contained the specified element.
	 * @throws ClassCastException
	 *             if the type of the specified element is incompatible with
	 *             this set (optional).
	 * @throws NullPointerException
	 *             if the specified element is null and this set does not permit
	 *             null elements (optional).
	 * @throws UnsupportedOperationException
	 *             if the <tt>remove</tt> operation is not supported by this
	 *             set.
	 */
	public boolean removeElement (E element)
	{
		boolean removed = m_sortedModel.remove (element);
		if (removed)
		{
			fireContentsChanged (this, 0, getSize ());
		}
		return (removed);
	}
}
