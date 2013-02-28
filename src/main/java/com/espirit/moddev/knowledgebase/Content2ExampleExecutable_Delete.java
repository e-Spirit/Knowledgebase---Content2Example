package com.espirit.moddev.knowledgebase;

/*
 * //**********************************************************************
 * Content2Example
 * %%
 * Copyright (C) 2013 e-Spirit AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *********************************************************************//*
 */


import de.espirit.common.Logging;
import de.espirit.firstspirit.access.script.Executable;
import de.espirit.firstspirit.access.script.ExecutionException;
import java.io.Writer;
import java.util.Map;

import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.store.contentstore.Content2ScriptContext;
import de.espirit.firstspirit.access.store.contentstore.ContentStoreRoot;
import de.espirit.firstspirit.access.store.contentstore.Content2;

import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.templatestore.Schema;

import de.espirit.firstspirit.agency.LanguageAgent;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.agency.StoreAgent;

import de.espirit.or.EntityList; 
import de.espirit.or.Session;
import de.espirit.or.schema.Entity;
import de.espirit.or.query.Equal;
import de.espirit.or.query.Select;

import de.espirit.firstspirit.ui.operations.RequestOperation;

import javax.script.ScriptContext;


/*
 * Example how to delete the data record (entity) with the id 130 in the FirstSpirit datasource (Content2-object) "pressreleases"
 * 
 * Template:
 * $CMS_RENDER(script:"executablescript",name:"test")$
 * 
 * Script:
 * #!executable-class
 * com.espirit.moddev.knowledgebase.Content2ExampleExecutable_Delete
*/

public class Content2ExampleExecutable_Delete implements Executable {

    Long entityId = 130L;  // the id of the entity you are working with#

    public Object execute(Map<String, Object> params) throws ExecutionException {
        final Content2ScriptContext context = (Content2ScriptContext) params.get("context");

        // get the MasterLanguage
        LanguageAgent languageAgent = ((SpecialistsBroker)context).requireSpecialist(LanguageAgent.TYPE);
        Language lang = languageAgent.getMasterLanguage();

        // get the content- and the templatestore
        StoreAgent storeAgent = ((SpecialistsBroker)context).requireSpecialist(StoreAgent.TYPE);
        ContentStoreRoot cs = (ContentStoreRoot) storeAgent.getStore(Store.Type.CONTENTSTORE);

        // get the content-store-element “pressreleases”
        Content2 csPressreleases = (Content2)cs.getStoreElement("pressreleases", Content2.UID_TYPE);

        // get the datasource schema and a session
        Schema schema = csPressreleases.getSchema();
        Session session = schema.getSession();

        //delete database entity
        Select select = session.createSelect(csPressreleases.getEntityType().getName());
        Equal eq = new Equal("fs_id", entityId.toString());
        select.setConstraint(eq);

        EntityList entityList = session.executeQuery(select);
        Entity entityPressrelease = entityList.get(0);
        session.delete(entityPressrelease);
        session.commit();


        //show a confirmation dialog in case of success
        //get operation agent

        OperationAgent operationAgent = context.requireSpecialist(OperationAgent.TYPE);

        //success message
        RequestOperation requestOperation = operationAgent.getOperation(RequestOperation.TYPE);
        requestOperation.setKind(RequestOperation.Kind.INFO);
        requestOperation.setTitle("Succes");
        requestOperation.perform("Pressrelease successfully deleted.");

        return true;
    }

    public Object execute(Map<String, Object> context, Writer out, Writer err) throws ExecutionException {
        return execute(context);
    }
}
