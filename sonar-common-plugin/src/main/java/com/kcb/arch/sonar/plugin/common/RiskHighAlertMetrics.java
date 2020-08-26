package com.kcb.arch.sonar.plugin.common;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

public class RiskHighAlertMetrics implements Metrics {

	private final Logger log = LoggerFactory.getLogger(RiskHighAlertMetrics.class);
	
    public static final Metric RISK_HIGH_SOURCE =
        new Metric.Builder(
            "Risk High Source",
            "KCB Sonarqube Plugin",
            Metric.ValueType.STRING)
            .setDescription("Sonarqube's risk high source decider")
            .setQualitative(true)
            .setDomain(CoreMetrics.DOMAIN_ISSUES)
            .create();

    public RiskHighAlertMetrics() {
        super();
        System.out.println("####### Metrics created ....." );
    }

    public List<Metric> getMetrics() {
    	System.out.println("###### getMetrics....");
        return Arrays.asList(RISK_HIGH_SOURCE);
    }
    
}