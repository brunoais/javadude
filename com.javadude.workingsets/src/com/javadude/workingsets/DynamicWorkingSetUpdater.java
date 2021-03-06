/*******************************************************************************
 * Copyright (c) 2008 Scott Stanchfield
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.javadude.workingsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetUpdater;

/**
 * Common functionality for both working set updaters.
 * This updater watches for changes to the workspace. In particular:
 * 	If projects are added or removed, we update the dynamic working
 *		sets to add or remove them. This includes projects that are closed.
 *	If a .project file is changed, assume its natures may have changed, so
 *		update nature working sets.
 *
 *	This class keeps track of all dynamic working sets that are in the
 *		workspace, allowing us to iterate over them to update them on the fly.
 * @author Scott Stanchfield
 */
public abstract class DynamicWorkingSetUpdater implements IWorkingSetUpdater {
	private static final Map<Class<?>, Map<String, IWorkingSet>> workingSets_ = Collections.synchronizedMap(new HashMap<Class<?>, Map<String, IWorkingSet>>());
	private String baseId_;

	protected abstract boolean shouldInclude(IResource resource, String workingSetId);

	public DynamicWorkingSetUpdater(String baseId) {
		baseId_ = baseId;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					if (event.getDelta() == null) {
						return;
					}
					event.getDelta().accept(new IResourceDeltaVisitor() {
						public boolean visit(IResourceDelta delta) throws CoreException {
							IProject project = null;
							switch (delta.getKind()) {
								case IResourceDelta.ADDED:
									// if it's a project being added
									if (delta.getResource() instanceof IProject) {
										// add to appropriate working set based on project natures
										project = (IProject) delta.getResource();
										addToWorkingSets(project, null);
										return false;
									}
									break;
								case IResourceDelta.CHANGED: // natures change or project opened/closed
									IResource resource = delta.getResource();
									if ((resource instanceof IProject) && (delta.getFlags() & IResourceDelta.OPEN) != 0) {
										project = (IProject) resource;

									} else if (".project".equals(delta.getResource().getName())) {
										// natures could have changed -- might need to update the working sets
										project = delta.getResource().getProject();
									}
									if (project != null) {
										Set<IWorkingSet> setsContainingProject = setsContainingProject(project);
										// add to appropriate working sets
										addToWorkingSets(project, setsContainingProject);
										for (IWorkingSet workingSet : setsContainingProject) {
	                                        removeFromWorkingSet(workingSet, project);
                                        }
										return false;
									}
									break;
								case IResourceDelta.REMOVED:
									if (delta.getResource() instanceof IProject) {
										project = (IProject) delta.getResource();
										// remove from all working sets
										Set<IWorkingSet> setsContainingProject = setsContainingProject(project);
										for (IWorkingSet workingSet : setsContainingProject) {
											removeFromWorkingSet(workingSet, project);
                                        }
										return false;
									}
									break;
							}
							return true;
						}
					});
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	protected void removeFromWorkingSet(IWorkingSet workingSet, IProject project) {
		IAdaptable[] elements = workingSet.getElements();
		if (elements.length == 0) {
			return;
		}
		List<IAdaptable> newElements = new ArrayList<IAdaptable>();
		boolean found = false;
		for (IAdaptable adaptable : elements) {
			if (adaptable != project) {
				newElements.add(adaptable);
			} else {
				found = true;
			}
		}
		if (!found) {
			return;
		}
		workingSet.setElements(newElements.toArray(new IAdaptable[newElements.size()]));
	}
	protected boolean allowClosedProjects() { return false; }
	protected void addToWorkingSets(IProject project, Set<IWorkingSet> setsContainingProject) {
		if (!allowClosedProjects() && !project.isOpen()) {
			return;
		}
		for (Map.Entry<String, IWorkingSet> entry : getMyWorkingSets().entrySet()) {
			if (shouldInclude(project, entry.getKey())) {
				// add project to working set
				IWorkingSet workingSet = entry.getValue();
				if (setsContainingProject != null) {
					setsContainingProject.remove(workingSet);
				}
				IAdaptable[] elements = workingSet.getElements();
				IAdaptable[] newElements = new IAdaptable[elements.length + 1];
				System.arraycopy(elements, 0, newElements, 0, elements.length);
				newElements[elements.length] = project;
				workingSet.setElements(newElements);
			}
		}
	}
	protected Set<IWorkingSet> setsContainingProject(IProject project) {
		Set<IWorkingSet> workingSetsContainingProject = new HashSet<IWorkingSet>();
		for (IWorkingSet workingSet : getMyWorkingSets().values()) {
			IAdaptable[] elements = workingSet.getElements();
			for (IAdaptable element : elements) {
				if (element.equals(project)) {
					workingSetsContainingProject.add(workingSet);
				}
			}
		}
		return workingSetsContainingProject;
	}
	private String getId(IWorkingSet workingSet) {
		String id = workingSet.getName();
		return id.substring(baseId_.length());
	}

	public void add(IWorkingSet workingSet) {
		Map<String, IWorkingSet> workingSets = workingSets_.get(getClass());
		if (workingSets == null) {
			workingSets = new HashMap<String, IWorkingSet>();
			workingSets_.put(getClass(), workingSets);
		}
		workingSets.put(getId(workingSet), workingSet);
	}

	public boolean contains(IWorkingSet workingSet) {
		Map<String, IWorkingSet> workingSets = workingSets_.get(getClass());
		if (workingSets == null) {
			return false;
		}
		return workingSets.values().contains(workingSet);
	}

	public void dispose() {
		workingSets_.remove(getClass());
	}

	public boolean remove(IWorkingSet workingSet) {
		Map<String, IWorkingSet> workingSets = workingSets_.get(getClass());
		if (workingSets == null) {
			return false;
		}
		return workingSets.remove(getId(workingSet)) != null;
	}

	private Map<String, IWorkingSet> getMyWorkingSets() {
		Map<String, IWorkingSet> workingSets = workingSets_.get(getClass());
		if (workingSets == null) {
			return Collections.emptyMap();
		}
		return workingSets;
	}
}
