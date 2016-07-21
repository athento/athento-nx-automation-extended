/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.athento.nuxeo.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.query.api.AbstractPageProvider;

import java.util.List;

/**
 * DocumentModel list provider with no only one page.
 * It overrides {@link org.nuxeo.ecm.platform.query.core.DocumentModelListPageProvider} because it is using only one page.
 *
 * @author victorsanchez
 */
public class DocumentModelListPageProvider extends AbstractPageProvider<DocumentModel> {

    private static final Log LOG = LogFactory.getLog(DocumentModelListPageProvider.class);

    private static final long serialVersionUID = 1L;

    protected final DocumentModelList docs;

    public DocumentModelListPageProvider() {
        docs = new DocumentModelListImpl();
    }

    @Override
    public List<DocumentModel> getCurrentPage() {
        long currentPageIndex = getCurrentPageIndex();
        int pageSize = (int) getPageSize();
        int startIdx = (int) (currentPageIndex * pageSize);
        int endIdx = startIdx + pageSize;
        return docs.subList(startIdx, endIdx < docs.size() ? endIdx : docs.size());
    }

    public DocumentModelListPageProvider(DocumentModelList docs) {
        this.docs = docs;
    }

    public void setDocumentModelList(List<DocumentModel> docs) {
        this.docs.addAll(docs);
    }

    public DocumentModelList getDocumentModelList() {
        return new DocumentModelListImpl(docs);
    }

    @Override
    public long getResultsCount() {
        return docs.size();
    }


}
