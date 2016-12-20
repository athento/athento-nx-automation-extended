package org.athento.nuxeo.operations;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Transform a CSV file to DocumentList given a document type.
 */
@Operation(id = CSVToListOperation.ID, category = "Athento", label = "CSV to Document List", description = "Transform a CSV file to DocumentList."
        , since = "6.0")
public class CSVToListOperation {

    private static final Log LOG = LogFactory.getLog(CSVToListOperation.class);

    public static final String ID = "Athento.CSVToList";

    @Context
    protected CoreSession session;

    @Param(name = "path", required = true, description = "CSV File absolute path")
    protected String csvFilePath;

    @Param(name = "columnId", required = false, description = "It is the column id to get the document from repository.")
    protected String columnId = "ecm:uuid";

    @Param(name = "delimiter", required = false, description = "It is the delimiter of CSV.")
    protected String delimiter = ",";

    @Param(name = "charset", required = false, description = "It is the charset to parse the CSV file.")
    protected String charset;

    /**
     * Run operation.
     *
     * @return document list
     * @throws OperationException on error
     */
    @SuppressWarnings("unchecked")
    @OperationMethod
    public DocumentModelList run() throws OperationException {
        File csvFile = new File(csvFilePath);
        if (!csvFile.exists()) {
            throw new OperationException("File " + csvFilePath + " is not found!");
        }

        // Check delimiter
        if (delimiter.length() > 1) {
            delimiter = delimiter.substring(0, 1);
        }

        // List result
        DocumentModelList docList = new DocumentModelListImpl();

        try {
            CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delimiter.charAt(0)).withHeader(columnId);
            CSVParser csvParser = CSVParser.parse(csvFile,
                    charset == null ? Charset.defaultCharset() : Charset.forName(charset),
                    format);
            if (LOG.isInfoEnabled()) {
                LOG.info("File " + csvFile.getAbsolutePath() + " was parsed with " + charset + ", " + delimiter
                        + " with " + csvParser.getRecordNumber() + " row(s)");
            }
            // Parse csv rows
            Iterator<CSVRecord> it = csvParser.iterator();
            while (it.hasNext()) {
                CSVRecord record = it.next();
                try {
                    String uuid = record.get(columnId);
                    try {
                        DocumentModel rowDoc = session.getDocument(new IdRef(uuid));
                        docList.add(rowDoc);
                    } catch (ClientException e) {
                        LOG.warn("Parse CSV error because document with id " + uuid + " is not found");
                    }
                } catch (IllegalArgumentException e) {
                    LOG.warn("Parse CSV document error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.error("Unable to parse CSV file to list", e);
        }
        return docList;
    }

}
