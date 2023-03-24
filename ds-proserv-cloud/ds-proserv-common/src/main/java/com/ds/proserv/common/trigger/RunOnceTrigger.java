package com.ds.proserv.common.trigger;

import java.util.Date;

import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.PeriodicTrigger;

public class RunOnceTrigger extends PeriodicTrigger {
	public RunOnceTrigger(long period) {
		super(period);
		setInitialDelay(period);
	}

	@Override
	public Date nextExecutionTime(TriggerContext triggerContext) {
		if (triggerContext.lastCompletionTime() == null) { // hasn't executed yet
			return super.nextExecutionTime(triggerContext);
		}
		return null;
	}
}