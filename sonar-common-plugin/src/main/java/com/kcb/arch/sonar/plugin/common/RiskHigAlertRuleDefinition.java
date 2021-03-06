package com.kcb.arch.sonar.plugin.common;

import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

public class RiskHigAlertRuleDefinition implements RulesDefinition {

	public static final String REPOSITORY = "java-example";
	public static final String JAVA_LANGUAGE = "java";
	public static final RuleKey RULE_ON_LINE_1 = RuleKey.of(REPOSITORY, "line1");
	
	@Override
	public void define(Context context) {
		NewRepository repository = context.createRepository(REPOSITORY, JAVA_LANGUAGE).setName("My Custom Java Analyzer");
	    NewRule x1Rule = repository.createRule(RULE_ON_LINE_1.rule())
	      .setName("Stupid rule")
	      .setHtmlDescription("Generates an issue on every line 1 of Java files")
	      .setSeverity(Severity.BLOCKER)
	      .setType(RuleType.VULNERABILITY)
	      // optional tags
	      .setTags("stuko", "stupid")
	      // optional status. Default value is READY.
	      .setStatus(RuleStatus.READY);
	      // default severity when the rule is activated on a Quality profile. Default value is MAJOR.
	    x1Rule.setDebtRemediationFunction(x1Rule.debtRemediationFunctions().linearWithOffset("1h", "30min"));
	    // don't forget to call done() to finalize the definition
	    repository.done();
	    System.out.println("@@@@@@@@@ create rule.............");
	}

}
