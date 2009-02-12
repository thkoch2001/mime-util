/*
 * Copyright 2007-2009 Medsea Business Solutions S.L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.medsea.mimeutil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class is used to represent a collection of <code>MimeType</code>(s).
 * <p>
 * It uses a {@link HashSet} as the backing collection and implements all
 * methods of both the {@link Set} and {@link Collection} interfaces.
 * </p>
 * <p>
 * Some of the methods have been adapted to make it more useful for <code>MimeType</code>(s)
 * such as the add methods. Most of the methods just delegate to the underlying HashSet. This class
 * can be used in place of a normal Collection or Set.
 * </p>
 * @author Steven McArdle
 *
 */
class MimeTypeHashSet implements Set, Collection {

	private HashSet hashSet = new HashSet();

	MimeTypeHashSet() {}

	/**
	 * Construct a new MimeTypeHashSet from an existing collection of
	 * MimeType(s). Iterates over the collection and only keeps MimeType(s)
	 * @param collection existing collection of MimeType(s).
	 */
	MimeTypeHashSet(final Collection collection) {
		for(Iterator it = collection.iterator(); it.hasNext();) {
			Object o = it.next();
			if(o instanceof MimeType) {
				hashSet.add(o);
			}
		}
	}

	MimeTypeHashSet(final int initialCapacity) {
		hashSet = new HashSet(initialCapacity);
	}

	MimeTypeHashSet(final int initialCapacity, float loadFactor) {
		hashSet = new HashSet(initialCapacity, loadFactor);
	}

	MimeTypeHashSet(final String arg0) {
		this(((String)arg0).split(","));
	}

	MimeTypeHashSet(final String [] arg0) {
		for(int i = 0; i < arg0.length; i++) {
			hashSet.add(new MimeType(arg0[i].trim()));
		}
	}

	MimeTypeHashSet(final MimeType mimeType) {
		hashSet.add(mimeType);
	}

	/**
	 * This method will create MimeType(s) from the argument and add it or them
	 * to the internal HashSet. It is able to take different types of object related to mime types and ad them.
	 *
	 * @param arg0 can be a String, String [] or MimeType
	 * @return true if the internal HashSet has been added to false other wise.
	 */
	public boolean add(final Object arg0) {
		if(arg0 instanceof String) {
			Collection c = new MimeTypeHashSet((String)arg0);
			return this.addAll(c);
		} else if(arg0 instanceof String []) {
			boolean added = false;
			for(int i = 0; i < ((String [])arg0).length; i++) {
				MimeType mimeType = new MimeType(((String[])arg0)[i]);
				updateSpecificity(mimeType);
				if(hashSet.add(mimeType)) {
					added = true;
				}
			}
			return added;
		}else if((arg0 instanceof MimeType)) {
			updateSpecificity(arg0);
			return hashSet.add(arg0);
		}
		throw new MimeException("Parameter must be an instance of a MimeType a String or a String [].");
	}

	/**
	 * Take a collection of objects and adds them to the internal HashSet if they are MimeType(s)
	 * @param arg0 is a collection that should contain MimeType(s). If any of the objects in the collection are
	 * not MimeType(s) they are ignored.
	 * @return true if the internal HashMap has been added to fals otherwise.
	 */
	public boolean addAll(final Collection arg0) {
		boolean added = false;
		for(Iterator it = arg0.iterator(); it.hasNext();) {
			Object o = it.next();
			if(o instanceof MimeType) {
				updateSpecificity((MimeType)o);
				if(add((MimeType)o)) {
					added = true;
				}

			}
		}
		return added;
	}

	public void clear() {
		hashSet.clear();
	}

	/**
	 * Checks if this MimeTypeHashSet contains either the MimeType or a String or a String [] of mime types
	 */
	public boolean contains(final Object o) {
		if(o instanceof String) {
			Collection c = new MimeTypeHashSet((String)o);
			return this.containsAll(c);
		} else if(o instanceof String []) {
			Collection c = new MimeTypeHashSet((String [])o);
			return this.containsAll(c);
		}
		return hashSet.contains(o);
	}

	/**
	 * Checks that this MimeTypeHashSet contains this collection of MimeTypes
	 */
	public boolean containsAll(final Collection arg0) {
		return hashSet.containsAll(arg0);
	}

	public boolean isEmpty() {
		return hashSet.isEmpty();
	}

	public Iterator iterator() {
		return hashSet.iterator();
	}

	public boolean remove(final Object o) {
		return hashSet.remove(o);
	}

	public boolean removeAll(final Collection arg0) {
		return hashSet.removeAll(arg0);
	}

	public boolean retainAll(final Collection arg0) {
		return hashSet.retainAll(arg0);
	}

	public int size() {
		return hashSet.size();
	}

	public Object[] toArray() {
		return hashSet.toArray();
	}

	public Object[] toArray(final Object[] arg0) {
		return hashSet.toArray(arg0);
	}

	/**
	 * Create a String representation of this Collection as a comma seperated list
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for(Iterator it = iterator(); it.hasNext();) {
			buf.append(((MimeType)it.next()).toString());
			if(it.hasNext()) {
				buf.append(",");
			}
		}
		return buf.toString();
	}

	/**
	 * Checks if this MimeTypeHashSet is equal to i.e. contains ALL of
	 * entries in the passed in argument.
	 * @param o can be a String, String [] or Collection
	 * @return true if this MimeTypeHashSet is the same size as the passed in
	 */
	public boolean equals(final Object o) {
		if(o instanceof MimeType) {
			return match(new MimeTypeHashSet((MimeType)o));

		}else if(o instanceof String) {
			return match(new MimeTypeHashSet((String)o));
		} else if(o instanceof String []) {
			return match(new MimeTypeHashSet((String [])o));

		}else if (o instanceof Collection) {
			return match((Collection)o);
		}
		return false;
	}

	private boolean match(final Collection c) {
		if(this.size() != c.size()) {
			return false;
		}
		MimeType [] mta = (MimeType[])c.toArray(new MimeType [c.size()]);

		for(int i = 0; i < mta.length; i++) {
			if(!this.contains(mta[i])) {
				return false;
			}
		}
		return true;
	}

	private void updateSpecificity(final Object o) {
		if(o instanceof MimeType) {
			updateMimeType((MimeType)o);
		}else {
			Collection mimeTypes = (Collection)o;
			for(Iterator it = mimeTypes.iterator(); it.hasNext();) {
				updateMimeType((MimeType)it.next());
			}
		}
	}

	private void updateMimeType(final MimeType mimeType) {
		if(!hashSet.contains(mimeType)) {
			return;
		}
		// Hate this but I didn't want to use a backing map even though HashSet is backed by a HashMap
		for(Iterator it = hashSet.iterator(); it.hasNext();) {
			MimeType mt = (MimeType)it.next();
			if(mt.equals(mimeType)) {
				mt.setSpecificity(mt.getSpecificity() + 1);
				return;
			}
		}
	}
}
