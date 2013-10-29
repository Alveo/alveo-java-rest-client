package com.nicta.vlabclient.entity;

import java.util.List;

/** A representation of an HCS vLab item list. Modelled fairly closely on the JSON REST model,
 * but at a slightly higher level, with a number of convenience methods to enable 
 * easier retrieval of related objects, for example
 * @author andrew.mackinlay
 *
 */
public interface ItemList {

	/** Get the URI which was used to retrieve this item from the REST API */
	public String getUri();

	/** Get the raw URIs for the items from this list.
	 * 
	 * {@link #getCatalogItems()} provides a higher-level interface to the
	 * candidate items which is probably more useful 
	 * @return an array of URIs, stored as strings.
	 */
	public String[] itemUris();

	/** Return the name of the item list */
	public String name();

	/** Return the number of items in the item list */
	public int numItems();

	/** Fetch the items associated with this item list */
	public List<Item> getCatalogItems();

}