# Multi-Granular Conflict and Dependency Analysis

*Implementation prototype of the analysis technique presented in the ICSE2018 paper "Multi-Granular Conflict and Dependency Analysis in Software Engineering based on Graph Transformation" (Lambers, Strüber, Taentzer, Born, Huebert).*

This repository is a fork of https://github.com/KristopherBorn/multiCDA. We here provide a stable, self-contained version of the prototype, while the latter repository is used to coordinate the integration of the technique into the Henshin core.

## Setup instructions

**Requirements**
* An Eclipse installation with EMF, preferably the Eclipse Modeling Tools (EMT) distribution. We recommend [Eclipse Oxygen  2, EMT](https://www.eclipse.org/downloads/packages/eclipse-modeling-tools/oxygen2) and Java 8.

**Deployment**
* Import all projects from this repository into your Eclipse workspace.

## Simple example

The project *org.eclipse.emf.henshin.multicda.cda.driver* contains a simple usage example.

* The folder *rules/refactoring* contains 8 selected refactoring rules, which we use to illustrate conflict detection.
* The class *RunConflictDetectionOnRefactoring.java* contains the code for executing our conflict detection on the example rule set. 

The class *RunConflictDetectionOnRefactoring* supports a configuration option regarding the desired granularity level: Our technique supports the detection of conflicts on three granularity levels:

 * **binary** specifies whether a conflict exists or not
 * **coarse-grained** identifies for a  given pair of rules all *minimal conflict reasons*, that is, problematic deletion components that lead to a conflict,
 * **fine-grained** identifies one representative example, called *initial conflict reason*, of each possible conflict. Initial conflict reasons correspond to *initial conflicts*, a distinguished subset of the set of critical pairs (which retains the completeness and local-confluence properties initially provided by critical pairs).

Per default, the analysis is performed on all three granularity levels:

```
public static List<Granularity> granularities =  Arrays.asList(
		Granularity.binary,
		Granularity.coarse,
		Granularity.fine
		);
```

Run this class. The expected console output looks as follows - for each granularity level, we see a matrix representation of the detected number of conflicts. Each field in the matrix represents the conflicts of a particular rule pair, e.g. the field with the number "6" in the second matrix denotes the number of results that the rule "newPackageForImplementations" has with itself.


```
Starting CDA with 8 rules.
[MultiCDA] Computing binary granularity:
1 1 1 0 0 0 0 1    | decapsulateAttribute
1 1 1 1 0 0 0 1    | pullUpEncapsulatedAttribute
1 1 1 0 0 0 0 1    | moveMethod
1 1 0 1 0 0 0 1    | moveAttribute
0 0 1 1 1 1 0 0    | deleteClass
0 0 0 0 1 1 1 1    | introduceNewPackageForSingleClass
0 0 0 0 1 1 1 1    | newPackageForImplementations
0 0 1 1 1 1 1 1    | joinClassesWithCommonSuperclass

[MultiCDA] Computing minimal conflict reasons:
2 2 2 0 0 0 0 2    | decapsulateAttribute
5 5 2 1 0 0 0 3    | pullUpEncapsulatedAttribute
2 2 1 0 0 0 0 1    | moveMethod
1 1 0 1 0 0 0 1    | moveAttribute
0 0 1 1 1 1 0 0    | deleteClass
0 0 0 0 1 1 3 1    | introduceNewPackageForSingleClass
0 0 0 0 2 2 6 2    | newPackageForImplementations
0 0 2 2 1 1 2 2    | joinClassesWithCommonSuperclass

[MultiCDA] Computing initial conflict reasons:
3 3 2 0 0 0 0 2    | decapsulateAttribute
13 13 2 1 0 0 0 5    | pullUpEncapsulatedAttribute
2 2 1 0 0 0 0 1    | moveMethod
1 1 0 1 0 0 0 1    | moveAttribute
0 0 1 1 1 1 0 0    | deleteClass
0 0 0 0 1 1 3 1    | introduceNewPackageForSingleClass
0 0 0 0 2 2 12 2    | newPackageForImplementations
0 0 2 2 1 1 2 2    | joinClassesWithCommonSuperclass

```

We can inspect the reported results in more detail, which is supported by the provided API. The results are represented by *Spans*, where a Span consists of a graph and its mappings to the two rules from the given rule pair.

For example, to inspect the nodes in the results on the coarse-grained level, add the following lines after line 128: 

```
for (Span span : result) {
					System.out.println();
					System.out.println(span.getGraph().getNodes());
					System.out.println(span.getMappingsInRule1());
					System.out.println(span.getMappingsInRule2());
					System.out.println();
				}
```

The expected console output should be:


```
[Node 14.4_10.3:Class, Node 14.1_10.1:Package]
[Node 14.1_10.1:Package -> Node 14.1:Package, Node 14.4_10.3:Class -> Node 14.4:Class]
[Node 14.1_10.1:Package -> Node 10.1:Package, Node 14.4_10.3:Class -> Node 10.3:Class]

```

## Reproduce  ICSE18 evaluation

The project *org.eclipse.emf.henshin.cpa.atomic.eval* includes various rule sets together with **Runner.java* classes for reproducing the evaluation from our ICSE18 paper. On execution, the evaluation results are written to the *logs* directory.

## Current limitations/scope

* We only identify **delete-use-conflicts** and **create-use-dependencies** for rules based on typed graphs with node inheritance. Attributes are currently outside our scope.
* Our technique relies on an overapproximation. In our experimentes, the number of fals epositives  (rule pairs for which a conflict was incorrently detected) was between 0% and 12% per rule set. False negatives (rule pairs for which incorrectly no conflict is  detected) should not occur.
