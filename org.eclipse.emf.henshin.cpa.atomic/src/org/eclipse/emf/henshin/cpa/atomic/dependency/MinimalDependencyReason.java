package org.eclipse.emf.henshin.cpa.atomic.dependency;

import java.util.Set;

import org.eclipse.emf.henshin.cpa.atomic.Span;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;

public class MinimalDependencyReason extends Span{

	public MinimalDependencyReason(Mapping nodeInRule1Mapping, Graph s1, Mapping nodeInRule2Mapping) {
		super(nodeInRule1Mapping, s1, nodeInRule2Mapping);
	}

	public MinimalDependencyReason(Set<Mapping> rule1Mappings, Graph s1, Set<Mapping> rule2Mappings) {
		super(rule1Mappings, s1, rule2Mappings);
	}

	public MinimalDependencyReason(Span extSpan, Node origin, Node image) {
		super(extSpan, origin, image);
	}

	public MinimalDependencyReason(Span s1) {
		super(s1);
	}
	

}
