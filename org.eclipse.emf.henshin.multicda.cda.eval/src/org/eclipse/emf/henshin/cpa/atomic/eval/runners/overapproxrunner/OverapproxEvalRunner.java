package org.eclipse.emf.henshin.cpa.atomic.eval.runners.overapproxrunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.henshin.cpa.atomic.ConflictAnalysis;
import org.eclipse.emf.henshin.cpa.atomic.DependencyAnalysis;
import org.eclipse.emf.henshin.cpa.atomic.MultiGranularAnalysis;
import org.eclipse.emf.henshin.cpa.atomic.Span;
import org.eclipse.emf.henshin.cpa.atomic.runner.RulePreparator;
import org.eclipse.emf.henshin.cpa.atomic.tester.CPATester;
import org.eclipse.emf.henshin.cpa.atomic.util.Granularity;
import org.eclipse.emf.henshin.cpa.atomic.util.NonDeletingPreparator;
import org.eclipse.emf.henshin.cpa.atomic.util.RulePair;
import org.eclipse.emf.henshin.cpa.atomic.util.Type;
import org.eclipse.emf.henshin.cpa.result.CriticalPair;
import org.eclipse.emf.henshin.model.Rule;

public abstract class OverapproxEvalRunner {
	String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
	String path= getDomainName() + "\\"+timeStamp+ ".log" ;
		
	public void run(Type type) {
		init();
		List<Rule> rules = getRules();
		prepareRules(rules);
		List<RulePair> nonDeleting = NonDeletingPreparator.prepareNonDeletingVersions(rules);
		initLogs();
	
		doAggBasedCpa( type, rules, nonDeleting);

	}
	
	protected void doAggBasedCpa(Type type, List<Rule> rules,
			List<RulePair> nonDeleting) {
			logn("[AGG] Computing essential critical pairs (R2 deleting):");
			for (Rule r1 : rules) {
				for (RulePair r2 : nonDeleting) {
					long time = System.currentTimeMillis();
					Set<CriticalPair> initspNormal = new HashSet<>();
					CPATester eTester = new CPATester(Collections.singletonList(r1),
							Collections.singletonList(r2.getOriginal()), true, type == Type.dependencies, false, false,
							false, false, true);
					initspNormal.addAll(eTester.getInitialCriticalPairs());
					log(initspNormal.size() + " ");
					tlog(System.currentTimeMillis() - time + " ");

				}
				logbn("   | " + r1.getName());
			}
			logbn("");

			logn("[AGG] Computing essential critical pairs (R2 non-deleting):");
			for (Rule r1 : rules) {
				for (RulePair r2 : nonDeleting) {
					long time = System.currentTimeMillis();
					Set<CriticalPair> initspNormal = new HashSet<>();
					CPATester eTester = new CPATester(Collections.singletonList(r1),
							Collections.singletonList(r2.getCopy()), true, type == Type.dependencies, false, false,
							false, false, true);
					initspNormal.addAll(eTester.getInitialCriticalPairs());
					log(initspNormal.size() + " ");
					tlog(System.currentTimeMillis() - time + " ");
				}
				logbn("   | " + r1.getName());
			}
			logbn("");
	}

	protected void initLogs() {
		try {
			Files.write(Paths.get( "logs\\time\\"+path), new String().getBytes(), StandardOpenOption.CREATE_NEW);
			Files.write(Paths.get( "logs\\results\\"+path), new String().getBytes(), StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void logbn(String string) {
		log(string+"\n");
		tlog(string+"\n");
	}

	protected void tlog(String string) {
			try {
				Files.write(Paths.get( "logs\\time\\"+path), string.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	protected void log(String string) {
		System.out.print(string);
		try {
			Files.write(Paths.get( "logs\\results\\"+path), string.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void logn(String string) {
		log(string+ "\n");
	}


	private MultiGranularAnalysis getAnalysis(Rule r1, Rule r2, Type type) {
		switch (type) {
		case conflicts:
			return new ConflictAnalysis(r1, r2);
		case dependencies:
			return new DependencyAnalysis(r1, r2);
		}
		return null;
	}

	private static void prepareRules(List<Rule> rules) {
		List<Rule> prepared = new ArrayList<Rule>();
		rules.removeAll(rules.stream().filter(r -> !r.getMultiRules().isEmpty()).collect(Collectors.toList()));
		rules.forEach(r -> prepared.add(RulePreparator.prepareRule(r)));
		rules.clear();
		rules.addAll(prepared);
	}

	public abstract void init();

	public abstract List<Rule> getRules();

	public abstract String getDomainName();
	
}
