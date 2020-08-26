package com.kcb.arch.sonar.plugin.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Plugin;

public class RiskHighAlertPlugin implements Plugin {
	
	private final Logger log = LoggerFactory.getLogger(RiskHighAlertPlugin.class);
	
	@Override
	public void define(Context context) {
		// implementations of extension points
		System.out.println("####### Plugin define ....." );
		context.addExtensions(RiskHighAlertMetrics.class, RiskHighAlertSensor.class , RiskHigAlertRuleDefinition.class);
	}
}