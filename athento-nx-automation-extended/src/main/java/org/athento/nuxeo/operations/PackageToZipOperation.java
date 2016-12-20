package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.common.utils.ZipUtils;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.jaxrs.io.documents.PaginableDocumentModelListImpl;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

/**
 * Created by victorsanchez on 19/7/16.
 */
@Operation(id = PackageToZipOperation.ID, category = "Athento", label = "Package document pages to ZIPs", description = "proviPackage document der to ZIPs files page by page")
public class PackageToZipOperation {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory.getLog(PackageToZipOperation.class);

    public static final String ID = "Athento.PackagePagesToZip";

    public static final String ZIP_ENTRY_ENCODING_PROPERTY = "zip.entry.encoding";

    public enum ZIP_ENTRY_ENCODING_OPTIONS {
        ascii
    }

    /**
     * Package size.
     */
    @Param(name = "packageSize", required = false, description = "Number of items per ZIP package")
    protected int packageSize = -1;

    /**
     * Limit file size (in bytes)
     */
    @Param(name = "fileMaxSize", required = false, description = "File max size per ZIP")
    protected int fileMaxSize = -1;

    /**
     * File name format.
     */
    @Param(name = "filename", required = false, description = "It is the filename or filename format.")
    protected String filename;

    /** Entry name from document metadata. */
    @Param(name = "entryNameProperty", required = false, description = "Use a metadata of document as entry filename")
    protected String entryNameProperty;


    /*
     * Filter documents.
     *
     * @param docs to filter
     * @return filtered documents
     * @throws OperationException
     */
    @OperationMethod
    public BlobList run(PaginableDocumentModelListImpl docs) throws Exception {
        // Get provider
        PageProvider<DocumentModel> provider = docs.getProvider();
        // Check package size
        if (packageSize != -1) {
            provider.setMaxPageSize(packageSize);
            provider.setPageSize(packageSize);
        }
        BlobList blobs = new BlobList();
        long totalPages = provider.getNumberOfPages();
        for (int i = 0; i < totalPages; i++) {
            List<DocumentModel> docList = provider.getCurrentPage();
            BlobList blobList = new BlobList();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating ZIP for page " + (i + 1));
            }
            long totalSize = 0L;
            for (DocumentModel doc : docList) {
                Blob blob = (Blob) doc.getPropertyValue("file:content");
                // Check content and limit size for zip
                if (hasContent(blob) && limitSizeIsValid(totalSize, blob)) {
                    if (entryNameProperty != null) {
                        Serializable value = doc.getPropertyValue(this.entryNameProperty);
                        if (value != null) {
                            blob.setFilename(value.toString());
                        }
                    }
                    blobList.add(blob);
                    totalSize += blob.getLength();
                }
            }
            if (!blobList.isEmpty()) {
                File file = File.createTempFile("athento-createzip-", ".tmp");
                file.deleteOnExit();
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
                try {
                    // Generate zip with blobs
                    zip(blobList, out);
                    FileBlob fileBlob = new FileBlob(file);
                    fileBlob.setFilename(getZipFilename(i + 1, blobList.size()));
                    fileBlob.setMimeType("application/zip");
                    blobs.add(fileBlob);
                } catch (Exception e) {
                    throw new Exception("Unable to generate ZIP from blobs.", e);
                } finally {
                    out.finish();
                    out.close();
                }
            }
            provider.nextPage();
        }
        // Rewind provider
        provider.setCurrentPageIndex(0);
        return blobs;
    }

    /**
     * Check limit size for ZIP.
     *
     * @param totalSize
     * @param blob
     * @return
     */
    private boolean limitSizeIsValid(long totalSize, Blob blob) {
        if (fileMaxSize == -1) {
            return true;
        } else {
            if (blob != null) {
                long totalWithDocContentSize = totalSize + blob.getLength();
                return totalWithDocContentSize <= this.fileMaxSize;
            } else {
                return true;
            }
        }
    }

    /**
     * Get filename for Zip given a page index.
     *
     * @param pageIndex
     * @param totalDocsPerPage for page
     * @return
     */
    private String getZipFilename(int pageIndex, long totalDocsPerPage) {
        String filenameResult = "zipAthento";
        if (filename != null) {
            String filename = new String(this.filename);
            filename = expandParam(filename, "page", pageIndex);
            filename = expandParam(filename, "total", totalDocsPerPage);
            filename = expandParam(filename, "date", Calendar.getInstance().getTime());
            // FIXME: Complete with more params
            filenameResult = filename;
        }
        return filenameResult;
    }

    /**
     * Expand param.
     *
     * @param patternFilename
     * @param param
     * @param value
     * @return
     */
    private String expandParam(String patternFilename, String param, Object value) {
        String filenameResult = null;
        if (value instanceof String) {
            filenameResult = patternFilename.replace("${" + param + "}", (String) value);
        } else if (value instanceof Date) {
            String pattern = "\\b\\$\\{" + param + "\\|.*}";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(patternFilename);
            if (m.find()) {
                String group = m.group();
                int start = m.start();
                int end = m.end();
                if (group.contains("|")) {
                    String[] data = group.split("\\|");
                    if (data.length > 1) {
                        // Date replacing
                        String datePattern = data[1].replace("}", "");
                        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
                        String dateString = sdf.format((Date) value);
                        filenameResult = new StringBuffer(patternFilename).replace(start, end, dateString).toString();
                    }
                }
            }
        } else {
            filenameResult = patternFilename.replace("${" + param + "}", value.toString());
        }
        return filenameResult;
    }

    /**
     * Check valid content.
     *
     * @param blob
     * @return
     */
    private boolean hasContent(Blob blob) {
        return blob != null;
    }

    /**
     * Generate ZIP.
     *
     * @param blobs
     * @param out
     * @throws Exception
     */
    protected void zip(BlobList blobs, ZipOutputStream out) throws Exception {
        // use a set to avoid zipping entries with same names
        Collection<String> names = new HashSet<String>();
        int cnt = 1;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generating zip with " + blobs.size() + " blobs.");
        }
        for (Blob blob : blobs) {
            String entry = getFileName(blob);
            if (!names.add(entry)) {
                entry = "renamed_" + (cnt++) + "_" + entry;
            }
            InputStream in = blob.getStream();
            try {
                ZipUtils._zip(entry, in, out);
            } finally {
                in.close();
            }
        }
    }

    /**
     * Get file from blob.
     *
     * @param blob
     * @return
     */
    protected String getFileName(Blob blob) {
        String entry = blob.getFilename();
        if (entry == null) {
            entry = "Unknown_" + System.identityHashCode(blob);
        }
        return escapeEntryPath(entry);
    }


    /**
     * Escape path.
     *
     * @param path
     * @return
     */
    protected String escapeEntryPath(String path) {
        String zipEntryEncoding = Framework.getProperty(ZIP_ENTRY_ENCODING_PROPERTY);
        if (zipEntryEncoding != null && zipEntryEncoding.equals(ZIP_ENTRY_ENCODING_OPTIONS.ascii.toString())) {
            return StringUtils.toAscii(path, true);
        }
        return path;
    }
}
