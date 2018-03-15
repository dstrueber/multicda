# Multi-Granular Conflict and Dependency Analysis

*Prototype implementation of the analysis technique presented in the ICSE paper "Multi-Granular Conflict and Dependency Analysis in Software Engineering based on Graph Transformation" (Lambers, Strüber, Taentzer, Born, Huebert).*

This repository is a fork of https://github.com/KristopherBorn/multiCDA. We here provide a stable, self-contained version of the prototype, while the latter repository is used to coordinate the integration of the technique into the Henshin core.

## Setup instructions

Requirements
* An Eclipse installation with EMF, preferably the Eclipse Modeling Tools (EMT) distribution. We recommend [Eclipse Oxygen  2, EMT](https://www.eclipse.org/downloads/packages/eclipse-modeling-tools/oxygen2) and Java 8.

Deployment
* Import all projects from this repository into your Eclipse workspace.

## Simple example

The project *org.eclipse.emf.henshin.multicda.cda.driver* contains a simple usage example.
* The folder *rules/refactoring* contains 8 selected refactoring rules, which we use to illustrate conflict detection.
* The class *RunConflictDetectionOnRefactoring.java* contains the code for executing our conflict detection on the example rule set. 

The class RunConflictDetectionOnRefactoring supports a configuration option regarding the desired granularity level: Our technique supports the detection of conflicts on binary, coarse-grained and fine-grained granularity. In a nutshell, binary specifies whether a conflict exists or not, coarse-grained identifies in the given pair of rules all problematic deletion components that lead to a conflict, and fine-grained gives one representative example of each possible conflict. The results of fine-grained conflict detection correspond to the notion of *initial conflicts*, a distinguished subset of the set of critical pairs (which retains the completeness and local-confluence properties initially provided by critical pairs).

Per default, the analysis is performed on all three granularity levels:
>	public static List<Granularity> granularities =  Arrays.asList(
>			Granularity.binary,
>			Granularity.coarse,
>			Granularity.fine
>			);
			
When executing the class,  the expected console output looks as follows. For each granularity level, we see a matrix representation of the detected number of conflicts:

> Starting CDA with 8 rules.
>[MultiCDA] Computing binary granularity:
>1 1 1 0 0 0 0 1    | decapsulateAttribute
>1 1 1 1 0 0 0 1    | pullUpEncapsulatedAttribute
>1 1 1 0 0 0 0 1    | moveMethod
>1 1 0 1 0 0 0 1    | moveAttribute
>0 0 1 1 1 1 0 0    | deleteClass
>0 0 0 0 1 1 1 1    | introduceNewPackageForSingleClass
>0 0 0 0 1 1 1 1    | newPackageForImplementations
>0 0 1 1 1 1 1 1    | joinClassesWithCommonSuperclass

> [MultiCDA] Computing minimal conflict reasons:
>2 2 2 0 0 0 0 2    | decapsulateAttribute
>5 5 2 1 0 0 0 3    | pullUpEncapsulatedAttribute
>2 2 1 0 0 0 0 1    | moveMethod
>1 1 0 1 0 0 0 1    | moveAttribute
>0 0 1 1 1 1 0 0    | deleteClass
>0 0 0 0 1 1 3 1    | introduceNewPackageForSingleClass
>0 0 0 0 2 2 6 2    | newPackageForImplementations
>0 0 2 2 1 1 2 2    | joinClassesWithCommonSuperclass

> [MultiCDA] Computing initial conflict reasons:
>3 3 2 0 0 0 0 2    | decapsulateAttribute
>13 13 2 1 0 0 0 5    | pullUpEncapsulatedAttribute
>2 2 1 0 0 0 0 1    | moveMethod
>1 1 0 1 0 0 0 1    | moveAttribute
>0 0 1 1 1 1 0 0    | deleteClass
>0 0 0 0 1 1 3 1    | introduceNewPackageForSingleClass
>0 0 0 0 2 2 12 2    | newPackageForImplementations
>0 0 2 2 1 1 2 2    | joinClassesWithCommonSuperclass

Inspecting the reported results in more detail is possible by using the provided API. The result on each level is represented as a set of Spans, where a Span consists of a graph and its mappings to the two rules from the given rule pair.

For example, to inspect the nodes in the results on the coarse-grained level, add the following lines after line 128: 

>	for (Span span : result) {
>						System.out.println();
>						System.out.println(span.getGraph().getNodes());
>						System.out.println(span.getMappingsInRule1());
>						System.out.println(span.getMappingsInRule2());
>						System.out.println();
					}
The expected console output should now be:

>  [Node 14.4_10.3:Class, Node 14.1_10.1:Package]
> [Node 14.1_10.1:Package -> Node 14.1:Package, Node 14.4_10.3:Class -> Node 14.4:Class]
> [Node 14.1_10.1:Package -> Node 10.1:Package, Node 14.4_10.3:Class -> Node 10.3:Class]


## Reproduce  ICSE18 evaluation

The project *org.eclipse.emf.henshin.cpa.atomic.eval* includes various rule sets together with **Runner.java* classes for reproducing the evaluation from our ICSE18 paper. On execution, the evaluation results are written to the *logs* directory.
