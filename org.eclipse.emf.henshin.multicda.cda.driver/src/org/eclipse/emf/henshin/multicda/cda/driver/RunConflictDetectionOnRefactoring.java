package org.eclipse.emf.henshin.multicda.cda.driver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.cpa.atomic.ConflictAnalysis;
import org.eclipse.emf.henshin.cpa.atomic.MultiGranularAnalysis;
import org.eclipse.emf.henshin.cpa.atomic.Span;
import org.eclipse.emf.henshin.cpa.atomic.runner.RulePreparator;
import org.eclipse.emf.henshin.cpa.atomic.util.Granularity;
import org.eclipse.emf.henshin.cpa.atomic.util.HenshinRuleLoader;
import org.eclipse.emf.henshin.cpa.atomic.util.NonDeletingPreparator;
import org.eclipse.emf.henshin.cpa.atomic.util.RulePair;
import org.eclipse.emf.henshin.cpa.atomic.util.Type;
import org.eclipse.emf.henshin.model.Rule;

public class RunConflictDetectionOnRefactoring {
	public static List<Granularity> granularities =  Arrays.asList(
			Granularity.binary,
			Granularity.coarse,
			Granularity.fine
			);
	
	String logTimeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
	String path= getDomainName() + "\\"+logTimeStamp+ ".log" ;
	private ResourceSetImpl resourceSet;

	public static void main(String[] args) {
		new RunConflictDetectionOnRefactoring().run(granularities);
	}
	
	public void run(List<Granularity> granularities) {
		init();
		List<Rule> rules = getRules();
		prepareRules(rules);
		List<RulePair> nonDeleting = NonDeletingPreparator.prepareNonDeletingVersions(rules);
		doMultiGranularCDA(granularities, rules, nonDeleting);
	}
	

	public void init() {
		EcorePackage.eINSTANCE.eClass();

		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		initLogs();
	}

	public List<Rule> getRules() {
		final File f = new File(RunConflictDetectionOnRefactoring.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String filePath = f.toString();
		String projectPath = filePath.replaceAll("bin", "");
		String subDirectoryPath = "rules\\refactoring";
		String fullSubDirectoryPath = projectPath + subDirectoryPath;
		File dir = new File(fullSubDirectoryPath);
		return HenshinRuleLoader.loadAllRulesFromFileSystemPaths(dir);
	}
	

	public String getDomainName() {
		return "refactoring";
	}

	protected void doMultiGranularCDA(List<Granularity> granularities,  List<Rule> rules,
			List<RulePair> nonDeleting) {
		logbn("Starting CDA with " + rules.size() + " rules.");

		if (granularities.contains(Granularity.atoms)) {
			logn("[MultiCDA] Computing conflict atoms:");
			for (Rule r1 : rules) {
				for (RulePair r2 : nonDeleting) {
					long time = System.currentTimeMillis();
					MultiGranularAnalysis ca = 
							 new ConflictAnalysis(r1, r2.getCopy());
					Set<Span> result = ca.computeAtoms();
					log(result.size() + " ");
					tlog(System.currentTimeMillis() - time + " ");
				}
				logbn("   | " + r1.getName());
			}
			logbn("");
		}

		if (granularities.contains(Granularity.binary)) {
			logn("[MultiCDA] Computing binary granularity:");
			for (Rule r1 : rules) {
				for (RulePair r2 : nonDeleting) {
					long time = System.currentTimeMillis();
					MultiGranularAnalysis ca = 
							 new ConflictAnalysis(r1, r2.getCopy());
					Span result = ca.computeResultsBinary();
					log(result == null ? "0 " : "1 ");
					tlog(System.currentTimeMillis() - time + " ");
				}
				logbn("   | " + r1.getName());
			}
			logbn("");
		}

		if (granularities.contains(Granularity.coarse)) {
			logn("[MultiCDA] Computing minimal conflict reasons:");
			for (Rule r1 : rules) {
				for (RulePair r2 : nonDeleting) {
					long time = System.currentTimeMillis();
					MultiGranularAnalysis ca = 
							 new ConflictAnalysis(r1, r2.getCopy());
					Set<Span> result = ca.computeResultsCoarse();
					log(result.size() + " ");
					tlog(System.currentTimeMillis() - time + " ");
				}
				logbn("   | " + r1.getName());
			}
			logbn("");
		}

		if (granularities.contains(Granularity.fine)) {
			logn("[MultiCDA] Computing initial conflict reasons:");
			for (Rule r1 : rules) {
				List<Integer> resultRow = new ArrayList<Integer>();
				for (RulePair r2 : nonDeleting) {
					long time = System.currentTimeMillis();
					MultiGranularAnalysis ca = 
							 new ConflictAnalysis(r1, r2.getCopy());
					Set<Span> result = ca.computeResultsFine();
					log(result.size() + " ");
					tlog(System.currentTimeMillis() - time + " ");
					resultRow.add(result.size());
				}
				logbn("   | " + r1.getName());
			}
			logbn("");
		}
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



	private static void prepareRules(List<Rule> rules) {
		List<Rule> prepared = new ArrayList<Rule>();
		rules.removeAll(rules.stream().filter(r -> !r.getMultiRules().isEmpty()).collect(Collectors.toList()));
		rules.forEach(r -> prepared.add(RulePreparator.prepareRule(r)));
		rules.clear();
		rules.addAll(prepared);
	}


	
}
