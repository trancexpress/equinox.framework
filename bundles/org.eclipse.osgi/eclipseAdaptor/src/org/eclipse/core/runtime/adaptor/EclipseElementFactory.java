/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.adaptor;

import java.io.IOException;
import java.security.ProtectionDomain;

import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.framework.adaptor.core.AbstractBundleData;
import org.eclipse.osgi.framework.adaptor.core.AbstractFrameworkAdaptor;
import org.eclipse.osgi.framework.adaptor.core.AdaptorElementFactory;
import org.eclipse.osgi.framework.internal.defaultadaptor.*;

public class EclipseElementFactory implements AdaptorElementFactory {

	public AbstractBundleData createBundleData(AbstractFrameworkAdaptor adaptor, long id) throws IOException {
		return new EclipseBundleData((DefaultAdaptor)adaptor, id);
	}

	public org.eclipse.osgi.framework.adaptor.BundleClassLoader createClassLoader(ClassLoaderDelegate delegate, ProtectionDomain domain, String[] bundleclasspath, AbstractBundleData data) {
		return new EclipseClassLoader(delegate, domain, bundleclasspath, data);
	}
	
}
