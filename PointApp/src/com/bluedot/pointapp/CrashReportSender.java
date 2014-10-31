package com.bluedot.pointapp;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.util.JSONReportBuilder.JSONReportException;

import au.com.bluedot.point.net.engine.Logger;

public class CrashReportSender implements ReportSender {

	public CrashReportSender() {
	}

	@Override
	public void send(CrashReportData report) throws ReportSenderException {
		try {
			Logger.file("Bluedot", "error", report.toJSON().toString());
		} catch (JSONReportException e) {
			e.printStackTrace();
		}
	}
}
