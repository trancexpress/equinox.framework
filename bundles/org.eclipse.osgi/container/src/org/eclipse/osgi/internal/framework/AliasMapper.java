/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.osgi.internal.framework;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import org.eclipse.osgi.framework.internal.core.Tokenizer;
import org.eclipse.osgi.internal.debug.Debug;

/**
 * This class maps aliases.
 */
public class AliasMapper {
	private static final Map<String, Collection<String>> processorAliasTable = new HashMap<String, Collection<String>>();
	private static final Map<String, String> processorCanonicalTable = new HashMap<String, String>();
	private static final Map<String, Collection<String>> osnameAliasTable = new HashMap<String, Collection<String>>();
	private static final Map<String, String> osnameCanonicalTable = new HashMap<String, String>();
	static {
		getTables("osname.aliases", osnameAliasTable, osnameCanonicalTable); //$NON-NLS-1$
		getTables("processor.aliases", processorAliasTable, processorCanonicalTable); //$NON-NLS-1$
	}

	private static void getTables(String resourceName, Map<String, Collection<String>> aliasTable, Map<String, String> canonicalTable) {
		InputStream in = AliasMapper.class.getResourceAsStream(resourceName);
		if (in != null) {
			try {
				initAliases(in, aliasTable, canonicalTable);
			} finally {
				try {
					in.close();
				} catch (IOException ee) {
					// nothing
				}
			}
		}
	}

	/**
	 * Return the master alias for the processor.
	 *
	 * @param processor Input name
	 * @return aliased name (if any)
	 */
	public Collection<String> getProcessorAliases(String processor) {
		return getAlias(processor.toLowerCase(), processorAliasTable);
	}

	/**
	 * Return the master alias for the osname.
	 *
	 * @param osname Input name
	 * @return aliased name (if any)
	 */
	public Collection<String> getOSNameAliases(String osname) {
		return getAlias(osname.toLowerCase(), osnameAliasTable);
	}

	public String getCanonicalOSName(String osname) {
		String result = osnameCanonicalTable.get(osname.toLowerCase());
		return result == null ? osname : result;
	}

	public String getCanonicalProcessor(String processor) {
		String result = processorCanonicalTable.get(processor.toLowerCase());
		return result == null ? processor : result;
	}

	private Collection<String> getAlias(String name, Map<String, Collection<String>> aliasMap) {
		if (name == null) {
			return Collections.emptyList();
		}
		Collection<String> aliases = aliasMap == null ? null : aliasMap.get(name);
		if (aliases != null) {
			return Collections.unmodifiableCollection(aliases);
		}
		return Collections.singletonList(name.toLowerCase());
	}

	/**
	 * Read alias data and populate a Map.
	 *
	 * @param in InputStream from which to read alias data.
	 * @return Map of aliases.
	 */
	private static Map<String, Collection<String>> initAliases(InputStream in, Map<String, Collection<String>> aliasTable, Map<String, String> canonicalTable) {
		try {
			BufferedReader br;
			try {
				br = new BufferedReader(new InputStreamReader(in, "UTF8")); //$NON-NLS-1$
			} catch (UnsupportedEncodingException e) {
				br = new BufferedReader(new InputStreamReader(in));
			}
			Map<String, Set<String>> multiMaster = new HashMap<String, Set<String>>();
			while (true) {
				String line = br.readLine();
				if (line == null) /* EOF */{
					break; /* done */
				}
				Tokenizer tokenizer = new Tokenizer(line);
				String master = tokenizer.getString("# \t"); //$NON-NLS-1$
				if (master != null) {
					String masterLower = master.toLowerCase();
					canonicalTable.put(masterLower, master);
					Collection<String> aliasLine = new ArrayList<String>(1);
					aliasLine.add(master);
					parseloop: while (true) {
						String alias = tokenizer.getString("# \t"); //$NON-NLS-1$
						if (alias == null) {
							break parseloop;
						}
						aliasLine.add(alias);
						String aliasLower = alias.toLowerCase();
						if (!canonicalTable.containsKey(aliasLower)) {
							canonicalTable.put(aliasLower, master);
						} else {
							// the alias has multiple masters just make its canonical name be the alias
							String existingMaster = canonicalTable.put(aliasLower, alias);
							Set<String> masters = multiMaster.get(aliasLower);
							if (masters == null) {
								masters = new HashSet<String>();
								multiMaster.put(aliasLower, masters);
								masters.add(existingMaster.toLowerCase());
							}
							masters.add(masterLower);
						}
					}
					aliasTable.put(masterLower, aliasLine);
				}
			}
			Map<String, Set<String>> multiMasterAliases = new HashMap<String, Set<String>>(multiMaster.size());
			for (Entry<String, Set<String>> entry : multiMaster.entrySet()) {
				Set<String> aliases = new HashSet<String>();
				for (String master : entry.getValue()) {
					aliases.addAll(aliasTable.get(master));
				}
				multiMasterAliases.put(entry.getKey(), aliases);
			}
			aliasTable.putAll(multiMasterAliases);
		} catch (IOException e) {
			Debug.printStackTrace(e);
		}
		return aliasTable;
	}
}
