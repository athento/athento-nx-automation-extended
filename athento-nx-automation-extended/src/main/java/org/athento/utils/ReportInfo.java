package org.athento.utils;

/**
 * Report info.
 */
public final class ReportInfo {

    private String reportEngine = "jr";
    private String reportAlias;
    private String reportOutput = "pdf";

    public ReportInfo(String reportString) {
        String [] data = reportString.split(":");
        if (data.length == 3) {
            this.reportEngine = data[0];
            this.reportAlias = data[1];
            this.reportOutput = data[2];
        } else if (data.length == 2) {
            this.reportAlias = data[0];
            this.reportOutput = data[1];
        } else if (data.length == 1) {
            this.reportAlias = data[0];
        }
    }

    public String getReportEngine() {
        return reportEngine;
    }

    public void setReportEngine(String reportEngine) {
        this.reportEngine = reportEngine;
    }

    public String getReportAlias() {
        return reportAlias;
    }

    public void setReportAlias(String reportAlias) {
        this.reportAlias = reportAlias;
    }

    public String getReportOutput() {
        return reportOutput;
    }

    public void setReportOutput(String reportOutput) {
        this.reportOutput = reportOutput;
    }

    public String getMimetype() {
        if (this.reportOutput != null) {
            if ("pdf".equals(this.reportOutput)) {
                return "application/pdf";
            } else if ("xls".equals(this.reportOutput)) {
                return "application/vnd.ms-excel";
            } else if ("html".equals(this.reportOutput)) {
                return "html/text";
            }
        }
        return null;
    }
}
