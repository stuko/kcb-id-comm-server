package com.kcb.arch.sonar.plugin.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;


public class RiskHighAlertSensor implements Sensor {

    private final Logger log = LoggerFactory.getLogger(RiskHighAlertSensor.class);
    
    @Override
    public void describe(SensorDescriptor descriptor) {
    	System.out.println("####### Sensor describe ....." );
      descriptor.name("Risk High Alert file Sensor");
    }

    @Override
    public void execute(SensorContext context) {
      System.out.println("####### Sensor execute ....." );
      FileSystem fs = context.fileSystem();
      Iterable<InputFile> files = fs.inputFiles(fs.predicates().hasType(InputFile.Type.MAIN));
      for (InputFile file : files) {
    	  System.out.println("####### file name : " + file.filename() );
    	  if(file.filename().indexOf("Mapper") >= 0) {
	        context.<String>newMeasure()
	          .forMetric(RiskHighAlertMetrics.RISK_HIGH_SOURCE)
	          .on(file)
	          .withValue(file.filename())
	          .save();
	
	        NewIssue newIssue = context.newIssue();
	        newIssue.forRule(RiskHigAlertRuleDefinition.RULE_ON_LINE_1);
	        
	        NewIssueLocation newLocation = new DefaultIssueLocation()
	        		.on(file)
	        		.at(file.selectLine(10))
	        		.message("Java Issue created........................");
    		
	        newIssue.at(newLocation);
	        System.out.println(newIssue);
            newIssue.save();
	        
    	  }else {
    		  System.out.println("####### file dont have Mapper");
    	  }
      }
    }
}