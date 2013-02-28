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
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.script.Executable;
import de.espirit.firstspirit.access.script.ExecutionException;
import java.io.Writer;
import java.util.Map;
import java.text.SimpleDateFormat;

import de.espirit.firstspirit.access.editor.fslist.IdProvidingFormData;
import de.espirit.firstspirit.access.editor.value.SectionFormsProducer;

import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.contentstore.ContentStoreRoot;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.templatestore.Schema;
import de.espirit.firstspirit.access.store.templatestore.TemplateStoreRoot;

import de.espirit.firstspirit.agency.LanguageAgent;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;

import de.espirit.firstspirit.forms.FormData;
import de.espirit.firstspirit.forms.FormDataList;
import de.espirit.firstspirit.forms.FormField;

import de.espirit.or.schema.Entity;
import de.espirit.or.Session;

/*
 * Example how to create a data record (entity) in a FirstSpirit datasource (Content2-object)
 * Template:
 * $CMS_RENDER(script:"executablescript",name:"test")$
 * 
 * Script:
 * #!executable-class
 * com.espirit.moddev.knowledgebase.Content2ExampleExecutable_Create
*/

public class Content2ExampleExecutable_Create implements Executable {

  final String headline = "Important news";
	final String subheadline = "Short information";
	final String teaser = "Look out!!";
	final String date = "2012-02-21";
	final String contentHeadline = "Content Headline";
	final String contentText = "Content Text";

    public static final Class<?> LOGGER = Content2ExampleExecutable_Create.class;

    public Object execute(Map<String, Object> params) throws ExecutionException {
        final Object context = params.get("context");

        // get the MasterLanguage
        LanguageAgent languageAgent = ((SpecialistsBroker)context).requireSpecialist(LanguageAgent.TYPE);
        Language lang = languageAgent.getMasterLanguage();

        // get the content- and the templatestore
        StoreAgent storeAgent = ((SpecialistsBroker)context).requireSpecialist(StoreAgent.TYPE);
        ContentStoreRoot cs = (ContentStoreRoot) storeAgent.getStore(Store.Type.CONTENTSTORE);
        TemplateStoreRoot ts = (TemplateStoreRoot) storeAgent.getStore(Store.Type.TEMPLATESTORE);

        // get the content-store-element “pressreleases”
        Content2 csPressreleases = (Content2)cs.getStoreElement("pressreleases", Content2.UID_TYPE);

        // get the datasource schema and a session
        Schema schema = csPressreleases.getSchema();
        Session session = schema.getSession();

        // create a new entity
        Entity entityPressrelease = session.createEntity(csPressreleases.getEntityType().getName());
        {
    	    try {

                csPressreleases.lock(entityPressrelease);

                Dataset dataSet = csPressreleases.getDataset(entityPressrelease);
                FormData data = dataSet.getFormData();

                // set the headline
                data.get(lang, "cs_headline").set(headline);

                // set the subheadline
                data.get(lang, "cs_subheadline").set(subheadline);

                // set the teaser
                data.get(lang, "cs_teaser").set(teaser);

                // set the date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                data.get(lang, "cs_date").set(sdf.parse(date));

                // FS-List
                FormField ff = data.get(lang, "cs_content");//cs_content is the FS_LIST
                FormDataList dataList = (FormDataList) ff.get();

                //create FormData with Section template

                /*
                 * we know that the FS_LIST contains paragraphs with the uid ‘textpicture’
                 * so we create an IdProvidingFormData object with the help of this template
                 */

                SectionFormsProducer producer = (SectionFormsProducer) dataList.getProducer();
                IdProvidingFormData sectionFormData = producer.create(ts.getSectionTemplates().getTemplate("textpicture"));

                // just add content into this paragraph as above:
                sectionFormData.get(lang, "st_headline").set(contentHeadline);

                // Dom in FS-List

                // get & set the FormField
                FormField dom = sectionFormData.get(lang, "st_text");
                dom.set(contentText);

                // add sectionFormData to our FS_LIST
                dataList.add(sectionFormData);
                ff.set(dataList);

               // set FormData and save the dataSet
               dataSet.setFormData(data);
               dataSet.save();

		    } catch (Exception ex) {
                ex.printStackTrace();
		    } finally {
			    try {
				    csPressreleases.unlock(entityPressrelease);
			    } catch (Exception e) {
                    System.out.println("FATAL!");
			    }
		    }
        }
        return true;
    }

    public Object execute(Map<String, Object> context, Writer out, Writer err) throws ExecutionException {
        return execute(context);
    }
}
