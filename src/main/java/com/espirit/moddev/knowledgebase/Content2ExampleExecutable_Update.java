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


import com.sun.org.apache.xpath.internal.operations.Operation;
import de.espirit.common.Logging;
import de.espirit.firstspirit.access.script.Executable;
import de.espirit.firstspirit.access.script.ExecutionException;
import java.io.Writer;
import java.util.Map;
import java.text.SimpleDateFormat;

import de.espirit.firstspirit.access.editor.fslist.IdProvidingFormData;
import de.espirit.firstspirit.access.editor.value.DomElement;

import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.contentstore.Content2ScriptContext;
import de.espirit.firstspirit.access.store.contentstore.ContentStoreRoot;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.templatestore.Schema;

import de.espirit.firstspirit.agency.LanguageAgent;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.agency.StoreAgent;

import de.espirit.firstspirit.forms.FormData;
import de.espirit.firstspirit.forms.FormDataList;
import de.espirit.firstspirit.forms.FormField;

import de.espirit.or.schema.Entity;
import de.espirit.or.Session;

import de.espirit.firstspirit.ui.operations.RequestOperation;


/*
 * Example how to update a data record (entity) in a FirstSpirit datasource (Content2-object)
 * 
 * Template:
 * $CMS_RENDER(script:"executablescript",name:"test")$
 * 
 * Script:
 * #!executable-class
 * com.espirit.moddev.knowledgebase.Content2ExampleExecutable_Update
*/

public class Content2ExampleExecutable_Update implements Executable {

    final String addHeadline = " number two";
    final String newDate = "2012-03-21";
	  final String addContentHeadline = " with information";
	  final String addContentText = " in FS_LIST";
    final Long entityId = 128L;  // the id of the entity you are working with


    public Object execute(Map<String, Object> params) throws ExecutionException {
        final Content2ScriptContext context = (Content2ScriptContext) params.get("context");

        // get the MasterLanguage
        LanguageAgent languageAgent = ((SpecialistsBroker)context).requireSpecialist(LanguageAgent.TYPE);
        Language lang = languageAgent.getMasterLanguage();

        // get the contentstore
        StoreAgent storeAgent = ((SpecialistsBroker)context).requireSpecialist(StoreAgent.TYPE);
        ContentStoreRoot cs = (ContentStoreRoot) storeAgent.getStore(Store.Type.CONTENTSTORE);

        // get the content-store-element “pressreleases”
        Content2 csPressreleases = (Content2)cs.getStoreElement("pressreleases", Content2.UID_TYPE);

        // get the datasource schema and a session
        Schema schema = csPressreleases.getSchema();
        Session session = schema.getSession();

        // get entity from datasource
	    Entity entityPressrelease = session.find(csPressreleases.getEntityType().getName(), entityId);
        {
            try {
                csPressreleases.lock(entityPressrelease);

                Dataset dataSet = csPressreleases.getDataset(entityPressrelease);
                FormData data = dataSet.getFormData();

                // add text to headline into database
                FormField formField = data.get(lang, "cs_headline");
                formField.set(formField.get() + addHeadline);

                // change date in database
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                data.get(lang, "cs_date").set(sdf.parse(newDate)); // simply overwrite the existing value

                // FS-List
                FormField ff = data.get(lang, "cs_content");//cs_content is the FS_LIST
                FormDataList dataList = (FormDataList) ff.get();

                IdProvidingFormData formData = dataList.get(0);

                // just add content into this paragraph as above:
                formField = formData.get(lang, "st_headline");
                formField.set(formField.get() + addContentHeadline);

                 // BEGIN DOM in FS-List

                   // just get and set the FormField like before
                    DomElement dom = (DomElement) formData.get(lang, "st_text").get();
                    String text = "" + dom.toText(false); //reads the existing text
                    dom.set(text + addContentText); //adds the new text to the old one

                 // END DOM

               // set the FormDataList in the FS_LIST
              ff.set(dataList);

               // set the FormData and save the dataSet
               dataSet.setFormData(data);
               dataSet.save();

               /*
                *  Should the dataSet be released?
                */
                csPressreleases.release(entityPressrelease);

            } catch (Exception ex) {
                // context.logError("Creation failed. Exception was: " + ex.printStackTrace());
                ex.printStackTrace();
            } finally {
                try {
                    csPressreleases.unlock(entityPressrelease);
                } catch (Exception e) {
                    // context.logError("Unlocking of element failed. Exception was: \n" + e);
                    e.printStackTrace();
                }
            }
        }

        OperationAgent operationAgent = context.requireSpecialist(OperationAgent.TYPE);
        RequestOperation requestOperation = operationAgent.getOperation(RequestOperation.TYPE);
        requestOperation.setKind(RequestOperation.Kind.INFO);
        requestOperation.setTitle("Status");
        requestOperation.perform("Pressrelease updated.");

        return true;
    }

    public Object execute(Map<String, Object> context, Writer out, Writer err) throws ExecutionException {
        return execute(context);
    }
}
